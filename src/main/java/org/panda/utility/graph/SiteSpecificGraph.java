package org.panda.utility.graph;

import org.panda.utility.CollectionUtil;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extension of Graph where the affected phospho sites of relations are also kept.
 *
 * @author Ozgun Babur
 */
public class SiteSpecificGraph extends DirectedGraph
{
	protected Map<String, Map<String, Set<String>>> sites;

	public SiteSpecificGraph(String name, String edgeType)
	{
		super(name, edgeType);
		this.sites = new HashMap<>();
	}

	public void putRelation(String source, String target, Set<String> mediators, String siteString)
	{
		super.putRelation(source, target, mediators);

		if (siteString != null && !siteString.isEmpty())
		{
			if (!sites.containsKey(source))
				sites.put(source, new HashMap<>());
			if (!sites.get(source).containsKey(target))
				sites.get(source).put(target, new HashSet<>());

			try
			{
				sites.get(source).get(target).addAll(Arrays.asList(siteString.split(";")));
			}catch(Exception e){
				System.out.println();
			}
		}
	}

	public void putRelation(String source, String target, String mediatorsStr, String siteString)
	{
		Set<String> meds = new HashSet<>(Arrays.asList(mediatorsStr.split(" |;")));
		this.putRelation(source, target, meds, siteString);
	}

	public Set<String> getSites(String source, String target)
	{
		if (hasSites(source, target))
		{
			return sites.get(source).get(target);
		}
		return Collections.emptySet();
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

	public void removeSite(String source, String target, String site)
	{
		if (!hasRelation(source, target))
		{
			throw new RuntimeException("Cannot remove site because the edge does not exist.");
		}

		if (sites.containsKey(source) && sites.get(source).containsKey(target))
		{
			sites.get(source).get(target).remove(site);
		}
	}

	public Set<String> getUpstream(String target, Set<String> sites)
	{
		Set<String> upstream = getUpstream(target, 1);
		return upstream.stream().filter(source -> !CollectionUtil.intersectionEmpty(getSites(source, target), sites)).collect(Collectors.toSet());
	}

	@Override
	public void merge(DirectedGraph graph)
	{
		super.merge(graph);

		if (graph instanceof SiteSpecificGraph)
		{
			SiteSpecificGraph pGraph = (SiteSpecificGraph) graph;

			for (String gene : pGraph.sites.keySet())
			{
				if (!sites.containsKey(gene))
					sites.put(gene, new HashMap<>());

				merge(sites.get(gene), pGraph.sites.get(gene));
			}
		}
	}

	@Override
	public void write(Writer writer)
	{
		try
		{
			for (String source : dwMap.keySet())
			{
				for (String target : dwMap.get(source))
				{
					writer.write(source + "\t" + getEdgeType() + "\t" + target + "\t");

					if (mediators.containsKey(source) && mediators.get(source).containsKey(target))
					{
						writer.write(convertMediatorsToString(mediators.get(source).get(target)));
					}

					writer.write("\t");

					Set<String> sites = getSites(source, target);
					if (sites != null)
					{
						writer.write(CollectionUtil.merge(sites, ";"));
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

	@Override
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
					if (token.length > 4)
					{
						putRelation(token[0], token[2], token[3], token[4]);
					}
					else if (token.length > 3)
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
}
