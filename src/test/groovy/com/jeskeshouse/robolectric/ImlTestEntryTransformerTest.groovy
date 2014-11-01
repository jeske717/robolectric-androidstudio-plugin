package com.jeskeshouse.robolectric

class ImlTestEntryTransformerTest extends GroovyTestCase {

    ImlTestEntryTransformer transformer

    File testFile

    void setUp() {
        transformer = new ImlTestEntryTransformer()
        testFile = File.createTempFile("ImlTestEntryTransformerTest", "xml")
        testFile.deleteOnExit()
    }

    void testCreateImlWithNewTestSourceRootAddsNewTestSourceRoot() {
        testFile.text = getImlWithoutSourceRootAdded()

        String result = transformer.createImlWithNewTestSourceRoot(testFile, "src/test/java")

        assertTrue(result.contains('<sourceFolder url="file://$MODULE_DIR$/src/test/java" isTestSource="true"/>'))
    }

    void testCreateImlWithNewTestSourceRootDoesNotAddRootWhenItAlreadyExists() {
        testFile.text = getImlWithSourceRootAdded()

        String result = transformer.createImlWithNewTestSourceRoot(testFile, "src/test/java")

        assertTrue(result.contains('<sourceFolder url="file://$MODULE_DIR$/src/test/java" isTestSource="false"/>'))
        assertFalse(result.contains('<sourceFolder url="file://$MODULE_DIR$/src/test/java" isTestSource="true"/>'))
    }

    static String getImlWithoutSourceRootAdded() {
        return """
        <module>
            <component name="FacetManager">
            </component>
            <component name="NewModuleRootManager" inherit-compiler-output="false">
                <content>
                </content>
            </component>
        </module>
        """
    }

    static String getImlWithSourceRootAdded() {
        return """
        <module>
            <component name="FacetManager">
            </component>
            <component name="NewModuleRootManager" inherit-compiler-output="false">
                <content>
                    <sourceFolder url="file://\$MODULE_DIR\$/src/test/java" isTestSource="false"/>
                </content>
            </component>
        </module>
        """
    }

}
