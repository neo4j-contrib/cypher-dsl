/*
 * Copyright (c) 2019-2023 "Neo4j,"
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
package org.neo4j.cypherdsl.core.fump;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.neo4j.cypherdsl.build.annotations.RegisterForReflection;
import org.neo4j.cypherdsl.core.KeyValueMapEntry;
import org.neo4j.cypherdsl.core.Match;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.NodeLabel;
import org.neo4j.cypherdsl.core.PatternElement;
import org.neo4j.cypherdsl.core.Relationship;
import org.neo4j.cypherdsl.core.SymbolicName;
import org.neo4j.cypherdsl.core.ast.Visitable;
import org.neo4j.cypherdsl.core.internal.ReflectiveVisitor;

/**
 * @author Michael J. Simons
 * @soundtrack Avenger - Prayers Of Steel
 * @since TBA
 */
@RegisterForReflection
public class Thing extends ReflectiveVisitor {

	private boolean inMatch = false;

	private final AtomicReference<PatternElement> currentPatternElement = new AtomicReference<>();

	private final Set<Token> tokens = new HashSet<>();

	private final Set<Property> properties = new HashSet<>();

	// TODO this is bonkers without scope
	private final Map<SymbolicName, PatternElement> patternLookup = new HashMap<>();

	// TODO make private
	public Thing() {
	}

	// TODO make package private
	public Things getResult() {
		return new Things(this.tokens, this.properties);
	}

	@Override
	protected boolean preEnter(Visitable visitable) {
		return true;
	}

	@Override
	protected void postLeave(Visitable visitable) {
	}

	void enter(Match match) {
		inMatch = true;
	}

	void leave(Match match) {
		inMatch = false;
	}

	void enter(Node node) {

		node.getSymbolicName().ifPresent(s -> patternLookup.put(s, node));
		currentPatternElement.compareAndSet(null, node);
	}

	void enter(KeyValueMapEntry mapEntry) {

		var owner = currentPatternElement.get();
		if (owner == null) {
			return;
		}

		if (owner instanceof Node node) {
			this.properties.add(new Property(node.getLabels().stream().map(Token::label).collect(Collectors.toSet()), mapEntry.getKey()));
		} else if (owner instanceof Relationship relationship) {
			this.properties.add(new Property(relationship.getDetails().getTypes().stream().map(Token::type).collect(Collectors.toSet()), mapEntry.getKey()));
		}
	}

	void leave(Node node) {
		currentPatternElement.compareAndSet(node, null);
	}

	void enter(Relationship relationship) {

		relationship.getSymbolicName().ifPresent(s -> patternLookup.put(s, relationship));
		currentPatternElement.compareAndSet(null, relationship);
	}

	void leave(Relationship relationship) {
		currentPatternElement.compareAndSet(relationship, null);
	}

	void enter(org.neo4j.cypherdsl.core.Property property) {

		if (property.getNames().size() != 1) {
			return;
		}
		var lookup = property.getNames().get(0);
		if (lookup.isDynamicLookup()) {
			return;
		}

		if (property.getContainerReference() instanceof SymbolicName s) {
			var patternElement = patternLookup.get(s);
			if (patternElement instanceof Node node) {
				lookup.accept(segment -> {
					if (segment instanceof SymbolicName name) {
						properties.add(new Property(node.getLabels().stream().map(Token::label).collect(Collectors.toSet()), name.getValue()));
					}
				});
			} else if (patternElement instanceof Relationship relationship) {
				lookup.accept(segment -> {
					if (segment instanceof SymbolicName name) {
						properties.add(new Property(relationship.getDetails().getTypes().stream().map(Token::type).collect(Collectors.toSet()), name.getValue()));
					}
				});
			}
		}
	}

	void enter(NodeLabel label) {
		this.tokens.add(new Token(Token.Type.NODE_LABEL, label.getValue()));
	}

	void enter(Relationship.Details details) {
		details.getTypes().stream().map(Token::type).forEach(tokens::add);
	}
}
