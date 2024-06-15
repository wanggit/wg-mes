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
package com.qcadoo.mes.deliveriesToMaterialFlow.states.aop.listener;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangePhase;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveriesToMaterialFlowConstants;
import com.qcadoo.mes.deliveriesToMaterialFlow.states.DeliveryStateServiceMF;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(DeliveriesToMaterialFlowConstants.PLUGIN_IDENTIFIER)
public class DeliveryStateServiceMFAspect extends AbstractStateListenerAspect {

    @Autowired
    private DeliveryStateServiceMF deliveryStateServiceMF;

    @Pointcut(DeliveryStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(DeliveryStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = DeliveryStateStringValues.RECEIVED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void preValidationOnReceivedDelivery(final StateChangeContext stateChangeContext, final int phase) {
        deliveryStateServiceMF.validateRequiredParameters(stateChangeContext);
        deliveryStateServiceMF.validateReceivedPackages(stateChangeContext);
    }

    @RunInPhase(DeliveryStateChangePhase.LAST)
    @RunForStateTransition(targetState = DeliveryStateStringValues.RECEIVED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void createTransfersForTheReceivedProducts(final StateChangeContext stateChangeContext, final int phase) {
        deliveryStateServiceMF.createDocumentsForTheReceivedProducts(stateChangeContext);
        deliveryStateServiceMF.createDocumentsForTheReceivedPackages(stateChangeContext);
    }

}
