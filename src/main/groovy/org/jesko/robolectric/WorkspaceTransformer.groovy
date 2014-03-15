package org.jesko.robolectric

class WorkspaceTransformer {

    String createWorkspaceWithGradleTask(File file, GradleRunConfiguration gradleRunConfiguration) {
        def parsedXml = new XmlParser().parse(file)
        def runManagerNode = findRunManagerNode(parsedXml)

        if(runManagerNode.configuration.find{ it.attributes()['name'] == gradleRunConfiguration.name } != null) {
            return XmlUtil.writeXmlToString(parsedXml)
        }

        def defaultGradleConfiguration = runManagerNode.configuration.find {
            it.attributes()['type'] == 'GradleRunConfiguration' && it.attributes()['default'] == 'true'
        }

        runManagerNode.append(gradleRunConfiguration.toXml(defaultGradleConfiguration))
        return XmlUtil.writeXmlToString(parsedXml)
    }

    String createWorkspaceWithJUnitDefaults(File file, List classpathEntries, GradleRunConfiguration configuration) {
        def parsedXml = new XmlParser().parse(file)
        def runManagerNode = findRunManagerNode(parsedXml)
        def junitConfiguration = runManagerNode.configuration.find { it.attributes()['type'] == 'JUnit' && it.attributes()['default'] == 'true' }

        def vmParams = junitConfiguration.option.find { it.attributes()['name'] == 'VM_PARAMETERS' }
        vmParams.attributes()['value'] = '-ea -classpath "' + classpathEntries.join(":") + ':$APPLICATION_HOME_DIR$/lib/idea_rt.jar:$APPLICATION_HOME_DIR$/plugins/junit/lib/junit-rt.jar' + '"'

        def runBeforeNode = junitConfiguration.method[0]
        if(runBeforeNode.find{ it.attributes()['run_configuration_name'] == configuration.name} == null) {
            new Node(runBeforeNode, 'option', [name: 'RunConfigurationTask', enabled: 'true', run_configuration_name: configuration.name, run_configuration_type: 'GradleRunConfiguration'])
        }

        return XmlUtil.writeXmlToString(parsedXml)
    }

    static def findRunManagerNode(parsedXml) {
        parsedXml.component.find { it.attributes()['name'] == 'RunManager' }
    }
}
