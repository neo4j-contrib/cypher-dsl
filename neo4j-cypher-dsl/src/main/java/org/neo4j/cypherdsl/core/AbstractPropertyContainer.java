/*
 * Copyright (c) 2019-2024 "Neo4j,"
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

import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Abstract base class for all property containers to avoid default interface methods to be overridable in inheritors.
 *
 * @author Michael J. Simons
 * @since 2021.1.0
 */
@API(status = INTERNAL, since = "2021.1.0")
abstract class AbstractPropertyContainer implements PropertyContainer {

	@NotNull
	@Override
	public final Property property(@NotNull String name) {
		return InternalPropertyImpl.create(this, name);
	}

	@NotNull
	@Override
	public final Property property(String... names) {
		return InternalPropertyImpl.create(this, names);
	}

	@NotNull
	@Override
	public final Property property(@NotNull Expression lookup) {
		return InternalPropertyImpl.create(this, lookup);
	}

	static IllegalStateException noNameException() {
		return new IllegalStateException(Cypher.MESSAGES.getString(MessageKeys.ASSERTIONS_REQUIRES_NAME_FOR_MUTATION));
	}

	@NotNull
	@Override
	@SuppressWarnings("deprecation")
	public final Operation mutate(Parameter<?> parameter) {
		return Operations.mutate(this.getSymbolicName().orElseThrow(AbstractPropertyContainer::noNameException), parameter);
	}

	@NotNull
	@Override
	@SuppressWarnings("deprecation")
	public final Operation mutate(MapExpression properties) {
		return Operations.mutate(this.getSymbolicName().orElseThrow(AbstractPropertyContainer::noNameException), properties);
	}

	@NotNull
	@Override
	@SuppressWarnings("deprecation")
	public final Operation set(Parameter<?> parameter) {
		return Operations.set(this.getSymbolicName().orElseThrow(AbstractPropertyContainer::noNameException), parameter);
	}

	@NotNull
	@Override
	@SuppressWarnings("deprecation")
	public final Operation set(MapExpression properties) {
		return Operations.set(this.getSymbolicName().orElseThrow(AbstractPropertyContainer::noNameException), properties);
	}

	@NotNull
	@Override
	public final MapProjection project(List<Object> entries) {
		return project(entries.toArray());
	}

	@NotNull
	@Override
	public final MapProjection project(Object... entries) {
		return getRequiredSymbolicName().project(entries);
	}
}
