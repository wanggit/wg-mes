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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.*;
import com.qcadoo.testing.model.EntityListMock;
import com.qcadoo.testing.model.EntityTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static com.qcadoo.mes.masterOrders.constants.OrderFieldsMO.MASTER_ORDER;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;
import static com.qcadoo.model.api.search.SearchRestrictions.*;
import static com.qcadoo.testing.model.EntityTestUtils.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class OrderValidatorsMOTest {

    private static final List<Entity> EMPTY_ENTITY_LIST = ImmutableList.of();

    private OrderValidatorsMO orderValidatorsMO;

    @Mock
    private DataDefinition orderDD, masterDD;

    @Mock
    private TranslationService translationService;

    @Mock
    private FieldDefinition productField, companyField, technologyField;

    private Entity order, masterOrder;

    @Mock
    private Entity product, company, technology, productMO, companyMO, technologyMO;

    @Mock
    private SearchCriterion technologyIsNullRestriction, technologyIsEmptyOrMatchTechPrototypeRestriction;

    @Before
    public void init() {
        orderValidatorsMO = new OrderValidatorsMO();
        MockitoAnnotations.initMocks(this);

        order = mockEntity(orderDD);
        masterOrder = mockEntity(masterDD);

        stubId(order, 1L);
        stubId(masterOrder, 2L);
        stubId(product, 3L);
        stubId(company, 4L);
        stubId(technology, 5L);
        stubId(technology, 6L);
        stubId(productMO, 7L);
        stubId(companyMO, 8L);
        stubId(technologyMO, 9L);

        ReflectionTestUtils.setField(orderValidatorsMO, "translationService", translationService);

        given(orderDD.getField(OrderFields.PRODUCT)).willReturn(productField);
        given(orderDD.getField(OrderFields.TECHNOLOGY)).willReturn(technologyField);
        given(orderDD.getField(OrderFields.COMPANY)).willReturn(companyField);

        stubBelongsToField(order, OrderFieldsMO.MASTER_ORDER, masterOrder);
        stubOrderProduct(product);
        stubOrderTechnology(mockEntity());

        stubMasterOrderProducts(EMPTY_ENTITY_LIST);
        SearchCriteriaBuilder scb = mockCriteriaBuilder(EMPTY_ENTITY_LIST);
        given(masterDD.find()).willReturn(scb);

        PowerMockito.mockStatic(SearchRestrictions.class);

        given(isNull(MasterOrderProductFields.TECHNOLOGY)).willReturn(technologyIsNullRestriction);
        given(or(isNull(MasterOrderProductFields.TECHNOLOGY),
                belongsTo(MasterOrderProductFields.TECHNOLOGY, technology)))
                        .willReturn(technologyIsEmptyOrMatchTechPrototypeRestriction);
    }

    private void stubOrderProduct(final Entity product) {
        EntityTestUtils.stubBelongsToField(order, OrderFields.PRODUCT, product);
    }

    private void stubOrderTechnology(final Entity technology) {
        EntityTestUtils.stubBelongsToField(order, OrderFields.TECHNOLOGY, technology);
    }

    private void stubMasterOrderProducts(final Collection<Entity> elements) {
        EntityList entityList = EntityListMock.create(elements);
        EntityTestUtils.stubHasManyField(masterOrder, MasterOrderFields.MASTER_ORDER_PRODUCTS, entityList);
    }

    private SearchCriteriaBuilder mockCriteriaBuilder(final List<Entity> results) {
        SearchCriteriaBuilder scb = mock(SearchCriteriaBuilder.class);
        given(scb.add(any(SearchCriterion.class))).willReturn(scb);
        given(scb.addOrder(any(SearchOrder.class))).willReturn(scb);
        given(scb.setProjection(any(SearchProjection.class))).willReturn(scb);
        given(scb.setMaxResults(anyInt())).willReturn(scb);

        given(scb.createAlias(anyString(), anyString())).willReturn(scb);
        given(scb.createAlias(anyString(), anyString(), any(JoinType.class))).willReturn(scb);

        given(scb.createCriteria(anyString(), anyString())).willReturn(scb);
        given(scb.createCriteria(anyString(), anyString(), any(JoinType.class))).willReturn(scb);

        if (!results.isEmpty()) {
            given(scb.uniqueResult()).willReturn(results.iterator().next());
        }

        SearchResult result = mock(SearchResult.class);
        given(result.getEntities()).willAnswer(new Answer<List<Entity>>() {

            @Override
            public List<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return Lists.newLinkedList(results);
            }
        });
        given(result.getTotalNumberOfEntities()).willReturn(results.size());

        given(scb.list()).willReturn(result);
        return scb;
    }

    @Test
    public final void shouldReturnTrueWhenMasterOrderIsNotSpecified() {
        // given
        given(order.getBelongsToField(MASTER_ORDER)).willReturn(null);

        // given
        boolean result = orderValidatorsMO.checkProductAndTechnology(orderDD, order);

        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldCheckCompanyAndDeadlineReturnTrueWhenMasterOrderIsNotSpecified() {
        // given
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(null);

        // given
        boolean result = orderValidatorsMO.checkCompanyAndDeadline(orderDD, order);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public final void shouldCheckOrderNumberReturnTrueWhenMasterOrderIsNotSpecified() {
        // given
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(null);

        // when
        boolean result = orderValidatorsMO.checkOrderNumber(orderDD, order);

        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldCheckOrderNumberReturnTrueWhenAddPrefixIsFalse() {
        // given
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(masterOrder);
        given(masterOrder.getBooleanField(MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER)).willReturn(false);

        // when
        boolean result = orderValidatorsMO.checkOrderNumber(orderDD, order);

        // then
        Assert.assertEquals(true, result);
    }

    @Test
    public final void shouldCheckOrderNumberReturnFalseWhenNumberIsIncorrect() {
        // given
        String masterOrderNumber = "ZL";
        String orderNumber = "ZXS";
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(masterOrder);
        given(masterOrder.getBooleanField(MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER)).willReturn(true);
        given(masterOrder.getStringField(MasterOrderFields.NUMBER)).willReturn(masterOrderNumber);
        given(order.getStringField(OrderFields.NUMBER)).willReturn(orderNumber);

        // when
        boolean result = orderValidatorsMO.checkOrderNumber(orderDD, order);

        // then
        Assert.assertEquals(false, result);
    }

    @Test
    public final void shouldCheckOrderNumberReturnTrueWhenNumberIsCorrect() {
        // given
        String masterOrderNumber = "ZL";
        String orderNumber = "ZLSADS";
        given(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER)).willReturn(masterOrder);
        given(masterOrder.getBooleanField(MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER)).willReturn(true);
        given(masterOrder.getStringField(MasterOrderFields.NUMBER)).willReturn(masterOrderNumber);
        given(order.getStringField(OrderFields.NUMBER)).willReturn(orderNumber);

        // when
        boolean result = orderValidatorsMO.checkOrderNumber(orderDD, order);

        // then
        Assert.assertEquals(true, result);
    }

}
