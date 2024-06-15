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
package com.qcadoo.mes.workPlans.workPlansColumnExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.workPlans.WorkPlansColumnLoaderService;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;

@Component
public class WorkPlansColumnLoader {

    private static final Logger LOG = LoggerFactory.getLogger(WorkPlansColumnLoader.class);

    @Autowired
    private WorkPlansColumnLoaderService workPlansColumnLoaderService;

    public void setDefaulValues() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Default values will be populated ...");
        }

        workPlansColumnLoaderService.setParameterDefaultValues();
    }

    public void addWorkPlansColumnsForOrders() {
        if (!workPlansColumnLoaderService.isColumnsForOrdersEmpty()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for orders table will be populated ...");
        }

        workPlansColumnLoaderService.fillColumnsForOrders(WorkPlansConstants.PLUGIN_IDENTIFIER);
    }

    public void deleteWorkPlansColumnsForOrders() {
        if (workPlansColumnLoaderService.isColumnsForOrdersEmpty()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for orders table will be unpopulated ...");
        }

        workPlansColumnLoaderService.clearColumnsForOrders(WorkPlansConstants.PLUGIN_IDENTIFIER);
    }

    public void addWorkPlansColumnsForProducts() {
        if (!workPlansColumnLoaderService.isColumnsForProductsEmpty()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for input and output products table will be populated ...");
        }

        workPlansColumnLoaderService.fillColumnsForProducts(WorkPlansConstants.PLUGIN_IDENTIFIER);
    }

    public void deleteWorkPlansColumnsForProducts() {
        if (workPlansColumnLoaderService.isColumnsForProductsEmpty()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for input and output products table will be unpopulated ...");
        }

        workPlansColumnLoaderService.clearColumnsForProducts(WorkPlansConstants.PLUGIN_IDENTIFIER);
    }

}
