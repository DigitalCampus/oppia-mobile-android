import java.util.Properties
import java.io.FileInputStream
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.tasks.Exec

plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("plugin.allopen")
    id("jacoco")
    id("kotlin-kapt")
}

apply(from = "../jacoco.gradle")
apply(from = "../sonarqube.gradle")


repositories {
    maven { url = uri("https://jitpack.io") }
}

android {

    val runtimeProps = Properties()
    runtimeProps.load(FileInputStream(rootProject.file("oppia-default.properties")))

    val oppiaPropsFile = rootProject.file("custom.properties")
    if (oppiaPropsFile.canRead()) {
        logger.lifecycle("Fetching properties from external file")
        runtimeProps.load(FileInputStream(oppiaPropsFile))
    } else {
        logger.error("No properties file found. Using default values.")
    }

    compileSdk = 33

    defaultConfig {
        versionCode = 111
        versionName = "7.4.6"

        applicationId = "org.digitalcampus.mobile.learning"

        minSdk = 21
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        testInstrumentationRunnerArguments["coverage"] = "true"
        testInstrumentationRunnerArguments["useTestStorageService"] = "true"

        vectorDrawables.useSupportLibrary = true


        val interfaceLangs = runtimeProps["INTERFACE_LANGUAGE_OPTIONS"].toString().split(",")
        resourceConfigurations += interfaceLangs
        // Language region format in android resources requires 'r' char
        interfaceLangs.map { lang ->
            lang.replace("-", "-r")
        }
        // resConfigs "en", "es", "ar", "fi", "fr", "hi", "ur-rPK", "am-rET", "om-rET"
    }

    packagingOptions {
        resources {
            excludes += listOf("META-INF/LICENSE.txt", "META-INF/NOTICE.txt", "README.md")
        }
    }

    lint {
        checkReleaseBuilds = true
        // Disable checks for String translations
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }

    compileOptions {

        // Configure to use Java8 features like try-with-resources, lambdas...
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        // Flag to enable support for the new language Java 8+ APIs: time, functional, concurrent...
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {

        all {
            //Define all the constant values that depend on the config properties

            var analyticsLibrary = "none"
            if (runtimeProps.containsKey("ANALYTICS_LIBRARY")){
                analyticsLibrary = runtimeProps["ANALYTICS_LIBRARY"] as String
                if (analyticsLibrary == ""){ analyticsLibrary = "none" }
            }
            if (analyticsLibrary == "none"){
                logger.error("No analytics library configured, will use empty value")
                runtimeProps["ANALYTICS_LIBRARY"] = analyticsLibrary
            }

            for (field in runtimeProps.keys()) {
                val value = runtimeProps.getProperty(field as String, "")

                val fieldType = when {
                    value.toLowerCase() == "true" || value.toLowerCase() == "false"  -> "boolean"
                    value.toIntOrNull() != null -> "int"
                    else -> "String"
                }

                buildConfigField(fieldType, field, if (fieldType == "String") "\"$value\"" else value  )
            }

            resValue("string", "MENU_ALLOW_LOGOUT_DEFAULT_VALUE", "\"${runtimeProps["MENU_ALLOW_LOGOUT"]}\"")
            resValue("string", "MENU_ALLOW_COURSE_DOWNLOAD_DEFAULT_VALUE", "\"${runtimeProps["MENU_ALLOW_COURSE_DOWNLOAD"]}\"")
            resValue("string", "MENU_ALLOW_LANGUAGE_DEFAULT_VALUE", "\"${runtimeProps["MENU_ALLOW_LANGUAGE"]}\"")
            resValue("string", "START_COURSEINDEX_COLLAPSED", "\"${runtimeProps["START_COURSEINDEX_COLLAPSED"]}\"")
            resValue("string", "prefServerDefault", "\"${runtimeProps["OPPIA_SERVER_DEFAULT"]}\"")
            resValue("string", "oppiaServerHost", "\"${runtimeProps["OPPIA_SERVER_HOST"]}\"")
            resValue("string", "oppiaServerDomain", "\"${runtimeProps["OPPIA_SERVER_DOMAIN"]}\"")
            resValue("string", "prefAdminPasswordDefault", "\"${runtimeProps["ADMIN_PROTECT_INITIAL_PASSWORD"]}\"")
            resValue("string", "prefGamificationPointsAnimationDefault", "\"${runtimeProps["GAMIFICATION_POINTS_ANIMATION"]}\"")
            resValue("string", "prefDurationGamificationPointsViewDefault", "\"${runtimeProps["DURATION_GAMIFICATION_POINTS_VIEW"]}\"")
            resValue("string", "SHOW_COURSE_DESCRIPTION", "\"${runtimeProps["SHOW_COURSE_DESCRIPTION"]}\"")
            resValue("string", "prefCoursesReminderIntervalDefault", "\"${runtimeProps["DEFAULT_REMINDER_INTERVAL"]}\"")
            resValue("string", "prefCoursesReminderTimeDefault", "\"${runtimeProps["DEFAULT_REMINDER_TIME"]}\"")
            resValue("string", "prefCoursesReminderDaysDefaultSerialized", "\"${runtimeProps["DEFAULT_REMINDER_DAYS"]}\"")
            resValue("string", "prefShowGamificationEventsDefault", "\"${runtimeProps["SHOW_GAMIFICATION_EVENTS"]}\"")
            resValue("string", "prefUpdateActivityOnLoginDefault", "\"${runtimeProps["UPDATE_ACTIVITY_ON_LOGIN"]}\"")
        }

        debug {
            multiDexEnabled = true
            enableUnitTestCoverage = true
            enableAndroidTestCoverage
            isDebuggable = true
        }

        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true

        tasks.withType<Test>().configureEach {
            extensions.configure(JacocoTaskExtension::class) {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }

        managedDevices {
            devices {
                maybeCreate<ManagedVirtualDevice>("pixel2api30").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 2"
                    // ATDs currently support only API level 30.
                    apiLevel = 30
                    // You can also specify "google-atd" if you require Google Play Services.
                    systemImageSource = "aosp"
                }

                maybeCreate<ManagedVirtualDevice>("nexusSapi28").apply {
                    device = "Nexus S"
                    apiLevel = 28
                    systemImageSource = "aosp"
                }

                maybeCreate<ManagedVirtualDevice>("nexus5api33").apply {
                    device = "Nexus 5"
                    apiLevel = 33
                    systemImageSource = "google"
                }
            }

            groups {
                maybeCreate("phones").apply {
                    targetDevices.add(devices["nexus5api33"])
                    targetDevices.add(devices["pixel2api30"])
                    targetDevices.add(devices["nexusSapi28"])
                }
            }
        }
    }

    flavorDimensions += "main"

    productFlavors {
        create("normal") {
            dimension = "main"
        }
    }

    useLibrary("android.test.mock")
    namespace = "org.digitalcampus.mobile.learning"

    android.buildFeatures.viewBinding = true
}

dependencies {
    val workVersion = "2.7.1"
    val roomVersion = "2.3.0"
    val fragmentVersion = "1.5.4"
    val daggerVersion = "2.41"
    val appcompatVersion = "1.6.0-rc01"
    val espressoVersion = "3.5.1"
    val kotlinVersion = "1.8.20"

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation ("joda-time:joda-time:2.10.13")
    implementation ("com.google.android.material:material:1.7.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    implementation("androidx.core:core:1.9.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.preference:preference:1.2.0")

    implementation("androidx.appcompat:appcompat:${appcompatVersion}")

    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    implementation("androidx.work:work-runtime:${workVersion}")
    implementation("com.google.guava:guava:31.1-android")
    androidTestImplementation("androidx.work:work-testing:${workVersion}")

    implementation("javax.xml.stream:stax-api:1.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("ly.count.android:sdk:20.11.8")
    implementation("io.github.inflationx:calligraphy3:3.1.1")
    implementation("io.github.inflationx:viewpump:2.0.3")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation("com.github.badoualy:stepper-indicator:1.0.7")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.hbb20:ccp:2.6.0") //Phone code picker

    //Dagger Dependencies
    implementation("com.google.dagger:dagger:${daggerVersion}")
    implementation("com.google.dagger:dagger-android:${daggerVersion}")
    implementation("com.google.dagger:dagger-android-support:${daggerVersion}")
    kapt("com.google.dagger:dagger-android-processor:${daggerVersion}")
    kapt("com.google.dagger:dagger-compiler:${daggerVersion}")
    androidTestImplementation("com.github.fabioCollini.daggermock:daggermock:0.8.5")

    implementation("com.squareup.inject:assisted-inject-annotations-dagger2:0.8.1")

    //JUnit Dependencies
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("org.json:json:20220924")

    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.annotation:annotation:1.3.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
    androidTestUtil("androidx.test.services:test-services:1.4.2")

    //Espresso Dependencies
    androidTestImplementation("androidx.test.espresso:espresso-core:${espressoVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-web:${espressoVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-intents:${espressoVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:${espressoVersion}") {
        exclude(module = "support-annotations")
        exclude(module = "support-v4")
        exclude(module = "support-v13")
        exclude(module = "recyclerview-v7")
        exclude(module = "appcompat-v7")
    }

    // Fragment testing
    implementation("androidx.fragment:fragment:${fragmentVersion}")
    debugImplementation("androidx.test:core:1.5.0")
    debugImplementation("androidx.fragment:fragment-testing:${fragmentVersion}")

    //Mockito Dependencies
    testImplementation("org.mockito:mockito-core:4.3.1")

    // Awaitility
    androidTestImplementation("org.awaitility:awaitility:4.2.0")

    val multidex_version = "2.0.1"
    implementation("androidx.multidex:multidex:${multidex_version}")
    androidTestImplementation("com.google.dexmaker:dexmaker:1.2")
    androidTestImplementation("com.google.dexmaker:dexmaker-mockito:1.2")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // RoomDB Dependencies
    implementation("androidx.room:room-runtime:${roomVersion}")
    kapt("androidx.room:room-compiler:${roomVersion}")

    // Kotlin Dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
}

tasks.register<Exec>("grantPermissions") {
    dependsOn("installNormalDebug")

    group = "test"
    description = "Grant permissions for testing."

    val adb = android.adbExecutable
    if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
        logger.lifecycle("Granting permissions for a non-windows OS")
    } else {
        logger.lifecycle("Granting permissions for a Windows OS")
    }

    commandLine("$adb shell pm grant ${android.defaultConfig.applicationId} android.permission.SET_ANIMATION_SCALE".split(" "))
    commandLine("$adb shell pm grant ${android.defaultConfig.applicationId} android.permission.SET_ANIMATION_SCALE".split(" "))
}

tasks.matching {
    name.startsWith("connectedAndroidTest") ||
    name.startsWith("connectedDebugAndroidTest") ||
    name.startsWith("assembleDebugAndroidTest") ||
    name.startsWith("assembleAndroidTest")
}.configureEach {
    dependsOn(":app:grantPermissions")
}

android.applicationVariants.all {
    val hasTest = gradle.startParameter.taskNames.any { it.contains("test") || it.contains("Test") }
    if (hasTest) {
        apply(plugin = "kotlin-allopen")
        configure<org.jetbrains.kotlin.allopen.gradle.AllOpenExtension> {
            annotation("org.digitalcampus.oppia.annotations.Mockable")
        }
    }
}
