package org.jesko.robolectric

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class TestSourceImlWriter extends DefaultTask {

    final ImlTestEntryTransformer imlTestEntryTransformer

    @Inject
    TestSourceImlWriter() {
        this(new ImlTestEntryTransformer())
    }

    TestSourceImlWriter(ImlTestEntryTransformer) {
        this.imlTestEntryTransformer = ImlTestEntryTransformer
    }

    @TaskAction
    def addRobolectricTestAsTestSourceRoot() {
        File file = getImlFile()
		if(file.exists()) {
			String newIml = imlTestEntryTransformer.createImlWithNewTestSourceRoot(file, "src/robolectricTest/java")
			file.text = newIml
		}
    }

    File getImlFile() {
        String configured = project.extensions.findByName("robolectric").imlFile
        project.file(configured)
    }

}
