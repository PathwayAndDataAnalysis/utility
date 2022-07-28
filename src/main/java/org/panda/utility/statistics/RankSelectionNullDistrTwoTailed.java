package org.panda.utility.statistics;

import org.panda.utility.Progress;

import java.util.*;

/**
 * This class generates a simulated null distribution for the total rank of randomly selected items from a ranked list.
 * It is a two-tailed distribution, meaning ranks are calculated wrt both left end and right end, and the smaller one is
 * selected.
 */
public class RankSelectionNullDistrTwoTailed extends RankSelectionNullDistrOneTailed
{
	public RankSelectionNullDistrTwoTailed(int listSize, Set<Integer> selectionSizes, int randomizationCount)
	{
		super(listSize, selectionSizes, randomizationCount);
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
		int maxRank = listSize - 1;

		Map<Integer, Map<Integer, Integer>> countsMap = new HashMap<>();
		selectionSizes.forEach(s -> countsMap.put(s, new HashMap<>()));

		Progress prg = new Progress(randomizationCount, "Generating null distribution");
		for (int i = 0; i < randomizationCount; i++, prg.tick())
		{
			rPermutation(list, rPerm);

			int sumL = 0;
			int sumR = 0;

			for (int j = 1; j <= maxSelSize; j++)
			{
				sumL += rPerm[j-1];
				sumR += maxRank - rPerm[j-1];

				Map<Integer, Integer> counts = countsMap.get(j);
				if (counts != null)
				{
					int minSum = Math.min(sumL, sumR);
					counts.put(minSum, counts.getOrDefault(minSum, 0) + 1);
				}
			}
		}

		// convert counts to cumulative counts
		calculateCumulativeCounts(countsMap);
	}
}
