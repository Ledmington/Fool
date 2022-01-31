plugins {
    java
    antlr
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    implementation("com.google.guava:guava:30.1.1-jre")
    antlr("org.antlr:antlr4:4.5")
}

application {
    mainClass.set("Fool.App")
}

tasks.test {
    useJUnitPlatform()
}
