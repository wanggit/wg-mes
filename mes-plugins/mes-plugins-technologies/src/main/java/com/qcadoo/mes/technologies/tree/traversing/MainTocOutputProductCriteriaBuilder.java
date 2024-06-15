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
package com.qcadoo.mes.technologies.tree.traversing;

import static com.qcadoo.model.api.search.SearchRestrictions.and;
import static com.qcadoo.model.api.search.SearchRestrictions.eqField;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;
import static com.qcadoo.model.api.search.SearchRestrictions.or;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;

@Service
public class MainTocOutputProductCriteriaBuilder {

    public static final class Aliases {

        private Aliases() {
        }

        public static final String OPERATION_PROD_OUT_COMPONENT = "opoc_alias";

        public static final String OPERATION_OUTPUT_PRODUCT = "opocProd_alias";

        public static final String TOC = "toc_alias";

        public static final String TECHNOLOGY = "tech_alias";

        public static final String TOC_PARENT = "tocParent_alias";

        private static final String TOC_PARENT_OPIC = "tocParentOpic_alias";

        private static final String TOC_PARENT_INPUT_PRODUCT = "tocParentOpicProduct_alias";

        private static final String TECHNOLOGY_PRODUCT = "technologyProduct_alias";
    }

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public SearchCriteriaBuilder create(final MasterOutputProductCriteria criteria) {
        SearchCriteriaBuilder tocScb = getTocDataDefinition().findWithAlias(Aliases.TOC);
        applyCriteriaIfPresent(tocScb, criteria.getTocCriteria());

        SearchCriteriaBuilder opocScb = tocScb.createCriteria(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS,
                Aliases.OPERATION_PROD_OUT_COMPONENT, JoinType.INNER);
        applyCriteriaIfPresent(opocScb, criteria.getOpocCriteria());

        SearchCriteriaBuilder prodScb = opocScb.createCriteria(OperationProductOutComponentFields.PRODUCT,
                Aliases.OPERATION_OUTPUT_PRODUCT, JoinType.INNER);
        applyCriteriaIfPresent(prodScb, criteria.getProdCriteria());

        SearchCriteriaBuilder techScb = tocScb.createCriteria(TechnologyOperationComponentFields.TECHNOLOGY, Aliases.TECHNOLOGY,
                JoinType.INNER);
        applyCriteriaIfPresent(techScb, criteria.getTechCriteria());

        SearchCriteriaBuilder parentTocScb = tocScb.createCriteria(TechnologyOperationComponentFields.PARENT, Aliases.TOC_PARENT,
                JoinType.LEFT);
        applyCriteriaIfPresent(parentTocScb, criteria.getParentTocCriteria());

        SearchCriteriaBuilder parentOpicScb = parentTocScb.createCriteria(
                TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS, Aliases.TOC_PARENT_OPIC, JoinType.LEFT);
        parentOpicScb.createCriteria(OperationProductInComponentFields.PRODUCT, Aliases.TOC_PARENT_INPUT_PRODUCT, JoinType.LEFT);
        applyCriteriaIfPresent(parentOpicScb, criteria.getParentOpicCriteria());
        techScb.createCriteria(TechnologyFields.PRODUCT, Aliases.TECHNOLOGY_PRODUCT, JoinType.INNER);
        SearchCriterion productIsConsumedByParentOp = eqField(Aliases.OPERATION_OUTPUT_PRODUCT + ".id",
                Aliases.TOC_PARENT_INPUT_PRODUCT + ".id");
        SearchCriterion opIsRootAndItsProductMatchTechProduct = and(
                isNull(Aliases.TOC + "." + TechnologyOperationComponentFields.PARENT),
                eqField(Aliases.TECHNOLOGY_PRODUCT + ".id", Aliases.OPERATION_OUTPUT_PRODUCT + ".id"));

        tocScb.add(or(productIsConsumedByParentOp, opIsRootAndItsProductMatchTechProduct));

        return tocScb;
    }

    private void applyCriteriaIfPresent(final SearchCriteriaBuilder scb, final Optional<SearchCriterion> criteria) {
        for (SearchCriterion searchCriterion : criteria.asSet()) {
            scb.add(searchCriterion);
        }
    }

    private DataDefinition getTocDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

}
