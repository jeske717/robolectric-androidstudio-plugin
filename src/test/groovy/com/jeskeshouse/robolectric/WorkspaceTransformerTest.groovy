package com.jeskeshouse.robolectric

class WorkspaceTransformerTest extends GroovyTestCase {

    RobolectricParameters parameters
    WorkspaceTransformer transformer
    File testFile

    void setUp() {
        transformer = new WorkspaceTransformer()
        testFile = File.createTempFile("WorkspaceTransformerTest", "xml")
        testFile.deleteOnExit()
        parameters = new RobolectricParameters()
        parameters.androidAssets = "assetsDir"
        parameters.androidResources = "resDir"
        parameters.androidManifest = "manifestFile"
    }

    void testCreateWorkspaceWithGradleTaskReturnsNewXmlWithGradleTask() {
        testFile.text = getWorkspaceXml()

        String result = transformer.createWorkspaceWithGradleTask(testFile, new GradleRunConfiguration("DoWork", ["this first", "this second"]))

        assertTrue(result.contains('<configuration default="false" type="GradleRunConfiguration" name="DoWork" factoryName="Gradle">'))
        assertTrue(result.contains('<option value="this first"/>'))
        assertTrue(result.contains('<option value="this second"/>'))
        assertTrue(result.contains('<option name="WORKING_DIRECTORY" value="file://\$PROJECT_DIR\$"/>'))
    }

    void testCreateWorkspaceWithGradleTaskDoesNotCreateAdditionalConfigurationWhenOneAlreadyExists() {
        testFile.text = getWorkspaceXmlWithPreExistingGradleTask("DoMoreWork")

        String result = transformer.createWorkspaceWithGradleTask(testFile, new GradleRunConfiguration("DoMoreWork", ["this first", "this second"]))

        assertTrue(result.contains('<configuration default="true" type="GradleRunConfiguration" name="DoMoreWork">'))
        assertFalse(result.contains('<configuration default="false" type="GradleRunConfiguration" name="DoMoreWork" factoryName="Gradle">'))
    }

    void testCreateWorkspaceWithJUnitDefaults() {
        testFile.text = getWorkspaceXml()

        String result = transformer.createWorkspaceWithJUnitDefaults(testFile, ["file1", "file2"], new GradleRunConfiguration("DoMoreWork", ["this first", "this second"]), true, parameters)

        assertTrue(result.contains('<option name="VM_PARAMETERS" value="-ea -classpath &quot;file1' +
                File.pathSeparator + 'file2' +
                File.pathSeparator + '$APPLICATION_HOME_DIR$/lib/idea_rt.jar' +
                File.pathSeparator + '$APPLICATION_HOME_DIR$/plugins/junit/lib/junit-rt.jar&quot; ' +
                "-Dandroid.manifest=$parameters.androidManifest -Dandroid.resources=$parameters.androidResources -Dandroid.assets=$parameters.androidAssets\"/>"));
        assertTrue(result.contains('<option name="RunConfigurationTask" enabled="true" run_configuration_name="DoMoreWork" run_configuration_type="GradleRunConfiguration"/>'));
        assertTrue(result.contains('<option name="Make" enabled="true"/>'));
    }

    void testCreateWorkspaceWithJUnitDefaultsEnablesMakeIfItIsDisabled() {
        testFile.text = getWorkspaceXmlWithExistingJUnitRunConfiguration("thename")

        String result = transformer.createWorkspaceWithJUnitDefaults(testFile, ["file1", "file2"], new GradleRunConfiguration("DoMoreWork", ["this first", "this second"]), true, parameters)

        assertFalse(result.contains('<option name="Make" enabled="false"/>'));
        assertTrue(result.contains('<option name="Make" enabled="true"/>'));
    }

    void testCreateWorkspaceWithJUnitDefaultsOnlyAddsMakeIfItIsNotAlreadyAdded() {
        testFile.text = getWorkspaceXmlWithMakeEnabledJUnitRunConfiguration("thename")

        String result = transformer.createWorkspaceWithJUnitDefaults(testFile, ["file1", "file2"], new GradleRunConfiguration("DoMoreWork", ["this first", "this second"]), true, parameters)

        assertTrue(result.indexOf('<option name="Make" enabled="true"/>') == result.lastIndexOf('<option name="Make" enabled="true"/>'));
    }

    void testCreateWorkspaceWithJUnitDefaultsDoesNotAddMakeIfShouldAddMakeToDefaultIsFalse() {
        testFile.text = getWorkspaceXml()

        String result = transformer.createWorkspaceWithJUnitDefaults(testFile, ["file1", "file2"], new GradleRunConfiguration("DoMoreWork", ["this first", "this second"]), false, parameters)

        assertFalse(result.contains('<option name="Make" enabled="true"/>'));
    }

