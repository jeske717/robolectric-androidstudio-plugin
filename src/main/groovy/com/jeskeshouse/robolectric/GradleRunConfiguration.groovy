package com.jeskeshouse.robolectric

import org.xml.sax.SAXParseException

class GradleRunConfiguration {

    String name
    List tasks

    GradleRunConfiguration(String name, List tasks) {
        this.name = name
        this.tasks = tasks
    }

    def toXml(defaults) {
        def defaultsCopy
        try {
            defaultsCopy = new XmlParser().parseText(XmlUtil.writeXmlToString(defaults))
        } catch(SAXParseException ignored) {
            defaultsCopy = getGradleRunConfigDefaultXml()
        }
        defaultsCopy.attributes()['default'] = 'false'
        defaultsCopy.attributes()['name'] = name
        defaultsCopy.attributes()['factoryName'] = 'Gradle'

        def externalSystemSettings = defaultsCopy.ExternalSystemSettings[0]
        def taskList = externalSystemSettings.find {
            it.attributes()['name'] == 'taskNames'
        }.list[0]

        tasks.collect { new Node(taskList, 'option', [value: it]) }
        externalSystemSettings.find { it.attributes()['name'] == 'externalProjectPath' }.attributes()['value'] = '$PROJECT_DIR$'

        return defaultsCopy
    }

    static def getGradleRunConfigDefaultXml() {
        return new XmlParser().parseText("""
         <configuration default="false" name="" type="GradleRunConfiguration" factoryName="Gradle">
          <ExternalSystemSettings>
            <option name="externalProjectPath" value="\\\$PROJECT_DIR\\\$" />
            <option name="externalSystemIdString" value="GRADLE" />
            <option name="scriptParameters" value="" />
            <option name="taskDescriptions">
              <list />
            </option>
            <option name="taskNames">
              <list />
            </option>
            <option name="vmOptions" value="" />
          </ExternalSystemSettings>
          <RunnerSettings RunnerId="ExternalSystemTaskRunner" />
          <ConfigurationWrapper RunnerId="ExternalSystemTaskRunner" />
          <method />
        </configuration>
        """)
    }

}
