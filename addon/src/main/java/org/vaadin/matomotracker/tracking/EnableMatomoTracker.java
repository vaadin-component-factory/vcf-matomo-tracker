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
