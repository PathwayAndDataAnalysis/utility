package org.panda.utility;

import org.panda.utility.graph.DirectedGraph;
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
		writeNeighborhood(sifFile, seed, outFile, StreamDirection.BOTHSTREAM);
	}

	public static void writeNeighborhood(String sifFile, Collection<String> seed, String outFile, StreamDirection d) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));

		Files.lines(Paths.get(sifFile)).forEach(l ->
		{
			String[] t = l.split("\t");

			if (t.length > 2)
			{
				if (((d == StreamDirection.DOWNSTREAM || d == StreamDirection.BOTHSTREAM) && seed.contains(t[0])) ||
					((d == StreamDirection.UPSTREAM || d == StreamDirection.BOTHSTREAM) && seed.contains(t[2])))
				{
					FileUtil.lnwrite(l, writer);
				}
			}
		});

		writer.close();
	}

	public static void writeSubgraph(String sifFile, DirectedGraph subgraph, String outFile) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));

		Files.lines(Paths.get(sifFile)).forEach(l ->
		{
			String[] t = l.split("\t");

			if (t.length > 2)
			{
				if (subgraph.hasRelation(t[0], t[2]))
				{
					FileUtil.lnwrite(l, writer);
				}
			}
		});

		writer.close();
	}

	/**
	 * The keep set needs to contain stings where source, relations and target are tab-separated.
	 */
	public static void writeSubgraph(String sifFile, Set<String> keep, String outFile) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));

		Files.lines(Paths.get(sifFile)).forEach(l ->
		{
			String[] t = l.split("\t");

			if (t.length > 2)
			{
				if (keep.contains(t[0] + "\t" + t[1] + "\t" + t[2]))
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

	public static DirectedGraph loadDirectedSingleGraph(String sifFile)
	{
		DirectedGraph graph = new DirectedGraph();
		FileUtil.linesTabbed(sifFile).filter(t -> t.length > 2).forEach(t ->
		{
			if (!graph.hasRelation(t[0], t[2]))
			{
				graph.putRelation(t[0], t[2]);
			}
		});
		return graph;
	}

	public static Set<String> getGenesInSIFFile(String file) throws IOException
	{
		Set<String> genes = new HashSet<>();
		Files.lines(Paths.get(file)).map(l -> l.split("\t")).filter(t -> t.length > 0 && t[0].length() > 0)
			.forEach(t ->
			{
				genes.add(t[0]);
				if (t.length > 2) genes.add(t[2]);
			});
		return genes;
	}

	public static int[] getNodeAndEdgeCounts(String sifFile) throws IOException
	{
		Set<String> edges = new HashSet<>();
		Set<String> nodes = new HashSet<>();

		Files.lines(Paths.get(sifFile)).map(l -> l.split("\t")).forEach(t ->
		{
			if (t.length > 2)
			{
				edges.add(t[0] + " " + t[1] + " " + t[2]);
				nodes.add(t[2]);
			}
			if (t.length > 0 && !t[0].isEmpty())
			{
				nodes.add(t[0]);
			}
		});

		return new int[]{nodes.size(), edges.size()};
	}

	public static Set<String> getRelationsAsString(String file) throws IOException
	{
		return Files.lines(Paths.get(file)).map(l -> l.split("\t")).filter(t -> t.length > 2)
			.map(t -> ArrayUtil.getString(" ", t[0], t[1], t[2])).collect(Collectors.toSet());
	}

	public static Set<String> getNeighborNodes(String sifFile, Collection<String> seed, StreamDirection d)
	{
		Set<String> neigh = new HashSet<>();
		FileUtil.lines(sifFile).map(l -> l.split("\t"))
			.filter(t -> t.length > 2)
			.filter(t -> (d == StreamDirection.DOWNSTREAM && seed.contains(t[0])) ||
				(d == StreamDirection.UPSTREAM && seed.contains(t[2])) ||
				(d == StreamDirection.BOTHSTREAM && (seed.contains(t[0]) || seed.contains(t[2])))).forEach(t ->
		{
			neigh.add(t[0]);
			neigh.add(t[2]);
		});

		neigh.removeAll(seed);
		return neigh;
	}
}
