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
package com.qcadoo.mes.technologies.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;

@Service
public class ProductDetailsListenersT {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";



    // TODO MAKU Fix passing values to another view
    public final void addTechnologyGroup(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologyGroups");

        String url = "../page/technologies/technologyGroupDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showTechnologiesWithTechnologyGroup(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        Entity technologyGroup = product.getBelongsToField("technologyGroup");

        if (technologyGroup == null) {
            return;
        }

        String technologyGroupNumber = technologyGroup.getStringField(NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("technologyGroupNumber", technologyGroupNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologies");

        String url = "../page/technologies/technologiesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showTechnologiesWithProduct(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        String productNumber = product.getStringField(NUMBER);

        if (productNumber == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("window.productId", product.getId());

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologies");

        String url = "../page/technologies/productTechnologiesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showTechnologiesWithFamilyProduct(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        Entity productDb = product.getDataDefinition().get(product.getId());
        Entity parent = productDb.getBelongsToField(ProductFields.PARENT);

        if (Objects.isNull(parent)) {
            return;
        }

        String productNumber = parent.getStringField(NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("productNumber", applyInOperator(productNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologies");

        String url = "../page/technologies/technologiesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showProductGroupTechnologies(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        Entity parent = product.getBelongsToField(ProductFields.PARENT);
        if (parent == null) {
            return;
        }

        String productNumber = parent.getStringField(NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("productNumber", applyInOperator(productNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologies");

        String url = "../page/technologies/technologiesList.html";
        view.redirectTo(url, false, true, parameters);
    }
    
    public final void showTechnologiesWithUsingProduct(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", product.getId());

        String url = "../page/technologies/technologiesWithUsingProductList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value){
        StringBuilder builder = new StringBuilder();
        return builder.append("[").append(value).append("]").toString();
    }
}
