package org.panda.utility;

import org.panda.utility.statistics.FDR;
import org.panda.utility.statistics.FishersExactTest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EnrichmentAnalyzer
{
	public static void run(Map<String, Set<String>> geneSets, Set<String> selected, Set<String> background,
						   int minimumSetSize, double pvalThr, String outFile) throws IOException
	{
		// Crop gene sets to the given background
		geneSets = cropGeneSets(geneSets, background, minimumSetSize);

		// Get representative names map of the duplicated gene sets
		Map<String, Set<String>> repMap = getRepresentativesMap(geneSets);

		// Remove the duplicate gene sets
		Set<String> surplus = geneSets.keySet().stream().filter(name -> !repMap.containsKey(name)).collect(Collectors.toSet());
		surplus.forEach(geneSets::remove);

		Map<String, Double>[] pvalArray = calcPVals(geneSets, selected);
		Map<String, Double> pvals = pvalArray[0];
		Map<String, Double> limits = pvalArray[1];

		writeEnrichmentResults(geneSets, repMap, selected, pvals, limits, pvalThr, outFile);
	}

	private static Map<String, Double>[] calcPVals(Map<String, Set<String>> geneSets, Set<String> selected)
	{
		// Update the background to the gene set coverage
		Set<String> background = getNewBackground(geneSets);

		int bgCnt = background.size();
		int selectedCnt = selected.size();

		Map<String, Double> mapP = new HashMap<>();
		Map<String, Double> mapL = new HashMap<>();

		for (String name : geneSets.keySet())
		{
			Set<String> set = geneSets.get(name);

			int featuredCnt = set.size();
			int overlap = CollectionUtil.countOverlap(set, selected);

			double pval = FishersExactTest.calcEnrichmentPval(bgCnt, featuredCnt, selectedCnt, overlap);

			int maxPossibleHit = Math.min(featuredCnt, selectedCnt);

			double limit = FishersExactTest.calcEnrichmentPval(bgCnt, featuredCnt, selectedCnt, maxPossibleHit);

			mapP.put(name, pval);
			mapL.put(name, limit);
		}

		return new Map[]{mapP, mapL};
	}

	private static Map<String, Set<String>> getRepresentativesMap(Map<String, Set<String>> geneSets)
	{
		Map<String, Set<String>> repMap = new HashMap<>();

		Map<Set<String>, String> reverseMap = new HashMap<>();

		for (String name : geneSets.keySet())
		{
			Set<String> set = geneSets.get(name);
			if (!reverseMap.containsKey(set))
			{
				reverseMap.put(set, name);
				repMap.put(name, new HashSet<>());
				repMap.get(name).add(name);
			}
			else
			{
				String rep = reverseMap.get(set);
				repMap.get(rep).add(name);
			}
		}

		return repMap;
	}

	private static Map<String, Set<String>> cropGeneSets(Map<String, Set<String>> geneSets, Set<String> background, int minMemCnt)
	{
		Map<String, Set<String>> newGS = new HashMap<>();

		geneSets.forEach((name, members) ->
		{
			Set<String> mems = new HashSet<>(members);
			mems.retainAll(background);
			if (mems.size() >= minMemCnt)
			{
				newGS.put(name, mems);
			}
		});

		return newGS;
	}

	private static Set<String> getNewBackground(Map<String, Set<String>> geneSets)
	{
		return geneSets.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	}

	public static void writeEnrichmentResults(Map<String, Set<String>> geneSets, Map<String, Set<String>> representatives,
									   Set<String> selected, Map<String, Double> pvals, Map<String, Double> limits,
									   double pvalThr, String filename)
		throws IOException
	{
		Map<String, Double> qvals = FDR.getQVals(pvals, limits);

		List<String> allNames = new ArrayList<>(qvals.keySet());
		allNames.sort(Comparator.comparing(o -> pvals.get(o)));

		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

		writer.write("# Gene set name(s): Names of the gene set.\n");
		writer.write("# P-value: Enrichment p-value of the gene set calculated by Fisher's exact test.\n");
		writer.write("# Q-value: Estimated FDR (false discovery rate) if this p-value is used as cutoff threshold.\n");
		writer.write("# Hit size: Number of query genes that overlaps with this gene set.\n");
		writer.write("# Effective gene set size: Intersection of genes in this gene set and the given background.\n");
		writer.write("# Molecules contributed to enrichment: Names of the selected genes that overlaps with this gene set.\n");
		writer.write("Gene set name\tP-value\tQ-value\tHit size\tEffective gene set size\tMolecules contributed to enrichment");
		for (String name : allNames)
		{
			if (pvals.get(name) > pvalThr) break;

			Set<String> set = geneSets.get(name);
			Set<String> hitGenes = CollectionUtil.getIntersection(set, selected);

			String names = representatives.get(name).toString();

			FileUtil.lnwrite(names + "\t" + pvals.get(name) + "\t" + qvals.get(name) +
				"\t" + hitGenes.size() + "\t" + set.size() + "\t" + CollectionUtil.merge(hitGenes, " "), writer);
		}
		writer.close();
	}
}
