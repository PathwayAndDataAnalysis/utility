package org.panda.utility.graph.query;

import org.panda.utility.graph.Graph;
import org.panda.utility.graph.query.algorithm.CommonNeighborhood;
import org.panda.utility.graph.query.algorithm.PathsBetween;

import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class QueryExecuter
{
	public static Set<QueryGraphObject> pathsBetween(Set<String> seed, Graph graph, int limit,
		boolean directed, int k, boolean ignoreLoops)
	{
		QueryGraph g = new QueryGraph(graph);
		PathsBetween query = new PathsBetween(g.getNodes(seed), directed, limit);

		if (k >= 0)
		{
			query.setUseShortestPlusK(true);
			query.setK(k);
		}

		query.setIgnoreSelfLoops(ignoreLoops);

		return query.run();
	}

	public static Set<QueryGraphObject> pathsFromTo(Set<String> source, Set<String> target, Graph graph,
		int limit, boolean directedGraph, int k, boolean ignoreLoops)
	{
		QueryGraph g = new QueryGraph(graph);
		PathsBetween query = new PathsBetween(g.getNodes(source), g.getNodes(target), directedGraph, limit);

		if (k >= 0)
		{
			query.setUseShortestPlusK(true);
			query.setK(k);
		}

		query.setIgnoreSelfLoops(ignoreLoops);

		return query.run();
	}

	public static Set<QueryGraphObject> commonNeighborhood(Set<String> seed, Graph graph, int commonalityThreshold)
	{
		QueryGraph g = new QueryGraph(graph);
		CommonNeighborhood query = new CommonNeighborhood(g.getNodes(seed), commonalityThreshold);
		return query.run();
	}


}
