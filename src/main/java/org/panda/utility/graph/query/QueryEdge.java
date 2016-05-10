package org.panda.utility.graph.query;

import org.panda.utility.graph.Graph;

/**
 * @author Ozgun Babur
 */
public class QueryEdge extends QueryGraphObject
{
	public QueryNode source;
	public QueryNode target;
	public String type;

	public QueryEdge(QueryNode source, QueryNode target, String type)
	{
		this.source = source;
		this.target = target;
		this.type = type;
	}

	@Override
	public int hashCode()
	{
		return source.hashCode() + target.hashCode() + type.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof QueryEdge && ((QueryEdge) obj).source.equals(source) &&
			((QueryEdge) obj).target.equals(target) && ((QueryEdge) obj).type.equals(type);
	}
}
