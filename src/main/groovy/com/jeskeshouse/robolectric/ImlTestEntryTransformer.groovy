package com.jeskeshouse.robolectric

class ImlTestEntryTransformer {

    String createImlWithNewTestSourceRoot(File imlFile, String testSourceRoot) {
        def parsedXml = new XmlParser().parse(imlFile)
        def node = parsedXml.find { it.@name == "NewModuleRootManager" }
        def sourceContentNode = node.content[0]
        def path = 'file://$MODULE_DIR$/' + testSourceRoot
        def set = sourceContentNode.find { it.@url == path }
        if (set == null) {
            new Node(sourceContentNode, 'sourceFolder', ['url': path, 'isTestSource': "true"])
            return XmlUtil.writeXmlToString(parsedXml)
        } else {
            return imlFile.text
        }
    }
}