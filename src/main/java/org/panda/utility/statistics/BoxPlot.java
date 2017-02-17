package org.panda.utility.statistics;

import org.panda.utility.ArrayUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class BoxPlot
{
	public static void write(String filename, String[] colNames, List<Double>[] vals) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
		writer.write(ArrayUtil.getString("\t", colNames));

		int limit = Arrays.stream(vals).mapToInt(List::size).max().getAsInt();

		for (int i = 0; i < limit; i++)
		{
			writer.write("\n");
			for (int j = 0; j < vals.length; j++)
			{
				if (vals[j].size() > i) writer.write(vals[j].get(i).toString());
				if (j+1 < vals.length) writer.write("\t");
			}
		}

		writer.close();
	}
}
