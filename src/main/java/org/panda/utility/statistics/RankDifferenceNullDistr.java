package org.panda.utility.statistics;

import org.panda.utility.Progress;

import java.util.*;

/**
 * This class generates a simulated null distribution for the difference of the total ranks of randomly selected items
 * from two ranked lists.
 */
public class RankDifferenceNullDistr extends RankSelectionNullDistrOneTailed
{
	public RankDifferenceNullDistr(int listSize, Set<Integer> selectionSizes, int randomizationCount)
	{
		super(listSize, selectionSizes, randomizationCount);
	}

	protected void init(int listSize, Set<Integer> selectionSizes, int randomizationCount)
	{
		int[] list1 = new int[listSize];
		int[] list2 = new int[listSize];

		for (int i = 0; i < listSize; i++)
		{
			list1[i] = i;
			list2[i] = i;
		}

		int maxSelSize = selectionSizes.stream().max(Integer::compareTo).get();
		int[] rPerm1 = new int[maxSelSize];
		int[] rPerm2 = new int[maxSelSize];

		Map<Integer, Map<Integer, Integer>> countsMap = new HashMap<>();
		selectionSizes.forEach(s -> countsMap.put(s, new HashMap<>()));

		Progress prg = new Progress(randomizationCount, "Generating null distribution");
		for (int i = 0; i < randomizationCount; i++, prg.tick())
		{
			rPermutation(list1, rPerm1);
			rPermutation(list2, rPerm2);

			int sum1 = 0;
			int sum2 = 0;

			for (int j = 1; j <= maxSelSize; j++)
			{
				sum1 += rPerm1[j-1];
				sum2 += rPerm2[j-1];

				Map<Integer, Integer> counts = countsMap.get(j);
				if (counts != null)
				{
					int dif = Math.abs(sum1 - sum2);
					counts.put(dif, counts.getOrDefault(dif, 0) + 1);
				}
			}
		}

		// convert counts to cumulative counts
		calculateCumulativeCounts(countsMap);
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
			for (int i = maxRankSum; i >= 0; i--)
			{
				cum += map.getOrDefault(i, 0);
				cumArray[i] = cum;
			}
		});
	}

	public double getPval(int selectionSize, int rankDiff)
	{
		int[] cum = cumCntMap.get(selectionSize);
		if (rankDiff >= cum.length) return 0;
		else return cum[rankDiff] / randomizationCount;
	}


	public static void main(String[] args)
	{
		test();
	}

	private static void test()
	{
	}
}
