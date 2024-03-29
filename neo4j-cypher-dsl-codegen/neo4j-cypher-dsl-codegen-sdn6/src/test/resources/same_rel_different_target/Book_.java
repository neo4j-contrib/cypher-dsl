package org.neo4j.cypherdsl.codegen.sdn6.models.same_rel_different_target;

import java.util.List;
import javax.annotation.Generated;
import org.neo4j.cypherdsl.core.MapExpression;
import org.neo4j.cypherdsl.core.NodeBase;
import org.neo4j.cypherdsl.core.NodeLabel;
import org.neo4j.cypherdsl.core.Properties;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.SymbolicName;

@Generated(
	value = "org.neo4j.cypherdsl.codegen.core.NodeImplBuilder",
	date = "2019-09-21T21:21:00+01:00",
	comments = "This class is generated by the Neo4j Cypher-DSL. All changes to it will be lost after regeneration."
)
public final class Book_ extends NodeBase<Book_> {
	public static final String $PRIMARY_LABEL = "Book";
	public static final Book_ BOOK = new Book_();

	public final Property TITLE = this.property("title");

	public Book_() {
		super($PRIMARY_LABEL);
	}

	private Book_(SymbolicName symbolicName, List<NodeLabel> labels, Properties properties) {
		super(symbolicName, labels, properties);
	}

	@Override
	public Book_ named(SymbolicName newSymbolicName) {
		return new Book_(newSymbolicName, getLabels(), getProperties());
	}

	@Override
	public Book_ withProperties(MapExpression newProperties) {
		return new Book_(getSymbolicName().orElse(null), getLabels(), Properties.create(newProperties));
	}
}
