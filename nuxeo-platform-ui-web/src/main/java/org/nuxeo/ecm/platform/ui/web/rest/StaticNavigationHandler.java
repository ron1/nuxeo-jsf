/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id: StaticNavigationHandler.java 21462 2007-06-26 21:16:36Z sfermigier $
 */

package org.nuxeo.ecm.platform.ui.web.rest;

import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ConfigNavigationCase;

/**
 * View id helper that matches view ids and outcomes thanks to navigation cases
 * defined in a faces-config.xml file.
 */
public class StaticNavigationHandler {

    private static final Log log = LogFactory.getLog(StaticNavigationHandler.class);

    private final HashMap<String, String> outcomeToViewId =
    		new HashMap<String, String>();

    private final HashMap<String, String> viewIdToOutcome =
    		new HashMap<String, String>();

	public StaticNavigationHandler(ServletContext context) {
		ApplicationAssociate associate = ApplicationAssociate
				.getCurrentInstance();
		for (List<ConfigNavigationCase> cases : associate
				.getNavigationCaseListMappings().values()) {
			for (ConfigNavigationCase cnc : cases) {
				String toViewId = cnc.getToViewId();
				String fromOutcome = cnc.getFromOutcome();
				outcomeToViewId.put(fromOutcome, toViewId);
				viewIdToOutcome.put(toViewId, fromOutcome);
			}
		}
	}

    public String getOutcomeFromViewId(String viewId) {
        if (viewId == null) {
            return null;
        }
		viewId = viewId.replace(".faces", ".xhtml");
		if (viewIdToOutcome.containsKey(viewId)) {
			return viewIdToOutcome.get(viewId);
		}
        return viewId;
    }

    public String getViewIdFromOutcome(String outcome) {
        if (outcome == null) {
            return null;
        }
		if (outcomeToViewId.containsKey(outcome)) {
			return outcomeToViewId.get(outcome).replace(".xhtml", ".faces");
		}
        return "/" + outcome + ".faces";
    }

}
