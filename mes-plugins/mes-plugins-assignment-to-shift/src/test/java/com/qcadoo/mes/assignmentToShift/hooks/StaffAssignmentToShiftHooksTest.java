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
package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.PRODUCTION_LINE;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.NUMBER;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class StaffAssignmentToShiftHooksTest {

    private StaffAssignmentToShiftHooks hooks;

    @Mock
    private DataDefinition staffAssignmentToShiftDD;

    @Mock
    private Entity staffAssignmentToShift, dictionary, productionLine;

    @Mock
    private StaffAssignmentToShiftDetailsHooks staffAssignmentToShiftDetailsHooks;

    @Before
    public void init() {
        hooks = new StaffAssignmentToShiftHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(hooks, "assignmentToShiftDetailsHooks", staffAssignmentToShiftDetailsHooks);
    }

    @Test
    public void shouldSaveOccupationTypeForGridValueWhenProductionLineIsSelected() {
        // given
        String technicalCode = "01workOnLine";
        String occupationType = "Praca na linii";
        String productionLineNumber = "00001";
        String occupationTypeForGridValue = "info";

        given(staffAssignmentToShift.getStringField(OCCUPATION_TYPE)).willReturn(occupationType);
        given(staffAssignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType)).willReturn(dictionary);
        given(dictionary.getStringField(TECHNICAL_CODE)).willReturn(technicalCode);
        given(staffAssignmentToShift.getBelongsToField(PRODUCTION_LINE)).willReturn(productionLine);
        given(productionLine.getStringField(NUMBER)).willReturn(productionLineNumber);

        // when
        hooks.setOccupationTypeForGridValue(staffAssignmentToShiftDD, staffAssignmentToShift);

        // then
        Assert.assertEquals("info", occupationTypeForGridValue);
    }

    @Test
    public void shouldAddErrorForEntityWhenProductionLineIsNull() {
        // given
        String technicalCode = "01workOnLine";
        String occupationType = "Praca na linii";

        given(staffAssignmentToShift.getStringField(OCCUPATION_TYPE)).willReturn(occupationType);
        given(staffAssignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType)).willReturn(dictionary);
        given(dictionary.getStringField(TECHNICAL_CODE)).willReturn(technicalCode);
        given(staffAssignmentToShift.getBelongsToField(PRODUCTION_LINE)).willReturn(null);

        // when
        hooks.setOccupationTypeForGridValue(staffAssignmentToShiftDD, staffAssignmentToShift);

        // then
        verify(staffAssignmentToShift).addError(staffAssignmentToShiftDD.getField(StaffAssignmentToShiftFields.PRODUCTION_LINE),
                "assignmentToShift.staffAssignmentToShift.productionLine.isEmpty");
    }

}
