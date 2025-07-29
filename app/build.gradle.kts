plugins {
    alias(libs.plugins.android.application)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.firebasevideostreamingapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.firebasevideostreamingapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    //enabling ViewBinding 
    buildFeatures { viewBinding = true }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))

    /* Add the dependencies for Firebase products you want to use
     When using the BoM, don't specify versions in Firebase dependencies*/
    implementation("com.google.firebase:firebase-analytics")

    // FirebaseUI for Firebase Realtime Database
    implementation("com.firebaseui:firebase-ui-database:7.1.1")


    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    //Exoplayer Version
    val version_exoplayer = "2.16.1"

    //Exoplayer libraries
    implementation("com.google.android.exoplayer:exoplayer:$version_exoplayer")
    implementation("com.google.android.exoplayer:exoplayer-core:$version_exoplayer")
    implementation("com.google.android.exoplayer:exoplayer-dash:$version_exoplayer")
    implementation("com.google.android.exoplayer:exoplayer-hls:$version_exoplayer")
    implementation("com.google.android.exoplayer:exoplayer-smoothstreaming:$version_exoplayer")
    implementation("com.google.android.exoplayer:exoplayer-ui:$version_exoplayer")

    //Picasso library
    implementation("com.squareup.picasso:picasso:2.8")

    //Expandable TextView Dependency
    implementation("io.github.glailton.expandabletextview:expandabletextview:1.0.4")

    //Circular ImageView Dependency
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //glide library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // GIF dependency
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.29")


    //    ExoPlayer media3 dependency
//    implementation("androidx.media3:media3-exoplayer:1.5.0")
//    implementation("androidx.media3:media3-exoplayer-dash:1.5.0")
//    implementation("androidx.media3:media3-ui:1.5.0")


}