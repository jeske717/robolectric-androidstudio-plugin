robolectric-androidstudio-plugin
================================
The goal of this project is to limit the amount of work necessary to use Robolectric with the gradle build system and Android Studio.  Tested on Mac/Windows using Android Studio 0.9.0, Gradle 2.1 and the android plugin for gradle version 0.14.0.

Example
-------
This is an example build script and workflow

	buildscript {
		repositories {
			jcenter()
		}
		dependencies {
			classpath 'com.jeskeshouse:robolectric-androidstudio-plugin:2.0.0'
			classpath 'com.android.tools.build:gradle:0.14.0'
		}
	}
	
	apply plugin: 'android'
	apply plugin: 'robolectric'
	
	dependencies {
		robolectricCompile 'junit:junit:4.10'
		robolectricCompile 'org.robolectric:robolectric:2.1'
		robolectricCompile 'com.google.android:android:4.1.1.4'
	}
	
	android {
	    configurations {
	        androidTestProvided.extendsFrom(robolectricCompile)
	    }
	}
	
	robolectric {
		imlFile '<relative path to the app project.iml>'
		dotIdeaDir '<relative path to .idea folder>'
		inProcessBuilds = false // see note below
	}
	
	// optional: the plugin provides this task to edit the project's .iml file, 
	// which adds src/robolectricTest/java as a test source root.
	// This configuration block makes that task run whenever Android Studio starts a build
	gradle.projectsEvaluated {
		generateDebugSources.doLast {
			tasks['addRobolectricTestSourcesToIml'].execute()
		}
	}
	
Once a project is setup (and anytime the dependencies change), you can reconfigure Android Studio's JUnit defaults using the following command:
	./gradlew configureJUnitDefaultToUseRobolectricClasspath
	
After that task is run and Android Studio reloads the project, you should be able to run POJO unit tests (roblectric included) out of src/robolectricTest/java.  Android tests can still be added at src/androidTest/java.  If any kind of project configuration is changed (dependencies added), the task will need to be run again.  In addition, since Android Studio creates JUnit run configurations on the fly, you may need to clear out any JUnit configurations that it has previously created.

Note about in process builds
----------------------------
Although the in-process build option in Android Studio should make typical builds go faster, the option has an unfortunate side-effect of not allowing custom gradle run configurations to complete when they are added to a JUnit configuration in the 'Before Launch' section.  Because of this, for a regular TDD workflow, it is recommended to turn that option OFF in Android Studio and to set the "inProcessBuilds" parameter to false.  Doing so will speed up builds for TDD cycles by not providing a 'Make' task to the JUnit configuration, which incurs the penalty of building a full .APK.  Instead, the plugin will only compile Java sources and any sources under the robolectricTest configuration.

TODO
----
This is essentially a hack that rewrites Android Studio's project files.  Hopefully this plugin won't be necessary as Android Studio moves towards a 1.0 release.  Other than that, I'm pretty sure that build variants won't work. Also, it would be nice to not be stuck with 'src/robolectricTest/java'.
