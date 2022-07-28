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
import java.util.stream.Collectors;

/**
 * This is a simple graph, built using external maps. All nodes are identified with a unique String.
 * Relations can be either directed or undirected, and can be mixed.
 *
 * @author Ozgun Babur
 */
public abstract class Graph implements Serializable
{
	private String edgeType;
	private String name;

	protected Map<String, Map<String, Set<String>>> mediators;

	protected boolean allowSelfEdges = false;

	public Graph()
	{
		this (null, null);
	}

	public Graph(String name, String edgeType)
	{
		this.name = name;
		this.edgeType = edgeType;
		mediators = new HashMap<>();
	}

	public boolean load(String filename, Set<String> types)
	{
		try
		{
			return load(new FileInputStream(filename), types);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean load(InputStream is, Set<String> types)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");

				if (token.length < 3) continue;

				if (types == null || types.contains(token[1]))
				{
					if (token.length > 3)
					{
						putRelation(token[0], token[2], token[3]);
					}
					else
					{
						putRelation(token[0], token[2]);
					}
				}
			}

			reader.close();
		}
		catch (IOException e) { e.printStackTrace(); return false; } return true;
	}

	public void write(String filename) { try
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
		write(writer);
		writer.close();
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public void write(String filename, Set<QueryGraphObject> subset) { try
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
		write(writer, subset);
		writer.close();
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public void write(Writer writer, Set<QueryGraphObject> subset)
	{
		subset.stream().filter(o -> o instanceof QueryEdge).map(o -> (QueryEdge) o).filter(e -> e.type.equals(edgeType))
			.forEach(e ->
		{
			FileUtil.write(e.source.id + "\t" + e.type + "\t" + e.target.id, writer);

			if (mediators.containsKey(e.source.id) && mediators.get(e.source.id).containsKey(e.target.id))
			{
				FileUtil.write("\t" + convertMediatorsToString(mediators.get(e.source.id).get(e.target.id)), writer);
			}

			FileUtil.write("\n", writer);
		});
	}

	public abstract void write(Writer writer);

	public String getMediatorsInString(String source, String target)
	{
		return convertMediatorsToString(getMediators(source, target));
	}

	public Set<String> getMediators(String source, String target)
	{
		Set<String> set = new HashSet<>();
		if (mediators.containsKey(source) && mediators.get(source).containsKey(target))
		{
			set.addAll(mediators.get(source).get(target));
		}
		return set;
	}

	protected String convertMediatorsToString(Set<String> set)
	{
		StringBuilder sb = new StringBuilder();
		for (String s : set)
		{
			sb.append(s).append(" ");
		}

		return sb.toString().trim();
	}

	public void clear()
	{
		this.mediators.clear();
	}

	public void putRelation(String source, String target, Set<String> mediators)
	{
		putRelation(source, target);

		addMediators(source, target, mediators);
		if (isUndirected()) addMediators(target, source, mediators);
	}

	public void putRelation(String source, String target, String mediatorsStr)
	{
		Set<String> meds = new HashSet<>(Arrays.asList(mediatorsStr.split(" |;")));
		putRelation(source, target, meds);
	}

	public void addMediators(String source, String target, Collection<String> meds)
	{
		if (meds != null && !meds.isEmpty())
		{
			if (!mediators.containsKey(source))
			{
				mediators.put(source, new HashMap<>());
			}

			if (!mediators.get(source).containsKey(target))
			{
				mediators.get(source).put(target, new HashSet<>());
			}

			mediators.get(source).get(target).addAll(meds);
		}
	}

	public void removeMediators(String source, String target)
	{
		if (mediators.containsKey(source))
		{
			mediators.get(source).remove(target);

			if (mediators.get(source).isEmpty())
			{
				mediators.remove(source);
			}
		}
	}

	public abstract void putRelation(String source, String target);

	public abstract void removeRelation(String source, String target);

	public abstract boolean isDirected();

	public boolean isUndirected()
	{
		return !isDirected();
	}

	public String getEdgeType()
	{
		return edgeType;
	}

	public void setEdgeType(String edgeType)
	{
		this.edgeType = edgeType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public abstract Set<String> goBFS(Set<String> seed, Set<String> visited);

	protected Set<String> goBFS(String seed, Map<String, Set<String>> map)
	{
		return goBFS(Collections.singleton(seed), null, map);
	}

	protected Set<String> goBFS(Set<String> seed, Set<String> visited, Map<String, Set<String>> map)
	{
		Set<String> neigh = new HashSet<>();
		for (String s : seed)
		{
			if (map.containsKey(s))
			{
				for (String n : map.get(s))
				{
					if (visited == null || !visited.contains(n))
					{
						neigh.add(n);
					}
				}
			}
		}
		return neigh;
	}

	protected Set<String> getNeighbors(String gene, int depth)
	{
		return getNeighbors(Collections.singleton(gene), depth);
	}

	protected Set<String> getNeighbors(Set<String> genes, int depth)
	{
		if (depth < 1) return new HashSet<>();

		Set<String> newSet = new HashSet<>(genes);
		Set<String> result = new HashSet<>();

		for (int i = 0; i < depth; i++)
		{
			newSet = getNeighbors(newSet);
			newSet.removeAll(result);
			result.addAll(newSet);
		}

		return result;
	}

	public abstract Set<String> getNeighbors(String gene);

	public Set<String> getNeighbors(Set<String> genes)
	{
		Set<String> n = new HashSet<>();
		for (String gene : genes)
		{
			n.addAll(getNeighbors(gene));
		}
		return n;
	}

	public Set<String> getConnectedComponent(String node)
	{
		Set<String> comp = new HashSet<>();
		Set<String> newGenes = getNeighbors(node);
		do
		{
			Set<String> n2 = new HashSet<>();
			for (String gene : newGenes)
			{
				n2.addAll(getNeighbors(gene));
			}
			comp.addAll(newGenes);
			n2.removeAll(comp);
			newGenes = n2;
		}
		while (!newGenes.isEmpty());
		return comp;
	}

	public int getDegree(String gene)
	{
		return getNeighbors(gene).size();
	}

	public abstract Set<String> getSymbols();

	public abstract Map<Integer, Integer> getDegreeDistibution();

	protected void collectDegrees(Map<Integer, Integer> dist, Map<String, Set<String>> map)
	{
		for (Set<String> set : map.values())
		{
			int degree = set.size();

			if (!dist.containsKey(degree)) dist.put(degree, 1);
			else dist.put(degree, dist.get(degree) + 1);
		}
	}

	public abstract int getEdgeCount();

	public void printStats()
	{
		System.out.println(name + " [" + edgeType + "]");
	}

	public void merge(Graph graph)
	{
		for (String gene : graph.mediators.keySet())
		{
			if (!mediators.containsKey(gene))
				mediators.put(gene, new HashMap<>());

			merge(mediators.get(gene), new HashMap<>(graph.mediators.get(gene)));
		}
	}

	protected void merge(Map<String, Set<String>> m1, Map<String, Set<String>> m2)
	{
		for (String s : m2.keySet())
		{
			if (!m1.containsKey(s)) m1.put(s, new HashSet<>());
			m1.get(s).addAll(m2.get(s));
		}
	}

	public Graph copy()
	{
		try
		{
			Graph copyG = getClass().newInstance();
			copyG.setName(name);
			copyG.setEdgeType(edgeType);
			copyG.merge(this);
			return copyG;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	protected double getAverageDegree(Map<String, Set<String>> map)
	{
		Set<String> nodes = getSymbols();
		List<Double> degrees = new ArrayList<>();
		for (String node : nodes)
		{
			if (map.containsKey(node)) degrees.add((double) map.get(node).size());
			degrees.add(0D);
		}
		return Summary.mean(degrees.toArray(new Double[0]));
	}

	/**
	 * Keeps the edges between the given symbols.
	 * @param symbols desired nodes that will remain in the graph
	 */
	public abstract void crop(Collection<String> symbols);

	/**
	 * Keeps the edges that at least one end is among the given genes.
	 * @param symbols seed
	 */
	public abstract void cropToNeighborhood(Collection<String> symbols);

	protected void crop(Map<String, Set<String>> map, Collection<String> symbols)
	{
		Set<String> remKeys = new HashSet<>();
		for (String s : map.keySet())
		{
			if (!symbols.contains(s)) remKeys.add(s);
			else
			{
				map.get(s).retainAll(symbols);

				if (map.get(s).isEmpty()) remKeys.add(s);
			}
		}
		for (String key : remKeys)
		{
			map.remove(key);
		}
	}

	protected void cropToNeighborhood(Map<String, Set<String>> map, Collection<String> symbols)
	{
		Set<String> remKeys = new HashSet<>();
		for (String s : map.keySet())
		{
			if (!symbols.contains(s))
			{
				map.get(s).retainAll(symbols);

				if (map.get(s).isEmpty()) remKeys.add(s);
			}
		}
		for (String key : remKeys)
		{
			map.remove(key);
		}
	}

	public void printVennIntersections(Graph... gArray)
	{
		List<Graph> graphs = new ArrayList<>(gArray.length + 1);
		graphs.add(this);
		Collections.addAll(graphs, gArray);

		List<Set<String>> relList = new ArrayList<>();
		for (Graph graph : graphs)
		{
			relList.add(graph.getRelationStrings());
		}

		CollectionUtil.printNameMapping(graphs.stream().map(Graph::getName).collect(Collectors.toList()).toArray(new String[0]));
		CollectionUtil.printVennCounts(relList.toArray(new Collection[relList.size()]));
	}

	protected abstract Set<String> getRelationStrings();

	public List<String> getEnrichedGenes(Set<String> query, Set<String> background, double fdrThr, int distance,
		int minMember)
	{
		Map<String, Double>[] scores = getEnrichmentScores(query, background, distance, minMember);
		if (fdrThr < 0)
		{
			fdrThr = FDR.decideBestFDR_BH(scores[0], scores[1]);
			System.out.println("fdrThr = " + fdrThr);
		}
		return FDR.select(scores[0], scores[1], fdrThr);
	}

	public Map<String, Double>[] getEnrichmentScores(Set<String> query, Set<String> background, int distance,
		int minMember)
	{
		Graph graph = this;

		if (background != null)
		{
			graph = copy();
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
			Set<String> neighbors = graph.getNeighbors(gene, distance);

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

	public void printDegreeDistribution(int bins)
	{
		List<Integer> list = new ArrayList<Integer>();
		for (String node : getSymbols())
		{
			Set<String> neighbors = getNeighbors(node);
			list.add(neighbors.size());
		}

		int max = CollectionUtil.maxIntInList(list);

		Histogram h = new Histogram(max / (double) bins);
		h.setBorderAtZero(true);
		for (Integer v : list)
		{
			h.count(v);
		}
		h.print();
	}

	public Graph cropToDegree(int minDegree)
	{
		Set<String> keep = new HashSet<String>();
		for (String node : getSymbols())
		{
			Set<String> neighbors = getNeighbors(node);
			if (neighbors.size() >= minDegree) keep.add(node);
		}
		Graph g = copy();
		g.crop(keep);
		return g;
	}

	public Map<String, Map<String, Integer>> getShortestDistances(int limit)
	{
		Map<String, Map<String, Integer>> dist = new HashMap<>();

		for (String source : getSymbols())
		{
			for (int i = 1; i <= limit; i++)
			{
				Set<String> dw = getNeighbors(Collections.singleton(source), i);

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
	public abstract Graph getRandomizedCopy(Set<String> withGenes);

	public static void main(String[] args) throws FileNotFoundException
	{
		String type = "downregulates-expression";
		DirectedGraph dg = new DirectedGraph(type, type);
		dg.load("/home/ozgunbabur/Data/causal-priors.txt", Collections.singleton(type));
		dg.printStats();
	}
}
