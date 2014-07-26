package org.jesko.robolectric

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
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

    void testRobolectricTestIsSetAsASourceFolder() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: AppPlugin
        project.apply plugin: RobolectricPlugin

        SourceSet robolectricSourceSet = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().findByName("robolectric")
        Iterator<File> srcDirIterator = robolectricSourceSet.getJava().srcDirs.iterator()
        assertEquals(project.file("src/robolectric/java"), srcDirIterator.next())
        assertEquals(project.file("src/robolectricTest/java"), srcDirIterator.next())
    }

    void testRobolectricExtensionIsAdded() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: AppPlugin

        project.apply plugin: RobolectricPlugin

        RobolectricPluginExtension extension = project.extensions.findByType(RobolectricPluginExtension)
        assertEquals(extension, project.extensions.findByName("robolectric"))
    }

    void testAddRobolectricTestSourcesToImlTaskIsAdded() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: AppPlugin

        project.apply plugin: RobolectricPlugin

        assertEquals(1, project.getTasksByName("addRobolectricTestSourcesToIml", false).size())
    }

    void testConfigureJUnitDefaultToUseRobolectricClasspathTaskIsAdded() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: AppPlugin

        project.apply plugin: RobolectricPlugin

        assertEquals(1, project.getTasksByName("configureJUnitDefaultToUseRobolectricClasspath", false).size())
    }

    void testSystemPropertiesAreSetForRobolectricTestRunner() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: AppPlugin

        project.apply plugin: RobolectricPlugin

        def props = project.getTasks().getByName("robolectricTest").getSystemProperties()
        assertEquals(project.file("src/main/AndroidManifest.xml"), props.get("android.manifest"))
        assertEquals(project.file("src/main/res"), props.get("android.resources"))
        assertEquals(project.file("src/main/assets"), props.get("android.assets"))
    }
}
