package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "my-app", variant = Lumo.DARK)
public class Application implements AppShellConfigurator {

    // Note, you should probably start TestApplication class form the src/test/java directory
    // instead! That starts a PostgreSQL container for testing purposes and populates some test data
    // This is for deployment/production time only. To make it succeed, you need a domain name and
    // a valid SSL certificate for it and a PostgreSQL database to connect to.
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
