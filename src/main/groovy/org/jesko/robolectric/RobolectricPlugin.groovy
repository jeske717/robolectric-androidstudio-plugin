package org.jesko.robolectric

import org.gradle.api.Plugin
import org.gradle.api.Project

class RobolectricPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        throw new IllegalArgumentException("Project must be either an android or android library project")
    }

}
