plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.t1"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    //logging-spring-boot-starter
    implementation("ru.t1.grigiv.starter:t1-logger-starter:0.0.1-SNAPSHOT")

    //Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    //Validation
    implementation ("org.springframework.boot:spring-boot-starter-validation")

    //DB
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")

    //Kafka
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams")

    //Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    //Mail
    implementation("org.springframework.boot:spring-boot-starter-mail")

    //Thymeleaf
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    //Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.withType<Test> {
    useJUnitPlatform()
}
