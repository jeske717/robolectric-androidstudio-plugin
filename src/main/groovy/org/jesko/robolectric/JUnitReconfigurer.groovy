package org.jesko.robolectric

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class JUnitReconfigurer extends DefaultTask {

    @TaskAction
    def reconfigureJUnitDefaults() {
        File file = getWorkspaceXml()
        def robolectricClasspath = project.sourceSets.robolectric.runtimeClasspath
        def robolectricClasses = robolectricClasspath.collect { it.absolutePath }
        def classpath = robolectricClasses.join(':') + ':$APPLICATION_HOME_DIR$/lib/idea_rt.jar:$APPLICATION_HOME_DIR$/plugins/junit/lib/junit-rt.jar:'

        def parsedXml = new XmlParser().parse(file)
        def runManagerNode = parsedXml.component.find { it.attributes()['name'] == 'RunManager' }

        if(runManagerNode.configuration.find{ it.attributes()['name'] == 'BuildRobolectric' } == null) {

            def defaultGradleConfiguration = runManagerNode.configuration.find {
                it.attributes()['type'] == 'GradleRunConfiguration' && it.attributes()['default'] == 'true'
            }
            def buildRobolectricGradleConfiguration = new XmlParser().parseText(writeXmlToString(defaultGradleConfiguration))
            buildRobolectricGradleConfiguration.attributes()['default'] = 'false'
            buildRobolectricGradleConfiguration.attributes()['name'] = 'BuildRobolectric'
            buildRobolectricGradleConfiguration.attributes()['factoryName'] = 'Gradle'

            def externalSystemSettings = buildRobolectricGradleConfiguration.ExternalSystemSettings[0]
            def taskList = externalSystemSettings.find {
                it.attributes()['name'] == 'taskNames'
            }.list[0]
            new Node(taskList, 'option', [value: 'assembleDebug'])
            new Node(taskList, 'option', [value: 'robolectricClasses'])
            runManagerNode.append(buildRobolectricGradleConfiguration)

            externalSystemSettings.find { it.attributes()['name'] == 'externalProjectPath' }.attributes()['value'] = '$PROJECT_DIR$'
        }

        def junitConfiguration = runManagerNode.configuration.find { it.attributes()['type'] == 'JUnit' && it.attributes()['default'] == 'true' }

        def vmParams = junitConfiguration.option.find { it.attributes()['name'] == 'VM_PARAMETERS' }
        vmParams.attributes()['value'] = '-ea -classpath "' + classpath + '"'

        def runBeforeNode = junitConfiguration.method[0]
        if(runBeforeNode.find{ it.attributes()['run_configuration_name'] == 'BuildRobolectric'} == null) {
            new Node(runBeforeNode, 'option', [name: 'RunConfigurationTask', enabled: 'true', run_configuration_name: 'BuildRobolectric', run_configuration_type: 'GradleRunConfiguration'])
        }

        file.text = writeXmlToString(parsedXml)
    }

    File getWorkspaceXml() {
        String configured = project.extensions.findByName("robolectric").dotIdeaDir + "/workspace.xml"
        project.file(configured)
    }

    static String writeXmlToString(xml) {
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer)).print(xml)

        writer.toString()
    }

}
