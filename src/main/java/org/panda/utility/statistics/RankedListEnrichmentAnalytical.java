package org.panda.utility.statistics;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.panda.utility.CollectionUtil;
import org.panda.utility.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedListEnrichmentAnalytical
{
	public static final double REPORT_RATIO = 0.25;

	public static <T> void reportEnrichment(List<T> rankedList, Map<String, Set<T>> featuredSets, String outFile) throws IOException
	{
		if (new HashSet<>(rankedList).size() != rankedList.size()) throw new IllegalArgumentException("rankedList has repeating elements!");

		System.out.println("before clip featuredSets = " + featuredSets.size());
		featuredSets = clipSetsToList(featuredSets, rankedList);
		System.out.println(" after clip featuredSets = " + featuredSets.size());

		Map<T, Double> ranksMap = getRanks(rankedList);

		Map<String, Double> pvals = calcEnrichmentPVals(ranksMap, featuredSets);
		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		List<String> setNames = pvals.keySet().stream().sorted(Comparator.comparing(pvals::get)).collect(Collectors.toList());

		BufferedWriter writer = FileUtil.newBufferedWriter(outFile);

		writer.write("Name\tP-value\tFDR\tMembers in top " + (int) Math.round(REPORT_RATIO * 100) + "%");

		int rankThr = (int) Math.round(rankedList.size() * REPORT_RATIO);

		for (String name : setNames)
		{
			writer.write("\n" + name + "\t" + pvals.get(name) + "\t" + qVals.get(name) + "\t");

			writer.write(CollectionUtil.merge(getTopMembers(ranksMap, featuredSets.get(name), rankThr), " "));
		}

		writer.close();
	}

	private static <T> Map<T, Double> getRanks(List<T> rankedList)
	{
		Map<T, Double> map = new HashMap<>();
		for (int i = 0; i < rankedList.size(); i++)
		{
			map.put(rankedList.get(i), (i - 0.5) / rankedList.size());
		}
		return map;
	}

	private static <T> List<T> getTopMembers(Map<T, Double> rankMap, Set<T> set, int rankThr)
	{
		List<T> list = new ArrayList<>(set);
		list.sort(Comparator.comparing(rankMap::get));

		int i = 0;
		while ( i < list.size() && rankMap.get(list.get(i)) <= rankThr) i++;

		return list.subList(0, i);
	}

	public static <T> Map<String, Double> calcEnrichmentPVals(Map<T, Double> rankMap, Map<String, Set<T>> featuredSets)
	{
		Map<String, Double> pvals = new HashMap<>();

		for (String name : featuredSets.keySet())
		{
			Set<T> set = featuredSets.get(name);
			double mr = getMeanRank(rankMap, set);

			double n = rankMap.size();
			double k = set.size();

			double sd = Math.sqrt(((n+1) * (n-k)) / (12 * k)) / n;

			NormalDistribution ndist = new NormalDistribution(0.5, sd);

			// One-tailed test, only looking for enriched sets
			double pval = ndist.cumulativeProbability(mr);

			pvals.put(name, pval);
		}

		return pvals;
	}

	private static <T> double getMeanRank(Map<T, Double> rankMap, Set<T> set)
	{
		double sum = 0;
		for (T ele : set)
		{
			sum += rankMap.get(ele);
		}
		return sum / set.size();
	}

	private static <T> Map<String, Set<T>> clipSetsToList(Map<String, Set<T>> origSets, List<T> list)
	{
		Set<T> target = new HashSet<>(list);
		Map<String, Set<T>> clipped = new HashMap<>();
		origSets.forEach((name, set) ->
		{
			Set<T> newSet = CollectionUtil.getIntersection(set, target);
			if (newSet.size() >= 5) clipped.put(name, newSet);
		});
		return clipped;
	}

	public static void main(String[] args)
	{
	}

}