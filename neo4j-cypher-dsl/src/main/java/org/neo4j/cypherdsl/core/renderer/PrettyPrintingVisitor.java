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
package org.neo4j.cypherdsl.core.renderer;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.neo4j.cypherdsl.core.Create;
import org.neo4j.cypherdsl.core.KeyValueMapEntry;
import org.neo4j.cypherdsl.core.MapExpression;
import org.neo4j.cypherdsl.core.Match;
import org.neo4j.cypherdsl.core.Merge;
import org.neo4j.cypherdsl.core.MergeAction;
import org.neo4j.cypherdsl.core.Operator;
import org.neo4j.cypherdsl.core.PropertyLookup;
import org.neo4j.cypherdsl.core.Return;
import org.neo4j.cypherdsl.core.Set;
import org.neo4j.cypherdsl.core.Where;
import org.neo4j.cypherdsl.core.With;
import org.neo4j.cypherdsl.core.renderer.Configuration.IndentStyle;

/**
 * @author Andreas Berger
 * @author Michael J. Simons
 */
@SuppressWarnings("unused")
class PrettyPrintingVisitor extends DefaultVisitor {

	private final BiConsumer<StringBuilder, Integer> indentionProvider;

	/**
	 * In contrast to the current level in the {@link DefaultVisitor} that contains the level of elements in the tree.
	 */
	private int indentationLevel;
	private int topCount;

	PrettyPrintingVisitor(IndentStyle indentStyle, int indentSize) {

		if (indentStyle == IndentStyle.TAB) {
			indentionProvider = (builder, width) -> {
				for (int i = 0; i < width; i++) {
					builder.append("\t");
				}
			};
		} else {
			indentionProvider = (builder, width) -> {
				for (int i = 0; i < width * indentSize; i++) {
					builder.append(" ");
				}
			};
		}
	}

	private void indent(int width) {
		indentionProvider.accept(builder, width);
	}

	@Override
	void enter(Where where) {
		if (currentVisitedElements.stream().noneMatch(visitable -> visitable instanceof Return)) {
			builder.append("\nWHERE ");
		} else {
			super.enter(where);
		}
	}

	@Override
	void enter(Return returning) {
		trimNewline();
		super.enter(returning);
	}

	@Override
	void enter(With with) {
		trimNewline();
		super.enter(with);
	}

	@Override
	void enter(Set set) {
		if (currentVisitedElements.stream().noneMatch(visitable -> visitable instanceof MergeAction)) {
			trimNewline();
		}
		super.enter(set);
	}

	@Override
	void enter(Match match) {
		if (topCount > 0) {
			trimNewline();
		}
		super.enter(match);
	}

	@Override
	void leave(Match match) {
		topCount++;
		super.leave(match);
	}

	@Override
	void enter(Create create) {
		if (topCount > 0) {
			trimNewline();
		}
		super.enter(create);
	}

	@Override
	void leave(Create create) {
		topCount++;
		super.leave(create);
	}

	@Override
	void enter(PropertyLookup propertyLookup) {
		if (currentVisitedElements.stream().skip(1).limit(1)
				.anyMatch(visitable -> visitable instanceof MapExpression)) {
			trimNewline();
			indent(indentationLevel);
		}
		super.enter(propertyLookup);
	}

	@Override
	void enter(KeyValueMapEntry map) {
		if (indentationLevel > 0) {
			trimNewline();
			indent(indentationLevel);
		}
		super.enter(map);
	}

	@Override
	void enter(Operator operator) {
		Operator.Type type = operator.getType();
		if (type == Operator.Type.LABEL) {
			return;
		}
		if (type != Operator.Type.PREFIX && operator != Operator.EXPONENTIATION) {
			builder.append(" ");
		}
		if (operator == Operator.OR || operator == Operator.AND || operator == Operator.XOR) {
			trimNewline();
			indent(1);
		}
		builder.append(operator.getRepresentation());
		if (type != Operator.Type.POSTFIX && operator != Operator.EXPONENTIATION) {
			builder.append(" ");
		}
	}

	@Override
	void enter(MapExpression map) {
		indentationLevel++;
		builder.append(" ");
		super.enter(map);
	}

	@Override
	void leave(MapExpression map) {
		indentationLevel--;
		trimNewline();
		indent(indentationLevel);
		super.leave(map);
	}

	@Override
	void enter(MergeAction onCreateOrMatchEvent) {
		trimNewline();
		indent(1);
		super.enter(onCreateOrMatchEvent);
	}

	@Override
	void enter(Merge merge) {
		if (topCount > 0) {
			trimNewline();
		}
		super.enter(merge);
	}

	@Override
	void leave(Merge merge) {
		topCount++;
		super.leave(merge);
	}

	private void trimNewline() {
		for (int i = builder.length() - 1; i >= 0; i--) {
			if (builder.charAt(i) == ' ') {
				builder.deleteCharAt(i);
			} else {
				break;
			}
		}
		builder.append("\n");
	}

	@Override
	protected Optional<String> escapeName(CharSequence unescapedName) {
		if (unescapedName == null) {
			return Optional.empty();
		}
		return Optional.of(super.escapeIfNecessary(unescapedName.toString()));
	}
}
