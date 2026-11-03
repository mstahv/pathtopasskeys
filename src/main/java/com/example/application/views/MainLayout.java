package com.example.application.views;

import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.dom.Style;

import java.util.List;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;

    private final AuthenticatedUser authenticatedUser;

    public MainLayout(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.getStyle().setFontSize("var(--aura-font-size-l)").setMargin("0");

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("PathToPasskeys");
        appName.getStyle().setFontWeight(Style.FontWeight.BOLDER).setFontSize("var(--aura-font-size-l)");
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            if (entry.icon() != null) {
                nav.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
            } else {
                nav.addItem(new SideNavItem(entry.title(), entry.path()));
            }
        });

        return nav;
    }

    private Footer createFooter() {
        return new Footer(authenticatedUser.get()
                .<Component>map(UserInfo::new)
                .orElseGet(() -> new Anchor("login", "Sign in")));
    }

    /**
     * The current user, with a button to sign out. With only one action, a
     * visible button is easier to find than a menu.
     */
    private class UserInfo extends Div {
        UserInfo(User user) {
            add(new Avatar(user.getName()) {{
                if (user.getProfilePicture() != null) {
                    setImageHandler(event -> {
                        event.setContentType("image/jpeg");
                        event.getOutputStream().write(user.getProfilePicture());
                    });
                }
                getStyle().setFlexShrink("0");
            }});
            add(new Span(user.getName()) {{
                // Long names are cut with an ellipsis instead of pushing the button out
                getStyle().setFlexGrow("1").setMinWidth("0")
                        .setOverflow(Style.Overflow.HIDDEN)
                        .setWhiteSpace(Style.WhiteSpace.NOWRAP)
                        .set("text-overflow", "ellipsis");
            }});
            add(new Button(VaadinIcon.SIGN_OUT.create(), e -> authenticatedUser.logout()) {{
                addThemeVariants(ButtonVariant.TERTIARY);
                setAriaLabel("Sign out");
                setTooltipText("Sign out");
            }});
            setWidthFull();
            getStyle().setDisplay(Style.Display.FLEX)
                    .setAlignItems(Style.AlignItems.CENTER)
                    .setGap("var(--vaadin-gap-s)");
        }
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        viewTitle.setText(getCurrentPageTitle());
    }
}