    void testCreateWorkspaceWithJUnitDefaultsDisablesMakeIfShouldAddMakeToDefaultIsFalse() {
        testFile.text = getWorkspaceXmlWithMakeEnabledJUnitRunConfiguration("thename")

        String result = transformer.createWorkspaceWithJUnitDefaults(testFile, ["file1", "file2"], new GradleRunConfiguration("DoMoreWork", ["this first", "this second"]), false, parameters)

        assertFalse(result.contains('<option name="Make" enabled="true"/>'));
        assertTrue(result.contains('<option name="Make" enabled="false"/>'));
    }

    void testCreateWorkspaceWithJUnitDefaultsAddsDisabledMakeIfShouldAddMakeToDefaultIsFalse() {
        testFile.text = getWorkspaceXml()

        String result = transformer.createWorkspaceWithJUnitDefaults(testFile, ["file1", "file2"], new GradleRunConfiguration("DoMoreWork", ["this first", "this second"]), false, parameters)

        assertTrue(result.contains('<option name="Make" enabled="false"/>'));
    }

    void testCreateWorkspaceWithJUnitDoesNotCreateDuplicateRunConfigurations() {
        testFile.text = getWorkspaceXmlWithExistingJUnitRunConfiguration("DoMoreWork")

        String result = transformer.createWorkspaceWithJUnitDefaults(testFile, ["file1", "file2"], new GradleRunConfiguration("DoMoreWork", ["this first", "this second"]), true, parameters)

        assertTrue(result.contains('<option name="RunConfigurationTask" enabled="true" run_configuration_name="DoMoreWork" run_configuration_type="GradleRunConfiguration" previous="true"/>'));
        assertFalse(result.contains('<option name="RunConfigurationTask" enabled="true" run_configuration_name="DoMoreWork" run_configuration_type="GradleRunConfiguration"/>'));
    }

    static String getWorkspaceXml() {
        return """
        <project>
            <component name="AndroidLayouts">
            </component>
            <component name="RunManager">
                <configuration default="true" type="GradleRunConfiguration">
                    <ExternalSystemSettings>
                        <option name="externalProjectPath" />
                        <option name="externalSystemIdString" value="GRADLE" />
                        <option name="scriptParameters" />
                        <option name="taskDescriptions">
                            <list />
                        </option>
                        <option name="taskNames">
                            <list />
                        </option>
                        <option name="vmOptions" />
                    </ExternalSystemSettings>
                </configuration>
                <configuration default="true" type="JUnit" factoryName="JUnit">
                    <module name="" />
                    <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="false" />
                    <option name="ALTERNATIVE_JRE_PATH" value="" />
                    <option name="PACKAGE_NAME" />
                    <option name="MAIN_CLASS_NAME" value="" />
                    <option name="METHOD_NAME" value="" />
                    <option name="TEST_OBJECT" value="class" />
                    <option name="VM_PARAMETERS" value="" />
                    <option name="PARAMETERS" value="" />
                    <option name="WORKING_DIRECTORY" value="file://\$PROJECT_DIR\$" />
                    <option name="ENV_VARIABLES" />
                    <option name="PASS_PARENT_ENVS" value="true" />
                    <option name="TEST_SEARCH_SCOPE">
                        <value defaultName="moduleWithDependencies" />
                    </option>
                    <envs />
                    <patterns />
                    <method />
                </configuration>
            </component>
        </project>
        """
    }

    static String getWorkspaceXmlWithExistingJUnitRunConfiguration(String runConfigName) {
        return """
        <project>
            <component name="AndroidLayouts">
            </component>
            <component name="RunManager">
                <configuration default="true" type="GradleRunConfiguration">
                    <ExternalSystemSettings>
                        <option name="externalProjectPath" />
                        <option name="externalSystemIdString" value="GRADLE" />
                        <option name="scriptParameters" />
                        <option name="taskDescriptions">
                            <list />
                        </option>
                        <option name="taskNames">
                            <list />
                        </option>
                        <option name="vmOptions" />
                    </ExternalSystemSettings>
                </configuration>
                <configuration default="true" type="JUnit" factoryName="JUnit">
                    <module name="" />
                    <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="false" />
                    <option name="ALTERNATIVE_JRE_PATH" value="" />
                    <option name="PACKAGE_NAME" />
                    <option name="MAIN_CLASS_NAME" value="" />
                    <option name="METHOD_NAME" value="" />
                    <option name="TEST_OBJECT" value="class" />
                    <option name="VM_PARAMETERS" value="" />
                    <option name="PARAMETERS" value="" />
                    <option name="WORKING_DIRECTORY" value="file://\$PROJECT_DIR\$" />
                    <option name="ENV_VARIABLES" />
                    <option name="PASS_PARENT_ENVS" value="true" />
                    <option name="TEST_SEARCH_SCOPE">
                        <value defaultName="moduleWithDependencies" />
                    </option>
                    <envs />
                    <patterns />
                    <method>
                        <option name="RunConfigurationTask" enabled="true" run_configuration_name="$runConfigName" run_configuration_type="GradleRunConfiguration" previous="true"/>
                        <option name="Make" enabled="false"/>
                    </method>
                </configuration>
            </component>
        </project>
        """
    }

