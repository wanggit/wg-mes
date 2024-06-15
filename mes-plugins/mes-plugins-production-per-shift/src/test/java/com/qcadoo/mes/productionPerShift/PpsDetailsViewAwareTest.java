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
package com.qcadoo.mes.productionPerShift;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.utils.EntityUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public abstract class PpsDetailsViewAwareTest {

    protected static final String PROGRESS_TYPE_COMBO_REF = "plannedProgressType";

    protected static final String ORDER_LOOKUP_REF = "order";

    @Mock
    protected ViewDefinitionState view;

    @Mock
    protected FieldComponent progressTypeComboBox;

    @Mock
    private LookupComponent orderLookup;

    @Mock
    protected Entity order, technology;

    public void init() {
        MockitoAnnotations.initMocks(this);
        orderLookup = mockLookup(order);
        stubViewComponent(ORDER_LOOKUP_REF, orderLookup);
        stubViewComponent(PROGRESS_TYPE_COMBO_REF, progressTypeComboBox);
    }

    protected void stubViewComponent(final String referenceName, final ComponentState component) {
        given(view.getComponentByReference(referenceName)).willReturn(component);
        given(view.tryFindComponentByReference(referenceName)).willReturn(Optional.fromNullable(component));
    }

    protected void stubFormComponent(final FormComponent form, final String componentName, final FieldComponent component) {
        given(form.findFieldComponentByName(componentName)).willReturn(component);
    }

    protected FormComponent mockForm(final Entity underlyingEntity) {
        FormComponent form = mock(FormComponent.class);
        given(form.getEntity()).willReturn(underlyingEntity);
        given(form.getPersistedEntityWithIncludedFormValues()).willReturn(underlyingEntity);
        given(form.getEntityId()).willAnswer(new Answer<Long>() {

            @Override
            public Long answer(final InvocationOnMock invocation) throws Throwable {
                return FluentOptional.fromNullable(underlyingEntity).flatMap(EntityUtils.getSafeIdExtractor()).toOpt().orNull();
            }
        });
        return form;
    }

    protected LookupComponent mockLookup(final Entity underlyingEntity) {
        LookupComponent lookupComponent = mock(LookupComponent.class);
        given(lookupComponent.getEntity()).willReturn(underlyingEntity);
        given(lookupComponent.getFieldValue()).willAnswer(new Answer<Long>() {

            @Override
            public Long answer(final InvocationOnMock invocation) throws Throwable {
                return FluentOptional.fromNullable(underlyingEntity).flatMap(EntityUtils.getSafeIdExtractor()).toOpt().orNull();
            }
        });
        return lookupComponent;
    }

    protected FieldComponent mockFieldComponent(final Object componentValue) {
        FieldComponent fieldComponent = mock(FieldComponent.class);
        given(fieldComponent.getFieldValue()).willReturn(componentValue);
        return fieldComponent;
    }

    protected void stubProgressType(final ProgressType progressType) {
        given(progressTypeComboBox.getFieldValue()).willReturn(progressType.getStringValue());
    }

}
