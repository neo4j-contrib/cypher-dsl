/*
 * Copyright (c) 2019-2021 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypherdsl.core.support;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apiguardian.api.API;

/**
 * This is a convenience class implementing a {@link Visitor} and it takes care of choosing the right methods
 * to dispatch the {@link Visitor#enter(Visitable)} and {@link Visitor#leave(Visitable)} calls to.
 * <p>
 * Classes extending this visitor need to provide corresponding {@code enter} and {@code leave} methods taking exactly
 * one argument of the type of {@link Visitable} they are interested it.
 * <p>
 * The type must be an exact match, this support class doesn't try to find a close match up in the class hierarchy if it
 * doesn't find an exact match.
 *
 * @author Michael J. Simons
 * @author Gerrit Meier
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public abstract class ReflectiveVisitor implements Visitor {

	/**
	 * Private enum to specify a visiting phase.
	 */
	private enum Phase {
		ENTER("enter"),
		LEAVE("leave");

		final String methodName;

		Phase(String methodName) {
			this.methodName = methodName;
		}
	}

	/**
	 * A shared cache of unbound methods for entering and leaving phases.
	 * The key is the concrete class of the visitor as well as the hierarchy of the visitable.
	 */
	private static final Map<TargetAndPhase, Optional<Method>> VISITING_METHODS_CACHE = new ConcurrentHashMap<>();

	/**
	 * Keeps track of the ASTs current level.
	 */
	protected Deque<Visitable> currentVisitedElements = new LinkedList<>();

	/**
	 * This is a hook that is called with the uncasted, raw visitable just before entering a visitable.
	 * <p>
	 * The hook is called regardless wither a matching {@code enter} is found or not.
	 *
	 * @param visitable The visitable that is passed on to a matching enter after this call.
	 * @return true, when visiting of elements should be stopped until this element is left again.
	 */
	protected abstract boolean preEnter(Visitable visitable);

	/**
	 * This is a hook that is called with the uncasted, raw visitable just after leaving the visitable.
	 * <p>
	 * The hook is called regardless wither a matching {@code leave} is found or not.
	 *
	 * @param visitable The visitable that is passed on to a matching leave after this call.
	 */
	protected abstract void postLeave(Visitable visitable);

	@Override
	public final void enter(Visitable visitable) {

		if (preEnter(visitable)) {
			currentVisitedElements.push(visitable);
			executeConcreteMethodIn(new TargetAndPhase(this, visitable.getClass(), Phase.ENTER), visitable);
		}
	}

	@Override
	public final void leave(Visitable visitable) {

		if (currentVisitedElements.peek() == visitable) {
			executeConcreteMethodIn(new TargetAndPhase(this, visitable.getClass(), Phase.LEAVE), visitable);
			postLeave(visitable);
			currentVisitedElements.pop();
		}
	}

	private void executeConcreteMethodIn(TargetAndPhase targetAndPhase, Visitable onVisitable) {
		Optional<Method> optionalMethod = VISITING_METHODS_CACHE
			.computeIfAbsent(targetAndPhase, ReflectiveVisitor::findHandleFor);
		optionalMethod.ifPresent(handle -> {
			try {
				handle.invoke(this, onVisitable);
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		});
	}

	private static Optional<Method> findHandleFor(TargetAndPhase targetAndPhase) {

		Class<?> visitorClass = targetAndPhase.visitorClass;
		do { // Loop over the hierarchy of visitors so that we catch overloaded and inherited methods.
			// the loop goes from concrete to abstract, so that the most concrete visitor wins
			for (Class<?> clazz : targetAndPhase.classHierarchyOfVisitable) {
				try {
					Method method = visitorClass.getDeclaredMethod(targetAndPhase.phase.methodName, clazz);
					method.setAccessible(true);
					return Optional.of(method);
				} catch (NoSuchMethodException e) {
					// We don't do anything if the method doesn't exists
					// Try the next parameter type in the hierarchy or the next visitor
				}
			}
			visitorClass = visitorClass.getSuperclass();
		} while (visitorClass != null && visitorClass != ReflectiveVisitor.class);

		return Optional.empty();
	}

	private static class TargetAndPhase {

		/**
		 * The most concrete visitor class. It may be that the handle that is eventually found will not be called
		 * with that class, but with a parent. The attribute here is just a starting point and later on, a cache key.
		 */
		private final Class<? extends ReflectiveVisitor> visitorClass;

		private final Set<Class<?>> classHierarchyOfVisitable;

		private final Phase phase;

		<T extends ReflectiveVisitor> TargetAndPhase(T visitor, Class<? extends Visitable> concreteVisitableClass, Phase phase) {
			this.visitorClass = visitor.getClass();
			this.phase = phase;
			this.classHierarchyOfVisitable = new LinkedHashSet<>();

			Class<?> classOfVisitable = concreteVisitableClass;
			do {
				this.classHierarchyOfVisitable.add(classOfVisitable);
				// Add all interfaces apart visitable too.
				Arrays.stream(classOfVisitable.getInterfaces())
					.filter(c -> c != Visitable.class)
					.forEach(this.classHierarchyOfVisitable::add);
				classOfVisitable = classOfVisitable.getSuperclass();
			} while (classOfVisitable != null && classOfVisitable != Object.class);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof TargetAndPhase)) {
				return false;
			}
			TargetAndPhase that = (TargetAndPhase) o;
			return visitorClass.equals(that.visitorClass) &&
				classHierarchyOfVisitable.equals(that.classHierarchyOfVisitable) &&
				phase == that.phase;
		}

		@Override
		public int hashCode() {
			return Objects.hash(visitorClass, classHierarchyOfVisitable, phase);
		}
	}
}
