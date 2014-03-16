package org.jesko.robolectric

class GradleRunConfigurationTest extends GroovyTestCase {

    void testToXmlReturnsXmlBlockWhenDefaultsArgumentUnparseable() {
        def config = new GradleRunConfiguration("TheTask", ["task1", "task2"])

        def result = config.toXml("")

        assertEquals(XmlUtil.writeXmlToString(getExpectedXml(config)), XmlUtil.writeXmlToString(result))
    }

    static def getExpectedXml(GradleRunConfiguration config) {
        return new XmlParser().parseText("""
         <configuration default="false" name="$config.name" type="GradleRunConfiguration" factoryName="Gradle">
          <ExternalSystemSettings>
            <option name="externalProjectPath" value="\$PROJECT_DIR\$" />
            <option name="externalSystemIdString" value="GRADLE" />
            <option name="scriptParameters" value="" />
            <option name="taskDescriptions">
              <list />
            </option>
            <option name="taskNames">
              <list>
                <option value="${config.tasks[0]}" />
                <option value="${config.tasks[1]}" />
              </list>
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
