package com.example.application.views.about;

import com.example.application.security.AuthenticatedUser;
import com.example.application.views.Profile;
import com.example.application.views.login.LoginView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Code;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("About")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE)
@AnonymousAllowed
public class AboutView extends VerticalLayout {

    public AboutView(AuthenticatedUser authenticatedUser) {
        setSpacing(false);

        add(new Image("images/empty-plant.png", "placeholder plant") {{
            setWidth("200px");
        }});

        add(new H2("From passwords to passkeys") {{
            getStyle().setMarginTop("var(--vaadin-gap-xl)").setMarginBottom("var(--vaadin-gap-m)");
        }});
        add(new Paragraph("""
                This demo app shows how a Vaadin + Spring Security app can drop passwords and \
                log users in with passkeys instead, using one-time tokens as a fallback."""));

        if (authenticatedUser.get().isEmpty()) {
            add(new Paragraph(new Text("Try it with a demo account, "), new Code("user"), new Text(" or "),
                    new Code("admin"), new Text(": sign in first with a one-time token, register a passkey "
                    + "in your profile, then sign out and back in with the passkey.")));
            add(new Button("Sign in", e -> UI.getCurrent().navigate(LoginView.class)) {{
                addThemeVariants(ButtonVariant.PRIMARY);
            }});
        } else {
            add(new Paragraph("Next step: register a passkey in your profile, then sign out and back in with it."));
            add(new Button("Go to profile", e -> UI.getCurrent().navigate(Profile.class)) {{
                addThemeVariants(ButtonVariant.PRIMARY);
            }});
        }

        add(new Paragraph(new Anchor("https://github.com/mstahv/pathtopasskeys", "Source code on GitHub",
                AnchorTarget.BLANK)) {{
            getStyle().setMarginTop("var(--vaadin-gap-l)");
        }});

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().setTextAlign(Style.TextAlign.CENTER).setMaxWidth("40em").setMargin("0 auto");
    }

}
