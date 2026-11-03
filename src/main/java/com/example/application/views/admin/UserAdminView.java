package com.example.application.views.admin;

import com.example.application.data.Role;
import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.example.application.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.firitin.components.button.DeleteButton;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A minimal user management for the demo: add users, see their passkeys, delete them.
 */
@Route("users")
@PageTitle("Users")
@Menu(order = 2, icon = LineAwesomeIconUrl.USERS_SOLID)
@RolesAllowed("ADMIN")
public class UserAdminView extends VerticalLayout {

    private final UserService userService;
    private final AuthenticatedUser authenticatedUser;
    private final UserGrid grid;

    public UserAdminView(UserService userService, AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        this.grid = new UserGrid();
        add(new Paragraph("New users sign in first with a one-time token, then register their own passkeys."));
        add(new NewUserForm());
        addAndExpand(grid);
        setSizeFull();
        grid.refresh();
    }

    class NewUserForm extends HorizontalLayout {
        private final TextField username = new TextField("Username");
        private final TextField name = new TextField("Full name");
        private final CheckboxGroup<Role> roles = new CheckboxGroup<>("Roles", Role.values());

        NewUserForm() {
            roles.setValue(Set.of(Role.USER));
            roles.addThemeVariants(CheckboxGroupVariant.AURA_HORIZONTAL);
            add(username, name, roles, new Button("Add user", e -> addUser()) {{
                addThemeVariants(ButtonVariant.PRIMARY);
            }});
            setAlignItems(FlexComponent.Alignment.BASELINE);
            setWrap(true);
        }

        private void addUser() {
            try {
                User user = userService.create(username.getValue().trim(), name.getValue().trim(), roles.getValue());
                Notification.show("Added " + user.getUsername());
                username.clear();
                name.clear();
                roles.setValue(Set.of(Role.USER));
                grid.refresh();
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.ERROR);
            }
        }
    }

    class UserGrid extends Grid<User> {
        UserGrid() {
            addColumn(User::getUsername).setHeader("Username").setAutoWidth(true);
            addColumn(User::getName).setHeader("Full name").setAutoWidth(true);
            addColumn(user -> user.getRoles().stream().map(Role::name).sorted()
                    .collect(Collectors.joining(", "))).setHeader("Roles").setAutoWidth(true);
            addColumn(userService::passkeyCount).setHeader("Passkeys").setAutoWidth(true);
            addComponentColumn(user -> new DeleteButton(() -> {
                userService.delete(user);
                refresh();
            }) {{
                setAriaLabel("Delete user " + user.getUsername());
                // Deleting yourself would end in a broken session
                setEnabled(!user.getUsername().equals(currentUsername()));
            }}).setAutoWidth(true).setFlexGrow(0);
        }

        void refresh() {
            setItems(userService.findAll());
        }
    }

    private String currentUsername() {
        return authenticatedUser.get().map(User::getUsername).orElse(null);
    }
}
