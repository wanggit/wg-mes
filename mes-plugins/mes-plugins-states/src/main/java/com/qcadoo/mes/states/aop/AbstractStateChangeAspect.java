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
package com.qcadoo.mes.states.aop;

import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;

import java.util.Date;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.exception.StateTransitionNotAlloweException;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.states.messages.util.ValidationMessageHelper;
import com.qcadoo.mes.states.service.StateChangePhaseUtil;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.Entity;

/**
 * Abstract service for changing entity state which provides default implementation.
 * 
 * @since 1.1.7
 */
@Aspect
@Configurable
@DeclarePrecedence("com.qcadoo.mes.states.aop.StateChangePhaseAspect, com.qcadoo.mes.states.aop.RunInPhaseAspect")
public abstract class AbstractStateChangeAspect implements StateChangeService {

    protected static final int DEFAULT_NUM_OF_PHASES = 2;

    private static final Logger LOGGER = Logger.getLogger(StateChangeService.class);

    @Override
    public void changeState(final StateChangeContext stateChangeContext) {
        try {
            performStateChange(stateChangeContext);
        } catch (Exception exception) {
            LOGGER.warn("Can't perform state change", exception);
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            final String messagesFieldName = stateChangeContext.getDescriber().getMessagesFieldName();
            stateChangeContext.setField(messagesFieldName, Lists.newArrayList());
            stateChangeContext.addMessage("states.messages.change.failure.internalServerError", StateMessageType.FAILURE);
            stateChangeContext.save();
            throw new StateChangeException(exception);
        }
    }

    @Transactional
    private void performStateChange(final StateChangeContext stateChangeContext) {
        final StateChangeEntityDescriber describer = stateChangeContext.getDescriber();

        describer.checkFields();
        for (int phase = stateChangeContext.getPhase() + 1; phase <= getNumOfPhases(); phase++) {
            if (StateChangePhaseUtil.canRun(stateChangeContext)) {
                stateChangeContext.setPhase(phase);
                changeStatePhase(stateChangeContext, phase);
            }
        }
        final Entity owner = stateChangeContext.getOwner();
        stateChangeContext.setOwner(owner);
        performChangeEntityState(stateChangeContext);
    }

    /**
     * Get number of state change phases. Default value is {@link AbstractStateChangeAspect#DEFAULT_NUM_OF_PHASES}.
     * 
     * @return number of phases
     */
    protected int getNumOfPhases() {
        return DEFAULT_NUM_OF_PHASES;
    }

    /**
     * Single state change phase join point.
     * 
     * @param stateChangeEntity
     * @param phaseNumber
     */
    protected abstract void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber);

    @Transactional
    protected void performChangeEntityState(final StateChangeContext stateChangeContext) {
        final StateChangeEntityDescriber describer = stateChangeContext.getDescriber();
        if (!StateChangePhaseUtil.canRun(stateChangeContext)) {
            if (!stateChangeContext.isOwnerValid()) {
                stateChangeContext.setStatus(StateChangeStatus.FAILURE);
                stateChangeContext.setField(describer.getDateTimeFieldName(), new Date());
                LOGGER.info(String.format("State change : failure. Entity name : %S id : %d.",
                        stateChangeContext.getOwner().getDataDefinition().getName(), stateChangeContext.getOwner().getId()));
            }
            return;
        }
        final Entity owner = stateChangeContext.getOwner();
        final StateEnum sourceState = stateChangeContext.getStateEnumValue(describer.getSourceStateFieldName());
        final StateEnum targetState = stateChangeContext.getStateEnumValue(describer.getTargetStateFieldName());

        if (sourceState != null && !sourceState.canChangeTo(targetState)) {
            throw new StateTransitionNotAlloweException(sourceState, targetState);
        }

        boolean ownerIsValid = stateChangeContext.isOwnerValid();
        if (ownerIsValid) {
            owner.setField(describer.getOwnerStateFieldName(), targetState.getStringValue());
            ownerIsValid = owner.getDataDefinition().save(owner).isValid();
        }

        if (ownerIsValid) {
            stateChangeContext.setStatus(SUCCESSFUL);
            LOGGER.info(String.format("State change : successful. Entity name : %S id : %d. Target state : %S",
                    owner.getDataDefinition().getName(), owner.getId(), targetState));
        } else {
            LOGGER.info(String.format("State change : failure. Entity name : %S id : %d. Target state : %S",
                    owner.getDataDefinition().getName(), owner.getId(), targetState));
            ValidationMessageHelper.copyErrorsFromEntity(stateChangeContext, owner);
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
        }
        stateChangeContext.setField(describer.getDateTimeFieldName(), new Date());
        stateChangeContext.save();
    }

}
