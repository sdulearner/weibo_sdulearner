plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.weibo_sunzhenyu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.weibo_sunzhenyu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

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

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //引用Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //引用EventBus
    implementation("org.greenrobot:eventbus:3.3.1")
    // 引用BaseRecyclerView
    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper:3.0.14")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // 引用Retrofit和Gson
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}