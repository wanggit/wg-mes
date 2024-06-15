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

import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.states.MockStateChangeDescriber;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateChangeTest;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunForStateTransitions;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.model.api.Entity;

public class RunForStateTransitionAspectTest extends StateChangeTest {

    private static final String SOURCE_STATE_1 = "sourceState01";

    private static final String SOURCE_STATE_2 = "sourceState02";

    private static final String TARGET_STATE_1 = "targetState01";

    private static final String TARGET_STATE_2 = "targetState02";

    private static final String MARKED_1 = "marked01";

    private static final String MARKED_2 = "marked02";

    private static final String MARKED_3 = "marked03";

    private static final String MARKED_4 = "marked04";

    private static final String MARKED_5 = "marked05";

    private static final String MARKED_MANY_1 = "markedMany01";

    private static final String MARKED_MANY_2 = "markedMany02";

    private TestStateChangeServiceAspect testService;

    @Test
    public final void checkAnnotationSelectorPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = RunForStateTransitions.class;
        assertEquals("com.qcadoo.mes.states.annotation.RunForStateTransitions", clazz.getCanonicalName());
    }

    @Aspect
    public static class TestStateChangeServiceAspect extends AbstractStateChangeAspect {

        @Override
        public void changeState(final StateChangeContext stateChangeContext) {
            // Do nothing
        }

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

        @Override
        protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
        }

    }

    @Aspect
    public static class TestStateChangeListenerAspect extends AbstractStateListenerAspect {

        @RunInPhase(1)
        @RunForStateTransition(sourceState = SOURCE_STATE_1, targetState = TARGET_STATE_1)
        @org.aspectj.lang.annotation.Before("changeStateExecution(stateChangeContext)")
        public void markEntity1(final StateChangeContext stateChangeContext) {
            final Entity stateChange = stateChangeContext.getStateChangeEntity();
            stateChange.setField(MARKED_1, true);
        }

        @RunInPhase(1)
        @RunForStateTransition(sourceState = SOURCE_STATE_1, targetState = WILDCARD_STATE)
        @org.aspectj.lang.annotation.Before("changeStateExecution(stateChangeContext)")
        public void markEntity2(final StateChangeContext stateChangeContext) {
            final Entity stateChange = stateChangeContext.getStateChangeEntity();
            stateChange.setField(MARKED_2, true);
        }

        @RunInPhase(1)
        @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = TARGET_STATE_1)
        @org.aspectj.lang.annotation.Before("changeStateExecution(stateChangeContext)")
        public void markEntity3(final StateChangeContext stateChangeContext) {
            final Entity stateChange = stateChangeContext.getStateChangeEntity();
            stateChange.setField(MARKED_3, true);
        }

        @RunInPhase(1)
        @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = WILDCARD_STATE)
        @org.aspectj.lang.annotation.Before("changeStateExecution(stateChangeContext)")
        public void markEntity4(final StateChangeContext stateChangeContext) {
            final Entity stateChange = stateChangeContext.getStateChangeEntity();
            stateChange.setField(MARKED_4, true);
        }

        @RunInPhase(1)
        @RunForStateTransitions({ @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = TARGET_STATE_2) })
        @org.aspectj.lang.annotation.Before("changeStateExecution(stateChangeContext)")
        public void markEntity5(final StateChangeContext stateChangeContext) {
            final Entity stateChange = stateChangeContext.getStateChangeEntity();
            stateChange.setField(MARKED_5, true);
        }

        @RunInPhase(1)
        @RunForStateTransitions({ @RunForStateTransition(sourceState = SOURCE_STATE_1, targetState = TARGET_STATE_1),
                @RunForStateTransition(sourceState = SOURCE_STATE_2, targetState = TARGET_STATE_2) })
        @org.aspectj.lang.annotation.Before("changeStateExecution(stateChangeContext)")
        public void markEntityMany1(final StateChangeContext stateChangeContext) {
            final Entity stateChange = stateChangeContext.getStateChangeEntity();
            stateChange.setField(MARKED_MANY_1, true);
        }

        @RunInPhase(1)
        @RunForStateTransitions({ @RunForStateTransition(sourceState = "undefinedSource1", targetState = "undefinedTarget1"),
                @RunForStateTransition(sourceState = "undefinedSource2", targetState = "undefinedTarget2") })
        @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = WILDCARD_STATE)
        @org.aspectj.lang.annotation.Before("changeStateExecution(stateChangeContext)")
        public void markEntityMany2(final StateChangeContext stateChangeContext) {
            final Entity stateChange = stateChangeContext.getStateChangeEntity();
            stateChange.setField(MARKED_MANY_2, true);
        }

        @Pointcut("this(TestStateChangeServiceAspect)")
        protected void targetServicePointcut() {
        }

    }

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        testService = new TestStateChangeServiceAspect();
        stubStateChangeEntity(DESCRIBER);
        stubStateChangeContext();
    }

    protected void stubStateChangeEntityStates(final String sourceState, final String targetState) {
        stubEntityField(stateChangeEntity, DESCRIBER.getSourceStateFieldName(), sourceState);
        stubEntityField(stateChangeEntity, DESCRIBER.getTargetStateFieldName(), targetState);
    }

    @Test
    public final void shouldRunIfMatchTransition1() {
        // given
        stubStateChangeEntityStates(SOURCE_STATE_1, TARGET_STATE_1);

        // when
        testService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity).setField(MARKED_1, true);
        verify(stateChangeEntity).setField(MARKED_2, true);
        verify(stateChangeEntity).setField(MARKED_3, true);
        verify(stateChangeEntity).setField(MARKED_4, true);
        verify(stateChangeEntity, never()).setField(MARKED_5, true);
        verify(stateChangeEntity).setField(MARKED_MANY_1, true);
        verify(stateChangeEntity, never()).setField(MARKED_MANY_2, true);
    }

    @Test
    public final void shouldRunIfMatchTransition2() {
        // given
        stubStateChangeEntityStates(SOURCE_STATE_2, TARGET_STATE_2);

        // when
        testService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity, never()).setField(MARKED_1, true);
        verify(stateChangeEntity, never()).setField(MARKED_2, true);
        verify(stateChangeEntity, never()).setField(MARKED_3, true);
        verify(stateChangeEntity).setField(MARKED_4, true);
        verify(stateChangeEntity).setField(MARKED_5, true);
        verify(stateChangeEntity).setField(MARKED_MANY_1, true);
        verify(stateChangeEntity, never()).setField(MARKED_MANY_2, true);
    }

    @Test
    public final void shouldRunIfMatchTransition3() {
        // given
        stubStateChangeEntityStates(SOURCE_STATE_2, TARGET_STATE_1);

        // when
        testService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity, never()).setField(MARKED_1, true);
        verify(stateChangeEntity, never()).setField(MARKED_2, true);
        verify(stateChangeEntity).setField(MARKED_3, true);
        verify(stateChangeEntity).setField(MARKED_4, true);
        verify(stateChangeEntity, never()).setField(MARKED_5, true);
        verify(stateChangeEntity, never()).setField(MARKED_MANY_1, true);
        verify(stateChangeEntity, never()).setField(MARKED_MANY_2, true);
    }

    @Test
    public final void shouldRunIfMatchTransition4() {
        // given
        stubStateChangeEntityStates("unsupported", "unsupported");

        // when
        testService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity, never()).setField(MARKED_1, true);
        verify(stateChangeEntity, never()).setField(MARKED_2, true);
        verify(stateChangeEntity, never()).setField(MARKED_3, true);
        verify(stateChangeEntity).setField(MARKED_4, true);
        verify(stateChangeEntity, never()).setField(MARKED_5, true);
        verify(stateChangeEntity, never()).setField(MARKED_MANY_1, true);
        verify(stateChangeEntity, never()).setField(MARKED_MANY_2, true);
    }

    @Test
    public final void shouldRunIfMatchTransition5() {
        // given
        stubStateChangeEntityStates(SOURCE_STATE_1, TARGET_STATE_2);

        // when
        testService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity, never()).setField(MARKED_1, true);
        verify(stateChangeEntity).setField(MARKED_2, true);
        verify(stateChangeEntity, never()).setField(MARKED_3, true);
        verify(stateChangeEntity).setField(MARKED_4, true);
        verify(stateChangeEntity).setField(MARKED_5, true);
        verify(stateChangeEntity, never()).setField(MARKED_MANY_1, true);
        verify(stateChangeEntity, never()).setField(MARKED_MANY_2, true);
    }

}
