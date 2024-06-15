/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.supplyNegotiations.listeners;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CompanyDetailsListenersSN {



    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    public void redirectToFilteredOffersList(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {

        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity company = companyForm.getEntity();

        if (company.getId() == null) {
            return;
        }

        String supplierName = company.getStringField("name");

        Map<String, String> filters = Maps.newHashMap();
        filters.put("supplierName", supplierName);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.offer");

        String url = "../page/supplyNegotiations/offersList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void redirectToFilteredRequestsList(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {

        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity company = companyForm.getEntity();

        if (company.getId() == null) {
            return;
        }

        String supplierName = company.getStringField("name");

        Map<String, String> filters = Maps.newHashMap();
        filters.put("supplierName", supplierName);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.requestsForQuotation");

        String url = "../page/supplyNegotiations/requestForQuotationsList.html";
        view.redirectTo(url, false, true, parameters);
    }

}
