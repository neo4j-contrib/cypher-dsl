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

import static org.apiguardian.api.API.Status.STABLE;

import java.util.List;

import org.apiguardian.api.API;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A container having properties. A property container must be {@link Named} with a non empty name to be able to refer
 * to properties.
 *
 * @author Andreas Berger
 * @author Michael J. Simons
 * @since 1.1
 */
@API(status = STABLE, since = "1.1")
public interface PropertyContainer extends Named, HasProperties {

	/**
	 * Creates an {@link Operation} mutating the properties of this container to a new value. The container does not
	 * track the operations created with this method.
	 *
	 * @param parameter the new properties
	 * @return A new operation.
	 * @since 2020.1.5
	 */
	@NotNull @Contract(pure = true)
	Operation mutate(Parameter<?> parameter);

	/**
	 * Creates an {@link Operation} mutating the properties of this container to a new value. The container does not
	 * track the operations created with this method.
	 *
	 * @param properties the new properties
	 * @return A new operation.
	 * @since 2020.1.5
	 */
	@NotNull @Contract(pure = true)
	Operation mutate(MapExpression properties);

	/**
	 * Creates an {@link Operation SET operation} setting the properties of this container to a new value. The container does not
	 * track the operations created with this method.
	 *
	 * @param parameter the new properties
	 * @return A new operation.
	 * @since 2022.5.0
	 */
	@NotNull @Contract(pure = true)
	Operation set(Parameter<?> parameter);

	/**
	 * Creates an {@link Operation SET operation} setting the properties of this container to a new value. The container does not
	 * track the operations created with this method.
	 *
	 * @param properties the new properties
	 * @return A new operation.
	 * @since 2022.5.0
	 */
	@NotNull @Contract(pure = true)
	Operation set(MapExpression properties);

	/**
	 * Unwraps the list of entries into an array before creating a projection out of it.
	 *
	 * @param entries A list of entries for the projection
	 * @return A map projection.
	 * @see SymbolicName#project(List)
	 */
	@NotNull @Contract(pure = true)
	MapProjection project(List<Object> entries);

	/**
	 * Creates a map projection based on this container. The container needs a symbolic name for this to work.
	 *
	 * @param entries A list of entries for the projection
	 * @return A map projection.
	 * @see SymbolicName#project(Object...)
	 */
	@NotNull @Contract(pure = true)
	MapProjection project(Object... entries);
}
