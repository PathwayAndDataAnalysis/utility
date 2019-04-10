package org.panda.utility.statistics;

import org.panda.utility.CollectionUtil;
import org.panda.utility.ValToColor;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

	public static void graphGeneSetOverlaps(Map<String, Double> pvals, Map<String, Set<String>> contributers,
		String outFileWithoutExtension, Set<String> upSet, Set<String> dwSet) throws IOException
	{
		BufferedWriter writer1 = Files.newBufferedWriter(Paths.get(outFileWithoutExtension + ".sif"));
		BufferedWriter writer2 = Files.newBufferedWriter(Paths.get(outFileWithoutExtension + ".format"));
		writer2.write("node\tall-nodes\tcolor\t255 255 255\nnode\tall-nodes\tborderwidth\t2\nedge\tall-edges\twidth\t2\n");

		ValToColor nodeBorderColor = new ValToColor(new double[]{0, 10}, new Color[]{new Color(250, 250, 250), Color.BLACK});
		ValToColor edgeColor = new ValToColor(new double[]{0, 1}, new Color[]{Color.WHITE, Color.BLACK});
		ValToColor nodeBgColor = new ValToColor(new double[]{-1, 0, 1}, new Color[]{Color.BLUE, Color.WHITE, Color.RED});

		Set<String> nodeMem = new HashSet<>();

		for (String setName1 : pvals.keySet())
		{
			for (String setName2 : pvals.keySet())
			{
				if (setName1.compareTo(setName2) < 0)
				{
					double j = CollectionUtil.getJaccardSimilarity(contributers.get(setName1), contributers.get(setName2));

					if (j > 0)
					{
						writer1.write(setName1 + "\tcorrelates-with\t" + setName2 + "\n");
						recordNodeData(pvals, contributers, upSet, dwSet, writer2, nodeBorderColor, nodeBgColor, nodeMem, setName1);
						recordNodeData(pvals, contributers, upSet, dwSet, writer2, nodeBorderColor, nodeBgColor, nodeMem, setName2);

						writer2.write("\nedge\t" + setName1 + " correlates-with " + setName2 + "\tcolor\t" +
							edgeColor.getColorInString(j));
					}
					else if (!nodeMem.contains(setName1))
					{
						recordNodeData(pvals, contributers, upSet, dwSet, writer2, nodeBorderColor, nodeBgColor, nodeMem, setName1);
						writer1.write(setName1 + "\n");
					}
				}
			}
		}

		writer1.close();
		writer2.close();
	}

	private static void recordNodeData(Map<String, Double> pvals, Map<String, Set<String>> contributers, Set<String> upSet, Set<String> dwSet, BufferedWriter writer, ValToColor nodeBorderColor, ValToColor nodeBgColor, Set<String> nodeMem, String setName) throws IOException
	{
		if (!nodeMem.contains(setName))
		{
			writer.write("\nnode\t" + setName + "\tbordercolor\t" +
				nodeBorderColor.getColorInString(-Math.log(pvals.get(setName))));
			if (upSet != null) writer.write("\nnode\t" + setName + "\tcolor\t" +
				nodeBgColor.getColorInString(getNodeUpness(contributers.get(setName), upSet, dwSet)));
			nodeMem.add(setName);
		}
	}

	private static double getNodeUpness(Set<String> contributors, Set<String> upSet, Set<String> dwSet)
	{
		int score = 0;

		for (String gene : contributors)
		{
			if (upSet.contains(gene)) score ++;
			if (dwSet.contains(gene)) score --;
		}

		return score / (double) contributors.size();
	}
}
