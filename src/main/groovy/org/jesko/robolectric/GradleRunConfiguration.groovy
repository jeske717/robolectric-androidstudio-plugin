package org.jesko.robolectric

class GradleRunConfiguration {

    String name
    List tasks

    GradleRunConfiguration(String name, List tasks) {
        this.name = name
        this.tasks = tasks
    }

    def toXml(defaults) {
        def defaultsCopy = new XmlParser().parseText(XmlUtil.writeXmlToString(defaults))
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

}
