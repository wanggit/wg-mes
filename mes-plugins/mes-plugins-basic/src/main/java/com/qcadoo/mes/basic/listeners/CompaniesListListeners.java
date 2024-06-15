/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.listeners;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CompaniesListListeners {

    private static final String L_ACTIONS = "actions";

    private static final String L_DELETE = "delete";

    @Autowired
    private CompanyService companyService;

    public void openCompaniesImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/basic/companiesImport.html");

        view.openModal(url.toString());
    }

    public void assignToGroupABC(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.companiesIds", gridComponent.getSelectedEntitiesIds().stream().map(String::valueOf).collect(Collectors.joining(",")));

        StringBuilder url = new StringBuilder("../page/basic/assignCompanyToGroupABC.html");

        view.openModal(url.toString(), parameters);
    }

    public void disabledRibbonForOwnerOrExternal(final ViewDefinitionState view, final ComponentState state,
                                                 final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> companies = grid.getSelectedEntities();

        boolean isEnabled = true;

        String buttonMessage = "basic.company.isOwner";

        for (Entity company : companies) {
            Entity companyFromDB = companyService.getCompany(company.getId());

            if (Objects.nonNull(companyFromDB)) {
                isEnabled = !companyService.isCompanyOwner(companyFromDB);

                if (!StringUtils.isEmpty(company.getStringField(CompanyFields.EXTERNAL_NUMBER))) {
                    buttonMessage = "basic.company.isExternalNumber";

                    isEnabled = false;
                }
            } else {
                isEnabled = false;
            }

            if (!isEnabled) {
                break;
            }
        }

        companyService.disableButton(view, L_ACTIONS, L_DELETE, isEnabled, buttonMessage);
    }

}
