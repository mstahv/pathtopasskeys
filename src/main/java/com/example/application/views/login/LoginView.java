package com.example.application.views.login;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Code;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends VerticalLayout {

    public LoginView(OttButton ottButton) {
        add(new PasskeyIcon());
        add(new H2("Sign in to PathToPasskeys") {{
            getStyle().setMarginTop("var(--vaadin-gap-l)");
        }});
        add(new Paragraph("""
                A demo of passkey login with Vaadin and Spring Security. A passkey replaces your \
                password: your device unlocks it with a fingerprint, face or screen lock, and it \
                can't be phished, guessed or leaked."""));
        add(new DemoAccountsHint());
        add(new LoginWithPasskeyButton() {{
            getStyle().setMarginTop("var(--vaadin-gap-m)");
        }});
        add(new Paragraph("No passkey yet, or on another device?") {{
            getStyle().setColor("var(--vaadin-text-color-secondary)")
                    .setMargin("var(--vaadin-gap-m) 0 0");
        }});
        add(ottButton);
        setSpacing(false);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().setTextAlign(Style.TextAlign.CENTER).setMaxWidth("34rem").setMargin("0 auto");
    }

    /**
     * A fingerprint in a circle, like the illustration in the About view.
     */
    static class PasskeyIcon extends Div {
        PasskeyIcon() {
            add(new SvgIcon(LineAwesomeIconUrl.FINGERPRINT_SOLID) {{
                setSize("72px");
                getStyle().setColor("var(--aura-accent-color)");
            }});
            getStyle().setDisplay(Style.Display.FLEX)
                    .setAlignItems(Style.AlignItems.CENTER)
                    .setJustifyContent(Style.JustifyContent.CENTER)
                    .setWidth("140px").setHeight("140px")
                    .setBorderRadius("50%")
                    .setBackground("var(--vaadin-background-container)")
                    .setFlexShrink("0");
        }
    }

    /**
     * This is a demo app without user registration, so tell how to get in.
     */
    static class DemoAccountsHint extends Div {
        DemoAccountsHint() {
            add(new H4("Demo accounts") {{
                getStyle().setMargin("0");
            }});
            add(new Paragraph(new Text("Use "), new Code("user"), new Text(" or "), new Code("admin"),
                    new Text(". Sign in first with a one-time token, then register a passkey in the Profile view. " +
                            "After that, \"Use passkey\" works.")) {{
                getStyle().setMargin("var(--vaadin-gap-xs) 0 0");
            }});
            getStyle()
                    .setPadding("var(--vaadin-padding-s) var(--vaadin-padding-m)")
                    .setBorderRadius("var(--vaadin-radius-m)")
                    .setBackground("var(--vaadin-background-container)")
                    .setColor("var(--vaadin-text-color-secondary)");
        }
    }
}
