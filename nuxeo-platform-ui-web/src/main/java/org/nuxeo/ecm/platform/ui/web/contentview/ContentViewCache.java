/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.platform.ui.web.contentview;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuxeo.ecm.platform.ui.web.cache.LRUCachingMap;

/**
 * Cache for content views, handling cache keys set on content views.
 * <p>
 * Each content view instance will be cached if its cache key is not null. Each
 * instance will be cached using the cache key so its state is restored. Also
 * handles refresh of caches when receiving events contfigured on the content
 * view.
 *
 * @author Anahide Tchertchian
 */
public class ContentViewCache implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default cache size, set to 5 instances per content view
     */
    public static final Integer DEFAULT_CACHE_SIZE = new Integer(5);

    protected Map<String, String> namedCacheKeys = new HashMap<String, String>();

    protected Map<String, ContentView> namedContentViews = new HashMap<String, ContentView>();

    protected Map<String, Map<String, ContentView>> cacheInstances = new HashMap<String, Map<String, ContentView>>();

    /**
     * Map holding content view names that need to be invalidated for a given
     * event
     */
    protected Map<String, Set<String>> eventToContentViewName = new HashMap<String, Set<String>>();

    public void add(ContentView cView) {
        if (cView != null) {
            String cacheKey = cView.getCacheKey();
            if (cacheKey == null) {
                // no cache
                return;
            }
            String name = cView.getName();
            Integer cacheSize = cView.getCacheSize();
            if (cacheSize == null) {
                cacheSize = DEFAULT_CACHE_SIZE;
            }
            Map<String, ContentView> cacheEntry = cacheInstances.get(name);
            if (cacheEntry == null) {
                cacheEntry = new LRUCachingMap<String, ContentView>(
                        cacheSize.intValue());
            }
            cacheEntry.put(cacheKey, cView);
            cacheInstances.put(name, cacheEntry);
            namedCacheKeys.put(name, cacheKey);
            namedContentViews.put(name, cView);
            List<String> events = cView.getRefreshEventNames();
            if (events != null && !events.isEmpty()) {
                for (String event : events) {
                    if (eventToContentViewName.containsKey(event)) {
                        eventToContentViewName.get(event).add(name);
                    } else {
                        Set<String> set = new HashSet<String>();
                        set.add(name);
                        eventToContentViewName.put(event, set);
                    }
                }
            }
        }
    }

    public ContentView get(String name) {
        ContentView cView = namedContentViews.get(name);
        if (cView != null) {
            String oldCacheKey = namedCacheKeys.get(name);
            String newCacheKey = cView.getCacheKey();
            if (newCacheKey != null && !newCacheKey.equals(oldCacheKey)) {
                Map<String, ContentView> contentViews = cacheInstances.get(name);
                if (contentViews.containsKey(newCacheKey)) {
                    cView = contentViews.get(newCacheKey);
                    // refresh named caches
                    namedCacheKeys.put(name, newCacheKey);
                    namedContentViews.put(name, cView);
                } else {
                    // cache not here or expired => return null
                    return null;
                }
            }
        }
        return cView;
    }

    public void refreshOnEvent(String eventName) {
        if (eventName != null) {
            Set<String> contentViewNames = eventToContentViewName.get(eventName);
            if (contentViewNames != null) {
                for (String contentViewName : contentViewNames) {
                    cacheInstances.remove(contentViewName);
                    namedCacheKeys.remove(contentViewName);
                    namedContentViews.remove(contentViewName);
                }
            }
        }
    }

}