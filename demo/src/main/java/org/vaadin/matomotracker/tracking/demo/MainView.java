package org.vaadin.matomotracker.tracking.demo;

import org.vaadin.matomotracker.tracking.MatomoTracker;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(layout = MainLayout.class)
@PageTitle("Main view")
public class MainView extends VerticalLayout {
    public MainView() {
        add(new Text("Main view"), new Button("Send an event", click -> {
            MatomoTracker.getCurrent().sendEvent("Examples",
                    "Event button");
        }));
    }
}
