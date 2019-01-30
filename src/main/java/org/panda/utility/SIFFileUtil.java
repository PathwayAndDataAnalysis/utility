package org.panda.utility;

import org.panda.utility.graph.UndirectedGraph;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SIFFileUtil
{
	public static void writeIntersection(String sifFile1, String sifFile2, String outFile) throws IOException
	{
		Set<String> keySet = Files.lines(Paths.get(sifFile1)).map(l -> l.split("\t")).filter(t -> t.length > 2)
			.map(t -> t[0] + "\t" + t[1] + "\t" + t[2]).collect(Collectors.toSet());

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));

		Files.lines(Paths.get(sifFile2)).forEach(l ->
		{
			String[] t = l.split("\t");

			if (t.length < 3) return;

			String key = t[0] + "\t" + t[1] + "\t" + t[2];

			if (keySet.contains(key))
			{
				FileUtil.writeln(l, writer);
			}
		});

		writer.close();
	}

	/**
	 * This method is good for selecting a neighborhood in a CausalPath result file, and adding PPI relations to that
	 * neighborhood to display other "related" proteomic changes along with causal explanations.
	 */
	public static void generateSubsetAndAddPPI(String sifFile, Collection<String> seed, String outFile,
		UndirectedGraph ppiGraph) throws IOException
	{
		Set<String> allNodes = new HashSet<>();

		Set<String> noPPI = new HashSet<>();

		Set<String> selectedRels = new HashSet<>();

		Files.lines(Paths.get(sifFile)).forEach(l ->
		{
			String[] t = l.split("\t");

			if (t.length > 2)
			{
				allNodes.add(t[0]);
				allNodes.add(t[2]);

				if (seed.contains(t[0]) || seed.contains(t[2]))
				{
					selectedRels.add(l);
					noPPI.add(t[0] + " " + t[2]);
					noPPI.add(t[2] + " " + t[0]);
				}
			}
			else
			{
				allNodes.add(t[0]);
			}
		});

		Set<String> ppiLines = new HashSet<>();

		for (String gene : seed)
		{
			for (String neighbor : ppiGraph.getNeighbors(gene))
			{
				if (allNodes.contains(neighbor))
				{
					String key = gene + " " + neighbor;

					if (!noPPI.contains(key))
					{
						noPPI.add(key);
						noPPI.add(neighbor + " " + gene);
						ppiLines.add(gene + "\tin-complex-with\t" + neighbor);
					}
				}
			}
		}

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));

		selectedRels.forEach(l -> FileUtil.writeln(l, writer));
		ppiLines.forEach(l -> FileUtil.writeln(l, writer));

		writer.close();
	}

	public static void writeNeighborhood(String sifFile, Collection<String> seed, String outFile) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));

		Files.lines(Paths.get(sifFile)).forEach(l ->
		{
			String[] t = l.split("\t");

			if (t.length > 2)
			{
				if (seed.contains(t[0]) || seed.contains(t[2]))
				{
					FileUtil.lnwrite(l, writer);
				}
			}
		});

		writer.close();
	}

	public static void writeDownstream(String sifFile, Collection<String> seed, String outFile) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));

		Files.lines(Paths.get(sifFile)).forEach(l ->
		{
			String[] t = l.split("\t");

			if (t.length > 2)
			{
				if (seed.contains(t[0]))
				{
					FileUtil.lnwrite(l, writer);
				}
			}
		});

		writer.close();
	}



}
