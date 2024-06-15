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

import static com.qcadoo.mes.states.constants.StateChangeStatus.PAUSED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.states.MockStateChangeDescriber;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateChangeTest;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class RunInPhaseAspectTest extends StateChangeTest {

    private static final int FIRST_PHASE = 1;

    private static final int TEST_PHASE = 3;

    private static final int LAST_PHASE = 8;

    private StateChangeService stateChangeService;

    @Test
    public final void checkPointcutsDefinition() throws NoSuchMethodException {
        assertEquals("com.qcadoo.mes.states.annotation.RunInPhase", RunInPhase.class.getCanonicalName());
        assertEquals("com.qcadoo.mes.states.aop.AbstractStateListenerAspect",
                AbstractStateListenerAspect.class.getCanonicalName());
        assertEquals("com.qcadoo.mes.states.aop.StatesXpiAspect", StatesXpiAspect.class.getCanonicalName());
    }

    @Test
    public final void checkXpi() throws NoSuchMethodException {
        final Class<?> clazz = StatesXpiAspect.class;
        assertEquals("com.qcadoo.mes.states.aop.StatesXpiAspect", clazz.getCanonicalName());
        assertNotNull(clazz.getDeclaredMethod("listenerExecution"));
    }

    @Aspect
    public static final class TestStateChangeAspect extends AbstractStateChangeAspect {

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

        @Override
        protected void performChangeEntityState(final StateChangeContext stateChangeContext) {
            final Entity stateChangeEntity = stateChangeContext.getStateChangeEntity();
            stateChangeEntity.setField("finished", true);
        }

        @Override
        protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
            final Entity stateChangeEntity = stateChangeContext.getStateChangeEntity();
            stateChangeEntity.setField("executedPhase", phaseNumber);
        }

        @Override
        protected int getNumOfPhases() {
            return LAST_PHASE;
        }

    }

    @Aspect
    public static final class AnotherStateChangeAspect extends AbstractStateChangeAspect {

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

        @Override
        protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
        }

        @Override
        protected int getNumOfPhases() {
            return LAST_PHASE;
        }

        @Override
        protected void performChangeEntityState(final StateChangeContext stateChangeContext) {
        }

    }

    @Aspect
    public static final class BreakStateChangeAspect extends AbstractStateChangeAspect {

        @Override
        public StateChangeEntityDescriber getChangeEntityDescriber() {
            return new MockStateChangeDescriber();
        }

        @Override
        protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
        }

        @Override
        protected int getNumOfPhases() {
            return LAST_PHASE;
        }

        @Override
        protected void performChangeEntityState(final StateChangeContext stateChangeContext) {
        }

    }

    @Aspect
    public static class TestListener extends AbstractStateListenerAspect {

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.Before(PHASE_EXECUTION_POINTCUT)
        public void testAdvice(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            stateEntity.setField("onceChecked", true);
            stateEntity.setField("checkedPhase", phase);
        }

        @RunInPhase({ FIRST_PHASE, TEST_PHASE, LAST_PHASE })
        @org.aspectj.lang.annotation.Before(PHASE_EXECUTION_POINTCUT)
        public void testAdvice2(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            stateEntity.setField("multiChecked", true);
            stateEntity.setField("anotherCheckedPhase", phase);
        }

        @org.aspectj.lang.annotation.Before(PHASE_EXECUTION_POINTCUT)
        public void testAdvice3(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            stateEntity.setField("withoutAtPhase", true);
        }

        @Pointcut("this(TestStateChangeAspect)")
        protected void targetServicePointcut() {
        }

    }

    @Aspect
    public static class AnotherListener extends AbstractStateListenerAspect {

        @org.aspectj.lang.annotation.Before(PHASE_EXECUTION_POINTCUT)
        public void testAdvice(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            stateEntity.setField("yetAnotherOnceChecked", true);
            stateEntity.setField("yetAnotherCheckedPhase", phase);
        }

        @Pointcut("this(AnotherStateChangeAspect)")
        protected void targetServicePointcut() {
        }
    }

    @Aspect
    @RunInPhase(FIRST_PHASE)
    public static class ClassLevelAnnotatedListener extends AbstractStateListenerAspect {

        @org.aspectj.lang.annotation.Before(PHASE_EXECUTION_POINTCUT)
        public void classAnnotationAdvice(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            stateEntity.setField("classAnnotationCheckedPhase", phase);
        }

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.Before(PHASE_EXECUTION_POINTCUT)
        public void methodAnnotationAdvice(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            stateEntity.setField("methodAnnotationCheckedPhase", phase);
        }

        @Pointcut("this(TestStateChangeAspect)")
        protected void targetServicePointcut() {
        }

    }

    @Aspect
    public static class BreakingListener extends AbstractStateListenerAspect {

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.Before(PHASE_EXECUTION_POINTCUT)
        public void testAdviceBeforeTest(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            stateEntity.setField("beforeTest", true);
        }

        @RunInPhase(LAST_PHASE)
        @org.aspectj.lang.annotation.Before(PHASE_EXECUTION_POINTCUT)
        public void testAdviceBeforeLast(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            stateEntity.setField("beforeLast", true);
        }

        @RunInPhase(TEST_PHASE)
        @org.aspectj.lang.annotation.After(PHASE_EXECUTION_POINTCUT)
        public void testAdviceAfterTest(final StateChangeContext stateChangeContext, final int phase) {
            final Entity stateEntity = stateChangeContext.getStateChangeEntity();
            final StateChangeEntityDescriber describer = stateChangeContext.getDescriber();
            stateEntity.setField("afterTest", true);
            stateEntity.setField(describer.getStatusFieldName(), PAUSED.getStringValue());
            given(stateEntity.getField(describer.getStatusFieldName())).willReturn(PAUSED.getStringValue());
            given(stateEntity.getStringField(describer.getStatusFieldName())).willReturn(PAUSED.getStringValue());
        }

        @Pointcut("this(BreakStateChangeAspect)")
        protected void targetServicePointcut() {
        }

    }

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        stateChangeService = new TestStateChangeAspect();
        final EntityList emptyEntityList = mockEntityList(Collections.<Entity> emptyList());
        stubStateChangeEntity(DESCRIBER);
        stubStateChangeContext();
        stubOwner();
        given(stateChangeEntity.getHasManyField(DESCRIBER.getMessagesFieldName())).willReturn(emptyEntityList);
    }

    @Test
    public final void shouldFireListeners() {
        // when
        stateChangeService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity).setField("onceChecked", true);
        verify(stateChangeEntity).setField(Mockito.eq("checkedPhase"), Mockito.anyInt());
        verify(stateChangeEntity).setField("checkedPhase", TEST_PHASE);

        verify(stateChangeEntity, times(3)).setField("multiChecked", true);
        verify(stateChangeEntity, times(3)).setField(Mockito.eq("anotherCheckedPhase"), Mockito.anyInt());
        verify(stateChangeEntity).setField("anotherCheckedPhase", FIRST_PHASE);
        verify(stateChangeEntity).setField("anotherCheckedPhase", TEST_PHASE);
        verify(stateChangeEntity).setField("anotherCheckedPhase", LAST_PHASE);

        verify(stateChangeEntity, times(LAST_PHASE)).setField("withoutAtPhase", true);

        verify(stateChangeEntity, never()).setField("yetAnotherOnceChecked", true);
        verify(stateChangeEntity, never()).setField(Mockito.eq("yetAnotherCheckedPhase"), Mockito.anyInt());

        verify(stateChangeEntity).setField("finished", true);
    }

    @Test
    public final void shouldNotFireListenersIfOwnerIsInvalid() {
        // given
        given(stateChangeContext.isOwnerValid()).willReturn(false);

        // when
        stateChangeService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity, never()).setField("onceChecked", true);
        verify(stateChangeEntity, never()).setField(Mockito.eq("checkedPhase"), Mockito.anyInt());
        verify(stateChangeEntity, never()).setField("checkedPhase", TEST_PHASE);

        verify(stateChangeEntity, never()).setField("multiChecked", true);
        verify(stateChangeEntity, never()).setField(Mockito.eq("anotherCheckedPhase"), Mockito.anyInt());
        verify(stateChangeEntity, never()).setField("anotherCheckedPhase", FIRST_PHASE);
        verify(stateChangeEntity, never()).setField("anotherCheckedPhase", TEST_PHASE);
        verify(stateChangeEntity, never()).setField("anotherCheckedPhase", LAST_PHASE);

        verify(stateChangeEntity, never()).setField("withoutAtPhase", true);

        verify(stateChangeEntity, never()).setField("yetAnotherOnceChecked", true);
        verify(stateChangeEntity, never()).setField(Mockito.eq("yetAnotherCheckedPhase"), Mockito.anyInt());

        verify(stateChangeEntity, never()).setField("finished", true);
    }

    @Test
    public final void shouldContinueFromLastPhaseChain() {
        // given
        given(stateChangeEntity.getField(DESCRIBER.getPhaseFieldName())).willReturn(TEST_PHASE);

        // when
        stateChangeService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity, times(1)).setField("multiChecked", true);
        verify(stateChangeEntity, times(1)).setField(Mockito.eq("anotherCheckedPhase"), Mockito.anyInt());
        verify(stateChangeEntity, never()).setField("anotherCheckedPhase", FIRST_PHASE);
        verify(stateChangeEntity, never()).setField("anotherCheckedPhase", TEST_PHASE);
        verify(stateChangeEntity).setField("anotherCheckedPhase", LAST_PHASE);

        verify(stateChangeEntity, times(LAST_PHASE - TEST_PHASE)).setField(Mockito.eq("executedPhase"), Mockito.anyInt());

        verify(stateChangeEntity).setField("finished", true);
    }

    @Test
    public final void shouldBreakPhaseChainExecution() {
        // given
        stateChangeService = new BreakStateChangeAspect();

        // when
        stateChangeService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity).setField("beforeTest", true);
        verify(stateChangeEntity).setField("afterTest", true);
        verify(stateChangeEntity, never()).setField("beforeLast", true);
    }

    @Test
    public final void shouldCheckClassLevelAnnotationIfMethodLevelAnnotationIsNotPresent() {
        // when
        stateChangeService.changeState(stateChangeContext);

        // then
        verify(stateChangeEntity).setField("classAnnotationCheckedPhase", FIRST_PHASE);
        verify(stateChangeEntity).setField("methodAnnotationCheckedPhase", TEST_PHASE);

    }

}
