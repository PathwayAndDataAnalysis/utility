package org.panda.utility.graph;

import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;
import org.panda.utility.graph.query.QueryEdge;
import org.panda.utility.graph.query.QueryGraphObject;
import org.panda.utility.statistics.FDR;
import org.panda.utility.statistics.FishersExactTest;
import org.panda.utility.statistics.Histogram;

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
public class UndirectedGraph extends Graph
{
	protected Map<String, Set<String>> ppMap;

	public UndirectedGraph()
	{
		ppMap = new HashMap<>();
	}

	public UndirectedGraph(String name, String edgeType)
	{
		super(name, edgeType);
		ppMap = new HashMap<>();
	}

	public void write(Writer writer)
	{
		try
		{
			for (String g1 : ppMap.keySet())
			{
				for (String g2 : ppMap.get(g1))
				{
					if (g1.compareTo(g2) <= 0)
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
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void clear()
	{
		super.clear();
		ppMap.clear();
	}

	public void putRelation(String source, String target)
	{
		if (!allowSelfEdges && source.equals(target)) return;

		if (!ppMap.containsKey(source)) ppMap.put(source, new HashSet<>());
		if (!ppMap.containsKey(target)) ppMap.put(target, new HashSet<>());
		ppMap.get(source).add(target);
		ppMap.get(target).add(source);
	}

	public void removeRelation(String source, String target)
	{
		if (ppMap.containsKey(source))
		{
			ppMap.get(source).remove(target);

			if (ppMap.get(source).isEmpty())
			{
				ppMap.remove(source);
			}
		}
		if (ppMap.containsKey(target))
		{
			ppMap.get(target).remove(source);

			if (ppMap.get(target).isEmpty())
			{
				ppMap.remove(target);
			}
		}
		removeMediators(source, target);
		removeMediators(target, source);
	}

	public boolean isDirected()
	{
		return false;
	}

	public Set<String> goBFS(Set<String> seed, Set<String> visited)
	{
		return goBFS(seed, visited, ppMap);
	}

	public Set<String> getNeighbors(String gene)
	{
		Set<String> n = new HashSet<>();
		if (ppMap.get(gene) != null) n.addAll(ppMap.get(gene));
		return n;
	}
	
	public List<Set<String>> getNeighborsTiered(Set<String> genes, int depth)
	{
		List<Set<String>> list = new ArrayList<>();

		for (int i = 1; i <= depth; i++)
		{
			Set<String> stream = getNeighbors(genes, i);

			for (Set<String> set : list)
			{
				stream.removeAll(set);
			}

			list.add(stream);
		}
		return list;
	}

	public Set<String> getSymbols()
	{
		return new HashSet<>(ppMap.keySet());
	}

	public Map<Integer, Integer> getDegreeDistibution()
	{
		Map<Integer, Integer> dist = new HashMap<>();
		collectDegrees(dist, ppMap);
		return dist;
	}

	public int getEdgeCount()
	{
		int edgeCnt = 0;

		for (Set<String> set : ppMap.values())
		{
			edgeCnt += set.size();
		}

		return edgeCnt / 2;
	}

	public void printStats()
	{
		super.printStats();

		Set<String> syms = getSymbols();
		int edgeCnt = getEdgeCount();

		System.out.println(syms.size() + " genes and " + edgeCnt + " edges");
		System.out.println("Avg degree: " + getAverageDegree());
	}

	public double getAverageDegree()
	{
		return getAverageDegree(ppMap);
	}

	public void merge(UndirectedGraph graph)
	{
		merge(this.ppMap, graph.ppMap);

		for (String gene : graph.mediators.keySet())
		{
			if (!mediators.containsKey(gene))
				mediators.put(gene, new HashMap<>());

			merge(mediators.get(gene), graph.mediators.get(gene));
		}
	}

	public DirectedGraph getDirectedCopy(String newRelationType)
	{
		DirectedGraph graph = new DirectedGraph(getName(), newRelationType);

		for (String g1 : getSymbols())
		{
			for (String g2 : ppMap.get(g1))
			{
				graph.putRelation(g1, g2, getMediatorsInString(g1, g2));
			}
		}

		return graph;
	}

	public void crop(Collection<String> symbols)
	{
		crop(ppMap, symbols);
	}

	public void cropToNeighborhood(Collection<String> symbols)
	{
		cropToNeighborhood(ppMap, symbols);
	}

	protected Set<String> getRelationStrings()
	{
		Set<String> set = new HashSet<String>();

		for (String g1 : ppMap.keySet())
		{
			for (String g2 : ppMap.get(g1))
			{
				if (g1.compareTo(g2) < 0 ) set.add(g1 + " " + g2);
			}
		}
		return set;
	}

	/**
	 * Provides a new graph that uses the same nodes, but connections are randomized.
	 */
	public UndirectedGraph getRandomizedCopy(Set<String> withGenes)
	{
		UndirectedGraph g = new UndirectedGraph(getName(), getEdgeType());

		int edgeCnt = getEdgeCount();

		List<String> genes = new ArrayList<>(withGenes != null ? withGenes : getSymbols());
		Random rand = new Random();

		for (int i = 0; i < edgeCnt; i++)
		{
			int ind1;
			int ind2;
			String g1;
			String g2;

			do
			{
				ind1 = rand.nextInt(genes.size());
				ind2 = rand.nextInt(genes.size());
				g1 = genes.get(ind1);
				g2 = genes.get(ind2);
			}
			while (ind1 == ind2 || (g.ppMap.containsKey(g1) && g.ppMap.get(g1).contains(g2)));

			g.putRelation(g1, g2);
		}
		return g;
	}

	public static void main(String[] args) throws FileNotFoundException
	{
	}
}
