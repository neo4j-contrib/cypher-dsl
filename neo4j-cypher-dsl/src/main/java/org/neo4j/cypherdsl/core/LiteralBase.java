/*
 * Copyright (c) 2019-2022 "Neo4j,"
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

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

/**
 * Represents a literal with an optional content.
 *
 * @author Michael J. Simons
 * @param <T> type of content
 * @since 2021.1.0
 */
@API(status = INTERNAL, since = "2021.1.0")
abstract class LiteralBase<T> implements Literal<T> {

	/**
	 * The content of this literal.
	 */
	private final T content;

	/**
	 * Creates a new literal from the given content
	 * @param content The content of the new literal
	 */
	protected LiteralBase(T content) {
		this.content = content;
	}

	/**
	 * @return The content of this literal, may be {@literal null}
	 */
	public final T getContent() {
		return content;
	}
}

