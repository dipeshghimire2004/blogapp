plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.blogapp"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    //JPA for database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //r2dbc
//    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
//    runtimeOnly("io.r2dbc:r2dbc-postgresql:0.8.21.RELEASE")

    //security/ passkey
    implementation("org.springframework.boot:spring-boot-starter-security:3.5.0")
    implementation("com.yubico:webauthn-server-core:2.5.0")
    testImplementation("org.springframework.security:spring-security-test")

    implementation("org.springframework.boot:spring-boot-devtools:3.4.3")


    implementation("com.vladmihalcea:hibernate-types-52:2.21.1")

    // AWS
    implementation("software.amazon.awssdk:s3:2.31.6")

    //redis
//    implementation("org.springframework.boot:spring-boot-starter-data-redis:3.4.4")

    // LocalStack (AWS SDK for S3 emulation)
//    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE") // AWS S3 support
//    implementation("org.springframework.cloud:spring-cloud-starter:4.2.1")

    //lombok
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    //uuid-creator
    implementation("com.github.f4b6a3:uuid-creator:6.1.1")

    //jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    implementation("org.springframework.boot:spring-boot-starter-security:3.4.4")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

    //mapstruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    //localstack
    implementation("com.amazonaws:aws-java-sdk:1.12.782")
//    implementation("com.amazonaws:aws-java-sdk:2.28.29")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.782")



    // https://mvnrepository.com/artifact/org.testcontainers/localstack
    testImplementation("org.testcontainers:localstack:1.20.6")

    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
//tasks.withType<Jar> {
//    archiveFileName.set("blogapp.jar") // Name of the output JAR.
//    manifest {
//        attributes["Main-Class"] = "com.example.blogapp.BlogappApplication"
//    }
//}