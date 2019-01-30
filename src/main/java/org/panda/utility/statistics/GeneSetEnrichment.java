package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;

public class GeneSetEnrichment
{
	/**
	 * Returns Fisher's exact test p-values for enrichment of each gene set. Does not correct for multiple hypothesis
	 * testing.
	 *
	 * @param background pass null if the background is just all possible genes in the given gene sets
	 */
	public static Map<String, Double> calculateEnrichment(Collection<String> queryGenes, Collection<String> background,
		int minNumOfMembersInAGroup, int maxNumOfMembersInAGroup, Map<String, Set<String>> geneSets)
	{
		Set<String> allGenes = geneSets.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
		if (background != null) allGenes.retainAll(background);

		int n = allGenes.size();

		Set<String> genes = new HashSet<>(queryGenes);
		genes.retainAll(allGenes);

		Map<String, Double> pvals = new HashMap<>();

		geneSets.forEach((go, set) ->
		{
			set = new HashSet<>(set);
			set.retainAll(allGenes);
			if (set.size() <= maxNumOfMembersInAGroup && set.size() >= minNumOfMembersInAGroup)
			{
				int o = CollectionUtil.countOverlap(genes, set);
				double p = FishersExactTest.calcEnrichmentPval(n, set.size(), genes.size(), o);
				pvals.put(go, p);
			}
		});

		return pvals;
	}
}
