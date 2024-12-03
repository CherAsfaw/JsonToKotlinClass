import org.hildan.github.changelog.builder.DEFAULT_TIMEZONE
import org.hildan.github.changelog.builder.SectionDefinition
import org.jetbrains.changelog.closure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.intellij") version "0.7.3"
    kotlin("jvm") version "1.5.20"
    id("org.jetbrains.changelog") version "1.1.1"
    id("org.hildan.github.changelog") version "1.6.0"
}
group = "wu.seal"
version = System.getenv("TAG") ?: "Unreleased"

intellij {
    version = "2020.1"
    pluginName = "JsonToKotlinClass"
}
tasks.patchPluginXml {
    untilBuild("")
    changeNotes(closure {
        changelogForIDEPlugin.getLatest().toHTML()
    })
}
tasks.publishPlugin {
    token(System.getenv("token"))
    channels(System.getProperty("channels", ""))
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    testImplementation("com.winterbe:expekt:0.5.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
tasks.getByPath("publishPlugin").dependsOn("generateChangelog")

changelogForIDEPlugin {
    version = project.version.toString()
    path = "${project.projectDir}/doc/CHANGELOG.md"
    unreleasedTerm = "Unreleased"
    itemPrefix = "**"
}
changelog {
    githubUser = "wuseal"
    githubRepository = rootProject.name
    githubToken = findProperty("githubToken")?.toString() ?: (System.getenv("GH_TOKEN")?.toString())
    title = "Change Log"
    showUnreleased = true
    unreleasedVersionTitle = "Unreleased"
    if (!System.getenv("TAG").isNullOrEmpty()) {
        println("TAG is ${System.getenv("TAG")}, Set future version to $version")
        futureVersionTag = version.toString()
    }
    sections = listOf(
        SectionDefinition("Features", "feature request"),
        SectionDefinition("Bugfix", listOf("bug", "bug fix")),
        SectionDefinition("Enhancement", "enhancement")
    ) // no custom sections by default, but default sections are prepended
    includeLabels = listOf("feature request", "bug", "bug fix", "enhancement")
    excludeLabels = listOf("duplicate", "invalid", "question", "wontfix")
    sinceTag = "V3.0.0"
    skipTags = listOf(

    )
    useMilestoneAsTag = true
    timezone = DEFAULT_TIMEZONE

    outputFile = file("${projectDir}/doc/CHANGELOG.md")
}

task("createGithubReleaseNotes") {
    doLast {
        val githubReleaseNoteFile = file("./githubReleaseNote.md")
        val content = "**" + file("${projectDir}/doc/CHANGELOG.md").readText()
            .substringAfter("**").substringBefore("##").trim()
        githubReleaseNoteFile.writeText(content)
    }
}
tasks.withType(KotlinCompile::class.java).configureEach {
    kotlinOptions {
        jvmTarget = "1.8" // Set the JVM target to match the bytecode you are inlining
    }
}
tasks.buildSearchableOptions {
    enabled = false
}