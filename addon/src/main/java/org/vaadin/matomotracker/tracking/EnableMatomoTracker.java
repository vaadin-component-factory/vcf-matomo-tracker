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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;

/**
 * Configuration annotation that enables automatic Matomo tracking if
 * present on the application's main layout.
 * <p>
 * Any navigation event will be tracked as a page view unless the @{@link Route}
 * class is also annotated with @{@link IgnoreMatomoTracker}. It is possible to
 * manually track page views or other events through the tracker instance
 * available through {@link MatomoTracker#getCurrent()} or
 * {@link MatomoTracker#get(UI)}.
 * <p>
 * This annotation only has properties for the most commonly used configuration
 * options. For low-level configuration or programmatic configuration, the
 * application's main layout can also implement {@link TrackerConfigurator}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableMatomoTracker {
    /**
     * The Matomo tracking Url with ID to use.
     * 
     * @return the tracking ID
     */
    String value();

    /**
     * The cookie domain setting. By default, none is used.
     * 
     * @return the cookie domain setting
     */
    String cookieDomain() default TrackerConfiguration.DEFAULT_COOKIE_DOMAIN;

    /**
     * The site ID setting. By default, "" is used.
     * 
     * @return the site id setting
     */
    String siteId() default TrackerConfiguration.DEFAULT_SITE_ID;

    /**
     * A prefix to add to the URL of all page views. By default, not prefix is
     * added.
     * 
     * @return the prefix to add to page view URLs
     */
    String pageviewPrefix() default "";

}
