package org.vaadin.matomotracker.tracking.demo;

import org.vaadin.matomotracker.tracking.MatomoTracker;
import org.vaadin.matomotracker.tracking.IgnoreMatomoTracker;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(layout = MainLayout.class)
@PageTitle("Ignored view")
@IgnoreMatomoTracker
public class IgnoredView extends VerticalLayout
        implements AfterNavigationObserver {
    public IgnoredView() {
        add(new Text("Ignored view"));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        MatomoTracker.getCurrent().sendPageView("custom/location","Custom Title");
    }
}
