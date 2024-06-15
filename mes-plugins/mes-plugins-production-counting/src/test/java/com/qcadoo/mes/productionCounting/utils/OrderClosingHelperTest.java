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
package com.qcadoo.mes.productionCounting.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchResult;

public class OrderClosingHelperTest {

    private static final int NUM_OF_OPERATIONS = 4;

    private static final Long PRODUCTION_RECORD_ID = 31781L;

    private OrderClosingHelper orderClosingHelper;

    @Mock
    private ParameterService parameterService;

    @Mock
    private DataDefinition productionTrackingDD;

    @Mock
    private Entity productionTracking, order, technology, parameter;

    @Before
    public final void init() {
        orderClosingHelper = new OrderClosingHelper();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(orderClosingHelper, "parameterService", parameterService);

        given(parameterService.getParameter()).willReturn(parameter);

        given(productionTracking.getId()).willReturn(PRODUCTION_RECORD_ID);
        given(productionTracking.getDataDefinition()).willReturn(productionTrackingDD);
        given(productionTracking.getField(ProductionTrackingFields.ORDER)).willReturn(order);
        given(productionTracking.getBelongsToField(ProductionTrackingFields.ORDER)).willReturn(order);

        EntityList tiocs = mockEntityList(NUM_OF_OPERATIONS);

        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)).willReturn(tiocs);
    }

    @Test
    public final void shouldOrderCanBeClosedWhenTypeIsCummulativeAndAcceptingLastRecord() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.CUMULATED);
        productionTrackingIsLast();

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertTrue(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsCummulativeAndAcceptingLastRecordButAutoCloseIsNotEnabled() {
        // given
        stubTypeOfProductionRecording(TypeOfProductionRecording.CUMULATED);
        productionTrackingIsLast();

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsCummulativeAndAcceptingNotLastRecord() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.CUMULATED);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndAcceptingNotLastRecord() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        stubSearchCriteriaResults(1L, 2L, 3L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndThereIsNotEnoughtLastRecords() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionTrackingIsLast();
        stubSearchCriteriaResults(1L, 2L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndRecordIsNotLastAndThereIsNotEnoughtLastRecords() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        stubSearchCriteriaResults(1L, 2L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanBeClosedWhenTypeIsForEachOpAndRecordIsLastAndThereIsEnoughtLastRecords() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionTrackingIsLast();
        stubSearchCriteriaResults(1L, 2L, 3L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertTrue(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndRecordIsLastAndThereIsEnoughtLastRecordsButAutoCloseIsNotEnabled() {
        // given
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionTrackingIsLast();
        stubSearchCriteriaResults(1L, 2L, 3L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanBeClosedWhenTypeIsForEachOpAndRecordIsLastAndThereIsMoreThanEnoughtLastRecords() {
        // given
        orderHasEnabledAutoClose();
        productionTrackingIsLast();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        stubSearchCriteriaResults(1L, 2L, 3L, 4L, 5L, 6L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertTrue(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndRecordIsLastAndThereIsMoreThanEnoughtLastRecordsButAutoCloseIsNotEnabled() {
        // given
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionTrackingIsLast();
        stubSearchCriteriaResults(1L, 2L, 3L, 4L, 5L, 6L);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertFalse(shouldClose);
    }

    @Test
    public final void shouldOrderCanBeClosedWhenTypeIsForEachOpAndRecordIsLastAndIsAlreadyAcceptedButThereIsEnoughtRecords() {
        // given
        orderHasEnabledAutoClose();
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionTrackingIsLast();
        stubSearchCriteriaResults(1L, 2L, 3L, PRODUCTION_RECORD_ID);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertTrue(shouldClose);
    }

    @Test
    public final void shouldOrderCanNotBeClosedWhenTypeIsForEachOpAndRecordIsLastButIsAlreadyAccepted() {
        // given
        stubTypeOfProductionRecording(TypeOfProductionRecording.FOR_EACH);
        productionTrackingIsLast();
        stubSearchCriteriaResults(1L, 2L, PRODUCTION_RECORD_ID);

        // when
        boolean shouldClose = orderClosingHelper.orderShouldBeClosed(productionTracking);

        // then
        assertFalse(shouldClose);
    }

    private void orderHasEnabledAutoClose() {
        given(parameter.getBooleanField(ParameterFieldsPC.AUTO_CLOSE_ORDER)).willReturn(true);
    }

    private void productionTrackingIsLast() {
        given(productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING)).willReturn(true);
        given(productionTracking.getField(ProductionTrackingFields.LAST_TRACKING)).willReturn(true);
    }

    private void stubTypeOfProductionRecording(final TypeOfProductionRecording type) {
        String typeAsString = type.getStringValue();
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeAsString);
        given(order.getField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeAsString);
    }

    private void stubSearchCriteriaResults(final Long... ids) {
        SearchCriteriaBuilder scb = mock(SearchCriteriaBuilder.class);

        // blind mock of fluent interface
        given(scb.add(any(SearchCriterion.class))).willReturn(scb);
        given(scb.setProjection(any(SearchProjection.class))).willReturn(scb);

        List<Entity> entities = Lists.newArrayList();
        for (Long id : ids) {
            Entity entity = mock(Entity.class);
            given(entity.getField("id")).willReturn(id);
            entities.add(entity);
        }

        SearchResult result = mock(SearchResult.class);
        given(result.getEntities()).willReturn(entities);
        given(scb.list()).willReturn(result);

        given(productionTrackingDD.find()).willReturn(scb);
    }

    private EntityList mockEntityList(final int size) {
        EntityList list = mock(EntityList.class);
        given(list.size()).willReturn(size);
        return list;
    }

}
