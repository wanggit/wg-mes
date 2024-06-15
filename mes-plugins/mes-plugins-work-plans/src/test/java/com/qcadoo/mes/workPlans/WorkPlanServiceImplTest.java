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
package com.qcadoo.mes.workPlans;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchResult;

import junit.framework.Assert;

public class WorkPlanServiceImplTest {

    private static final String L_TRANSLATED_STRING = "translated string";

    private WorkPlansService workPlanService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private TranslationService translationService;

    @Mock
    private Entity workPlan, entity, parameter;

    @Mock
    private DataDefinition workPlanDD, entityDD;

    @Mock
    private FieldDefinition attachmentFieldDef;

    @Mock
    private ParameterService parameterService;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        workPlanService = new WorkPlansServiceImpl();

        ReflectionTestUtils.setField(workPlanService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(workPlanService, "translationService", translationService);
        ReflectionTestUtils.setField(workPlanService, "parameterService", parameterService);

        when(translationService.translate(Mockito.anyString(), Mockito.any(Locale.class), Mockito.anyString())).thenReturn(
                L_TRANSLATED_STRING);
        when(dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN)).thenReturn(
                workPlanDD);

        when(workPlanDD.get(Mockito.anyLong())).thenReturn(workPlan);

        when(workPlan.getDataDefinition()).thenReturn(workPlanDD);
        when(workPlan.getId()).thenReturn(1L);
        when(parameterService.getParameter()).thenReturn(parameter);
    }

    @Test
    public final void shouldReturnWorkPlan() throws Exception {
        // when
        Entity result = workPlanService.getWorkPlan(1L);

        // then
        Assert.assertSame(workPlanDD, result.getDataDefinition());
        Assert.assertEquals(workPlan.getId(), result.getId());

    }

    @Test
    public final void shouldGenerateWorkPlanEntity() throws Exception {
        // given
        Entity emptyWorkPlan = mock(Entity.class);
        when(workPlanDD.create()).thenReturn(emptyWorkPlan);
        when(workPlanDD.save(emptyWorkPlan)).thenReturn(emptyWorkPlan);
        when(emptyWorkPlan.getDataDefinition()).thenReturn(workPlanDD);

        when(parameter.getField(ParameterFieldsWP.DONT_PRINT_ORDERS_IN_WORK_PLANS)).thenReturn(true);
        Entity order = mock(Entity.class);

        @SuppressWarnings("unchecked")
        Iterator<Entity> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, true, false);
        when(iterator.next()).thenReturn(order);

        @SuppressWarnings("unchecked")
        List<Entity> orders = mock(List.class);
        when(orders.iterator()).thenReturn(iterator);
        when(orders.size()).thenReturn(3);
        when(orders.get(Mockito.anyInt())).thenReturn(order);

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> listArgCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> stringArgCaptor = ArgumentCaptor.forClass(String.class);

        // when
        workPlanService.generateWorkPlanEntity(orders);

        // then
        verify(emptyWorkPlan, times(1)).setField(Mockito.eq("orders"), listArgCaptor.capture());
        @SuppressWarnings("unchecked")
        List<Entity> resultOrders = listArgCaptor.getValue();
        Assert.assertEquals(orders.size(), resultOrders.size());
        Assert.assertSame(order, resultOrders.get(0));
        Assert.assertSame(order, resultOrders.get(1));
        Assert.assertSame(order, resultOrders.get(2));

        verify(emptyWorkPlan, times(1)).setField(Mockito.eq("name"), stringArgCaptor.capture());
        Assert.assertEquals(L_TRANSLATED_STRING, stringArgCaptor.getValue());

        verify(emptyWorkPlan, times(1)).setField(Mockito.eq("type"), stringArgCaptor.capture());
        Assert.assertEquals(WorkPlanType.NO_DISTINCTION.getStringValue(), stringArgCaptor.getValue());

    }

    @Test
    public final void shouldReturnOrdersById() throws Exception {
        // given
        Entity order1 = mock(Entity.class);
        when(order1.getId()).thenReturn(1L);

        Entity order2 = mock(Entity.class);
        when(order2.getId()).thenReturn(2L);

        Entity order3 = mock(Entity.class);
        when(order3.getId()).thenReturn(3L);

        @SuppressWarnings("unchecked")
        Iterator<Long> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, true, true, false);
        when(iterator.next()).thenReturn(1L, 2L, 3L, 4L);

        @SuppressWarnings("unchecked")
        Set<Long> selectedOrderIds = mock(Set.class);
        when(selectedOrderIds.iterator()).thenReturn(iterator);
        when(selectedOrderIds.size()).thenReturn(4);

        SearchCriteriaBuilder criteria = mock(SearchCriteriaBuilder.class);
        when(criteria.add(Mockito.any(SearchCriterion.class))).thenReturn(criteria);

        SearchResult result = mock(SearchResult.class);
        when(criteria.list()).thenReturn(result);

        when(result.getTotalNumberOfEntities()).thenReturn(3);
        when(result.getEntities()).thenReturn(Lists.newArrayList(order1, order2, order3));

        DataDefinition orderDD = mock(DataDefinition.class);
        when(orderDD.find()).thenReturn(criteria);

        when(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).thenReturn(orderDD);

        // when
        List<Entity> resultList = workPlanService.getSelectedOrders(selectedOrderIds);

        // then
        Assert.assertEquals(3, resultList.size());

        Assert.assertNotNull(resultList.get(0));
        Assert.assertSame(1L, resultList.get(0).getId());

        Assert.assertNotNull(resultList.get(1));
        Assert.assertSame(2L, resultList.get(1).getId());

        Assert.assertNotNull(resultList.get(2));
        Assert.assertSame(3L, resultList.get(2).getId());
    }

    @Test
    public final void shouldReturnEmptyListIfOrdersWithGivenIdDoesNotExist() throws Exception {
        // given
        Entity order1 = mock(Entity.class);
        when(order1.getId()).thenReturn(1L);

        Entity order2 = mock(Entity.class);
        when(order2.getId()).thenReturn(2L);

        Entity order3 = mock(Entity.class);
        when(order3.getId()).thenReturn(3L);

        @SuppressWarnings("unchecked")
        Iterator<Long> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, true, true, false);
        when(iterator.next()).thenReturn(1L, 2L, 3L, 4L);

        @SuppressWarnings("unchecked")
        Set<Long> selectedOrderIds = mock(Set.class);
        when(selectedOrderIds.iterator()).thenReturn(iterator);
        when(selectedOrderIds.size()).thenReturn(4);

        SearchCriteriaBuilder criteria = mock(SearchCriteriaBuilder.class);
        when(criteria.add(Mockito.any(SearchCriterion.class))).thenReturn(criteria);

        SearchResult result = mock(SearchResult.class);
        when(criteria.list()).thenReturn(result);

        when(result.getTotalNumberOfEntities()).thenReturn(0);

        DataDefinition orderDD = mock(DataDefinition.class);
        when(orderDD.find()).thenReturn(criteria);

        when(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).thenReturn(orderDD);

        // when
        List<Entity> resultList = workPlanService.getSelectedOrders(selectedOrderIds);

        // then
        Assert.assertEquals(0, resultList.size());
    }

    @Test
    public final void shouldReturnEmptyListIfGivenIdsSetIsEmpty() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        Iterator<Long> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(false);

        @SuppressWarnings("unchecked")
        Set<Long> selectedOrderIds = mock(Set.class);
        when(selectedOrderIds.iterator()).thenReturn(iterator);
        when(selectedOrderIds.size()).thenReturn(0);
        when(selectedOrderIds.isEmpty()).thenReturn(true);

        // when
        List<Entity> resultList = workPlanService.getSelectedOrders(selectedOrderIds);

        // then
        Assert.assertEquals(0, resultList.size());
    }

    @Test
    public final void shouldMarkExtensionAsValidForNullAttachmentPathValue() {
        // given
        final String oldValue = "valid.png";
        final String newValue = null;

        // when
        final boolean isValid = workPlanService
                .checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue, newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidForEmptyAttachmentPathValue() {
        // given
        final String oldValue = "valid.png";
        final String newValue = "";

        // when
        final boolean isValid = workPlanService
                .checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue, newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidForBlankAttachmentPathValue() {
        // given
        final String oldValue = "valid.png";
        final String newValue = "   ";

        // when
        final boolean isValid = workPlanService
                .checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue, newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidIfNewValueIsEqualToOldOne() {
        // given
        final String oldValue = "valid.png";
        final String newValue = oldValue;

        // when
        final boolean isValid = workPlanService
                .checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue, newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidIfNewValueIsEqualToOldOneEvenIfBothValuesAreInvalid() {
        // given
        final String oldValue = "invalid.mp3";
        final String newValue = oldValue;

        // when
        final boolean isValid = workPlanService
                .checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue, newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidIfNewValueIsValidAndOldValueIsInvalid() {
        // given
        final String oldValue = "invalid.mp3";
        final String newValue = "valid.png";

        // when
        final boolean isValid = workPlanService
                .checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue, newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidEvenIfNewValueHasManyDots() {
        // given
        final String oldValue = null;
        final String newValue = "valid.wav.mp3.rmvb.pdf.sh.png";

        // when
        final boolean isValid = workPlanService
                .checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue, newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkAllowedExtensionAsValid() {
        // given
        final String oldValue = null;
        final String newValuePrefix = "someFile.";

        // when
        for (String allowedFileExtension : WorkPlansConstants.FILE_EXTENSIONS) {
            boolean isValid = workPlanService.checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue,
                    newValuePrefix + allowedFileExtension);
            Assert.assertTrue(isValid);
        }
    }

    @Test
    public final void shouldMarkDisallowedExtensionAsInvalid() {
        // given
        final String oldValue = null;
        final String newValuePrefix = "someFile.";
        final String disallowedExtension = ".mp3";

        // when
        for (String allowedFileExtension : WorkPlansConstants.FILE_EXTENSIONS) {
            boolean isValid = workPlanService.checkAttachmentExtension(entityDD, attachmentFieldDef, entity, oldValue,
                    newValuePrefix + allowedFileExtension + disallowedExtension);
            Assert.assertFalse(isValid);
        }
    }

}
