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
package com.qcadoo.mes.masterOrders.validators;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriterion;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DEADLINE_FOR_ORDER_BASED_ON_DELIVERY_DATE;
import static com.qcadoo.model.api.search.SearchRestrictions.*;

@Service
public class MasterOrderValidators {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    public boolean onValidate(final DataDefinition masterOrderDD, final Entity masterOrder) {
        boolean isValid = true;

        isValid = checkIfDatesAreOk(masterOrderDD, masterOrder) && isValid;
        isValid = checkIfCanChangeMasterOrderPrefixField(masterOrder) && isValid;

        return isValid;
    }

    public boolean checkIfDatesAreOk(final DataDefinition masterOrderDD, final Entity masterOrder) {
        Date startDate = masterOrder.getDateField(MasterOrderFields.START_DATE);
        Date finishDate = masterOrder.getDateField(MasterOrderFields.FINISH_DATE);

        if ((startDate != null) && (finishDate != null) && finishDate.before(startDate)) {
            masterOrder.addError(masterOrderDD.getField(OrderFields.FINISH_DATE),
                    "masterOrders.masterOrder.finishDate.isBeforeStartDate");

            return false;
        }

        return true;
    }

    private boolean checkIfCanChangeMasterOrderPrefixField(final Entity masterOrder) {
        Boolean orderNumbersPrefixIsNotRequired = !masterOrder.getBooleanField(MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER);

        return isNewlyCreated(masterOrder) || orderNumbersPrefixIsNotRequired
                || checkIfEachOrderHasNumberStartingWithMasterOrderNumber(masterOrder);
    }

    public boolean checkIfCanChangeCompany(final DataDefinition masterOrderDD, final FieldDefinition fieldDefinition,
                                           final Entity masterOrder, final Object fieldOldValue,
                                           final Object fieldNewValue) {

        if (isNewlyCreated(masterOrder) || areSame((Entity) fieldOldValue, (Entity) fieldNewValue)
                || checkIfCanSetCompany(masterOrder, fieldNewValue)) {
            return true;
        }

        masterOrder.addError(fieldDefinition, "masterOrders.masterOrder.company.orderAlreadyExists");

        return false;
    }

    public boolean checkIfCanSetCompany(final Entity masterOrder, final Object fieldNewValue) {
        boolean isValid = true;

        List<Entity> orders = masterOrderOrdersDataProvider.findBelongingOrders(masterOrder, null, null, null);

        if (orders.isEmpty()) {
            return isValid;
        }

        Entity companyInMasterOrder = (Entity) fieldNewValue;

        if (companyInMasterOrder == null) {
            return isValid;
        }

        for (Entity order : orders) {
            Entity companyInOrder = order.getBelongsToField(OrderFields.COMPANY);

            if (companyInOrder == null) {
                isValid = false;
            } else if (!companyInMasterOrder.getId().equals(companyInOrder.getId())) {
                isValid = false;
            }
        }

        return isValid;
    }

    public boolean checkIfCanChangeDeadline(final DataDefinition masterOrderDD, final FieldDefinition fieldDefinition,
                                            final Entity masterOrder, final Object fieldOldValue,
                                            final Object fieldNewValue) {
        if (isNewlyCreated(masterOrder) || areSame(fieldOldValue, fieldNewValue) || doesNotHaveAnyPendingOrder(masterOrder)
                || checkIfCanSetDeadline(masterOrder, fieldNewValue)) {
            return true;
        }

        masterOrder.addError(fieldDefinition, "masterOrders.masterOrder.deadline.orderAlreadyExists");

        return false;
    }

    public boolean checkIfCanSetDeadline(final Entity masterOrder, final Object fieldNewValue) {
        boolean isValid = true;
        Entity parameter = parameterService.getParameter();
        boolean deadlineForOrderBasedOnDeliveryDate = parameter.getBooleanField(DEADLINE_FOR_ORDER_BASED_ON_DELIVERY_DATE);
        if (deadlineForOrderBasedOnDeliveryDate) {
            return isValid;
        }

        List<Entity> orders = masterOrderOrdersDataProvider.findBelongingOrders(masterOrder, null, null, null);

        if (orders.isEmpty()) {
            return isValid;
        }

        Date deadlineInMasterOrder = (Date) fieldNewValue;

        if (deadlineInMasterOrder == null) {
            return isValid;
        }

        for (Entity order : orders) {
            Date deadlineInOrder = order.getDateField(OrderFields.DEADLINE);

            if (deadlineInOrder == null) {
                isValid = false;
            } else if (!deadlineInMasterOrder.equals(deadlineInOrder)) {
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean doesNotHaveAnyPendingOrder(final Entity masterOrder) {
        return (masterOrderOrdersDataProvider.countBelongingOrders(masterOrder,
                ne(OrderFields.STATE, OrderState.PENDING.getStringValue())) == 0);
    }

    private boolean checkIfEachOrderHasNumberStartingWithMasterOrderNumber(final Entity masterOrder) {
        String newMasterOrderNumber = masterOrder.getStringField(MasterOrderFields.NUMBER);

        SearchCriterion criteria = not(like(OrderFields.NUMBER, newMasterOrderNumber + "%"));

        Collection<String> unsupportedOrderNumbers = masterOrderOrdersDataProvider.findBelongingOrderNumbers(masterOrder,
                criteria);

        if (unsupportedOrderNumbers.isEmpty()) {
            return true;
        }

        addUnsupportedOrdersError(masterOrder, MasterOrderFields.NUMBER,
                "masterOrders.order.number.alreadyExistsOrderWithWrongNumber", unsupportedOrderNumbers);

        return false;
    }

    private void addUnsupportedOrdersError(final Entity targetEntity, final String errorTargetFieldName,
                                           final String errorMessageKey,
                                           final Collection<String> unsupportedOrderNumbers) {
        FieldDefinition errorTargetFieldDef = targetEntity.getDataDefinition().getField(errorTargetFieldName);

        targetEntity.addError(errorTargetFieldDef, errorMessageKey, StringUtils.join(unsupportedOrderNumbers, ", "));
    }

    private boolean isNewlyCreated(final Entity masterOrder) {
        return masterOrder.getId() == null;
    }

    private boolean areSame(final Object newValue, final Object oldValue) {
        return ObjectUtils.equals(newValue, oldValue);
    }

    private boolean areSame(final Entity newValue, final Entity oldValue) {
        return (newValue == null && oldValue == null)
                || (newValue != null && oldValue != null && ObjectUtils.equals(newValue.getId(), oldValue.getId()));
    }

}
