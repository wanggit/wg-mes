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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EndProductGroupingContainer extends AbstractGroupingContainer {

    public EndProductGroupingContainer(final Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductInColumnToAlignment,
            final Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationComponentIdProductOutColumnToAlignment,
            final String titleAppend) {
        super(orderColumnToAlignment, operationComponentIdProductInColumnToAlignment,
                operationComponentIdProductOutColumnToAlignment, titleAppend);
    }

    @Override
    public void add(Entity order, Entity operationComponent, List<Entity> productionCountingQuantitiesIn,
            List<Entity> productionCountingQuantitiesOut) {

    }

    @Override
    public void add(final OrderOperationComponent orderOperationComponent) {
        Entity technology = orderOperationComponent.getOrder().getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(technology)) {
            return;
        }

        store(title(technology), orderOperationComponent);
    }

    private String title(final Entity technology) {
        Entity endProduct = technology.getBelongsToField(TechnologyFields.PRODUCT);
        String endProductName = endProduct.getStringField(ProductFields.NAME);

        return new StringBuilder(titleAppend).append(" ").append(endProductName).toString();
    }

}
