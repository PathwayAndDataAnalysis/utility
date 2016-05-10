package org.panda.utility.graph.query.algorithm;

import org.panda.utility.graph.query.QueryEdge;
import org.panda.utility.graph.query.QueryGraphObject;
import org.panda.utility.graph.query.QueryNode;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class PathsBetween
{
	private Set<QueryNode> sourceSeed;
	private Set<QueryNode> targetSeed;
	private boolean directed;
	private int limit;

	/**
	 * If false, then any path in length limit will come. If true, shortest=k limit will be used,
	 * again bounded by limit.
	 */
	private boolean useShortestPlusK = false;

	/**
	 * If true, will ignore cycles.
	 */
	private boolean ignoreSelfLoops = true;

	/**
	 * If true, then a shortest path will be considered for each distinct pair. If false, then a
	 * shortest path length per gene will be used.
	 */
	private boolean considerAllPairs = false;

	/**
	 * If true, and if the reverse path is longer, it wont be retrieved.
	 */
	private boolean shortestAnyDir = true;
	private Map<QueryNode, Map<QueryNode, Integer>> shortestPairLengths;
	private Map<QueryNode, Integer> shortestSingleLengths;
	private int k = 0;

	Set<QueryGraphObject> goi;

	private Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> fwdLabel;
	private Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> bkwLabel;
	private Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> labelMap;

	private Set<QueryGraphObject> visitedGlobal;
	private Set<QueryGraphObject> visitedStep;

	public PathsBetween(Set<QueryNode> seed, boolean directed, int limit)
	{
		this(seed, seed, directed, limit);
	}

	public PathsBetween(Set<QueryNode> sourceSeed, Set<QueryNode> targetSeed, boolean directed, int limit)
	{
		this.sourceSeed = sourceSeed;
		this.targetSeed = targetSeed;
		this.directed = directed;
		this.limit = limit;
	}

	public void setUseShortestPlusK(boolean useShortestPlusK)
	{
		this.useShortestPlusK = useShortestPlusK;
	}

	public void setIgnoreSelfLoops(boolean ignoreSelfLoops)
	{
		this.ignoreSelfLoops = ignoreSelfLoops;
	}

	public void setConsiderAllPairs(boolean considerAllPairs)
	{
		this.considerAllPairs = considerAllPairs;
	}

	public void setShortestAnyDir(boolean shortestAnyDir)
	{
		this.shortestAnyDir = shortestAnyDir;
	}

	public void setShortestPairLengths(Map<QueryNode, Map<QueryNode, Integer>> shortestPairLengths)
	{
		this.shortestPairLengths = shortestPairLengths;
	}

	public void setK(int k)
	{
		this.k = k;
	}

	public Set<QueryGraphObject> run()
	{
		goi = new HashSet<>();
		visitedGlobal = new HashSet<>();
		visitedStep = new HashSet<>();

		if (directed)
		{
			this.fwdLabel = new HashMap<>();
			this.bkwLabel = new HashMap<>();
		}
		else this.labelMap = new HashMap<>();

		for (QueryNode node : sourceSeed)
		{
			initSeed(node);

			if (directed)
			{
				runBFS_directed(node, FORWARD);
			}
			else
			{
				runBFS_undirected(node);
			}

			// Record distances for that seed node
			recordDistances(node);

			// Remove all algorithm specific labels
			clearLabels();
		}

		for (QueryNode node : targetSeed)
		{
			if (!directed && sourceSeed.contains(node)) continue;

			initSeed(node);

			if (directed)
			{
				runBFS_directed(node, BACKWARD);
			}
			else
			{
				runBFS_undirected(node);
			}

			// Record distances for that seed node
			recordDistances(node);

			// Remove all algorithm specific labels
			clearLabels();
		}

		if (useShortestPlusK) findShortestPaths();

		// Reformat the label maps

		if (directed)
		{
			mergeLabels(fwdLabel);
			mergeLabels(bkwLabel);
		}
		else mergeLabels(labelMap);

		// Select graph objects that are traversed with the BFS. It is important to process nodes
		// before edges.
		selectSatisfyingElements();

		// Prune so that no non-seed degree-1 nodes remain
		pruneResult();

		assert checkEdgeSanity();

		return goi;
	}

	private void runBFS_directed(QueryNode seed, boolean direction)
	{
		assert directed;

		// Initialize queue to contain all seed nodes

		LinkedList<QueryNode> queue = new LinkedList<>();
		queue.add(seed);
		visitedStep.add(seed);

		// Run BFS forward or backward till queue is not empty

		while (!queue.isEmpty())
		{
			QueryNode node = queue.poll();
			BFS_directed(node, direction, queue);
		}
	}

	private void runBFS_undirected(QueryNode seed)
	{
		assert !directed;

		// Initialize queue to contain all seed nodes

		LinkedList<QueryNode> queue = new LinkedList<>();
		queue.add(seed);
		visitedStep.add(seed);

		// Run BFS till queue is not empty

		while (!queue.isEmpty())
		{
			QueryNode node = queue.poll();
			BFS_undirected(node, queue);
		}
	}

	private void BFS_directed(QueryNode node, boolean forward, LinkedList<QueryNode> queue)
	{
		assert directed;

		if (forward)
		{
			BFStep(node, DOWNSTREAM, DIST_FORWARD, queue);
		}
		else
		{
			BFStep(node, UPSTREAM, DIST_BACKWARD, queue);
		}
	}

	private void BFStep(QueryNode node, boolean upstr, String label, LinkedList<QueryNode> queue)
	{
		int d = getLabel(node, label);

		if (d < limit)
		{
			for (QueryEdge edge : upstr? node.incoming : node.outgoing)
			{
				if (visitedStep.contains(edge)) continue;

				setLabel(edge, label, !upstr && label.equals(DIST_FORWARD) ? d + 1 : d);

				QueryNode n = upstr ? edge.source : edge.target;

				int d_n = getLabel(n, label);

				if (d_n > d + 1)
				{
					if (d + 1 < limit && !visitedStep.contains(n) && !queue.contains(n)
						&& (!ignoreSelfLoops || !(sourceSeed.contains(n) || targetSeed.contains(n))))
						queue.add(n);

					setLabel(n, label, d + 1);
				}
			}
		}
	}

	private void BFS_undirected(QueryNode node, LinkedList<QueryNode> queue)
	{
		assert !directed;

		BFStep(node, UPSTREAM, DIST, queue);
		BFStep(node, DOWNSTREAM, DIST, queue);
	}

	private void initSeed(QueryGraphObject obj)
	{
		if (directed)
		{
			setLabel(obj, DIST_FORWARD, 0);
			setLabel(obj, DIST_BACKWARD, 0);
		}
		else
		{
			setLabel(obj, DIST, 0);
		}
	}

	private void selectSatisfyingElements()
	{
		for (QueryGraphObject go : visitedGlobal)
		{
			if (distanceSatisfies(go))
			{
				goi.add(go);
			}
		}

		// Remove edges in the result whose node is not in the result

		Set<QueryEdge> extra = new HashSet<>();
		for (QueryGraphObject go : goi)
		{
			if (go instanceof QueryEdge)
			{
				QueryEdge edge = (QueryEdge) go;
				if (!goi.contains(edge.source) || !goi.contains(edge.target))
				{
					extra.add(edge);
				}
			}
		}
		goi.removeAll(extra);
	}

	private boolean distanceSatisfies(QueryGraphObject go)
	{
		if (directed)
		{
			return this.distanceSatisfies(go, fwdLabel, bkwLabel);
		}
		else
		{
			return this.distanceSatisfies(go, labelMap, labelMap);

			// just to remember old

//			if (!labelMap.containsKey(go)) return false;
//
//			for (Integer i : labelMap.get(go).keySet())
//			{
//				if (i <= limit && labelMap.get(go).get(i).size() > 1) return true;
//			}
//			return false;
		}
	}

	private boolean distanceSatisfies(QueryGraphObject go,
		Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> fwdLabel,
		Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> bkwLabel)
	{
		if (!fwdLabel.containsKey(go) || !bkwLabel.containsKey(go)) return false;

		for (Integer i : fwdLabel.get(go).keySet())
		{
			for (Integer j : bkwLabel.get(go).keySet())
			{
				int dist = i + j;

				if (!directed && go instanceof QueryEdge) dist++;

				if (dist <= limit)
				{
					if (setsSatisfy(fwdLabel.get(go).get(i), bkwLabel.get(go).get(j), dist))
						return true;
				}
			}
		}
		return false;
	}

	private void pruneResult()
	{
		for (QueryGraphObject go : new HashSet<>(goi))
		{
			if (go instanceof QueryNode)
			{
				prune((QueryNode) go);
			}
		}
	}

	private void prune(QueryNode node)
	{
		if (goi.contains(node) && !(sourceSeed.contains(node) || targetSeed.contains(node)))
		{
			if (getNeighborsInResult(node).size() <= 1)
			{
				goi.remove(node);
				goi.removeAll(node.incoming);
				goi.removeAll(node.outgoing);

				for (QueryNode n : getNeighborsOverResultEdges(node))
				{
					prune(n);
				}
			}
		}
	}


	private Set<QueryNode> getNeighborsOverResultEdges(QueryNode node)
	{
		Set<QueryNode> set = new HashSet<>();

		for (QueryEdge edge : node.incoming)
		{
			if (goi.contains(edge))
			{
				set.add(edge.source);
			}
		}
		for (QueryEdge edge : node.outgoing)
		{
			if (goi.contains(edge))
			{
				set.add(edge.target);
			}
		}
		set.remove(node);
		return set;
	}

	private Set<QueryNode> getNeighborsInResult(QueryNode node)
	{
		Set<QueryNode> set = getNeighborsOverResultEdges(node);
		set.retainAll(goi);
		return set;
	}

	private void clearLabels()
	{
		for (QueryGraphObject go : visitedStep)
		{
			if (directed)
			{
				go.removeLabel(DIST_FORWARD);
				go.removeLabel(DIST_BACKWARD);
			}
			else
			{
				go.removeLabel(DIST);
			}
		}
		visitedStep.clear();
	}

	private boolean checkEdgeSanity()
	{
		for (QueryGraphObject go : goi)
		{
			if (go instanceof QueryEdge)
			{
				QueryEdge edge = (QueryEdge) go;

				assert goi.contains(edge.source);
				assert goi.contains(edge.target);
			}
		}
		return true;
	}

	private int getLabel(QueryGraphObject go, String label)
	{
		if (go.hasLabel(label)) return (Integer) go.getLabel(label);
		else return Integer.MAX_VALUE / 2;
	}
	private void setLabel(QueryGraphObject go, String label, Integer value)
	{
		go.putLabel(label, value);
		visitedStep.add(go);
		visitedGlobal.add(go);
	}

	private void recordDistances(QueryNode seed)
	{
		for (QueryGraphObject go : visitedGlobal)
		{
			if (directed)
			{
				recordDistance(go, seed, DIST_FORWARD, fwdLabel);
				recordDistance(go, seed, DIST_BACKWARD, bkwLabel);
			}
			else recordDistance(go, seed, DIST, labelMap);
		}
	}

	private void recordDistance(QueryGraphObject go, QueryNode seed, String label,
		Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> map)
	{
		int d = getLabel(go, label);
		if (d > limit) return;
		if (!map.containsKey(go)) map.put(go, new HashMap<>());
		if (!map.get(go).containsKey(d)) map.get(go).put(d, new HashSet<>());
		map.get(go).get(d).add(seed);
	}

	private void mergeLabels(Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> map)
	{
		for (QueryGraphObject go : map.keySet())
		{
			for (int i = 0; i < limit; i++)
			{
				if (map.get(go).containsKey(i))
				{
					for (int j = i+1; j <= limit; j++)
					{
						if (map.get(go).containsKey(j))
						{
							map.get(go).get(j).addAll(map.get(go).get(i));
						}
					}
				}
			}
		}
	}

	private boolean setsSatisfy(Set<QueryNode> set1, Set<QueryNode> set2, int length)
	{
		assert !set1.isEmpty();
		assert !set2.isEmpty();

		if (useShortestPlusK)
		{
			for (QueryNode source : set1)
			{
				for (QueryNode target : set2)
				{
					if (ignoreSelfLoops && source.equals(target)) continue;
					if (!sourceSeed.contains(source) || !targetSeed.contains(target)) continue;

					if ((considerAllPairs && shortestPairLengths.containsKey(source) &&
						shortestPairLengths.get(source).containsKey(target)) ||
						(!considerAllPairs && shortestSingleLengths.containsKey(source) &&
							shortestSingleLengths.containsKey(target)))
					{
						// decide limit
						int limit;

						if (considerAllPairs)
						{
							limit = shortestPairLengths.get(source).get(target);
							if (shortestAnyDir && shortestPairLengths.containsKey(target) &&
								shortestPairLengths.get(target).containsKey(source))
							{
								limit = Math.min(limit, shortestPairLengths.get(target).get(source));
							}
						}
						else
						{
							limit = Math.max(shortestSingleLengths.get(source),
								shortestSingleLengths.get(target));
						}

						limit = Math.min(limit + k, this.limit);

						if (limit >= length) return true;
					}
				}
			}
			return false;
		}
		else
		{
			for (QueryNode source : set1)
			{
				for (QueryNode target : set2)
				{
					if (ignoreSelfLoops && source.equals(target)) continue;
					if (sourceSeed.contains(source) && targetSeed.contains(target)) return true;
				}
			}
			return false;
		}
	}

	private void findShortestPaths()
	{
		if (directed) this.findShortestPaths(fwdLabel, bkwLabel);
		else this.findShortestPaths(labelMap, labelMap);
	}

	private void findShortestPaths(Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> fwdLabel,
		Map<QueryGraphObject, Map<Integer, Set<QueryNode>>> bkwLabel)
	{
		if (considerAllPairs) shortestPairLengths = new HashMap<>();
		else shortestSingleLengths = new HashMap<>();

		for (QueryGraphObject go : fwdLabel.keySet())
		{
			if (go instanceof QueryEdge) continue;

			Map<Integer, Set<QueryNode>> fwMap = fwdLabel.get(go);
			Map<Integer, Set<QueryNode>> bwMap = bkwLabel.get(go);

			if (fwMap == null || bwMap == null) continue;

			for (Integer d1 : fwMap.keySet())
			{
				for (QueryNode source : fwMap.get(d1))
				{
					for (Integer d2 : bwMap.keySet())
					{
						if (d1 + d2 > limit) continue;

						for (QueryNode target : bwMap.get(d2))
						{
							if (ignoreSelfLoops && source.equals(target)) continue;

							if (considerAllPairs)
							{
								if (!shortestPairLengths.containsKey(source))
								{
									shortestPairLengths.put(source, new HashMap<>());
								}

								if (!shortestPairLengths.get(source).containsKey(target) ||
									shortestPairLengths.get(source).get(target) > d1 + d2)
								{
									shortestPairLengths.get(source).put(target, d1 + d2);
								}
							}
							else
							{
								if (!shortestSingleLengths.containsKey(source) ||
									shortestSingleLengths.get(source) > d1 + d2)
								{
									shortestSingleLengths.put(source, d1 + d2);
								}
								if (!shortestSingleLengths.containsKey(target) ||
									shortestSingleLengths.get(target) > d1 + d2)
								{
									shortestSingleLengths.put(target, d1 + d2);
								}
							}
						}
					}
				}
			}
		}
	}

	public static final String DIST = "DIST";
	public static final String DIST_FORWARD = "DIST_FORWARD";
	public static final String DIST_BACKWARD = "DIST_BACKWARD";

	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;
	public static final boolean UPSTREAM = true;
	public static final boolean DOWNSTREAM = false;
}
