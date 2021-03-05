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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.List;

import org.apiguardian.api.API;

/**
 * A container having properties. A property container must be {@link Named} with a non empty name to be able to refer
 * to properties.
 *
 * @author Andreas Berger
 * @author Michael J. Simons
 * @since 1.1
 */
@API(status = EXPERIMENTAL, since = "1.1")
public interface PropertyContainer extends Named {

	/**
	 * Creates a new {@link Property} associated with this property container. This property can be used as a lookup in
	 * other expressions. It does not add a value to the property.
	 * <p>
	 * Note: The property container does not track property creation and there is no possibility to enumerate all
	 * properties that have been created for this property container.
	 *
	 * @param name property name, must not be {@literal null} or empty.
	 * @return a new {@link Property} associated with this named container
	 */
	Property property(String name);

	Property property(String... names);

	/**
	 * Creates a new {@link Property} associated with this property container. This property can be used as a lookup in
	 * other expressions. It does not add a value to the property.
	 * <p>
	 * The new {@link Property} object is a dynamic lookup, based on the {@code expression} passed to this method. The
	 * expression can be example another property, a function result or a Cypher parameter. A property defined in such a way will
	 * render as {@code p[expression]}.
	 * <p>
	 * Note: The property container does not track property creation and there is no possibility to enumerate all
	 * properties that have been created for this property container.
	 *
	 * @param lookup the expression that is evaluated to lookup this property.
	 * @return a new {@link Property} associated with this named container
	 * @since 2021.0.0
	 */
	Property property(Expression lookup);

	/**
	 * Creates an {@link Operation} mutating the properties of this container to a new value. The container does not
	 * track the operations created with this method.
	 *
	 * @param parameter the new properties
	 * @return A new operation.
	 * @since 2020.1.5
	 */
	Operation mutate(Parameter parameter);

	/**
	 * Creates an {@link Operation} mutating the properties of this container to a new value. The container does not
	 * track the operations created with this method.
	 *
	 * @param properties the new properties
	 * @return A new operation.
	 * @since 2020.1.5
	 */
	Operation mutate(MapExpression properties);

	/**
	 * Unwraps the list of entries into an array before creating a projection out of it.
	 *
	 * @param entries A list of entries for the projection
	 * @return A map projection.
	 * @see SymbolicName#project(List)
	 */
	MapProjection project(List<Object> entries);

	/**
	 * Creates a map projection based on this container. The container needs a symbolic name for this to work.
	 *
	 * @param entries A list of entries for the projection
	 * @return A map projection.
	 * @see SymbolicName#project(Object...)
	 */
	MapProjection project(Object... entries);
}
