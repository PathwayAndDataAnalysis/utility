package org.panda.utility.graph.query.algorithm;

import org.panda.utility.graph.query.QueryEdge;
import org.panda.utility.graph.query.QueryGraphObject;
import org.panda.utility.graph.query.QueryNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds the common neighborhood of the given nodes. This is an undirected query.
 *
 * @author Ozgun Babur
 */
public class CommonNeighborhood
{
	private Set<QueryNode> sourceSeed;

	private int commonalityThreshold;

	public CommonNeighborhood(Set<QueryNode> seed, int commonalityThreshold)
	{
		this.sourceSeed = seed;
		this.commonalityThreshold = commonalityThreshold;
	}

	public Set<QueryGraphObject> run()
	{
		Set<QueryGraphObject> wanted = new HashSet<>(sourceSeed);

		Set<QueryGraphObject> consider = new HashSet<>();
		for (QueryNode node : sourceSeed)
		{
			consider.addAll(getNeighbors(node));
		}
		consider.addAll(sourceSeed);

		for (QueryGraphObject go : consider)
		{
			if (go instanceof QueryNode)
			{
				QueryNode node = (QueryNode) go;

				Set<QueryGraphObject> neigh = getNeighbors(node);

				neigh.retainAll(sourceSeed);

				if (neigh.size() >= commonalityThreshold) wanted.add(go);
			}
		}

		return consider.stream().filter(o -> o instanceof QueryEdge).map(o -> (QueryEdge) o)
			.filter(e -> wanted.contains(e.source) && wanted.contains(e.target))
			.map(e -> new QueryGraphObject[]{e, e.source, e.target}).flatMap(Arrays::stream)
			.collect(Collectors.toSet());
	}

	public Set<QueryGraphObject> getNeighbors(QueryNode node)
	{
		Set<QueryGraphObject> objects = new HashSet<>();
		for (QueryEdge edge : node.incoming)
		{
			objects.add(edge);
			objects.add(edge.source);
		}
		for (QueryEdge edge : node.outgoing)
		{
			objects.add(edge);
			objects.add(edge.target);
		}
		return objects;
	}
}
