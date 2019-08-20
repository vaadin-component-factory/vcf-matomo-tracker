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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.JsonCodec;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.shared.ui.LoadMode;
import com.vaadin.pro.licensechecker.LicenseChecker;

import elemental.json.JsonObject;

/**
 * Sends commands to Matomo in the browser. An instance of the tracker
 * can be retrieved from a given UI instance ({@link #get(UI)}) or for the
 * current UI instance ({@link #getCurrent()}).
 * <p>
 * Page view commands will automatically be sent for any Flow navigation if the
 * tracker can be configured.
 * <p>
 * The first time any command is sent, the tracker will configure itself based
 * on the top-level router layout in the corresponding UI. The layout should be
 * annotated with @{@link EnableMatomoTracker} or implement
 * {@link TrackerConfigurator} for the configuration to succeed.
 */
public class MatomoTracker {
    private final UI ui;

    private boolean inited = false;

    private String pageViewPrefix = "";

    private static String PROJECT_VERSION = "1.0.0";
    private static String PROJECT_NAME = "vaadin-matomo-tracker"; 
    
    /**
     * List of actions to send before the next Flow response is created.
     * Initialization can only happen after routing has completed since the
     * top-level layout can only be identified at that point. This queue is only
     * needed for actions that are issues before initialization has happened,
     * but it is still used in all cases to keep the internal logic simpler.
     */
    private ArrayList<Serializable[]> pendingActions = new ArrayList<>();

    private MatomoTracker(UI ui) {
        this.ui = ui;
    }

