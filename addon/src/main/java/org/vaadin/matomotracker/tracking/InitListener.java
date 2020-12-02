/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.matomotracker.tracking;

import java.util.List;

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
                    tracker.sendPageView(
                            "/" + navigationEvent.getLocation().getPathWithQueryParameters(),
                            navigationEvent.getLocation().getPath());
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
