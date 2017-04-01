package org.panda.utility.graph.query;

import org.panda.utility.graph.DirectedGraph;
import org.panda.utility.graph.Graph;
import org.panda.utility.graph.GraphList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * For running graph queries on simple binary graphs.
 *
 * @author Ozgun Babur
 */
public class QueryGraph
{
	Map<String, QueryNode> nodeMap;
	Set<QueryEdge> edges;

	public QueryGraph(Graph... graphs)
	{
		if (graphs.length == 1 && graphs[0] instanceof GraphList)
		{
			graphs = ((GraphList) graphs[0]).getGraphs().toArray(new Graph[0]);
		}

		nodeMap = new HashMap<>();
		edges = new HashSet<>();

		for (Graph graph : graphs)
		{
			if (graph.isDirected())
			{
				for (String sourceID : ((DirectedGraph) graph).getOneSideSymbols(true))
				{
					for (String targetID : ((DirectedGraph) graph).getDownstream(sourceID))
					{
						addEdge(new QueryEdge(getNode(sourceID), getNode(targetID), graph.getEdgeType()));
					}
				}
			}
		}
	}

	public void addEdge(QueryEdge edge)
	{
		if (edges.contains(edge)) return;

		edge.source.outgoing.add(edge);
		edge.target.incoming.add(edge);
		edges.add(edge);
	}

	public void removeEdge(QueryEdge edge)
	{
		if (!edges.contains(edge)) return;

		edge.source.outgoing.remove(edge);
		edge.target.incoming.remove(edge);
		edges.remove(edge);
	}

	public QueryNode getNode(String id)
	{
		if (!nodeMap.containsKey(id)) nodeMap.put(id, new QueryNode(id));
		return nodeMap.get(id);
	}

	public Set<QueryNode> getNodes(Set<String> names)
	{
		return names.stream().map(nodeMap::get).filter(n -> n != null).collect(Collectors.toSet());
	}
}