    /**
     * Gets or creates a tracker for the current UI.
     * 
     * @see UI#getCurrent()
     * 
     * @return the tracker for the current UI, or <code>null</code> if there is
     *         no current UI
     */
    public static MatomoTracker getCurrent() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return null;
        }
        return get(ui);
    }

    /**
     * Gets or creates a tracker for the given UI.
     * 
     * @param ui
     *            the UI for which to get at tracker, not <code>null</code>
     * @return the tracker for the given ui
     */
    public static MatomoTracker get(UI ui) {
        MatomoTracker tracker = ComponentUtil.getData(ui, MatomoTracker.class);
        if (tracker == null) {
            tracker = new MatomoTracker(ui);
            ComponentUtil.setData(ui, MatomoTracker.class, tracker);
        }
        return tracker;
    }

    private void verifyLicense(boolean productionMode) {
        if (!productionMode) {
            LicenseChecker.checkLicense(PROJECT_NAME, PROJECT_VERSION);
            UsageStatistics.markAsUsed(PROJECT_NAME, PROJECT_VERSION);
        }
    }
    
    private void init() {
    	verifyLicense(ui.getSession().getConfiguration().isProductionMode());
    	
        TrackerConfiguration config = createConfig(ui);

        if (config == null) {
            throw new IllegalStateException(
                    "There are pending actions for a tracker that isn't initialized and cannot be initialized automatically. Ensure there is a @"
                            + EnableMatomoTracker.class.getSimpleName()
                            + " on the application's main layout or that it implements "
                            + TrackerConfigurator.class.getSimpleName() + ".");
        }

        String trackingUrl = config.getTrackingUrl();
        if (trackingUrl == null || trackingUrl.isEmpty()) {
            throw new IllegalStateException("No tracking url has been defined.");
        }

        String siteId = config.getSiteId();
        if (siteId == null || siteId.isEmpty()) {
            throw new IllegalStateException("No site id has been defined.");
        }
        
        pageViewPrefix = config.getPageViewPrefix();

       	ui.getPage().addJavaScript(trackingUrl+"/matomo.js", LoadMode.EAGER);
       	
        ui.getPage()
                .executeJavaScript(
                		"Matomo.addTracker();"
                		+ "var _paq = window._paq || [];"
                		+ "_paq.push(['enableLinkTracking']);"
                		+ "var u='"+ trackingUrl +"/';"
                		+ "_paq.push(['setTrackerUrl', u+'matomo.php']);"
                		+ "_paq.push(['setSiteId', '" + siteId + "']);");

        if (!config.getCookieDomain().isEmpty())
        		sendAction(createAction("setCookieDomain", config.getCookieDomain()));

        inited = true;
    }

    private static TrackerConfiguration createConfig(UI ui) {
        TrackerConfiguration config = null;

        HasElement routeLayout = findRouteLayout(ui);

        EnableMatomoTracker annotation = routeLayout.getClass().getAnnotation(EnableMatomoTracker.class);

        if (annotation != null) {
            config = TrackerConfiguration.fromAnnotation(annotation);
        }

        if (routeLayout instanceof TrackerConfigurator) {
            if (config == null) {
                // Use same defaults as in the annotation
                TrackerConfiguration.create();
            }

            ((TrackerConfigurator) routeLayout).configureTracker(config);
        }

        return config;
    }

    private static HasElement findRouteLayout(UI ui) {
        List<HasElement> routeChain = ui.getInternals().getActiveRouterTargetsChain();
        if (routeChain.isEmpty()) {
            throw new IllegalStateException("Cannot initialize when no router target is active");
        }
        return routeChain.get(routeChain.size() - 1);
    }

    private void sendAction(Serializable[] action) {
        /*
         * Append prefix for page views. This is done in the send phase so that
         * the prefix is considered also if the page view was created before the
         * prefix was read from the config.
         */
        if (!pageViewPrefix.isEmpty()) {
            if (action.length == 2 && "setCustomUrl".equals(action[0])) {
                action[1] = pageViewPrefix + action[1];
            }
        }
        if (action.length == 1) {
        	ui.getPage().executeJavaScript("_paq.push(['"+action[0]+"']);");
        } else {
        	String command = "_paq.push(['"+action[0];
            for (int i=1;i<action.length;i++)
            	command = command + "','"+action[i];
            command = command + "']);";
    		ui.getPage().executeJavaScript(command);        	
        }
    }

    private static Serializable[] createAction(String command,
            Serializable... fields) {
        if (fields == null) {
            fields = new Serializable[] { null };
        }

        Stream<Serializable> argsStream = Stream.concat(Stream.of(command), Stream.of(fields));

        return argsStream.toArray(Serializable[]::new);
    }

    private static JsonObject toJsonObject(Map<String, ? extends Serializable> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        return JsonUtils.createObject(map, JsonCodec::encodeWithoutTypeInfo);
    }

    /**
     * Sends a generic command to Matomo. 
     * 
     * @param command
     *            the name of the command to send, not <code>null</code>
     * @param fields
     *            a list of field values to send
     */
    public void matomo(String command, Serializable... fields) {
        if (pendingActions.isEmpty()) {
            ui.beforeClientResponse(ui, context -> {
                if (!inited) {
                    init();
                }
                pendingActions.forEach(this::sendAction);
                pendingActions.clear();
            });
        }

        pendingActions.add(createAction(command, fields));
    }

    /**
     * Sends a page view command to Matomo.
     * 
     * @param location
     *            the location of the viewed page, not <code>null</code>
     */
    public void sendPageView(String location) {
        sendPageView(location, null);
    }

    /**
     * Sends a page view command with custom page title to Matomo.
     * See <a href="https://developer.matomo.org/guides/tracking-javascript-guide">JavaScript Tracking Client</a> and 
     * <a href="https://developer.matomo.org/guides/spa-tracking">Single-Page Application Tracking</a>
     *   
     * 
     * @param location
     *            the location of the viewed page, not <code>null</code>
     * @param title
     *            Page title
     */
    public void sendPageView(String location, String title) {
        matomo("setCustomUrl", location);
        if (title != null) matomo("setDocumentTitle", title);
        matomo("deleteCustomVariables","page");
        matomo("setGenerationTimeMs",0);
        matomo("trackPageView");
    }

    /**
     * Sends an event command with the given category and action.
     * See <a href="https://developer.matomo.org/guides/tracking-javascript-guide">JavaScript Tracking Client</a>  
     * <a href="https://matomo.org/docs/event-tracking/#tracking-events">Event Tracking</a>
     * 
     * @param category
     *            the category name, not <code>null</code>
     * @param action
     *            the action name, not <code>null</code>
     */
    public void sendEvent(String category, String action) {
        matomo("trackEvent", category, action);
    }

    /**
     * Sends an event command with the given category, action and label.
     * See <a href="https://developer.matomo.org/guides/tracking-javascript-guide">JavaScript Tracking Client</a> and 
     * <a href="https://matomo.org/docs/event-tracking/#tracking-events">Event Tracking</a>
     * 
     * @param category
     *            the category name, not <code>null</code>
     * @param action
     *            the action name, not <code>null</code>
     * @param label
     *            the event label, not <code>null</code>
     */
    public void sendEvent(String category, String action, String label) {
        matomo("trackEvent", category, action, label);
    }

    /**
     * Sends an event command with the given category, action, label and value.
     * See <a href="https://developer.matomo.org/guides/tracking-javascript-guide">JavaScript Tracking Client</a> and 
     * <a href="https://matomo.org/docs/event-tracking/#tracking-events">Event Tracking</a>
     * 
     * @param category
     *            the category name, not <code>null</code>
     * @param action
     *            the action name, not <code>null</code>
     * @param label
     *            the event label, not <code>null</code>
     * @param value
     *            the event value
     */
    public void sendEvent(String category, String action, String label, double value) {
        matomo("trackEvent", category, action, label, Double.valueOf(value));
    }


    /**
     * Checks whether this tracker has been initialized.
     * 
     * @return <code>true</code> if this tracker is initialized, otherwise
     *         <code>false</code>
     */
    public boolean isInitialized() {
        return inited;
    }
}
