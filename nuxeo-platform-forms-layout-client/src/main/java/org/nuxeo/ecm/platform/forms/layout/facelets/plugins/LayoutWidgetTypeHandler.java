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
package org.nuxeo.ecm.platform.forms.layout.facelets.plugins;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.platform.forms.layout.api.BuiltinWidgetModes;
import org.nuxeo.ecm.platform.forms.layout.api.FieldDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.exceptions.WidgetException;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.forms.layout.facelets.LayoutTagHandler;
import org.nuxeo.ecm.platform.forms.layout.facelets.LeafFaceletHandler;
import org.nuxeo.ecm.platform.forms.layout.facelets.TagConfigFactory;
import org.nuxeo.ecm.platform.forms.layout.facelets.ValueExpressionHelper;
import org.nuxeo.ecm.platform.ui.web.component.seam.UIHtmlText;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.TagConfig;

/**
 * Widget rendering a layout
 *
 * @author Anahide Tchertchian
 * @Since 5.4
 */
public class LayoutWidgetTypeHandler extends AbstractWidgetTypeHandler {

    private static final long serialVersionUID = 1L;

    @Override
    public FaceletHandler getFaceletHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget, FaceletHandler[] subHandlers)
            throws WidgetException {
        FaceletHandlerHelper helper = new FaceletHandlerHelper(ctx, tagConfig);
        String widgetId = widget.getId();
        String widgetMode = widget.getMode();

        TagAttributes attributes = helper.getTagAttributes(widget,
                Arrays.asList(new String[] { "mode" }), true);
        attributes = FaceletHandlerHelper.addTagAttribute(attributes,
                helper.createAttribute("id", widgetId));

        // add mode attribute
        String modeValue;
        Serializable modeFromProps = widget.getProperty("mode");
        if ((modeFromProps instanceof String)
                && !StringUtils.isBlank((String) modeFromProps)) {
            modeValue = (String) modeFromProps;
        } else {
            modeValue = widgetMode;
        }
        attributes = FaceletHandlerHelper.addTagAttribute(attributes,
                helper.createAttribute("mode", modeValue));

        // value is required on layout tag => generate it to map the parent
        // widget or layout value if not set through field definitions (or
        // explicitly through properties)
        FieldDefinition[] fields = widget.getFieldDefinitions();
        Serializable valueFromProps = widget.getProperty("value");
        if ((fields == null || fields.length == 0) && valueFromProps == null) {
            TagAttribute valueAttr = helper.createAttribute(
                    "value",
                    ValueExpressionHelper.createExpressionString(
                            widget.getValueName(), null));
            attributes = FaceletHandlerHelper.addTagAttribute(attributes,
                    valueAttr);
        }

        FaceletHandler leaf = new LeafFaceletHandler();
        String widgetTagConfigId = widget.getTagConfigId();
        TagConfig layoutTagConfig = TagConfigFactory.createTagConfig(tagConfig,
                widgetTagConfigId, attributes, leaf);
        FaceletHandler res = new LayoutTagHandler(layoutTagConfig);
        if (BuiltinWidgetModes.PDF.equals(widgetMode)) {
            // add a surrounding p:html tag handler
            return helper.getHtmlComponentHandler(widgetTagConfigId,
                    new TagAttributes(new TagAttribute[0]), res,
                    UIHtmlText.class.getName(), null);
        } else {
            return res;
        }

    }

}
