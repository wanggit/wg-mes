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
package com.qcadoo.mes.deliveries.states.aop.listeners;

import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveries.ProductSynchronizationService;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangePhase;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled({ DeliveriesConstants.PLUGIN_IDENTIFIER, "integration" })
public class DeliveryStateAspect extends AbstractStateListenerAspect {

    @Autowired
    private ProductSynchronizationService productSynchronizationService;

    @Pointcut(DeliveryStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(DeliveryStateChangePhase.LAST)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = DeliveryStateStringValues.APPROVED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void makeProductsSynchronized(final StateChangeContext stateChangeContext, final int phase) {
        if (productSynchronizationService.shouldSynchronize(stateChangeContext)) {
            productSynchronizationService.synchronizeProducts(stateChangeContext, true);
        }
    }

}
