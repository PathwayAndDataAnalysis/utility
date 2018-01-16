package org.panda.utility;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class is for randomization of boolean matrices, preserving row and column sums.
 *
 * @author Ozgun Babur
 */
public class BooleanMatrixRandomizer implements Serializable
{
	Random r = new Random();
	public static final String PHOSPHO_DIR = "phosphoprotein";
	public static final String TOTAL_PROT_DIR = "total-protein";

	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		BooleanMatrixRandomizer bmr = new BooleanMatrixRandomizer();
		String baseDir = "/home/babur/Documents/RandomizedMatrices/";
		String name = "PNLL-causality-formatted";

		RandomizedMatrices m = bmr.readRandomMatrices(new File(baseDir + name + "/" + TOTAL_PROT_DIR).getPath(), Collections.singleton("TP53"));
		System.out.println("m.randVals.length = " + m.randVals.length);
		if (true) return;

		String file = "/home/babur/Documents/RPPA/TCGA/PNNL/" + name + ".txt";
		Matrix[] matrices = bmr.loadOvarian(file);

		int bundleSize = 500;

		for (int i = 0; i < 20; i++)
		{
			Kronometre k = new Kronometre();
			bmr.generateAndWrite(baseDir + name + "/" + TOTAL_PROT_DIR, matrices[0], bundleSize);
			bmr.generateAndWrite(baseDir + name + "/" + PHOSPHO_DIR, matrices[1], bundleSize);
			k.print();
		}
	}

	public void generateAndWrite(String dir, Matrix m, int bundleSize) throws IOException
	{
		int cycle = getConservativeSaturationCycle(m.vals);
		boolean[][][] r = getRandomizedArrays(m.vals, bundleSize, cycle);
		RandomizedMatrices rm = new RandomizedMatrices(r, m.rowIDs, m.colIDs);

		if (!(new File(dir)).exists())
		{
			new File(dir).mkdirs();
		}

		String filename = dir + "/" + System.currentTimeMillis() + ".txt";
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
		rm.writeAsText(writer);
		writer.close();
	}

	public RandomizedMatrices readRandomMatrices(String dirPath, Set<String> rowIDs) throws IOException, ClassNotFoundException
	{
		File dir = new File(dirPath);
		if (dir.isDirectory())
		{
			List<RandomizedMatrices> list = new ArrayList<>();

			File[] files = dir.listFiles();

			Progress p = new Progress(files.length, "Reading " + dirPath);
			for (File file : files)
			{
				try
				{
					RandomizedMatrices rm = readOneBatchOfMatrices(file, rowIDs);
					list.add(rm);
				}
				catch (Exception e)
				{
					throw new RuntimeException("The file \"" + file + "\" seems invalid. Skipping it", e);
				}
				p.tick();
			}

			int size = 0;
			for (RandomizedMatrices rm : list)
			{
				size += rm.randVals.length;
			}

			boolean[][][] rv = new boolean[size][][];
			int start = 0;
			for (RandomizedMatrices rm : list)
			{
				System.arraycopy(rm.randVals, 0, rv, start, rm.randVals.length);
				start += rm.randVals.length;
			}

			return new RandomizedMatrices(rv, list.get(0).rowIDs, list.get(0).colIDs);
		}
		return null;
	}

	private RandomizedMatrices readOneBatchOfMatrices(File file, Set<String> cropToRows) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		RandomizedMatrices rm = new RandomizedMatrices(reader, cropToRows);
		reader.close();
		return rm;
	}

	public boolean[][][] getRandomizedArrays(boolean[][] b, int num, int cycle)
	{
		boolean[][][] r = new boolean[num][][];

		Progress p = new Progress(num, "Generating a batch");
		for (int i = 0; i < num; i++)
		{
			boolean[][] c = copy(b);
			randomize(c, cycle);
			r[i] = c;
			p.tick();
		}

		return r;
	}

	public int getConservativeSaturationCycle(boolean[][] b)
	{
		List<Integer> list = new ArrayList<>();

		for (int i = 0; i < 11; i++)
		{
			boolean[][] c = copy(b);
			int prevDist = 0;
			int j = 1;
			while (true)
			{
				randomize(c, 1);
				int dist = distance(c, b);

				if (dist < prevDist)
				{
					list.add(j);
					break;
				}

				prevDist = dist;
				j++;
			}
		}

		Collections.sort(list);
		return list.get(list.size() - 1);
	}

	public boolean[][] copy(boolean[][] b)
	{
		boolean[][] c = new boolean[b.length][b[0].length];
		for (int i = 0; i < b.length; i++)
		{
			System.arraycopy(b[i], 0, c[i], 0, b[i].length);
		}
		return c;
	}

	public void randomize(boolean[][] b, int times)
	{
		for (int i = 0; i < times; i++)
		{
			for (int r1 = 0; r1 < b.length; r1++)
			{
				for (int c1 = 0; c1 < b[r1].length; c1++)
				{
					int c2 = r.nextInt(b[r1].length);

					if (b[r1][c1] != b[r1][c2])
					{
						int r2 = r.nextInt(b.length);

						if (b[r1][c1] == b[r2][c2] && b[r1][c2] == b[r2][c1])
						{
							b[r1][c1] = !b[r1][c1];
							b[r1][c2] = !b[r1][c2];
							b[r2][c1] = !b[r2][c1];
							b[r2][c2] = !b[r2][c2];
						}
					}
				}
			}
		}
	}

	public int distance(boolean[][] b1, boolean[][] b2)
	{
		int d = 0;

		for (int i = 0; i < b1.length; i++)
		{
			for (int j = 0; j < b1[i].length; j++)
			{
				if (b1[i][j] != b2[i][j]) d++;
			}
		}
		return d;
	}

	private Matrix[] loadOvarian(String file) throws IOException
	{
		String[] header = Files.lines(Paths.get(file)).findFirst().get().split("\t");

		List<boolean[]> totalList = new ArrayList<>();
		List<String> totalRowNames = new ArrayList<>();
		List<String> totalColNames = new ArrayList<>();

		totalColNames.addAll(Arrays.asList(header).subList(4, header.length));

		Files.lines(Paths.get(file)).skip(1).map(l -> l.split("\t")).filter(t -> t[2].isEmpty()).forEach(t ->
		{
			boolean[] b = new boolean[t.length - 4];
			for (int i = 0; i < b.length; i++)
			{
				b[i] = !t[i + 4].equals("NaN");
			}
			totalList.add(b);
			totalRowNames.add(t[0]);
		});

		boolean[] in = new boolean[header.length];

		Files.lines(Paths.get(file)).skip(1).map(l -> l.split("\t")).filter(t -> !t[2].isEmpty()).forEach(t ->
		{
			for (int i = 4; i < t.length; i++)
			{
				if (!t[i].equals("NaN")) in[i] = true;
			}
		});

		int size = ArrayUtil.countValue(in, true);
		List<boolean[]> phosphoList = new ArrayList<>();
		List<String> phosphoRowNames = new ArrayList<>();
		List<String> phosphoColNames = new ArrayList<>();

		Files.lines(Paths.get(file)).skip(1).map(l -> l.split("\t")).filter(t -> !t[2].isEmpty()).forEach(t ->
		{
			for (int i = 4; i < t.length; i++)
			{
				if (!t[i].equals("NaN")) in[i] = true;
			}
		});

		for (int i = 0; i < header.length; i++)
		{
			if (in[i]) phosphoColNames.add(header[i]);
		}

		Files.lines(Paths.get(file)).skip(1).map(l -> l.split("\t")).filter(t -> !t[2].isEmpty()).forEach(t ->
		{
			boolean[] b = new boolean[size];

			int j = 0;
			for (int i = 4; i < t.length; i++)
			{
				if (in[i]) b[j++] = !t[i].equals("NaN");
			}
			assert j == size;

			phosphoList.add(b);
			phosphoRowNames.add(t[0]);
		});

		return new Matrix[]{
			new Matrix(totalList.toArray(new boolean[totalList.size()][]), totalRowNames, totalColNames),
			new Matrix(phosphoList.toArray(new boolean[phosphoList.size()][]), phosphoRowNames, phosphoColNames)};
	}

	public class Matrix implements Serializable
	{
		boolean[][] vals;
		List<String> rowIDs;
		List<String> colIDs;

		public Matrix(boolean[][] vals, List<String> rowIDs, List<String> colIDs)
		{
			this.vals = vals;
			this.rowIDs = rowIDs;
			this.colIDs = colIDs;
		}
	}
}
