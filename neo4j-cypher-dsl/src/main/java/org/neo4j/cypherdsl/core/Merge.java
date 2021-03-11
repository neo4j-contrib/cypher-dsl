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
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.neo4j.cypherdsl.core.support.Visitable;
import org.neo4j.cypherdsl.core.support.Visitor;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M15/railroad/Create.html">Create</a>.
 *
 * @author Michael J. Simons
 * @soundtrack Die Ärzte - Seitenhirsch
 * @since 1.0
 */
@API(status = EXPERIMENTAL, since = "1.0")
public final class Merge implements UpdatingClause {

	/**
	 * A literal for the blank.
	 */
	private static final Literal<String> BLANK = new LiteralBase<String>(" ") {
		@Override public String asString() {
			return super.getContent();
		}
	};

	private final Pattern pattern;
	private final List<Visitable> onCreateOrMatchEvents;

	Merge(Pattern pattern) {
		this.pattern = pattern;
		this.onCreateOrMatchEvents = Collections.emptyList();
	}

	Merge(Pattern pattern, List<MergeAction> mergeActions) {
		this.pattern = pattern;

		this.onCreateOrMatchEvents = new ArrayList<>();
		this.onCreateOrMatchEvents.add(BLANK);
		this.onCreateOrMatchEvents.addAll(mergeActions);
	}

	/**
	 * @return True if there are any events defined for the merge statement.
	 */
	@API(status = INTERNAL)
	public boolean hasEvents() {
		return !this.onCreateOrMatchEvents.isEmpty();
	}

	@Override
	public void accept(Visitor visitor) {

		visitor.enter(this);
		this.pattern.accept(visitor);
		this.onCreateOrMatchEvents.forEach(s -> s.accept(visitor));
		visitor.leave(this);
	}
}

