plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    implementation("org.projectlombok:lombok:1.18.20")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation("io.projectreactor:reactor-core:3.4.15")
    testImplementation( "org.awaitility:awaitility:4.2.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}