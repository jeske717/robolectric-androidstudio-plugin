package com.jeskeshouse.robolectric

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

    String createWorkspaceWithJUnitDefaults(File file, List classpathEntries, GradleRunConfiguration configuration, boolean addMakeToDefault, RobolectricParameters parameters) {
        def parsedXml = new XmlParser().parse(file)
        def runManagerNode = findRunManagerNode(parsedXml)
        def junitConfiguration = runManagerNode.configuration.find { it.attributes()['type'] == 'JUnit' && it.attributes()['default'] == 'true' }

        def vmParams = junitConfiguration.option.find { it.attributes()['name'] == 'VM_PARAMETERS' }
        def intellijClasspath = ['$APPLICATION_HOME_DIR$/lib/idea_rt.jar', '$APPLICATION_HOME_DIR$/plugins/junit/lib/junit-rt.jar']

        def fullClasspath = classpathEntries
        fullClasspath.addAll(intellijClasspath)
        vmParams.attributes()['value'] = "-ea -classpath \"${fullClasspath.join(File.pathSeparator)}\" ${getParameterString(parameters)}"

        def runBeforeNode = junitConfiguration.method[0]
        if(runBeforeNode.find{ it.attributes()['run_configuration_name'] == configuration.name} == null) {
            new Node(runBeforeNode, 'option', [name: 'RunConfigurationTask', enabled: 'true', run_configuration_name: configuration.name, run_configuration_type: 'GradleRunConfiguration'])
        }

        def makeTask = runBeforeNode.find { it.attributes()['name'] == 'Make' }
        if(makeTask == null) {
            new Node(runBeforeNode, 'option', [name: 'Make', enabled: addMakeToDefault])
        } else {
            makeTask.attributes()['enabled'] = addMakeToDefault
        }

        return XmlUtil.writeXmlToString(parsedXml)
    }

    String getParameterString(RobolectricParameters parameters) {
        "-Dandroid.manifest=$parameters.androidManifest -Dandroid.resources=$parameters.androidResources -Dandroid.assets=$parameters.androidAssets"
    }

    static def findRunManagerNode(parsedXml) {
        parsedXml.component.find { it.attributes()['name'] == 'RunManager' }
    }
}
