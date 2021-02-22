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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.neo4j.cypherdsl.core.Relationship.Direction;
import org.neo4j.cypherdsl.core.support.Visitable;
import org.neo4j.cypherdsl.core.support.Visitor;
import org.neo4j.cypherdsl.core.utils.Assertions;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M15/railroad/NodePattern.html">NodePattern</a>.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = EXPERIMENTAL, since = "1.0")
public final class Node implements PatternElement, PropertyContainer, ExposesRelationships<Relationship>, ExposesProperties<Node> {

	static Node create(String primaryLabel, String... additionalLabels) {

		return create(primaryLabel, null, additionalLabels);
	}

	static Node create(String primaryLabel, MapExpression properties, String... additionalLabels) {

		Assertions.hasText(primaryLabel, "A primary label is required.");

		for (String additionalLabel : additionalLabels) {
			Assertions.hasText(additionalLabel, "An empty label is not allowed.");
		}

		return new Node(primaryLabel, properties == null ? null : new Properties(properties), additionalLabels);
	}

	/**
	 * @return A node without labels and properties
	 */
	static Node create() {
		return new Node(null, null);
	}

	private volatile SymbolicName symbolicName;

	private final List<NodeLabel> labels;

	private final Properties properties;

	private Node(String primaryLabel, Properties properties, String... additionalLabels) {

		this.symbolicName = null;

		this.labels = new ArrayList<>();
		if (!(primaryLabel == null || primaryLabel.isEmpty())) {
			this.labels.add(new NodeLabel(primaryLabel));
		}
		this.labels.addAll(Arrays.stream(additionalLabels).map(NodeLabel::new).collect(Collectors.toList()));
		this.properties = properties;
	}

	private Node(SymbolicName symbolicName, Properties properties, List<NodeLabel> labels) {

		this.symbolicName = symbolicName;

		this.labels = new ArrayList<>(labels);
		this.properties = properties;
	}

	@API(status = INTERNAL)
	List<NodeLabel> getLabels() {
		return Collections.unmodifiableList(labels);
	}

	/**
	 * Creates a copy of this node with a new symbolic name.
	 *
	 * @param newSymbolicName the new symbolic name.
	 * @return The new node.
	 */
	public Node named(String newSymbolicName) {

		Assertions.hasText(newSymbolicName, "Symbolic name is required.");
		return new Node(SymbolicName.of(newSymbolicName), properties, labels);
	}

	/**
	 * Creates a copy of this node with a new symbolic name.
	 *
	 * @param newSymbolicName the new symbolic name.
	 * @return The new node.
	 */
	public Node named(SymbolicName newSymbolicName) {

		Assertions.notNull(newSymbolicName, "Symbolic name is required.");
		return new Node(newSymbolicName, properties, labels);
	}

	@Override
	public Node withProperties(MapExpression newProperties) {

		return new Node(this.symbolicName, newProperties == null ? null : new Properties(newProperties), labels);
	}

	@Override
	public Node withProperties(Object... keysAndValues) {

		MapExpression newProperties = null;
		if (keysAndValues != null && keysAndValues.length != 0) {
			newProperties = MapExpression.create(keysAndValues);
		}
		return withProperties(newProperties);
	}

	/**
	 * @return The optional symbolic name of this node.
	 */
	public Optional<SymbolicName> getSymbolicName() {
		return Optional.ofNullable(symbolicName);
	}

	@Override
	public SymbolicName getRequiredSymbolicName() {

		SymbolicName requiredSymbolicName = this.symbolicName;
		if (requiredSymbolicName == null) {
			synchronized (this) {
				requiredSymbolicName = this.symbolicName;
				if (requiredSymbolicName == null) {
					this.symbolicName = SymbolicName.unresolved();
					requiredSymbolicName = this.symbolicName;
				}
			}
		}
		return requiredSymbolicName;
	}

	/**
	 * @return A new function invocation returning the internal id of this node.
	 */
	public FunctionInvocation internalId() {
		return Functions.id(this);
	}

	/**
	 * @return A new function invocation returning the labels of this node.
	 */
	public FunctionInvocation labels() {
		return Functions.labels(this);
	}

	@Override
	public Relationship relationshipTo(Node other, String... types) {
		return Relationship.create(this, Direction.LTR, other, types);
	}

	@Override
	public Relationship relationshipFrom(Node other, String... types) {
		return Relationship.create(this, Direction.RTL, other, types);
	}

	@Override
	public Relationship relationshipBetween(Node other, String... types) {
		return Relationship.create(this, Direction.UNI, other, types);
	}

	/**
	 * A condition that checks for the presence of labels on a node.
	 *
	 * @param labelsToQuery A list of labels to query
	 * @return A condition that checks whether this node has all of the labels to query
	 */
	public Condition hasLabels(String... labelsToQuery) {
		return HasLabelCondition.create(this.getSymbolicName()
				.orElseThrow(() -> new IllegalStateException("Cannot query a node without a symbolic name.")),
			labelsToQuery);
	}

	@Override
	public void accept(Visitor visitor) {

		visitor.enter(this);
		Visitable.visitIfNotNull(this.symbolicName, visitor);
		this.labels.forEach(label -> label.accept(visitor));
		Visitable.visitIfNotNull(this.properties, visitor);
		visitor.leave(this);
	}

	@Override
	public String toString() {
		return "Node{" +
				"symbolicName=" + symbolicName +
				", labels=" + labels +
				'}';
	}

	/**
	 * Creates a new condition whether this node is equal to {@literal otherNode}.
	 *
	 * @param otherNode The node to compare this node to.
	 * @return A condition.
	 */
	public Condition isEqualTo(Node otherNode) {

		return this.getRequiredSymbolicName().isEqualTo(otherNode.getRequiredSymbolicName());
	}

	/**
	 * Creates a new condition whether this node is not equal to {@literal otherNode}.
	 *
	 * @param otherNode The node to compare this node to.
	 * @return A condition.
	 */
	public Condition isNotEqualTo(Node otherNode) {

		return this.getRequiredSymbolicName().isNotEqualTo(otherNode.getRequiredSymbolicName());
	}

	/**
	 * Creates a new condition based on this node whether it is null.
	 *
	 * @return A condition.
	 */
	public Condition isNull() {

		return this.getRequiredSymbolicName().isNull();
	}

	/**
	 * Creates a new condition based on this node whether it is not null.
	 *
	 * @return A condition.
	 */
	public Condition isNotNull() {

		return this.getRequiredSymbolicName().isNotNull();
	}

	/**
	 * Creates a new sort item of this node in descending order.
	 *
	 * @return A sort item.
	 */
	public SortItem descending() {

		return this.getRequiredSymbolicName().descending();
	}

	/**
	 * Creates a new sort item of this node in ascending order.
	 *
	 * @return A sort item.
	 */
	public SortItem ascending() {

		return this.getRequiredSymbolicName().ascending();
	}

	/**
	 * Creates an alias for this node.
	 *
	 * @param alias The alias to use.
	 * @return The aliased expression.
	 */
	public AliasedExpression as(String alias) {

		return this.getRequiredSymbolicName().as(alias);
	}
}
