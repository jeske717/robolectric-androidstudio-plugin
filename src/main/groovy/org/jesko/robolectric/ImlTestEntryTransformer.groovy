package org.jesko.robolectric

class ImlTestEntryTransformer {

    String createImlWithNewTestSourceRoot(File imlFile, String testSourceRoot) {
        def parsedXml = new XmlParser().parse(imlFile)
        def node = parsedXml.find { it.@name == "NewModuleRootManager" }
        def path = 'file://$MODULE_DIR$/' + testSourceRoot
        def set = node.find { it.@url == path }
        if (set == null) {
            new Node(node, 'sourceFolder', ['url': path, 'isTestSource': "true"])
            def writer = new StringWriter()
            new XmlNodePrinter(new PrintWriter(writer)).print(parsedXml)
            return writer.toString()
        } else {
            return imlFile.text
        }
    }
}