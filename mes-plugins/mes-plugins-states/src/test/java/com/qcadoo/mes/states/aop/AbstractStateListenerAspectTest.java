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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.Pointcut;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.StateChangeViewClient;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;

public class AbstractStateListenerAspectTest {

    @Test
    public final void checkStaticPointcutSelectors() throws NoSuchMethodException {
        final Class<?> clazz = AbstractStateListenerAspect.class;
        assertNotNull(clazz.getMethod("phaseExecution", StateChangeContext.class, int.class));
    }

    @Test
    public final void shouldHaveAbstractTargetServicePointcut() throws NoSuchMethodException {
        final Class<?> clazz = AbstractStateListenerAspect.class;
        final Method method = clazz.getDeclaredMethod("targetServicePointcut");
        assertNotNull(method);
        assertFalse(Modifier.isPrivate(method.getModifiers()));
        assertNotNull(method.getAnnotation(Pointcut.class));
    }

    @Test
    public final void checkChangeStateExecutionPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = StateChangeService.class;
        assertEquals("com.qcadoo.mes.states.service.StateChangeService", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("changeState", StateChangeContext.class);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
        assertEquals(void.class, method.getReturnType());
    }

    @Test
    public final void checkViewClientExecutionPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = StateChangeViewClient.class;
        assertEquals("com.qcadoo.mes.states.service.client.StateChangeViewClient", clazz.getCanonicalName());
        final Map<Integer, Class<?>> argsMap = Maps.newHashMap();
        argsMap.put(0, ViewContextHolder.class);
        final Method method = findMethod(clazz, "changeState", argsMap);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
        assertEquals(void.class, method.getReturnType());
    }

    private Method findMethod(final Class<?> clazz, final String name, final Map<Integer, Class<?>> argsMap) {
        final List<Method> methods = Lists.newArrayList(clazz.getMethods());
        methods.addAll(Lists.newArrayList(clazz.getDeclaredMethods()));
        for (Method method : methods) {
            if (method.getName().equals(name) && methodMatchArguments(method, argsMap)) {
                return method;
            }
        }
        Assert.fail("method not found");
        return null;
    }

    private boolean methodMatchArguments(final Method method, final Map<Integer, Class<?>> argsMap) {
        final Class<?>[] arguments = method.getParameterTypes();
        for (Map.Entry<Integer, Class<?>> argEntry : argsMap.entrySet()) {
            final int argIndex = argEntry.getKey();
            final Class<?> argType = argEntry.getValue();
            if (!argType.isAssignableFrom(arguments[argIndex])) {
                return false;
            }
        }
        return true;
    }

}
