package com.example.application;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Application wide configuration of the Vaadin UI. Quarkus needs no main
 * method: run the app in development mode with {@code mvn} (quarkus:dev).
 */
// Aura is the default theme since Vaadin 25, styles.css (in META-INF/resources) has our few tweaks
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
@Push
public class Application implements AppShellConfigurator {
}
