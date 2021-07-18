package org.panda.utility.graph;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;
import org.panda.utility.graph.query.QueryEdge;
import org.panda.utility.graph.query.QueryGraphObject;
import org.panda.utility.statistics.FDR;
import org.panda.utility.statistics.FishersExactTest;
import org.panda.utility.statistics.Histogram;
import org.panda.utility.statistics.Summary;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * This is a simple graph, built using external maps. All nodes are identified with a unique String.
 * Relations can be either directed or undirected, and can be mixed.
 *
 * @author Ozgun Babur
 */
public class DirectedGraph extends Graph
{
	protected Map<String, Set<String>> dwMap;
	protected Map<String, Set<String>> upMap;

	public DirectedGraph()
	{
		dwMap = new HashMap<>();
		upMap = new HashMap<>();
	}

	public DirectedGraph(String name, String edgeType)
	{
		super(name, edgeType);
		dwMap = new HashMap<>();
		upMap = new HashMap<>();
	}

	public void write(Writer writer)
	{
		try
		{
			for (String g1 : dwMap.keySet())
			{
				for (String g2 : dwMap.get(g1))
				{
					writer.write(g1 + "\t" + getEdgeType() + "\t" + g2);

					if (mediators.containsKey(g1) && mediators.get(g1).containsKey(g2))
					{
						writer.write("\t" + convertMediatorsToString(mediators.get(g1).get(g2)));
					}

					writer.write("\n");
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void clear()
	{
		super.clear();
		upMap.clear();
		dwMap.clear();
	}

	public void putRelation(String source, String target)
	{
		if (!allowSelfEdges && source.equals(target)) return;

		if (!upMap.containsKey(target)) upMap.put(target, new HashSet<>());
		if (!dwMap.containsKey(source)) dwMap.put(source, new HashSet<>());
		upMap.get(target).add(source);
		dwMap.get(source).add(target);
	}

	public void removeRelation(String source, String target)
	{
		if (upMap.containsKey(target))
		{
			upMap.get(target).remove(source);

			if (upMap.get(target).isEmpty())
			{
				upMap.remove(target);
			}
		}
		if (dwMap.containsKey(source))
		{
			dwMap.get(source).remove(target);

			if (dwMap.get(source).isEmpty())
			{
				dwMap.remove(source);
			}
		}

		removeMediators(source, target);
	}

	public void removeSubgraph(DirectedGraph sub)
	{
		for (String source : sub.getOneSideSymbols(true))
		{
			for (String target : sub.getDownstream(source))
			{
				removeRelation(source, target);
			}
		}
	}

	public DirectedGraph getIntersectingGraph(DirectedGraph other)
	{
		DirectedGraph graph = new DirectedGraph("Intersection", getEdgeType());
		Set<String> sources = getOneSideSymbols(true);
		for (String source : other.getOneSideSymbols(true))
		{
			if (sources.contains(source))
			{
				for (String target : other.getDownstream(source))
				{
					if (this.hasRelation(source, target)) graph.putRelation(source, target);
				}
			}
		}
		return graph;
	}

	public DirectedGraph getDownstreamSubgraph(Set<String> seeds)
	{
		DirectedGraph subgraph = new DirectedGraph("Downstream graph", getEdgeType());

		Set<String> currentSources = new HashSet<>();
		Set<String> previousLayers = new HashSet<>();
		Set<String> nextSources = new HashSet<>(seeds);

		while (!nextSources.isEmpty())
		{
			previousLayers.addAll(currentSources);
			currentSources.clear();
			currentSources.addAll(nextSources);
			nextSources.clear();

			for (String node : currentSources)
			{
				Set<String> downstream = new HashSet<>(getDownstream(node));
				downstream.removeAll(previousLayers);
				for (String dw : downstream)
				{
					subgraph.putRelation(node, dw, getMediators(node, dw));
				}
				downstream.removeAll(currentSources);
				nextSources.addAll(downstream);
			}
		}

		return subgraph;
	}

	public boolean isDirected()
	{
		return true;
	}

	public Set<String> goBFS(String seed, boolean downstream)
	{
		return goBFS(seed, downstream ? dwMap : upMap);
	}

	public Set<String> goBFS(Set<String> seed, Set<String> visited, boolean downstream)
	{
		return goBFS(seed, visited, downstream ? dwMap : upMap);
	}

	public Set<String> getUpstream(Collection<String> genes)
	{
		return getStream(genes, true);
	}

	public Set<String> getDownstream(Collection<String> genes)
	{
		return getStream(genes, false);
	}

	protected Set<String> getStream(Collection<String> genes, boolean upstream)
	{
		Set<String> result = new HashSet<String>();
		for (String gene : genes)
		{
			result.addAll(upstream ? getUpstream(gene) : getDownstream(gene));
		}
		return result;
	}

	public Set<String> getUpstream(String gene)
	{
		if (upMap.containsKey(gene)) return upMap.get(gene);
		else return Collections.emptySet();
	}

	public Set<String> getUpstream(String gene, int depth)
	{
		return getUpstream(Collections.singleton(gene), depth);
	}

	public Set<String> getUpstream(Set<String> genes, int depth)
	{
		return getStream(genes, true, depth);
	}

	public Set<String> getUpstream(Set<String> genes)
	{
		Set<String> result = new HashSet<>();
		for (String gene : genes)
		{
			result.addAll(getUpstream(gene));
		}
		return result;
	}

	public Set<String> getDownstream(String gene, int depth)
	{
		return getDownstream(Collections.singleton(gene), depth);
	}

	public Set<String> getDownstream(Set<String> genes, int depth)
	{
		return getStream(genes, false, depth);
	}

	public Set<String> getBothstream(String gene, int depth)
	{
		return getBothstream(Collections.singleton(gene), depth);
	}

	public Set<String> getBothstream(Set<String> genes, int depth)
	{
		Set<String> stream = getStream(genes, true, depth);
		stream.addAll(getStream(genes, false, depth));
		return stream;
	}


	private Set<String> getStream(Set<String> genes, boolean upstream, int depth)
	{
		if (depth < 1) return new HashSet<>();

		Set<String> newSet = new HashSet<String>(genes);
		Set<String> result = new HashSet<String>();

		for (int i = 0; i < depth; i++)
		{
			newSet = upstream ? getUpstream(newSet) : getDownstream(newSet);
			newSet.removeAll(result);
			result.addAll(newSet);
		}

		return result;
	}

	public Set<String> getDownstream(String gene)
	{
		if (dwMap.containsKey(gene)) return dwMap.get(gene);
		else return Collections.emptySet();
	}

	public Set<String> goBFS(Set<String> seed, Set<String> visited)
	{
		Set<String> set = goBFS(seed, visited, dwMap);
		set.addAll(goBFS(seed, visited, upMap));
		return set;
	}

	public Set<String> getNeighbors(String gene)
	{
		Set<String> n = new HashSet<>();
		if (upMap.get(gene) != null) n.addAll(upMap.get(gene));
		if (dwMap.get(gene) != null) n.addAll(dwMap.get(gene));
		return n;
	}

	public List<Set<String>> getNeighborsTiered(Set<String> genes, int depth, boolean upstream)
	{
		List<Set<String>> list = new ArrayList<>();

		for (int i = 1; i <= depth; i++)
		{
			Set<String> stream = getStream(genes, upstream, i);

			for (Set<String> set : list)
			{
				stream.removeAll(set);
			}

			list.add(stream);
		}
		return list;
	}

	public Set<String> getGenesWithCommonDownstream(String gene)
	{
		Set<String> up = getUpstream(gene);
		Set<String> dw = getDownstream(gene);
		Set<String> ot = getUpstream(dw);

		Set<String> result = new HashSet<>(up);
		result.addAll(dw);
		result.addAll(ot);
		return result;
	}

	public Set<String> getGenesWithCommonDownstream(Set<String> genes)
	{
		Set<String> up = getUpstream(genes);
		Set<String> dw = getDownstream(genes);
		Set<String> ot = getUpstream(dw);

		Set<String> result = new HashSet<>(up);
		result.addAll(dw);
		result.addAll(ot);
		return result;
	}

	public Set<String> getPathElements(String from, Set<String> to, int limit)
	{
		Set<String> result = new HashSet<>();
		getPathElements(from, to, limit, 0, result);
		return result;
	}

	private void getPathElements(String from, Set<String> to, int limit, int i, Set<String> result)
	{
		Set<String> set = Collections.singleton(from);
		Set<String> neigh = goBFS(set, set, true);
		for (String n : neigh)
		{
			if (to.contains(n)) result.add(n);
			else if (i < limit)
			{
				int prevSize = result.size();
				getPathElements(n, to, limit, i+1, result);
				if (result.size() > prevSize) result.add(n);
			}
		}
	}
	
	public List<CommPoint> getCommonDownstream(Set<String> seed, int limit)
	{
		Map<String, Set<String>> reachMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> breadthMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> visitedMap = new HashMap<String, Set<String>>();

		Set<CommPoint> points = new HashSet<>();
		
		for (String s : seed)
		{
			reachMap.put(s, new HashSet<>(Arrays.asList(s)));
			breadthMap.put(s, new HashSet<>(Arrays.asList(s)));
			visitedMap.put(s, new HashSet<>(Arrays.asList(s)));
		}

		for (int i = 1; i < limit; i++)
		{
			for (String s : seed)
			{
				Set<String> neigh = goBFS(breadthMap.get(s), visitedMap.get(s), true);
				for (String n : neigh)
				{
					if (!reachMap.containsKey(n))
						reachMap.put(n, new HashSet<>(Arrays.asList(s)));
					else reachMap.get(n).add(s);
				}
				breadthMap.put(s, neigh);
				visitedMap.get(s).addAll(neigh);
			}

			for (String r : reachMap.keySet())
			{
				if (reachMap.get(r).size() > 1)
				{
					CommPoint p = new CommPoint(r, reachMap.get(r), i);
					if (!containsBetter(points, p)) points.add(p);
				}
			}
		}

		List<CommPoint> list = new ArrayList<>(points);
		Collections.sort(list);
		return list;
	}
	
	private boolean containsBetter(Set<CommPoint> set, CommPoint p)
	{
		if (set.contains(p)) return true;
		for (CommPoint cp : set)
		{
			if (cp.dist < p.dist && cp.upstr.containsAll(p.upstr)) return true;
		}
		return false;
	}

	public DirectedGraph getInducedSubgraphWithoutDisconnectedNodes(Set<String> nodes)
	{
		DirectedGraph graph = new DirectedGraph("Subgraph of " + getName(), getEdgeType());

		for (String node : nodes)
		{
			for (String dwstr : getDownstream(node))
			{
				if (nodes.contains(dwstr))
				{
					graph.putRelation(node, dwstr, getMediators(node, dwstr));
				}
			}
		}
		return graph;
	}

	public DirectedGraph getNeighborhoodInTheInducedSubgraph(Set<String> nodes, Set<String> focus)
	{
		DirectedGraph graph = new DirectedGraph("Subgraph of " + getName(), getEdgeType());

		for (String node : nodes)
		{
			for (String dwstr : getDownstream(node))
			{
				if (nodes.contains(dwstr))
				{
					if (focus.contains(node) || focus.contains(dwstr))
					{
						graph.putRelation(node, dwstr, getMediators(node, dwstr));
					}
				}
			}
		}
		return graph;
	}

	public Set<String> getOneSideSymbols(boolean source)
	{
		Set<String> syms = new HashSet<>();
		syms.addAll(source ? dwMap.keySet() : upMap.keySet());
		return syms;
	}

	public Set<String> getSymbols()
	{
		Set<String> syms = new HashSet<>();

		syms.addAll(upMap.keySet());
		syms.addAll(dwMap.keySet());

		return syms;
	}

	public boolean hasNode(String name)
	{
		return upMap.containsKey(name) || dwMap.containsKey(name);
	}

	public DirectedGraph getReverseGraph()
	{
		DirectedGraph graph = new DirectedGraph("Reverse of " + getName(), getEdgeType());

		for (String source : getOneSideSymbols(true))
		{
			for (String target : getDownstream(source))
			{
				graph.putRelation(target, source, getMediators(source, target));
			}
		}

		return graph;
	}

	class CommPoint implements Comparable
	{
		String s;
		Set<String> upstr;
		int dist;

		CommPoint(String s, Set<String> upstr, int dist)
		{
			this.s = s;
			this.upstr = upstr;
			this.dist = dist;
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof CommPoint)
			{
				CommPoint p = (CommPoint) o;
				if (p.s.equals(s) && p.upstr.containsAll(upstr) && upstr.containsAll(p.upstr) && 
					p.dist == dist) return true;
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return s.hashCode();
		}

		@Override
		public int compareTo(Object o)
		{
			if (o instanceof CommPoint)
			{
				CommPoint p = (CommPoint) o;
				new Integer(p.upstr.size()).compareTo(upstr.size());
			}
			return 0;
		}
	}

	/**
	 * Gets the common downstream of length 1, but allows more length if path to downstream is also
	 * in the seed set.
	 */
	public Set<String> getLinkedCommonDownstream(Set<String> seed)
	{
		Map<String, Set<String>> map = new HashMap<>();

		for (String s : seed)
		{
			map.put(s, new HashSet<>(getDownstream(s)));
			map.get(s).add(s);
		}

		boolean loop = true;

		while(loop)
		{
			loop = false;

			for (String s1 : seed)
			{
				for (String s2 : seed)
				{
					if (s1.equals(s2)) continue;

					if (map.get(s2).contains(s1))
					{
						boolean changed = map.get(s2).addAll(map.get(s1));
						loop = changed || loop;
					}
				}
			}
		}

		HashSet[] sets = map.values().toArray(new HashSet[map.values().size()]);
		Set<String> result = new HashSet<String>(sets[0]);

		for (int i = 1; i < sets.length; i++)
		{
			result.retainAll(sets[i]);
		}

		return result;
	}

	public Map<Integer, Integer> getDegreeDistibution(boolean indegree)
	{
		Map<Integer, Integer> dist = new HashMap<>();
		collectDegrees(dist, indegree ? upMap : dwMap);
		return dist;
	}

	public Map<Integer, Integer> getDegreeDistibution()
	{
		Map<Integer, Integer> dist = new HashMap<>();
		collectDegrees(dist, upMap);
		collectDegrees(dist, dwMap);
		return dist;
	}

	public int getEdgeCount()
	{
		int edgeCnt = 0;
		for (Set<String> set : upMap.values()) edgeCnt += set.size();
		return edgeCnt;
	}

	public void printStats()
	{
		super.printStats();

		Set<String> syms = getSymbols();
		int edgeCnt = getEdgeCount();

		System.out.println(syms.size() + " genes (source: " + dwMap.keySet().size() + ", " +
			"target: " + upMap.keySet().size() + ") and " + edgeCnt + " edges");
		System.out.println("Avg in-degree: " + getAverageInDegree() + ", Avg out-degree: " + getAverageOutDegree());
	}

	public double getAverageInDegree()
	{
		return getAverageDegree(upMap);
	}

	public double getAverageOutDegree()
	{
		return getAverageDegree(dwMap);
	}

	public void merge(DirectedGraph graph)
	{
		merge(this.upMap, graph.upMap);
		merge(this.dwMap, graph.dwMap);

		super.merge(graph);
	}

	public void crop(Collection<String> symbols)
	{
		crop(upMap, symbols);
		crop(dwMap, symbols);
	}

	public void cropToNeighborhood(Collection<String> symbols)
	{
		cropToNeighborhood(upMap, symbols);
		cropToNeighborhood(dwMap, symbols);
	}

	protected Set<String> getRelationStrings()
	{
		Set<String> set = new HashSet<String>();

		for (String targ : upMap.keySet())
		{
			for (String sour : upMap.get(targ))
			{
				set.add(sour + " " + targ);
			}
		}
		return set;
	}

	public Set<String> toString(Set<String> from, Set<String> to)
	{
		Set<String> result = new HashSet<String>();
		for (String f : from)
		{
			for (String t : getDownstream(f))
			{
				if (to.contains(t)) result.add(f + "\t" + getEdgeType() + "\t" + t);
			}
		}
		return result;
	}

	public List<String> getEnrichedGenes(Set<String> query, Set<String> background, double fdrThr,
		NeighborType type, int distance, int minMember)
	{
		Map<String, Double>[] scores = getEnrichmentScores(query, background, type, distance, minMember);
		if (fdrThr < 0)
		{
			fdrThr = FDR.decideBestFDR_BH(scores[0], scores[1]);
			System.out.println("fdrThr = " + fdrThr);
		}
		return FDR.select(scores[0], scores[1], fdrThr);
	}

	public Map<String, Double>[] getEnrichmentScores(Set<String> query, Set<String> background, NeighborType type,
		int distance, int minMember)
	{
		DirectedGraph graph = this;

		if (background != null)
		{
			graph = (DirectedGraph) copy();
			graph.crop(background);
		}

		background = graph.getSymbols();
		int n = background.size();

		query = new HashSet<>(query);
		query.retainAll(background);

		int qSize = query.size();

		Map<String, Double> pvals = new HashMap<>();
		Map<String, Double> limit = new HashMap<>();

		for (String gene : background)
		{
			Set<String> neighbors = type == NeighborType.UPSTREAM ? graph.getUpstream(gene, distance) :
				type == NeighborType.DOWNSTREAM ? graph.getDownstream(gene, distance) :
					type == NeighborType.BOTHSTREAM ? graph.getBothstream(gene, distance) :
						graph.getNeighbors(gene, distance);

			if (neighbors.size() < minMember) continue;

			neighbors = new HashSet<>(neighbors);
			neighbors.add(gene);
			int nSize = neighbors.size();

			neighbors.retainAll(query);
			int o = neighbors.size();

			pvals.put(gene, FishersExactTest.calcEnrichmentPval(n, qSize, nSize, o));
			limit.put(gene, FishersExactTest.calcEnrichmentPval(n, qSize, nSize,
				Math.min(qSize, nSize)));
		}

		return new Map[]{pvals, limit};
	}

	public enum NeighborType
	{
		UPSTREAM,
		DOWNSTREAM,
		BOTHSTREAM,
		UNDIRECTED
	}

	public Map<String, Map<String, Integer>> getShortestDistances(int limit)
	{
		Map<String, Map<String, Integer>> dist = new HashMap<>();

		for (String source : getOneSideSymbols(true))
		{
			for (int i = 1; i <= limit; i++)
			{
				Set<String> dw = getDownstream(Collections.singleton(source), i);

				for (String target : dw)
				{
					if (!dist.containsKey(source)) dist.put(source, new HashMap<>());
					if (!dist.get(source).containsKey(target)) dist.get(source).put(target, i);
				}
			}
		}
		return dist;
	}

	/**
	 * Provides a new graph that uses the same nodes, but connections are randomized.
	 */
	public DirectedGraph getRandomizedCopy(Set<String> withGenes)
	{
		DirectedGraph g = new DirectedGraph(getName(), getEdgeType());

		int edgeCnt = getEdgeCount();

		List<String> genes = new ArrayList<>(withGenes != null ? withGenes : getSymbols());
		Random rand = new Random();

		for (int i = 0; i < edgeCnt; i++)
		{
			int ind1;
			int ind2;
			String source;
			String target;

			do
			{
				ind1 = rand.nextInt(genes.size());
				ind2 = rand.nextInt(genes.size());
				source = genes.get(ind1);
				target = genes.get(ind2);
			}
			while (ind1 == ind2 || g.hasRelation(source, target));

			g.putRelation(source, target);
		}
		return g;
	}

	public boolean hasRelation(String source, String target)
	{
		return dwMap.containsKey(source) && dwMap.get(source).contains(target);
	}

	public UndirectedGraphWithEdgeWeights getDownstreamSimilarityGraph()
	{
		UndirectedGraphWithEdgeWeights graph = new UndirectedGraphWithEdgeWeights();

		for (String s1 : getOneSideSymbols(true))
		{
			Set<String> d1 = getDownstream(s1);

			for (String s2 : getOneSideSymbols(true))
			{
				if (s1.compareTo(s2) < 0)
				{
					Set<String> d2 = getDownstream(s2);

					double weight = CollectionUtil.getJaccardSimilarity(d1, d2);

					if (weight > 0) graph.putRelation(s1, s2, weight);
				}
			}
		}

		return graph;
	}

	public static void main(String[] args) throws FileNotFoundException
	{
	}
}
