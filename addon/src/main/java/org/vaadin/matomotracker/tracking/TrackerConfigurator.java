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

/**
 * Programmatically configures a Matomo tracker if implemented by the
 * application's main router layout.
 */
public interface TrackerConfigurator {
    /**
     * Configure a tracker by updating the provided configuration.
     * 
     * @param configuration
     *            the configuration to update, not <code>null</code>
     */
    void configureTracker(TrackerConfiguration configuration);
}
