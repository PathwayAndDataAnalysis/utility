package org.panda.utility.graph.query;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class QueryNode extends QueryGraphObject
{
	public String id;
	public Set<QueryEdge> incoming;
	public Set<QueryEdge> outgoing;

	public QueryNode(String id)
	{
		this.id = id;
		incoming = new HashSet<>();
		outgoing = new HashSet<>();
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof QueryNode && ((QueryNode) obj).id.equals(id);
	}
}