    static String getWorkspaceXmlWithMakeEnabledJUnitRunConfiguration(String runConfigName) {
        return """
        <project>
            <component name="AndroidLayouts">
            </component>
            <component name="RunManager">
                <configuration default="true" type="GradleRunConfiguration">
                    <ExternalSystemSettings>
                        <option name="externalProjectPath" />
                        <option name="externalSystemIdString" value="GRADLE" />
                        <option name="scriptParameters" />
                        <option name="taskDescriptions">
                            <list />
                        </option>
                        <option name="taskNames">
                            <list />
                        </option>
                        <option name="vmOptions" />
                    </ExternalSystemSettings>
                </configuration>
                <configuration default="true" type="JUnit" factoryName="JUnit">
                    <module name="" />
                    <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="false" />
                    <option name="ALTERNATIVE_JRE_PATH" value="" />
                    <option name="PACKAGE_NAME" />
                    <option name="MAIN_CLASS_NAME" value="" />
                    <option name="METHOD_NAME" value="" />
                    <option name="TEST_OBJECT" value="class" />
                    <option name="VM_PARAMETERS" value="" />
                    <option name="PARAMETERS" value="" />
                    <option name="WORKING_DIRECTORY" value="file://\$PROJECT_DIR\$" />
                    <option name="ENV_VARIABLES" />
                    <option name="PASS_PARENT_ENVS" value="true" />
                    <option name="TEST_SEARCH_SCOPE">
                        <value defaultName="moduleWithDependencies" />
                    </option>
                    <envs />
                    <patterns />
                    <method>
                        <option name="RunConfigurationTask" enabled="true" run_configuration_name="$runConfigName" run_configuration_type="GradleRunConfiguration" previous="true"/>
                        <option name="Make" enabled="true"/>
                    </method>
                </configuration>
            </component>
        </project>
        """
    }

    static String getWorkspaceXmlWithPreExistingGradleTask(String taskName) {
        return """
        <project>
            <component name="AndroidLayouts">
            </component>
            <component name="RunManager">
                <configuration default="true" type="GradleRunConfiguration">
                    <ExternalSystemSettings>
                        <option name="externalProjectPath" />
                        <option name="externalSystemIdString" value="GRADLE" />
                        <option name="scriptParameters" />
                        <option name="taskDescriptions">
                            <list />
                        </option>
                        <option name="taskNames">
                            <list />
                        </option>
                        <option name="vmOptions" />
                    </ExternalSystemSettings>
                </configuration>
                <configuration default="true" type="GradleRunConfiguration" name="$taskName">
                    <ExternalSystemSettings>
                        <option name="externalProjectPath" />
                        <option name="externalSystemIdString" value="GRADLE" />
                        <option name="scriptParameters" />
                        <option name="taskDescriptions">
                            <list />
                        </option>
                        <option name="taskNames">
                            <list />
                        </option>
                        <option name="vmOptions" />
                    </ExternalSystemSettings>
                </configuration>
                <configuration default="true" type="JUnit" factoryName="JUnit">
                    <module name="" />
                    <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="false" />
                    <option name="ALTERNATIVE_JRE_PATH" value="" />
                    <option name="PACKAGE_NAME" />
                    <option name="MAIN_CLASS_NAME" value="" />
                    <option name="METHOD_NAME" value="" />
                    <option name="TEST_OBJECT" value="class" />
                    <option name="VM_PARAMETERS" value="" />
                    <option name="PARAMETERS" value="" />
                    <option name="WORKING_DIRECTORY" value="file://\$PROJECT_DIR\$" />
                    <option name="ENV_VARIABLES" />
                    <option name="PASS_PARENT_ENVS" value="true" />
                    <option name="TEST_SEARCH_SCOPE">
                        <value defaultName="moduleWithDependencies" />
                    </option>
                    <envs />
                    <patterns />
                    <method />
                </configuration>
            </component>
        </project>
        """
    }

}
