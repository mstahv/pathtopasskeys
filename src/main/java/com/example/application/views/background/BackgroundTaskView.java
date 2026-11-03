package com.example.application.views.background;

import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Who is the user, when a background task updates the UI (with push)?
 * <p>
 * Quarkus knows the user only during an HTTP request, but a background task
 * that updates the UI with {@code UI.access()} runs without one. Using the
 * {@link SecurityIdentity} there fails. This view shows the problem and two
 * ways around it.
 */
@Route("background")
@PageTitle("Background task")
@Menu(order = 3, icon = LineAwesomeIconUrl.HOURGLASS_HALF_SOLID)
@PermitAll
public class BackgroundTaskView extends VerticalLayout {

    private final Div results = new Div();

    public BackgroundTaskView(SecurityIdentity identity, AuthenticatedUser authenticatedUser) {
        add(new Paragraph("""
                Who is the user in a background task? Quarkus finds out the signed-in user for \
                each HTTP request, from the login cookie. But a background task that updates the UI \
                later, with UI.access() and push, runs without an HTTP request, so there is no \
                SecurityIdentity to ask from."""));
        add(new Paragraph("""
                Each button below starts a slow background task, which reports back to the UI and \
                tells who it worked for. The first one fails, the other two show ways around the \
                problem: read the user before the task starts, or use AuthenticatedUser, which \
                remembers the user in the VaadinSession. (With Spring Security, Vaadin does the latter \
                for you.)"""));

        // Fails: there is no request scope for the SecurityIdentity in UI.access() from a background thread
        add(new Button("Problem: SecurityIdentity in the task", e ->
                runInBackground(() -> identity.getPrincipal().getName())));

        // Workaround 1: read the user while handling the click (an HTTP request), before the task starts
        add(new Button("Workaround 1: read the user before the task", e -> {
            String username = identity.getPrincipal().getName();
            runInBackground(() -> username);
        }));

        // Workaround 2: AuthenticatedUser falls back to the user remembered in the VaadinSession
        add(new Button("Workaround 2: AuthenticatedUser in the task", e ->
                runInBackground(() -> authenticatedUser.getUsername().orElse("nobody"))));

        add(results);
    }

    private void runInBackground(Supplier<String> whoAmI) {
        UI ui = UI.getCurrent();
        CompletableFuture.runAsync(() -> {
            // ... something slow here, e.g. a call to an external service ...
            ui.access(() -> {
                try {
                    results.add(new Paragraph("Task done for " + whoAmI.get()));
                } catch (RuntimeException ex) {
                    results.add(new Paragraph("Task failed: " + ex.getClass().getSimpleName()));
                }
            });
        }, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
    }
}
