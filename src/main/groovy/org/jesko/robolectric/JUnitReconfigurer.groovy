package org.jesko.robolectric

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class JUnitReconfigurer extends DefaultTask {

    final WorkspaceTransformer transformer

    @Inject
    JUnitReconfigurer() {
        this(new WorkspaceTransformer())
    }

    JUnitReconfigurer(WorkspaceTransformer transformer) {
        this.transformer = transformer
    }

    @TaskAction
    def reconfigureJUnitDefaults() {
        def robolectricClasspath = project.sourceSets.robolectric.runtimeClasspath
        List robolectricClasses = robolectricClasspath.collect { it.absolutePath }

        File file = getWorkspaceXml()
        GradleRunConfiguration configuration = new GradleRunConfiguration("BuildRobolectric", ["compileDebugJava", "robolectricClasses"])
        String newWorkspaceXml = transformer.createWorkspaceWithGradleTask(file, configuration)
        file.text = newWorkspaceXml
        newWorkspaceXml = transformer.createWorkspaceWithJUnitDefaults(file, robolectricClasses, configuration, shouldAddMakeToJunitDefault())

        file.text = newWorkspaceXml
    }

    boolean shouldAddMakeToJunitDefault() {
        project.extensions.findByName("robolectric").inProcessBuilds
    }

    File getWorkspaceXml() {
        String configured = project.extensions.findByName("robolectric").dotIdeaDir + "/workspace.xml"
        project.file(configured)
    }

}
