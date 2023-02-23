package org.panda.utility.statistics;

import org.panda.utility.Progress;

import java.util.*;

/**
 * This class generates a simulated null distribution for the total rank of randomly selected items from a ranked list.
 */
public class RankSelectionNullDistrOneTailed
{
	protected Map<Integer, int[]> cumCntMap;
	protected double randomizationCount;
	protected Random rand;

	public RankSelectionNullDistrOneTailed(int listSize, Set<Integer> selectionSizes, int randomizationCount)
	{
		this.randomizationCount = randomizationCount;
		rand = new Random();
		init(listSize, selectionSizes, randomizationCount);
	}

	protected void init(int listSize, Set<Integer> selectionSizes, int randomizationCount)
	{
		int[] list = new int[listSize];
		for (int i = 0; i < listSize; i++)
		{
			list[i] = i;
		}

		int maxSelSize = selectionSizes.stream().max(Integer::compareTo).get();
		int[] rPerm = new int[maxSelSize];

		Map<Integer, Map<Integer, Integer>> countsMap = new HashMap<>();
		selectionSizes.forEach(s -> countsMap.put(s, new HashMap<>()));

		Progress prg = new Progress(randomizationCount, "Generating null distribution");
		for (int i = 0; i < randomizationCount; i++, prg.tick())
		{
			rPermutation(list, rPerm);

			int sum = 0;
			for (int j = 1; j <= maxSelSize; j++)
			{
				sum += rPerm[j-1];

				Map<Integer, Integer> counts = countsMap.get(j);
				if (counts != null)
				{
					counts.put(sum, counts.getOrDefault(sum, 0) + 1);
				}
			}
		}

		// convert counts to cumulative counts
		calculateCumulativeCounts(countsMap);
	}

	protected void rPermutation(int[] list, int[] perm)
	{
		int n = list.length;
		for (int i = 0; i < perm.length; i++)
		{
			int rInd = rand.nextInt(n - i) + i;
			perm[i] = list[rInd];
			list[rInd] = list[i];
			list[i] = perm[i];
		}
	}

	protected void calculateCumulativeCounts(Map<Integer, Map<Integer, Integer>> countsMap)
	{
		cumCntMap = new HashMap<>();
		countsMap.forEach((s, map) ->
		{
			int maxRankSum = map.keySet().stream().max(Integer::compareTo).get();
			int[] cumArray = new int[maxRankSum + 1];
			cumCntMap.put(s, cumArray);

			int cum = 0;
			for (int i = 0; i <= maxRankSum; i++)
			{
				cum += map.getOrDefault(i, 0);
				cumArray[i] = cum;
			}
		});
	}

	public double getPval(int selectionSize, int rankSum)
	{
		int[] cum = cumCntMap.get(selectionSize);
		if (rankSum >= cum.length) return 1;
		else return Math.max(cum[rankSum], 1) / randomizationCount;
	}

	public static void main(String[] args)
	{
		test();
	}

	private static void test()
	{
		int listSize = 100;
		Set<Integer> set1 = new HashSet<>(Arrays.asList(4, 6, 1, 7, 37, 67));
		Set<Integer> set2 = new HashSet<>(Arrays.asList(87, 32, 14, 56, 24, 75, 68, 86, 25, 67));
		Set<Integer> set3 = new HashSet<>(Arrays.asList(27, 36, 16, 84, 27, 55, 98));
		Set<Integer> set4 = new HashSet<>(Arrays.asList(7, 45, 14, 87, 33, 77, 93, 56, 91, 47, 35));

		RankSelectionNullDistrOneTailed nd = new RankSelectionNullDistrOneTailed(listSize,
			new HashSet<>(Arrays.asList(set1.size(), set2.size(), set3.size(), set4.size())), 1000000);

		List<Integer> list = new ArrayList<>(listSize);
		for (int i = 0; i < listSize; i++)
		{
			list.add(i);
		}

		Map<Integer, Double> pvals = new HashMap<>();
		list.forEach(ele -> pvals.put(ele, (set1.contains(ele) || set2.contains(ele)) ?
			Math.random() * Math.random() * Math.random() : Math.random()));

		list.sort(Comparator.comparing(pvals::get));

		System.out.println("set1 = " + nd.getPval(set1.size(), getRankSum(list, set1)));
		System.out.println("set2 = " + nd.getPval(set2.size(), getRankSum(list, set2)));
		System.out.println("set3 = " + nd.getPval(set3.size(), getRankSum(list, set3)));
		System.out.println("set4 = " + nd.getPval(set4.size(), getRankSum(list, set4)));
	}

	private static <T> int getRankSum(List<T> list, Set<T> set)
	{
		int sum = 0;
		for (T ele : set)
		{
			sum += list.indexOf(ele);
		}
		return sum;
	}
}
