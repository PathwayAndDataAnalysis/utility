package org.panda.utility.graph;

import java.util.*;

/**
 * Extension of Graph where the affected phospho sites of relations are also kept.
 *
 * @author Ozgun Babur
 */
public class PhosphoGraph extends DirectedGraph
{
	protected Map<String, Map<String, Set<String>>> sites;

	public PhosphoGraph(String name, String edgeType)
	{
		super(name, edgeType);
		this.sites = new HashMap<>();
	}

	public void putRelation(String source, String target, String mediatorsStr, String siteString)
	{
		super.putRelation(source, target, mediatorsStr);

		if (!sites.containsKey(source))
			sites.put(source, new HashMap<>());
		if (!sites.get(source).containsKey(target))
			sites.get(source).put(target, new HashSet<>());

		sites.get(source).get(target).addAll(Arrays.asList(siteString.split(";")));
	}

	public Set<String> getSites(String source, String target)
	{
		if (hasSites(source, target))
		{
			return sites.get(source).get(target);
		}
		return null;
	}

	public boolean hasSites(String source, String target)
	{
		return sites.containsKey(source) && sites.get(source).containsKey(target);
	}

	public void addSite(String source, String target, String site)
	{
		if (!hasRelation(source, target))
		{
			throw new RuntimeException("Cannot add site because the edge does not exist.");
		}

		if (!sites.containsKey(source))
			sites.put(source, new HashMap<>());
		if (!sites.get(source).containsKey(target))
			sites.get(source).put(target, new HashSet<>());

		sites.get(source).get(target).add(site);
	}

	@Override
	public void merge(Graph graph)
	{
		super.merge(graph);

		if (graph instanceof PhosphoGraph)
		{
			PhosphoGraph pGraph = (PhosphoGraph) graph;

			for (String gene : pGraph.sites.keySet())
			{
				if (!sites.containsKey(gene))
					sites.put(gene, new HashMap<>());

				merge(sites.get(gene), pGraph.sites.get(gene));
			}
		}
	}
}
