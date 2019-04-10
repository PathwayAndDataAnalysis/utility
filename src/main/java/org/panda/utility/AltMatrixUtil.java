package org.panda.utility;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * For the matrices in the format Map<String, boolean[]>.
 */
public class AltMatrixUtil
{
	public static int getCoverage(List<boolean[]> matrix)
	{
		int n = matrix.get(0).length;
		int cov = 0;
		for (int i = 0; i < n; i++)
		{
			for (boolean[] b : matrix)
			{
				if (b[i])
				{
					cov++;
					break;
				}
			}
		}
		return cov;
	}

	public static int getCoverage(Map<String, boolean[]> matrix, Set<String> genes)
	{
		int n = matrix.get(genes.iterator().next()).length;
		int cov = 0;
		for (int i = 0; i < n; i++)
		{
			for (String gene : genes)
			{
				if (matrix.get(gene)[i])
				{
					cov++;
					break;
				}
			}
		}
		return cov;
	}

	public static Map<Integer, Long> getSampleHitCounts(List<Edge> edges, Set<String> genes)
	{
		return edges.stream().filter(e -> genes.contains(e.gene)).map(e -> e.sampleIndex)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	public static int getOverlap(List<boolean[]> matrix)
	{
		int n = matrix.get(0).length;
		int ov = 0;

		for (int i = 0; i < n; i++)
		{
			boolean covered = false;
			for (boolean[] b : matrix)
			{
				if (b[i])
				{
					if (!covered) covered = true;
					else ov++;
				}
			}
		}
		return ov;
	}

	public static int getOverlap(Map<String, boolean[]> matrix, Set<String> genes)
	{
		int n = matrix.get(genes.iterator().next()).length;
		int ov = 0;

		for (int i = 0; i < n; i++)
		{
			boolean covered = false;
			for (String gene : genes)
			{
				if (matrix.get(gene)[i])
				{
					if (!covered) covered = true;
					else ov++;
				}
			}
		}
		return ov;
	}

	public static double getOverallMutexnessPValue(List<boolean[]> m, int trials)
	{
		int current = getCoverage(m);

//		Histogram h = new Histogram(1);
//		h.setBorderAtZero(true);
//		h.setUseLowerBorderForPrinting(true);
		int meet = 0;
		for (int i = 0; i < trials; i++)
		{
			for (boolean[] b : m)
			{
				shuffleArray(b);
			}

			int c = getCoverage(m);
//			h.count(c);
			if (c >= current) meet++;
		}
//		h.printDensity();

		return meet / (double) trials;
	}

	// Implementing Fisher–Yates shuffle
	public static void shuffleArray(boolean[] b)
	{
		Random rnd = new Random();
		for (int i = b.length - 1; i > 0; i--)
		{
			int index = rnd.nextInt(i + 1);
			// Simple swap
			boolean a = b[index];
			b[index] = b[i];
			b[i] = a;
		}
	}

	public static List<boolean[]> toList(Map<String, boolean[]> matrix)
	{
		return new ArrayList<>(matrix.values());
	}

	public static Set<String> selectGenesWithSimilarCoverage(Map<String, boolean[]> matrix,
		Set<String> currentSelection, Set<String> exclude)
	{
		Map<String, Set<Integer>> geneToIndices = getGeneToIndices(matrix);
		int targetCov = getCoverageFromGeneToInds(geneToIndices, currentSelection);

		List<String> allGenes = new ArrayList<>(matrix.keySet());
		allGenes.removeAll(exclude);
		Collections.shuffle(allGenes);

		int cov = 0;
		Set<String> select = new HashSet<>();

		int index = 0;
		while (cov < targetCov && select.size() < allGenes.size())
		{
			select.add(allGenes.get(index++));
			cov = getCoverageFromGeneToInds(geneToIndices, select);
		}

		return select;
	}

	public static Map<String, Set<String>> getRandomControlsWithSimilarCoverages(Map<String, boolean[]> matrix,
		Map<String, Set<String>> geneSets, Set<String> exclude)
	{
		return geneSets.keySet().stream().peek(System.out::println).collect(Collectors.toMap(
			name -> name + "-control", name -> selectGenesWithSimilarCoverage(matrix, geneSets.get(name), exclude)));
	}

	public static Map<String, Double>[] getMutexCoocPvalsPreserveSampleWeights(Map<String, boolean[]> matrix,
		Map<String, Set<String>> geneSets, int iteration, String outDir) throws IOException
	{
		// Get edge representation of the matrix
		List<Edge> edges = getEdges(matrix);

		// Initiate current coverage maps
		Map<String, Map<Integer, Long>> coverageMap = geneSets.keySet().stream().collect(
			Collectors.toMap(Function.identity(), name -> getSampleHitCounts(edges, geneSets.get(name))));

		// Initiate group meet maps
		Map<String, Integer> mutexMeetMap = geneSets.keySet().stream().collect(
			Collectors.toMap(Function.identity(), name -> 0));
		Map<String, Integer> coocMeetMap = geneSets.keySet().stream().collect(
			Collectors.toMap(Function.identity(), name -> 0));

		// Initiate gene-specific meet maps
		Map<String, Map<String, Integer>> geneMutexMeetMaps = geneSets.keySet().stream().collect(Collectors.toMap(Function.identity(),
			name -> geneSets.get(name).stream().collect(Collectors.toMap(Function.identity(), gene -> 0))));
		Map<String, Map<String, Integer>> geneCoocMeetMaps = geneSets.keySet().stream().collect(Collectors.toMap(Function.identity(),
			name -> geneSets.get(name).stream().collect(Collectors.toMap(Function.identity(), gene -> 0))));

		// Get current gene to sample indices
		Map<String, Map<String, Set<Integer>>> origGeneToIndices = geneSets.keySet().stream().collect(Collectors.toMap(Function.identity(),
			name -> getGeneToIndices(edges, geneSets.get(name))));

		// Initiate gene-specific current sample hits
		Map<String, Map<String, Long>> geneSampleHitMaps = geneSets.keySet().stream().collect(Collectors.toMap(Function.identity(),
			name -> geneSets.get(name).stream().collect(Collectors.toMap(Function.identity(),
				gene -> getSampleHitsForGeneInGroup(origGeneToIndices.get(name), gene, coverageMap.get(name))))));

		// Start shuffling and recording
		Progress prg = new Progress(iteration, "Shuffling the matrix " + iteration + " times");
		for (int i = 0; i < iteration; i++)
		{
			shuffleMatrixPreserveSampleWeights(matrix, edges);
			coverageMap.forEach((name, origCov) ->
			{
				Map<Integer, Long> sampleHitCounts = getSampleHitCounts(edges, geneSets.get(name));
				int cov = sampleHitCounts.size();
				if (cov >= origCov.size()) mutexMeetMap.put(name, mutexMeetMap.get(name) + 1);
				if (cov <= origCov.size()) coocMeetMap.put(name, coocMeetMap.get(name) + 1);

				Map<String, Set<Integer>> geneToIndices = getGeneToIndices(edges, geneSets.get(name));

				updateGeneMutexMeetMap(geneToIndices, sampleHitCounts, geneMutexMeetMaps.get(name), geneSampleHitMaps.get(name));
				updateGeneCoocMeetMap(geneToIndices, sampleHitCounts, geneCoocMeetMaps.get(name), geneSampleHitMaps.get(name));
			});
			prg.tick();
		}

		// Calculate gene p-values
		Map<String, Map<String, Double>> geneMutexPvalMaps = geneSets.keySet().stream().collect(Collectors.toMap(Function.identity(),
			name -> geneSets.get(name).stream().collect(Collectors.toMap(Function.identity(),
				gene -> geneMutexMeetMaps.get(name).get(gene) / (double) iteration))));
		Map<String, Map<String, Double>> geneCoocPvalMaps = geneSets.keySet().stream().collect(Collectors.toMap(Function.identity(),
			name -> geneSets.get(name).stream().collect(Collectors.toMap(Function.identity(),
				gene -> geneCoocMeetMaps.get(name).get(gene) / (double) iteration))));

		// Write gene p-values
		for (String name : geneSets.keySet())
		{
			BufferedWriter writer1 = Files.newBufferedWriter(Paths.get(outDir + "/" + escapeSpecialCharacters(name + "-mutex.txt")));
			geneMutexPvalMaps.get(name).keySet().stream().sorted(Comparator.comparing(geneMutexPvalMaps.get(name)::get))
				.forEach(gene -> FileUtil.writeln(gene + "\t" + geneMutexPvalMaps.get(name).get(gene), writer1));
			writer1.close();

			BufferedWriter writer2 = Files.newBufferedWriter(Paths.get(outDir + "/" + escapeSpecialCharacters(name + "-cooc.txt")));
			geneCoocPvalMaps.get(name).keySet().stream().sorted(Comparator.comparing(geneCoocPvalMaps.get(name)::get))
				.forEach(gene -> FileUtil.writeln(gene + "\t" + geneCoocPvalMaps.get(name).get(gene), writer2));
			writer2.close();
		}

		// Calculate and return group p-values
		return new Map[]{
			mutexMeetMap.keySet().stream().collect(Collectors.toMap(
				Function.identity(), name -> mutexMeetMap.get(name) / (double) iteration)),
			coocMeetMap.keySet().stream().collect(Collectors.toMap(
				Function.identity(), name -> coocMeetMap.get(name) / (double) iteration))
		};
	}

	private static String escapeSpecialCharacters(String s)
	{
		return s.replaceAll(":", "_").replaceAll("/", "_");
	}

	private static long getSampleHitsForGeneInGroup(Map<String, Set<Integer>> geneToInds, String gene,
		Map<Integer, Long> sampleHitCounts)
	{
		return geneToInds.get(gene).stream().map(sampleHitCounts::get).reduce((h1, h2) -> h1 + h2).get();
	}

	private static void updateGeneMutexMeetMap(Map<String, Set<Integer>> geneToIndices, Map<Integer, Long> sampleHitCounts,
		Map<String, Integer> meetMap, Map<String, Long> origGeneSampleHits)
	{
		geneToIndices.keySet().forEach(gene ->
		{
			long hit = getSampleHitsForGeneInGroup(geneToIndices, gene, sampleHitCounts);
			if (hit <= origGeneSampleHits.get(gene)) meetMap.put(gene, meetMap.get(gene) + 1);
		});
	}

	private static void updateGeneCoocMeetMap(Map<String, Set<Integer>> geneToIndices, Map<Integer, Long> sampleHitCounts,
		Map<String, Integer> meetMap, Map<String, Long> origGeneSampleHits)
	{
		geneToIndices.keySet().forEach(gene ->
		{
			long hit = getSampleHitsForGeneInGroup(geneToIndices, gene, sampleHitCounts);
			if (hit >= origGeneSampleHits.get(gene)) meetMap.put(gene, meetMap.get(gene) + 1);
		});
	}

	public static List<Edge> shuffleMatrixPreserveSampleWeights(Map<String, boolean[]> matrix, List<Edge> edges)
	{
		if (edges == null) edges = getEdges(matrix);
		int E = edges.size();

		Random r = new Random();

		for (int i = 0; i < 100; i++)
		{
			for (int j = 0; j < edges.size(); j++)
			{
				Edge edge1 = edges.get(r.nextInt(E));
				Edge edge2 = edges.get(r.nextInt(E));

				if (edge1 != edge2)
				{
					boolean[] b1 = matrix.get(edge1.gene);
					boolean[] b2 = matrix.get(edge2.gene);

					if (!b1[edge2.sampleIndex] && !b2[edge1.sampleIndex])
					{
						b1[edge1.sampleIndex] = !b1[edge1.sampleIndex];
						b1[edge2.sampleIndex] = !b1[edge2.sampleIndex];
						b2[edge1.sampleIndex] = !b2[edge1.sampleIndex];
						b2[edge2.sampleIndex] = !b2[edge2.sampleIndex];

						int temp = edge1.sampleIndex;
						edge1.sampleIndex = edge2.sampleIndex;
						edge2.sampleIndex = temp;
					}
				}
			}
		}
		return edges;
	}

	public static int getCoverageFromGeneToInds(Map<String, Set<Integer>> geneToInds, Set<String> select)
	{
		return (int) geneToInds.keySet().stream().filter(select::contains).map(geneToInds::get)
			.flatMap(Collection::stream).distinct().count();
	}

	public static Map<String, Set<Integer>> getGeneToIndices(List<Edge> edges, Set<String> genes)
	{
		Map<String, Set<Integer>> map = new HashMap<>();
		edges.stream().filter(e -> genes.contains(e.gene)).forEach(edge ->
		{
			if (!map.containsKey(edge.gene)) map.put(edge.gene, new HashSet<>());
			map.get(edge.gene).add(edge.sampleIndex);
		});
		return map;
	}

	public static Map<String, Set<Integer>> getGeneToIndices(Map<String, boolean[]> matrix)
	{
		Map<String, Set<Integer>> map = matrix.keySet().stream().collect(Collectors.toMap(
			Function.identity(), gene -> new HashSet<>()));

		map.forEach((gene, inds) ->
		{
			boolean[] b = matrix.get(gene);
			for (int i = 0; i < b.length; i++)
			{
				if (b[i]) inds.add(i);
			}
		});

		return map;
	}

	public static List<Edge> getEdges(Map<String, boolean[]> matrix)
	{
		List<Edge> edges = new ArrayList<>();
		for (String gene : matrix.keySet())
		{
			boolean[] b = matrix.get(gene);
			for (int i = 0; i < b.length; i++)
			{
				if (b[i]) edges.add(new Edge(gene, i));
			}
		}
		return edges;
	}

	public static String[] loadMatrixColumnNamesFromFile(String file) throws IOException
	{
		String s = Files.lines(Paths.get(file)).findFirst().get();
		s = s.substring(s.indexOf("\t") + 1);
		return s.split("\t");
	}

	public static Map<String, boolean[]> loadMatrixFromFile(String file) throws IOException
	{
		Map<String, boolean[]> matrix = new HashMap<>();
		Files.lines(Paths.get(file)).skip(1).map(l -> l.split("\t")).forEach(t ->
		{
			boolean[] b = new boolean[t.length - 1];
			for (int i = 1; i < t.length; i++)
			{
				b[i-1] = !t[i].equals("0");
			}
			matrix.put(t[0], b);
		});
		return matrix;
	}

	static class Edge
	{
		public Edge(String gene, int sampleIndex)
		{
			this.gene = gene;
			this.sampleIndex = sampleIndex;
		}

		String gene;
		int sampleIndex;
	}

	public static void main(String[] args) throws IOException
	{
		String[] samples = loadMatrixColumnNamesFromFile("/home/ozgun/Documents/Grants/Mental/mutex/data-matrix.txt");
		System.out.println("samples.length = " + samples.length);
	}
}
