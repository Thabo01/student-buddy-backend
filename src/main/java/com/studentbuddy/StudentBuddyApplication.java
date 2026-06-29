package com.studentbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for Student Buddy.
 *
 * @SpringBootApplication bundles three annotations:
 *  - @Configuration       : this class can define beans
 *  - @EnableAutoConfiguration : Spring Boot wires sensible defaults from the classpath
 *  - @ComponentScan       : scans com.studentbuddy.* for components (controllers, services, etc.)
 */
@SpringBootApplication
public class StudentBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentBuddyApplication.class, args);
    }
}
