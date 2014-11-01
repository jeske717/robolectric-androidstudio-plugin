package com.jeskeshouse.robolectric

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test

import java.util.concurrent.Callable
import java.util.regex.Pattern

class RobolectricPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        validateProject(project)

        setupConfiguration(project.getConfigurations())
        setupSourceSets(project)
        setupTest(project)

        project.afterEvaluate {
            setupDependencies(project)
        }

        project.extensions.create("robolectric", RobolectricPluginExtension)
        project.getTasks().create("addRobolectricTestSourcesToIml", TestSourceImlWriter)
        project.getTasks().create("configureJUnitDefaultToUseRobolectricClasspath", JUnitReconfigurer)
    }

    private static void setupDependencies(Project project) {
        JavaPluginConvention javaPlugin = project.getConvention().getPlugin(JavaPluginConvention)
        SourceSet robolectric = javaPlugin.getSourceSets().findByName("robolectric");

        def androidPlugin
        if(project.getPlugins().hasPlugin(AppPlugin)) {
            androidPlugin = project.getPlugins().getPlugin(AppPlugin)
        } else {
            androidPlugin = project.getPlugins().getPlugin(LibraryPlugin)
        }

        androidPlugin.mainSourceSet.java.srcDirs.each { dir ->
            def buildDir = dir.getAbsolutePath().split(Pattern.quote(File.separator))
            buildDir = (buildDir[0..(buildDir.length - 4)] + ['build', 'intermediates', 'classes', 'debug']).join(File.separator)
            robolectric.compileClasspath += project.files(buildDir)
            robolectric.runtimeClasspath += project.files(buildDir)
        }

        androidPlugin.variantDataList.each {
            it.variantDependency.getJarDependencies().each {
                robolectric.compileClasspath += project.files(it.jarFile)
                robolectric.runtimeClasspath += project.files(it.jarFile)
            }
        }

        androidPlugin.prepareTaskMap.each {
            robolectric.compileClasspath += project.fileTree(dir: it.value.explodedDir, include: '*.jar')
            robolectric.runtimeClasspath += project.fileTree(dir: it.value.explodedDir, include: '*.jar')
        }

    }

    private static void setupTest(final Project project) {
        JavaPluginConvention pluginConvention = project.getConvention().getPlugin(JavaPluginConvention)
        project.getTasks().withType(Test.class, new Action<Test>() {
            public void execute(final Test test) {
                test.workingDir 'src/main'
                test.getConventionMapping().map("testClassesDir", new Callable<Object>() {
                    public Object call() throws Exception {
                        return pluginConvention.getSourceSets().getByName("robolectric").getOutput().getClassesDir();
                    }
                });

                test.getConventionMapping().map("classpath", new Callable<Object>() {
                    public Object call() throws Exception {
                        return pluginConvention.getSourceSets().getByName("robolectric").getRuntimeClasspath();
                    }
                });

                test.getConventionMapping().map("testSrcDirs", new Callable<Object>() {
                    public Object call() throws Exception {
                        return new ArrayList<File>(pluginConvention.getSourceSets().getByName("robolectric").getJava().getSrcDirs());
                    }
                });
            }
        });

        Test test = project.getTasks().create("robolectricTest", Test);
        project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(test);
        test.setDescription("Runs the unit tests using robolectric.");
        test.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
        test.getSystemProperties().put("android.manifest", project.file("src/main/AndroidManifest.xml"))
        test.getSystemProperties().put("android.resources", project.file("src/main/res"))
        test.getSystemProperties().put("android.assets", project.file("src/main/assets"))

        test.dependsOn(project.getTasks().findByName('robolectricClasses'))
        test.dependsOn(project.getTasks().findByName('assemble'))
    }

    private static void setupSourceSets(Project project) {
        SourceSet robolectric = project.getConvention().getPlugin(JavaPluginConvention).getSourceSets().create("robolectric")
        robolectric.java.srcDir(project.file("src/robolectricTest/java"))
        robolectric.resources.srcDir(project.file("src/robolectricTest/resources"))
        robolectric.compileClasspath += project.configurations.robolectric
        robolectric.runtimeClasspath += robolectric.compileClasspath
    }

    private static void setupConfiguration(ConfigurationContainer configurations) {
        Configuration compileConfiguration = configurations.getByName("compile")
        Configuration robolectric = configurations.create("robolectric")
        robolectric.extendsFrom(compileConfiguration);
    }

    private static void validateProject(Project project) {
        if(!project.getPlugins().hasPlugin(AppPlugin) && !project.getPlugins().hasPlugin(LibraryPlugin)) {
            throw new IllegalArgumentException("Project must be either an android or android library project")
        }
    }

}
