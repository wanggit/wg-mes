/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ReportColumnWidthFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Component
public class ReportColumnWidthLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ReportColumnWidthLoader.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void loadReportColumnWidths() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Report column width table will be populated ...");
            }
            readDataFromXML();
        }
    }

    private void readDataFromXML() {
        LOG.info("Loading data from reportColumnWidth.xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();

            Document document = builder.build(getReportColumnWidthXmlFile());
            Element rootNode = document.getRootElement();

            @SuppressWarnings("unchecked")
            List<Element> nodes = rootNode.getChildren("row");

            for (Element node : nodes) {
                parseAndAddReportColumnWidth(node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void parseAndAddReportColumnWidth(final Element node) {
        @SuppressWarnings("unchecked")
        List<Attribute> attributes = node.getAttributes();

        Map<String, String> values = Maps.newHashMap();

        for (Attribute attribute : attributes) {
            values.put(attribute.getName().toLowerCase(Locale.ENGLISH), attribute.getValue());
        }

        addReportColumnWidth(values);
    }

    private void addReportColumnWidth(final Map<String, String> values) {
        DataDefinition reportColumnWidthDD = getReportColumnWidthDD();

        Entity reportColumnWidth = reportColumnWidthDD.create();

        reportColumnWidth.setField(ReportColumnWidthFields.IDENTIFIER,
                values.get(ReportColumnWidthFields.IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        reportColumnWidth.setField(ReportColumnWidthFields.NAME,
                values.get(ReportColumnWidthFields.NAME.toLowerCase(Locale.ENGLISH)));
        reportColumnWidth.setField(ReportColumnWidthFields.CHAR_TYPE,
                values.get(ReportColumnWidthFields.CHAR_TYPE.toLowerCase(Locale.ENGLISH)));
        reportColumnWidth.setField(ReportColumnWidthFields.PARAMETER, parameterService.getParameter());

        reportColumnWidth = reportColumnWidthDD.save(reportColumnWidth);

        if (reportColumnWidth.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Report column width saved {reportColumnWidth : " + reportColumnWidth.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved report column width entity have validation errors - "
                    + values.get(ReportColumnWidthFields.IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        }
    }

    private boolean databaseHasToBePrepared() {
        return getReportColumnWidthDD().find().list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getReportColumnWidthDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_REPORT_COLUMN_WIDTH);
    }

    private InputStream getReportColumnWidthXmlFile() throws IOException {
        return ReportColumnWidthLoader.class.getResourceAsStream("/basic/model/data/reportColumnWidth.xml");
    }

}
