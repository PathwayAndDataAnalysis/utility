package org.panda.utility.graph;

import org.panda.utility.graph.query.QueryGraphObject;

import java.io.*;
import java.util.*;

/**
 * This is a set of graphs.
 *
 * @author Ozgun Babur
 */
public class GraphList extends Graph
{
	protected List<Graph> graphs;

	protected Map<String, Graph> type2graph;

	public GraphList(String name)
	{
		super(name, null);
		graphs = new ArrayList<>();
		type2graph = new HashMap<>();
		super.mediators = null;
	}

	public void addGraph(Graph graph)
	{
		this.graphs.add(graph);
		type2graph.put(graph.getEdgeType(), graph);
	}

	public List<Graph> getGraphs()
	{
		return graphs;
	}

	public Graph getGraph(String edgeType)
	{
		return type2graph.get(edgeType);
	}

	public void write(Writer writer)
	{
		for (Graph graph : graphs)
		{
			graph.write(writer);
		}
	}

	@Override
	public void putRelation(String source, String target)
	{
		throw new RuntimeException("Cannot put a relation to a graph list.");
	}

	public boolean isDirected()
	{
		for (Graph graph : graphs)
		{
			if (graph.isDirected()) return true;
		}
		return false;
	}

	public boolean isUndirected()
	{
		for (Graph graph : graphs)
		{
			if (graph.isUndirected()) return true;
		}
		return false;
	}

	public Set<String> goBFS(String seed, boolean downstream)
	{
		Set<String> result = new HashSet<>();
		for (Graph graph : graphs)
		{
			result.addAll(((DirectedGraph) graph).goBFS(seed, downstream));
		}
		return result;
	}

	public Set<String> goBFS(Set<String> seed, Set<String> visited, boolean downstream)
	{
		Set<String> result = new HashSet<>();
		for (Graph graph : graphs)
		{
			result.addAll(((DirectedGraph) graph).goBFS(seed, visited, downstream));
		}
		return result;
	}

	public Set<String> goBFS(Set<String> seed, Set<String> visited)
	{
		Set<String> result = new HashSet<>();
		for (Graph graph : graphs)
		{
			result.addAll(graph.goBFS(seed, visited));
		}
		return result;
	}

	public Set<String> getUpstream(String gene)
	{
		Set<String> result = new HashSet<>();
		for (Graph graph : graphs)
		{
			if (graph instanceof DirectedGraph)
			{
				result.addAll(((DirectedGraph) graph).getUpstream(gene));
			}
		}
		return result;
	}

	public Set<String> getDownstream(String gene)
	{
		Set<String> result = new HashSet<>();
		for (Graph graph : graphs)
		{
			if (graph instanceof DirectedGraph)
			{
				result.addAll(((DirectedGraph) graph).getDownstream(gene));
			}
		}
		return result;
	}

	public Set<String> getNeighbors(String gene)
	{
		Set<String> result = new HashSet<>();
		for (Graph graph : graphs)
		{
			result.addAll(graph.getNeighbors(gene));
		}
		return result;
	}

	public Set<String> getSymbols()
	{
		Set<String> syms = new HashSet<>();

		for (Graph graph : graphs)
		{
			syms.addAll(graph.getSymbols());
		}
		return syms;
	}

	@Override
	public Map<Integer, Integer> getDegreeDistibution()
	{
		throw new UnsupportedOperationException("Dgree distribution is not supported for graph lists");
	}

	@Override
	public int getEdgeCount()
	{
		int cnt = 0;
		for (Graph graph : graphs)
		{
			cnt += graph.getEdgeCount();
		}
		return cnt;
	}

	public void printStats()
	{
		System.out.println("Graph list: " + getName());
		System.out.println("--------");
		for (Graph graph : graphs)
		{
			graph.printStats();
			System.out.println();
		}
		System.out.println("--------");
	}

	public Graph copy()
	{
		GraphList copy = new GraphList(getName());
		for (Graph graph : graphs)
		{
			copy.addGraph(graph.copy());
		}
		return copy;
	}

	public Set<String> getOneSideSymbols(boolean source)
	{
		Set<String> syms = new HashSet<>();
		for (Graph graph : graphs)
		{
			syms.addAll(((DirectedGraph) graph).getOneSideSymbols(source));
		}
		return syms;
	}


	public void crop(Collection<String> symbols)
	{
		for (Graph graph : graphs)
		{
			graph.crop(symbols);
		}
	}

	public void cropToNeighborhood(Collection<String> symbols)
	{
		for (Graph graph : graphs)
		{
			graph.cropToNeighborhood(symbols);
		}
	}

	protected Set<String> getRelationStrings()
	{
		Set<String> set = new HashSet<String>();

		for (Graph graph : graphs)
		{
			set.addAll(graph.getRelationStrings());
		}
		return set;
	}

	@Override
	public Graph getRandomizedCopy(Set<String> withGenes)
	{
		throw new UnsupportedOperationException("Cannot generate randomized copy of a graph list.");
	}

	public Set<String> toString(Set<String> from, Set<String> to)
	{
		Set<String> result = new HashSet<String>();
		for (Graph graph : graphs)
		{
			result.addAll(((DirectedGraph) graph).toString(from, to));
		}
		return result;
	}

	@Override
	public void write(Writer writer, Set<QueryGraphObject> subset)
	{
		for (Graph graph : graphs)
		{
			graph.write(writer, subset);
		}
	}

	@Override
	public void removeRelation(String source, String target)
	{
		for (Graph graph : graphs)
		{
			graph.removeRelation(source, target);
		}
	}

	public String getMediatorsInString(String seed, Set<String> neighbors)
	{
		Set<String> meds = new HashSet<>();

		for (Graph graph : graphs)
		{
			for (String neighbor : neighbors)
			{
				meds.addAll(graph.getMediators(seed, neighbor));
				meds.addAll(graph.getMediators(neighbor, seed));
			}
		}

		return convertMediatorsToString(meds);
	}

	public static void main(String[] args)
	{

	}
}
