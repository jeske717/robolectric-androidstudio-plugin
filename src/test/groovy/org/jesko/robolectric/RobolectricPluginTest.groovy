package org.jesko.robolectric

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder


class RobolectricPluginTest extends GroovyTestCase {

    void testApplyPluginThrowsIllegalStateExceptionWhenAndroidPluginNotApplied() {
        Project project = ProjectBuilder.builder().build()

        shouldFail(IllegalArgumentException.class) {
            project.apply plugin: RobolectricPlugin
        }
    }

}
