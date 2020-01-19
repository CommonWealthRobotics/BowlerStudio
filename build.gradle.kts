import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.spotbugs.SpotBugsTask
import info.solidsoft.gradle.pitest.PitestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.diffplug.gradle.spotless")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.spotbugs")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("com.adarshr.test-logger")
    id("info.solidsoft.pitest")
    id("com.github.johnrengelman.shadow")
    id("com.jfrog.bintray") version "1.8.3"
    `maven-publish`
    `java-library`
    jacoco
    pmd
    checkstyle
    application
}

val bowlerScriptKernelProject = project(":libraries:bowler-script-kernel")
val javaBowlerProject = project(":libraries:bowler-script-kernel:java-bowler")
val jcsgProject = project(":libraries:bowler-script-kernel:JCSG")

val kotlinProjects = setOf<Project>()

val javaProjects = setOf<Project>(rootProject) + kotlinProjects

val publishedProjects = setOf<Project>(rootProject)

val pitestProjects = setOf<Project>()

// val spotlessLicenseHeaderDelimiter = "(@|package|import)"

buildscript {
    repositories {
        mavenCentral() // Needed for kotlin gradle plugin
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/staging/")
    }

    configurations.maybeCreate("pitest")

    dependencies {
        // Gives us the KotlinJvmProjectExtension
        classpath(kotlin("gradle-plugin", property("kotlinVersion") as String))
        "pitest"("org.pitest:pitest-junit5-plugin:0.9")
    }
}

