package org.panda.utility.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension of UndirectedGraph implementing edge weights.
 *
 * @author Ozgun Babur
 */
public class UndirectedGraphWithEdgeWeights extends UndirectedGraph
{
	protected Map<String, Map<String, Double>> weightMap;

	public UndirectedGraphWithEdgeWeights()
	{
		super();
		weightMap = new HashMap<>();
	}

	public UndirectedGraphWithEdgeWeights(String name, String edgeType)
	{
		super(name, edgeType);
		weightMap = new HashMap<>();
	}

	public void clear()
	{
		super.clear();
		weightMap.clear();
	}

	public void putRelation(String source, String target, double weight)
	{
		if (!allowSelfEdges && source.equals(target)) return;

		super.putRelation(source, target);

		if (!weightMap.containsKey(source)) weightMap.put(source, new HashMap<>());
		if (!weightMap.containsKey(target)) weightMap.put(target, new HashMap<>());
		weightMap.get(source).put(target, weight);
		weightMap.get(target).put(source, weight);
	}

	public double getWeight(String source, String target)
	{
		if (weightMap.containsKey(source) && weightMap.get(source).containsKey(target))
		{
			return weightMap.get(source).get(target);
		}
		return Double.NaN;
	}

	public void removeRelation(String source, String target)
	{
		super.removeRelation(source, target);

		if (weightMap.containsKey(source))
		{
			weightMap.get(source).remove(target);

			if (weightMap.get(source).isEmpty())
			{
				weightMap.remove(source);
			}
		}
		if (weightMap.containsKey(target))
		{
			weightMap.get(target).remove(source);

			if (weightMap.get(target).isEmpty())
			{
				weightMap.remove(target);
			}
		}
	}
}
