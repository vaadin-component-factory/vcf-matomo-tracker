package org.vaadin.matomotracker.tracking;

/*
 * #%L
 * Vaadin Matomo Tracker add-on for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

/**
 * Automatically registers a navigation listener that sends page views to Matomo.
 */
public class InitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInit -> {
            UI ui = uiInit.getUI();

            ui.addAfterNavigationListener(navigationEvent -> {
                MatomoTracker tracker = MatomoTracker.get(ui);                
                if (shouldTrack(tracker, navigationEvent)) {
                    tracker.sendPageView(navigationEvent.getLocation().getPathWithQueryParameters(),navigationEvent.getLocation().getPath());
                }
            });
        });
    }

    private static boolean shouldTrack(MatomoTracker tracker, AfterNavigationEvent navigationEvent) {
        if (hasIgnore(navigationEvent)) {
            return false;
        }

        /*
         * Track if tracker is already initialized or if it can be initialized
         * based on the current navigation event.
         */
        return tracker.isInitialized() || canInitialize(navigationEvent);
    }

    private static boolean canInitialize(AfterNavigationEvent navigationEvent) {
        List<HasElement> routerChain = navigationEvent.getActiveChain();
        if (routerChain.isEmpty()) {
            return false;
        }

        Class<? extends HasElement> rootLayoutClass = getRootLayout(routerChain);

        return rootLayoutClass.getAnnotation(EnableMatomoTracker.class) != null
                || TrackerConfigurator.class.isAssignableFrom(rootLayoutClass);
    }

    private static Class<? extends HasElement> getRootLayout(List<HasElement> routerChain) {
        return routerChain.get(routerChain.size() - 1).getClass();
    }

    private static boolean hasIgnore(AfterNavigationEvent navigationEvent) {
        return navigationEvent.getActiveChain().stream().anyMatch(InitListener::hasIgnoreAnnotation);
    }

    private static boolean hasIgnoreAnnotation(HasElement target) {
        return target.getClass().getAnnotation(IgnoreMatomoTracker.class) != null;
    }
}
