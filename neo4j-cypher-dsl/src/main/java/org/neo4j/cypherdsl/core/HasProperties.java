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

import org.apiguardian.api.API;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * This interface represents an element that has properties.
 *
 * @author Andreas Berger
 * @since 2023.9.4
 */
@API(status = STABLE, since = "2023.9.4")
public interface HasProperties {

	/**
	 * Creates a new {@link Property} associated with this element. This property can be used as a lookup in
	 * other expressions. It does not add a value to the property.
	 * <p>
	 * Note: The element does not track property creation and there is no possibility to enumerate all
	 * properties that have been created for this property container.
	 *
	 * @param name property name, must not be {@literal null} or empty.
	 * @return a new {@link Property} associated with this element
	 * @since 2023.9.4
	 */
	@NotNull @Contract(pure = true)
	Property property(@NotNull String name);

	/**
	 * @param names a list of nested property names
	 * @return a new {@link Property} associated with this element
	 * @see  #property(String)
	 * @since 2023.9.4
	 */
	@NotNull @Contract(pure = true)
	Property property(String... names);

	/**
	 * Creates a new {@link Property} associated with this element. This property can be used as a lookup in
	 * other expressions. It does not add a value to the property.
	 * <p>
	 * The new {@link Property} object is a dynamic lookup, based on the {@code expression} passed to this method. The
	 * expression can be example another property, a function result or a Cypher parameter. A property defined in such a way will
	 * render as {@code p[expression]}.
	 * <p>
	 * Note: The element does not track property creation and there is no possibility to enumerate all
	 * properties that have been created for this property container.
	 *
	 * @param lookup the expression that is evaluated to lookup this property.
	 * @return a new {@link Property} associated with this element
	 * @since 2023.9.4
	 */
	@NotNull @Contract(pure = true)
	Property property(@NotNull Expression lookup);
}
