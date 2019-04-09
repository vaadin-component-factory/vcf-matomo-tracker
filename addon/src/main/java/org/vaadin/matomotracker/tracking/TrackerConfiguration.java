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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for a Matomo tracker. By default, the configuration
 * is created based on an @{@link EnableMatomoTracker} annotation on the
 * application's outermost router layout class. The layout class can also
 * implement {@link TrackerConfigurator} to declaratively update the
 * configuration.
 */
public class TrackerConfiguration {
    /**
     * The default cookie domain value.
     */
    public static final String DEFAULT_COOKIE_DOMAIN = "";
    public static final String DEFAULT_SITE_ID = "";

    private String trackingUrl;
    private String siteId = DEFAULT_SITE_ID;
    private String cookieDomain = DEFAULT_COOKIE_DOMAIN;
    private String pageViewPrefix = "";

    private final Map<String, Serializable> createParameters = new LinkedHashMap<>();

    private TrackerConfiguration() {
        // Create through static factory methods
    }

    /**
     * Gets the Matomo tracking url with ID in use.
     * 
     * @return the tracking url, not <code>null</code>
     */
    public String getTrackingUrl() {
        return trackingUrl;
    }

    /**
     * Sets the Matomo tracking url with ID to use.
     * 
     * @param trackingUrl
     *            the tracking url, to use, not <code>null</code> and not an empty
     *            string
     * @return this configuration, for chaining
     */
    public TrackerConfiguration setTrackingUrl(String trackingUrl) {
        if (trackingUrl == null || trackingUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking id must be defined");
        }

        this.trackingUrl = trackingUrl;

        return this;
    }

    /**
     * Gets the cookie domain setting.
     * 
     * @return the cookie domain setting, not <code>null</code>
     */
    public String getCookieDomain() {
        return cookieDomain;
    }

    /**
     * Sets the cookie domain value to use.
     * 
     * @see <a href=
     *      "https://developer.matomo.org/guides/tracking-javascript-guide">Reference
     *      documentation</a>
     * 
     * @param cookieDomain
     *            the cookie domain value to set, or <code>null</code> to
     *            restore the default value.
     * @return this configuration, for chaining
     */
    public TrackerConfiguration setCookieDomain(String cookieDomain) {
        this.cookieDomain = Objects.requireNonNull(cookieDomain);
        return this;
    }

    /**
     * Sets a prefix that will be added to the location of all tracked page
     * views.
     * 
     * @param pageViewPrefix
     *            a page view prefix to use, not <code>null</code>
     * @return this configuration, for chaining
     */
    public TrackerConfiguration setPageViewPrefix(String pageViewPrefix) {
        this.pageViewPrefix = Objects.requireNonNull(pageViewPrefix);
        return this;
    }

    /**
     * Gets the current page view prefix.
     * 
     * @return the current page view prefix, not <code>null</code>
     */
    public String getPageViewPrefix() {
        return pageViewPrefix;
    }

    /**
     * Sets the Site ID to be used with the Matomo script.
     * 
     * @param siteId
     *            the script URL to use, not <code>null</code>
     * @return this configuration, for chaining
     */
    public TrackerConfiguration setSiteId(String siteId) {
        this.siteId = Objects.requireNonNull(siteId);
        return this;
    }

    /**
     * Gets the Site ID used with Matomo script.
     * 
     * @return Currently used Site ID
     */
    public String getSiteId() {
        return siteId;
    }

    /**
     * Sets a custom field value to use when creating the client-side tracker.
     * 
     * @see <a href=
     *      "https://developer.matomo.org/guides/tracking-javascript-guide">Reference
     *      documentation</a>
     * 
     * @param name
     *            the name of the field to set, not <code>null</code>
     * @param value
     *            the field value
     * @return this configuration, for chaining
     */
    public TrackerConfiguration setCreateField(String name, Serializable value) {
        createParameters.put(Objects.requireNonNull(name), value);
        return this;
    }

    /**
     * Removes a create field value.
     * 
     * @see #setCreateField(String, Serializable)
     * 
     * @param name
     *            the name of the field, not <code>null</code>
     * @return this configuration, for chaining
     */
    public TrackerConfiguration removeCreateField(String name) {
        createParameters.remove(Objects.requireNonNull(name));
        return this;
    }

    /**
     * Gets all the custom fields to pass when creating the client-side tracker.
     * 
     * @see #setCreateField(String, Serializable)
     * 
     * @return an unmodifiable map of parameter values, not <code>null</code>
     */
    public Map<String, Serializable> getCreateFields() {
        return Collections.unmodifiableMap(createParameters);
    }

    /**
     * Creates a tracker configuration with default settings based on a log
     * level and whether to actually enable sending commands to Matomo
     * 
     * @return a newly created tracker configuration, not <code>null</code>
     */
    public static TrackerConfiguration create() {
        TrackerConfiguration config = new TrackerConfiguration();
        return config;
    }

    /**
     * Creates a tracker configuration based on annotation values.
     * 
     * @param annotation
     *            the configuration annotation to use
     * @return a newly created tracker configuration, not <code>null</code>
     */
    public static TrackerConfiguration fromAnnotation(EnableMatomoTracker annotation) {

        TrackerConfiguration config = create();

        config.setTrackingUrl(annotation.value());
        config.setSiteId(annotation.siteId());
        config.setCookieDomain(annotation.cookieDomain());
        config.setPageViewPrefix(annotation.pageviewPrefix());

        return config;
    }
}
