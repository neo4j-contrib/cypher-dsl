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
package org.neo4j.cypherdsl.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.neo4j.cypherdsl.core.support.ReflectiveVisitor;
import org.neo4j.cypherdsl.core.support.Visitable;

/**
 * This is an implementation of a visitor to the Cypher AST created by the Cypher builder
 * based on the {@link ReflectiveVisitor reflective visitor}.
 * <p>
 * It collects all parameters with bound value into a map of used parameters.
 * <p>
 *
 * @author Andreas Berger
 * @author Michael J. Simons
 * @since 2021.0.0
 */
final class ParameterCollectingVisitor extends ReflectiveVisitor {

	static class ParameterInformation {

		final Set<String> names;
		final Map<String, Object> values;

		ParameterInformation(Set<String> names, Map<String, Object> values) {
			this.names = Collections.unmodifiableSet(names);
			this.values = Collections.unmodifiableMap(values);
		}
	}

	private final Set<String> names = new TreeSet<>();
	private final Map<String, Object> values = new TreeMap<>();
	private final Map<String, Set<Object>> erroneousParameters = new TreeMap<>();

	@Override
	protected boolean preEnter(Visitable visitable) {
		return true;
	}

	@Override
	protected void postLeave(Visitable visitable) {
	}

	@SuppressWarnings("unused")
	void enter(Parameter parameter) {

		boolean knownParameterName = !this.names.add(parameter.getName());

		Object newValue = parameter.getValue();
		Object oldValue = knownParameterName && this.values.containsKey(parameter.getName()) ?
			this.values.get(parameter.getName()) :
			Parameter.NO_VALUE;
		if (parameter.hasValue()) {
			this.values.put(parameter.getName(), newValue);
		}
		if (knownParameterName && !Objects.equals(oldValue, newValue)) {
			Set<Object> conflictingObjects = this.erroneousParameters.computeIfAbsent(parameter.getName(), s -> {
				HashSet<Object> list = new HashSet<>();
				list.add(oldValue);
				return list;
			});
			conflictingObjects.add(newValue);
		}
	}

	public ParameterInformation getResult() {

		if (!erroneousParameters.isEmpty()) {
			throw new ConflictingParametersException(erroneousParameters);
		}

		return new ParameterInformation(this.names, this.values);
	}
}
