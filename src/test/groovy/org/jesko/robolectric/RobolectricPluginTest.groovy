package org.jesko.robolectric

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.testfixtures.ProjectBuilder

class RobolectricPluginTest extends GroovyTestCase {

    void setUp() {
        AppPlugin.TEST_SDK_DIR = new File(".")
    }

    void testApplyPluginThrowsIllegalStateExceptionWhenAndroidPluginNotApplied() {
        Project project = ProjectBuilder.builder().build()

        shouldFail(IllegalArgumentException.class) {
            project.apply plugin: RobolectricPlugin
        }
    }

    void testApplyPluginAddsRobolectricTaskToProjectWhenAndroidPluginApplied() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: AppPlugin
        project.apply plugin: RobolectricPlugin

        assertEquals(1, project.getTasksByName("robolectricTest", false).size())
    }

    void testApplyPluginAddsRobolectricTaskToProjectWhenAndroidLibraryPluginApplied() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: LibraryPlugin
        project.apply plugin: RobolectricPlugin

        assertEquals(1, project.getTasksByName("robolectricTest", false).size())
    }

    void testJavaBasePluginIsApplied() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: AppPlugin
        project.apply plugin: RobolectricPlugin

        assertTrue(project.getPlugins().hasPlugin(JavaBasePlugin.class))
    }
}
