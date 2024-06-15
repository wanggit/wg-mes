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
package com.qcadoo.mes.productionCounting.states.aop.listener;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.states.ProductionCountingOrderStatesListenerService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(ProductionCountingConstants.PLUGIN_IDENTIFIER)
public class ProductionCountingOrderStatesListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private ProductionCountingOrderStatesListenerService productionCountingOrderStatesListenerService;

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(OrderStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = OrderStateStringValues.WILDCARD_STATE, targetState = OrderStateStringValues.COMPLETED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnCompleted(final StateChangeContext stateChangeContext, final int phase) {
        productionCountingOrderStatesListenerService.validationOnComplete(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = OrderStateStringValues.WILDCARD_STATE, targetState = OrderStateStringValues.COMPLETED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onComplete(final StateChangeContext stateChangeContext, final int phase) {
        productionCountingOrderStatesListenerService.onComplete(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = OrderStateStringValues.WILDCARD_STATE, targetState = OrderStateStringValues.ABANDONED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnAbandone(final StateChangeContext stateChangeContext, final int phase) {
        productionCountingOrderStatesListenerService.validationOnAbandone(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = OrderStateStringValues.WILDCARD_STATE, targetState = OrderStateStringValues.DECLINED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnRefuse(final StateChangeContext stateChangeContext, final int phase) {
        productionCountingOrderStatesListenerService.validationOnDecline(stateChangeContext);

    }

}
