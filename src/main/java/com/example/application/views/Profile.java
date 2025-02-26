package com.example.application.views;

import com.example.application.data.User;
import com.example.application.data.WebAuthnRecord;
import com.example.application.security.AuthenticatedUser;
import com.example.application.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.vaadin.firitin.components.button.DeleteButton;
import org.vaadin.firitin.components.notification.VNotification;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;

@Route
@Menu(order = 1, icon = LineAwesomeIconUrl.USER)
@PermitAll
public class Profile extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;

    public Profile(UserService userService, AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        initView();
    }

    private void initView() {
        removeAll();
        User user = authenticatedUser.get().get(); // Who came up with this API 🤣
        add(new H1("Profile: " + user.getUsername()));

        add(new Paragraph("Full name: " + user.getName()));

        add(new H3("Your Roles:"));
        user.getRoles().forEach(role -> add(new Paragraph(role.name())));

        add(new H3("Passkeys:"));

        List<WebAuthnRecord> passkeys = authenticatedUser.getPasskeys();
        if (passkeys.isEmpty()) {
            add(new Paragraph("No passkeys registered"));
        } else {
            passkeys.forEach(passkey -> {
                add(new HorizontalLayout() {{
                    add(new Paragraph(passkey.asCredentialRecord().getLabel()));
                    add(new DeleteButton(() -> {
                        authenticatedUser.deletePasskey(passkey);
                        initView();
                    }));
                }});
            });
        }

        add(new Button("Register new passkey", e -> {
            authenticatedUser.startWebAuthnRegistration()
                    .thenRun(() -> {
                        VNotification.prominent("Passkey registered!");
                        initView();
                    });
        }));

    }
}
