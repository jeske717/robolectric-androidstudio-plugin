package org.jesko.robolectric

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TestSourceImlWriter extends DefaultTask {


    @TaskAction
    def addRobolectricTestAsTestSourceRoot() {
        def src = ['src/robolectricTest/java']

        try {
            File file = getImlFile()
            def parsedXml = (new XmlParser()).parse(file)
            def node = parsedXml.component[1].content[0]
            src.each {
                def path = 'file://$MODULE_DIR$/' + "${it}"
                def set = node.find { it.@url == path }
                if (set == null) {
                    new Node(node, 'sourceFolder', ['url': path, 'isTestSource': "true"])
                    def writer = new StringWriter()
                    new XmlNodePrinter(new PrintWriter(writer)).print(parsedXml)
                    file.text = writer.toString()
                }
            }
        } catch (FileNotFoundException e) {
            // nop, iml not found
        }
    }

    File getImlFile() {
        String configured = project.extensions.findByName("robolectric").imlFile
        project.file(configured)
    }

}