allprojects {
    version = property("bowlerstudio.version") as String
    group = "com.neuronrobotics"

    apply {
        plugin("com.diffplug.gradle.spotless")
        plugin("com.adarshr.test-logger")
    }

    repositories {
        jcenter()
        mavenCentral()
    }

    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = property("jacoco-tool.version") as String
        }
    }

    tasks.withType<Test> {
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    testlogger {
        theme = ThemeType.STANDARD_PARALLEL
        showStandardStreams = true
    }

    spotless {
        /*
         * We use spotless to lint the Gradle Kotlin DSL files that make up the build.
         * These checks are dependencies of the `check` task.
         */
        kotlinGradle {
            ktlint(property("ktlint.version") as String)
            trimTrailingWhitespace()
        }
        // freshmark {
        //     trimTrailingWhitespace()
        //     indentWithSpaces(2)
        //     endWithNewline()
        // }
        format("extraneous") {
            target("src/**/*.fxml")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
    }
}

configure(javaProjects) {
    apply {
        plugin("java")
        plugin("jacoco")
        plugin("checkstyle")
        plugin("com.github.spotbugs")
        plugin("pmd")
    }

    dependencies {
        testImplementation(
                group = "org.junit.jupiter",
                name = "junit-jupiter",
                version = property("junit-jupiter.version") as String
        )

        // testImplementation(
        //     group = "io.kotlintest",
        //     name = "kotlintest-runner-junit5",
        //     version = property("kotlintest.version") as String
        // )

        // TODO: Go back to the old dependencies once 4.x.x is out
        // https://github.com/wpilibsuite/Axon/issues/84
//        testImplementation(
//                files(
//                        "$rootDir/libraries/kotlintest-runner-junit5-jvm-4.0.2631-SNAPSHOT.jar",
//                        "$rootDir/libraries/kotlintest-runner-console-jvm-4.0.2631-SNAPSHOT.jar",
//                        "$rootDir/libraries/kotlintest-runner-jvm-jvm-4.0.2631-SNAPSHOT.jar",
//                        "$rootDir/libraries/kotlintest-core-jvm-4.0.2631-SNAPSHOT.jar"
//                )
//        )
        testImplementation(
                group = "org.slf4j",
                name = "slf4j-api",
                version = "1.7.25"
        )

        testRuntime(
                group = "org.junit.platform",
                name = "junit-platform-launcher",
                version = property("junit-platform-launcher.version") as String
        )
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    }

    val test by tasks.getting(Test::class) {
        @Suppress("UnstableApiUsage")
        useJUnitPlatform {
            filter {
                includeTestsMatching("*Test")
                includeTestsMatching("*Tests")
                includeTestsMatching("*Spec")
            }

            /*
            These tests just test performance and should not run in CI.
             */
            excludeTags("performance")

            /*
            These tests are too slow to run in CI.
             */
            excludeTags("slow")

            /*
            These tests need some sort of software that can't be reasonably installed on CI servers.
             */
            excludeTags("needsSpecialSoftware")

            if (!project.hasProperty("hasDockerSupport")) {
                excludeTags("needsDockerSupport")
            }
        }

        jvmArgs!!.add("-Xss512m")
        // jvmArgs!!.add("-Djsse.enableSNIExtension=false")
        if (project.hasProperty("jenkinsBuild") || project.hasProperty("headless")) {
            jvmArgs!!.addAll(
                    listOf(
                            "-Djava.awt.headless=true",
                            "-Dtestfx.robot=glass",
                            "-Dtestfx.headless=true",
                            "-Dprism.order=sw",
                            "-Dprism.text=t2k"
                    )
            )
        }

        testLogging {
            events(
                    TestLogEvent.FAILED,
                    TestLogEvent.PASSED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.STARTED
            )
            displayGranularity = 0
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }

        @Suppress("UnstableApiUsage")
        reports.junitXml.destination = file("${rootProject.buildDir}/test-results/${project.name}")
    }

    tasks.withType<JacocoReport> {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }

    spotless {
        java {
            googleJavaFormat()
            removeUnusedImports()
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            // @Suppress("INACCESSIBLE_TYPE")
            // licenseHeaderFile(
            //        "${rootProject.rootDir}/config/spotless/bowler-kernel.license",
            //        spotlessLicenseHeaderDelimiter
            // )
        }
    }

    checkstyle {
        toolVersion = property("checkstyle-tool.version") as String
    }

    spotbugs {
        toolVersion = property("spotbugs-tool.version") as String
        excludeFilter = file("${rootProject.rootDir}/config/spotbugs/spotbugs-excludeFilter.xml")
    }

    tasks.withType<SpotBugsTask> {
        @Suppress("UnstableApiUsage")
        reports {
            xml.isEnabled = false
            emacs.isEnabled = false
            html.isEnabled = true
        }
    }

    pmd {
        toolVersion = property("pmd-tool.version") as String
        ruleSets = emptyList() // Needed so PMD only uses our custom ruleset
        ruleSetFiles = files("${rootProject.rootDir}/config/pmd/pmd-ruleset.xml")
    }
}

configure(kotlinProjects) {
    val kotlinVersion = property("kotlinVersion") as String

    apply {
        plugin("kotlin")
        plugin("kotlinx-serialization")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jetbrains.dokka")
    }

    repositories {
        maven("https://dl.bintray.com/kotlin/ktor")
        maven("https://dl.bintray.com/kotlin/kotlinx")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8", kotlinVersion))
        implementation(kotlin("reflect", kotlinVersion))
        implementation(
                group = "org.jetbrains.kotlinx",
                name = "kotlinx-coroutines-core",
                version = property("kotlin-coroutines.version") as String
        )
        implementation(
                group = "org.jetbrains.kotlinx",
                name = "kotlinx-serialization-runtime",
                version = property("kotlinx-serialization-runtime.version") as String
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf(
                    "-Xjvm-default=enable",
                    "-Xinline-classes",
                    "-Xuse-experimental=kotlin.Experimental",
                    "-progressive"
            )
        }
    }

    spotless {
        kotlin {
            ktlint(property("ktlint.version") as String)
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
            // @Suppress("INACCESSIBLE_TYPE")
            // licenseHeaderFile(
            //        "${rootProject.rootDir}/config/spotless/bowler-kernel.license",
            //        spotlessLicenseHeaderDelimiter
            // )
        }
    }

    detekt {
        toolVersion = property("detekt-tool.version") as String
        input = files("src/main/kotlin", "src/test/kotlin")
        parallel = true
        config = files("${rootProject.rootDir}/config/detekt/config.yml")
    }
}

//val jacocoRootReport by tasks.creating(JacocoReport::class) {
//    group = "verification"
//    dependsOn(subprojects.flatMap { it.tasks.withType(JacocoReport::class) } - this)
//
//    val allSrcDirs = subprojects.map { it.sourceSets.main.get().allSource.srcDirs }
//    additionalSourceDirs.setFrom(allSrcDirs)
//    sourceDirectories.setFrom(allSrcDirs)
//    classDirectories.setFrom(subprojects.map { it.sourceSets.main.get().output })
//    executionData.setFrom(subprojects.filter {
//        File("${it.buildDir}/jacoco/test.exec").exists()
//    }.flatMap { it.tasks.withType(JacocoReport::class).map { it.executionData } })
//
//    reports {
//        html.isEnabled = true
//        xml.isEnabled = true
//        csv.isEnabled = false
//    }
//}

configure(pitestProjects) {
    apply {
        plugin("info.solidsoft.pitest")
    }

    pitest {
        testPlugin.set("junit5")
        pitestVersion.set("1.4.10")
        threads.set(4)
        avoidCallsTo.set(setOf("kotlin.jvm.internal", "kotlinx.coroutines"))
        excludedMethods.set(
                setOf(
                        "hashCode",
                        "equals",
                        "checkIndexOverflow",
                        "throwIndexOverflow",
                        "collectionSizeOrDefault"
                )
        )
        excludedClasses.set(
                setOf(
                        "NoSuchElementException",
                        "NoWhenBranchMatchedException",
                        "IllegalStateException"
                )
        )
        timeoutConstInMillis.set(10000)
        mutators.set(setOf("NEW_DEFAULTS"))
    }
}

tasks.withType<PitestTask> {
    onlyIf { project in pitestProjects }
}

fun Jar.setManifest() {
    isZip64 = true
    manifest {
        attributes(
                "Main-Class" to "com.neuronrobotics.bowlerstudio.BowlerStudio",
                "SplashScreen-Image" to "com/neuronrobotics/nrconsole/images/splash.png",
                "Manifest-Version" to "1.0",
                "Created-By" to "CommonWealth Robotics Cooperative",
                "Specification-Title" to rootProject.property("bowlerstudio.name"),
                "Specification-Version" to rootProject.property("bowlerstudio.version"),
                "Specification-Vendor" to "CommonWealth Robotics Cooperative",
                "Implementation-Title" to rootProject.property("bowlerstudio.name"),
                "Implementation-Version" to rootProject.property("bowlerstudio.version"),
                "Implementation-Vendor" to "CommonWealth Robotics Cooperative"
        )
    }
}

configure(publishedProjects) {
    apply {
        plugin("com.jfrog.bintray")
        plugin("maven-publish")
        plugin("java-library")
        plugin("com.github.johnrengelman.shadow")
    }

    val projectName = property("bowlerstudio.name") as String

    tasks.named<Jar>("jar") {
        setManifest()
    }

    task<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        archiveBaseName.set("$projectName-${this@configure.name.toLowerCase()}")
        from(sourceSets.main.get().allSource)
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        archiveBaseName.set("$projectName-${this@configure.name.toLowerCase()}")
        from(tasks.dokka)
    }

    val shadowJar2 by tasks.creating(ShadowJar::class) {
        setManifest()
        mergeServiceFiles()
    }

    val publicationName = "publication-$projectName-${name.toLowerCase()}"

    publishing {
        publications {
            create<MavenPublication>(publicationName) {
                artifactId = "$projectName-${this@configure.name.toLowerCase()}"
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(dokkaJar)
                artifact(shadowJar2)
            }
        }
    }

    bintray {
        val bintrayApiUser = properties["bintray.api.user"] ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey = properties["bintray.api.key"] ?: System.getenv("BINTRAY_API_KEY")
        user = bintrayApiUser as String?
        key = bintrayApiKey as String?
        setPublications(publicationName)
        with(pkg) {
            repo = "maven-artifacts"
            name = projectName
            userOrg = "commonwealthrobotics"
            publish = true
            setLicenses("LGPL-3.0")
            vcsUrl = "https://github.com/CommonWealthRobotics/BowlerStudio.git"
            githubRepo = "https://github.com/CommonWealthRobotics/BowlerStudio"
            with(version) {
                name = property("bowlerstudio.version") as String
                desc = "A Full-Stack Robotics Development Environment."
            }
        }
    }
}

tasks.dokka {
    dependsOn(tasks.classes)
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

tasks.wrapper {
    gradleVersion = rootProject.property("gradle-wrapper.version") as String
    distributionType = Wrapper.DistributionType.ALL
}

configure(listOf(rootProject)) {
    apply {
        plugin("application")
    }

    repositories {
        maven("https://oss.sonatype.org/content/repositories/staging/")

        maven {
            url = uri("https://dl.bintray.com/s1m0nw1/KtsRunner")
            content { includeGroup("de.swirtz") }
        }

        maven {
            url = uri("https://dl.bintray.com/clearcontrol/ClearControl")
            content { includeGroup("org.dockfx") }
        }
    }

    dependencies {
        implementation(bowlerScriptKernelProject)
        implementation(javaBowlerProject)
        implementation(jcsgProject)
        implementation(group = "jfree", name = "jfreechart", version = property("jfreechart.version") as String)
        implementation(group = "org.dockfx", name = "DockFX", version = property("dockfx.version") as String)
        implementation(group = "com.fifesoft", name = "rsyntaxtextarea", version = property("rsyntaxtextarea.version") as String)
    }

    application {
        mainClassName = "com.neuronrobotics.bowlerstudio.BowlerStudio"
    }
}
