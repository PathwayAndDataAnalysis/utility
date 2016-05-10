package org.panda.utility.graph;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class PhosphoGraph extends Graph
{
	protected Map<String, Map<String, Set<String>>> sites;

	public PhosphoGraph(String name, String edgeType)
	{
		super(name, edgeType);
		this.sites = new HashMap<>();
	}

	public void putRelation(String source, String target, String mediatorsStr, boolean directed, String siteString)
	{
		super.putRelation(source, target, mediatorsStr, directed);

		if (!sites.containsKey(source))
			sites.put(source, new HashMap<>());
		if (!sites.get(source).containsKey(target))
			sites.get(source).put(target, new HashSet<>());

		sites.get(source).get(target).addAll(Arrays.asList(siteString.split(";")));
	}

	public Set<String> getSites(String source, String target)
	{
		if (sites.containsKey(source) && sites.get(source).containsKey(target))
		{
			return sites.get(source).get(target);
		}
		return null;
	}
}
