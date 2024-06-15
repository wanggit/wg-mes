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
package com.qcadoo.mes.orders.states;

import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;

public class OrderStateValidationServiceTest {

    private static final String L_MISSING_MESSAGE = "orders.order.orderStates.fieldRequired";

    private static final String L_TECHNOLOGY_WRONG_STATE = "orders.validate.technology.error.wrongState.accepted";

    private static final String L_WRONG_DEADLINE = "orders.validate.global.error.deadline";

    private static final String L_WRONG_EFFECTIVE_DATE_TO = "orders.validate.global.error.effectiveDateTo";

    private OrderStateValidationService orderStateValidationService;

    @Mock
    private Entity order;

    @Mock
    private Entity technology;

    @Mock
    private StateChangeContext stateChangeContext;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        orderStateValidationService = new OrderStateValidationService();

        given(stateChangeContext.getOwner()).willReturn(order);
        stubTechnologyField(technology);
        stubTechnologyState(TechnologyState.ACCEPTED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationAccepted() throws Exception {
        // when
        orderStateValidationService.validationOnAccepted(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationInProgress() throws Exception {
        // when
        orderStateValidationService.validationOnInProgress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationCompleted() throws Exception {
        // when
        orderStateValidationService.validationOnCompleted(null);
    }

    @Test
    public void shouldPerformValidationCompleted() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");
        given(order.getField(EFFECTIVE_DATE_FROM)).willReturn(new Date(System.currentTimeMillis() - 10000));

        // when
        orderStateValidationService.validationOnCompleted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.eq(L_MISSING_MESSAGE));
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.eq(L_WRONG_EFFECTIVE_DATE_TO));
    }

    private void stubTechnologyField(final Entity value) {
        given(order.getBelongsToField(TECHNOLOGY)).willReturn(value);
        given(order.getField(TECHNOLOGY)).willReturn(value);
    }

    private void stubTechnologyState(final TechnologyState technologyState) {
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(technologyState.getStringValue());
        given(technology.getField(TechnologyFields.STATE)).willReturn(technologyState.getStringValue());
    }
}