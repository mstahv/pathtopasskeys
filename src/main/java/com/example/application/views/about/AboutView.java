package com.example.application.views.about;

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

    public AboutView() {
        add(new Image("images/empty-plant.png", "placeholder plant") {{
            setWidth("200px");
        }});
        add(new H2("PathToPasskeys"));
        add(new Paragraph("An example app, step by step from passwords to passkeys."));
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().setTextAlign(Style.TextAlign.CENTER);
    }
}
