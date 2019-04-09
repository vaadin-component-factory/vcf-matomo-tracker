package org.vaadin.matomotracker.tracking.demo;

import org.vaadin.matomotracker.tracking.EnableMatomoTracker;
import org.vaadin.matomotracker.tracking.TrackerConfiguration;
import org.vaadin.matomotracker.tracking.TrackerConfigurator;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;

@EnableMatomoTracker(value = "place your local Matomo site url", siteId = "site id")
public class MainLayout extends VerticalLayout implements RouterLayout, TrackerConfigurator {
    public MainLayout() {
        add(new HorizontalLayout(new RouterLink("Main view", MainView.class),
                new RouterLink("Second view", SecondView.class), new RouterLink("Ignored view", IgnoredView.class)));
    }

    @Override
    public void configureTracker(TrackerConfiguration configuration) {
    	// configuration.setPageViewPrefix(pageViewPrefix)
    }
}
