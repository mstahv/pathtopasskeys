package com.example.application.views;

import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.example.application.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.vaadin.firitin.components.notification.VNotification;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Route
@Menu(order = 1, icon = LineAwesomeIconUrl.USER)
@PermitAll
public class Profile extends VerticalLayout {

    public Profile(UserService userService, AuthenticatedUser authenticatedUser) {
        User user = authenticatedUser.get().get(); // Who came up with this API 🤣
        add(new H1("Profile:" + user.getUsername()));

        add(new Paragraph("Full name: " + user.getName()));

        add(new H3("Your Roles:"));
        user.getRoles().forEach(role -> add(new Paragraph(role.name())));

        add(new H3("Change password:"));

        var newPassword = new PasswordField("New password");
        var newPassword2 = new PasswordField("Repeat password");
        add(
                newPassword,
                newPassword2,
                new Button("Change password") {{
                    addClickListener(e -> {
                        if (newPassword.getValue().equals(newPassword2.getValue())) {
                            // Change password
                            userService.changePassword(user, newPassword.getValue());
                            VNotification.prominent("Password changed!");
                            newPassword.clear();
                            newPassword2.clear();
                        } else {
                            // Show error
                            VNotification.prominent("Passwords do not match!");
                        }
                    });
                }}
        );

    }
}
