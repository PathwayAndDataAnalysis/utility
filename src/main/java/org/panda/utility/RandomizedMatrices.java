package org.panda.utility;

import org.panda.utility.statistics.TTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ozgun Babur
 */
public class RandomizedMatrices implements Serializable
{
	boolean[][][] randVals;
	List<String> rowIDs;
	List<String> colIDs;
	Map<String, Integer> rowToInd;
	Map<String, Integer> colToInd;

	public RandomizedMatrices(BufferedReader reader, Set<String> cropToRows) throws IOException
	{
		readFromText(reader, cropToRows);
	}

	public RandomizedMatrices(boolean[][][] randVals, List<String> rowIDs, List<String> colIDs)
	{
		this.randVals = randVals;
		this.rowIDs = rowIDs;
		this.colIDs = colIDs;
		rowToInd = new HashMap<>();
		colToInd = new HashMap<>();
		rowIDs.forEach(r -> rowToInd.put(r, rowIDs.indexOf(r)));
		colIDs.forEach(c -> rowToInd.put(c, colIDs.indexOf(c)));
	}

	public boolean[] convertSamplesToBooleanArray(Collection<String> samples)
	{
		boolean[] b = new boolean[colIDs.size()];
		for (int i = 0; i < colIDs.size(); i++)
		{
			b[i] = samples.contains(colIDs.get(i));
		}
		return b;
	}

	public int[] getIndicesOfSamples(List<String> samples)
	{
		int[] ind = new int[samples.size()];
		for (int i = 0; i < ind.length; i++)
		{
			ind[i] = colIDs.indexOf(samples.get(i));
		}
		return ind;
	}

	public int[] getThisToListIndexMapping(List<String> samples)
	{
		int[] ind = new int[samples.size()];
		for (int i = 0; i < ind.length; i++)
		{
			ind[i] = samples.indexOf(colIDs.get(i));
		}
		return ind;
	}

	public List<String> getCommonColumns(RandomizedMatrices rm)
	{
		List<String> list = new ArrayList<>(this.colIDs);
		list.retainAll(rm.colIDs);
		return list;
	}

	public double getPValueForOverlapSignificance(String id1, String id2, int observed, boolean[] use)
	{
		int row1 = rowIDs.indexOf(id1);
		int row2 = rowIDs.indexOf(id2);

		int cntBig = 0;
		int cntSm = 0;
		for (int i = 0; i < randVals.length; i++)
		{
			int ov = 0;
			for (int j = 0; j < use.length; j++)
			{
				if (use[j])
				{
					if (randVals[i][row1][j] == randVals[i][row2][j]) ov++;
				}
			}
			if (ov >= observed) cntBig++;
			if (ov <= observed) cntSm++;
		}
		return Math.min(Math.min(cntBig / (double) randVals.length, cntSm / (double) randVals.length) * 2, 1);
	}

	public double getPValueForOverlapSignificance(String id, RandomizedMatrices other, String idO, int observed,
		List<String> use)
	{
		int row = rowToInd.get(id);
		int rowO = other.rowToInd.get(idO);

		int cntBig = 0;
		int cntSm = 0;
		int rSize = Math.min(randVals.length, other.randVals.length);
		for (int i = 0; i < rSize; i++)
		{
			int ov = 0;

			for (String sample : use)
			{
				int j = colToInd.get(sample);
				int jO = other.colToInd.get(sample);

				if (randVals[i][row][j] == other.randVals[i][rowO][jO]) ov++;
			}
			if (ov >= observed) cntBig++;
			if (ov <= observed) cntSm++;
		}
		return Math.min((Math.min(cntBig, cntSm) / (double) rSize) * 2, 1);
	}

	public double getPValueForObservedAmount(String id, int observed, boolean[] look)
	{
		int row = rowIDs.indexOf(id);

		int cntBig = 0;
		int cntSm = 0;
		for (int i = 0; i < randVals.length; i++)
		{
			int ov = 0;
			for (int j = 0; j < look.length; j++)
			{
				if (look[j])
				{
					if (!randVals[i][row][j]) ov++;
				}
			}
			if (ov >= observed) cntBig++;
			if (ov <= observed) cntSm++;
		}
		return Math.min((Math.min(cntBig, cntSm) / (double) randVals.length) * 2, 1);
	}

	public double getPValueForSignificantT(String id, double[] valO, double observedP, int[] ind, int minSampleSize)
	{
		int cnt = 0;
		int n = 0;
		int rowID = rowToInd.get(id);
		for (boolean[][] m : randVals)
		{
			int[] c = ArrayUtil.countFalseAndTrue(m[rowID]);
			if (c[0] < minSampleSize || c[1] < minSampleSize) continue;

			double[] v0 = new double[c[0]];
			double[] v1 = new double[c[1]];
			int i0 = 0;
			int i1 = 0;

			for (int i = 0; i < m[rowID].length; i++)
			{
				if (m[rowID][i]) v1[i1++] = valO[ind[i]];
				else v0[i0++] = valO[ind[i]];
			}

			Tuple tup = TTest.test(v0, v1);

			if (tup.p <= observedP) cnt++;

			n++;
		}

		return cnt / (double) n;
	}

	public void writeAsText(BufferedWriter writer) throws IOException
	{
		writer.write(CollectionUtil.merge(rowIDs, "\t") + "\n");
		writer.write(CollectionUtil.merge(colIDs, "\t") + "\n");
		writer.write(randVals.length + "\t" + randVals[0].length + "\t" + randVals[0][0].length + "\n");
		for (boolean[][] m : randVals)
		{
			for (boolean[] c : m)
			{
				for (boolean b : c)
				{
					writer.write(b ? "1" : "0");
				}
				writer.write("\n");
			}
		}
	}

	public void readFromText(BufferedReader reader, Set<String> cropToRows) throws IOException
	{
		this.rowIDs = new ArrayList<>(Arrays.asList(reader.readLine().split("\t")));

		boolean[] useRow = null;
		if (cropToRows != null)
		{
			useRow = new boolean[rowIDs.size()];
			for (int i = 0; i < rowIDs.size(); i++)
			{
				useRow[i] = cropToRows.contains(rowIDs.get(i));
			}
			rowIDs.retainAll(cropToRows);
		}

		this.colIDs = Arrays.asList(reader.readLine().split("\t"));
		String[] t = reader.readLine().split("\t");
		int rowCnt = Integer.valueOf(t[1]);
		randVals = new boolean[Integer.valueOf(t[0])][rowIDs.size()][Integer.valueOf(t[2])];
		for (int k = 0; k < randVals.length; k++)
		{
			int r = 0;
			for (int i = 0; i < rowCnt; i++)
			{
				String line = reader.readLine();

				if (useRow == null || useRow[i])
				{
					t = line.split("");
					for (int j = 0; j < t.length; j++)
					{
						if (!t[j].equals("0")) randVals[k][r][j] = true;
					}
					r++;
				}
			}
			assert r == randVals[k].length;
		}
	}
}
