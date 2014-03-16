package org.jesko.robolectric

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WorkspaceTransformer {

    static final Logger log = LoggerFactory.getLogger(WorkspaceTransformer)

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
        def intellijClasspath = ['$APPLICATION_HOME_DIR$/lib/idea_rt.jar', '$APPLICATION_HOME_DIR$/plugins/junit/lib/junit-rt.jar']

        def fullClasspath = classpathEntries
        fullClasspath.addAll(intellijClasspath)
        vmParams.attributes()['value'] = '-ea -classpath "' + fullClasspath.join(File.pathSeparator) + '"'

        def runBeforeNode = junitConfiguration.method[0]
        if(runBeforeNode.find{ it.attributes()['run_configuration_name'] == configuration.name} == null) {
            new Node(runBeforeNode, 'option', [name: 'RunConfigurationTask', enabled: 'true', run_configuration_name: configuration.name, run_configuration_type: 'GradleRunConfiguration'])
        }

        new Node(runBeforeNode, 'option', [name: 'Make', enabled: 'true'])
        def disabledMakeTask = runBeforeNode.find { it.attributes()['name'] == 'Make' && it.attributes()['enabled'] == 'false' }
        if(disabledMakeTask != null) {
           runBeforeNode.remove(disabledMakeTask)
        }

        return XmlUtil.writeXmlToString(parsedXml)
    }

    static def findRunManagerNode(parsedXml) {
        parsedXml.component.find { it.attributes()['name'] == 'RunManager' }
    }
}
