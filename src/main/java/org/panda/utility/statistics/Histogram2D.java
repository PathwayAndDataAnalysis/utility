package org.panda.utility.statistics;

import org.panda.utility.Waiter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class Histogram2D
{
	private Map<Bin, Integer> bins;
	private double range;
	private int total;
	private String name;

	public Histogram2D(double range, String filename)
	{
		this(range);
		loadFromFile(filename);
		name = filename;
	}

	public Histogram2D(double range)
	{
		this.range = range;
		bins = new HashMap<Bin, Integer>();
		total = 0;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getRange()
	{
		return range;
	}

	public int getMaxBinValue()
	{
		int max = 0;

		for (Integer i : bins.values())
		{
			if (max < i) max = i;
		}
		return max;
	}

	public boolean isEmpty()
	{
		return bins.isEmpty();
	}

	public void count(double x, double y)
	{
		count(x, y, 1);
	}

	public void count(double x, double y, int add)
	{
		if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y))
			return;

		Bin b = getBin(x, y);

		if (bins.containsKey(b))
		{
			bins.put(b, bins.get(b) + add);
		}
		else
		{
			bins.put(b, add);
		}
		total += add;
	}

	private Bin getBin(double x, double y)
	{
		int i = (int) Math.floor((x / range) + 0.5);
		int j = (int) Math.floor((y / range) + 0.5);

		return new Bin(i, j);
	}

	public int getValue(double x, double y)
	{
		Bin b = getBin(x, y);

		if (bins.containsKey(b)) return bins.get(b);
		else return 0;
	}

	public void add(Bin bin, int value)
	{
		if (bins.containsKey(bin))
		{
			bins.put(bin, bins.get(bin) + value);
		}
		else
		{
			bins.put(bin, value);
		}
		total += value;
	}

	public void takeLog()
	{
		for (Bin bin : bins.keySet())
		{
			int c = bins.get(bin);

			if (c > 0)
			{
				bins.put(bin, (int) Math.round(Math.log(c)));
			}
		}
	}

	public void add(Histogram2D h)
	{
		assert range == h.range;

		for (Bin bin : h.bins.keySet())
		{
			add(bin, h.bins.get(bin));
		}
	}

	public int[] getBinLimits()
	{
		if (bins.isEmpty()) return new int[]{0,0,0,0};

		Bin b = bins.keySet().iterator().next();
		int maxx = b.x;
		int minx = b.x;
		int maxy = b.y;
		int miny = b.y;

		for (Bin bin : bins.keySet())
		{
			if (bin.x > maxx) maxx = bin.x;
			if (bin.x < minx) minx = bin.x;
			if (bin.y > maxy) maxy = bin.y;
			if (bin.y < miny) miny = bin.y;
		}
		return new int[]{minx, maxx, miny, maxy};
	}

	public double[] getRealLimits()
	{
		int[] bl = getBinLimits();
		return new double[]{bl[0] * range, bl[1] * range, bl[2] * range, bl[3] * range};
	}

	public List<double[]> getOccupiedPoints()
	{
		List<double[]> points = new ArrayList<double[]>(bins.size());

		for (Bin bin : bins.keySet())
		{
			points.add(new double[]{bin.x * range, bin.y * range});
		}
		return points;
	}

	public void writeWithWriter(Writer out, boolean withEmptyBins)
	{
		try
		{
			int[] limit = getBinLimits();

			for (int i = limit[0]; i< limit[1]; i++)
			{
				double x = i * range;

				for (int j = limit[2]; j< limit[3]; j++)
				{
					double y = j * range;

					Bin b = new Bin(i, j);

					if (bins.containsKey(b))
					{
						out.write(x + "\t" + y + "\t" + bins.get(b) + "\n");
					}
					else if (withEmptyBins)
					{
						out.write(x + "\t" + y + "\t0\n");
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void writeToFile(String filename, boolean withEmptyBins)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writeWithWriter(writer, withEmptyBins);
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void loadFromFile(String filename)
	{
		try
		{
			bins.clear();
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] terms = line.split("\t");

				if (terms.length == 3)
				{
					count(Double.parseDouble(terms[0]),
						Double.parseDouble(terms[1]),
						Integer.parseInt(terms[2]));
				}
			}

			reader.close();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public int getTotal()
	{
		return total;
	}

	private class Bin
	{
		int x;
		int y;

		private Bin(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public boolean equals(Object obj)
		{
			if (obj instanceof Bin)
			{
				Bin b = (Bin) obj;

				return b.x == x && b.y == y;
			}
			return false;
		}

		public int hashCode()
		{
			return x + y;
		}
	}

	public void plot()
	{
		plot(true);
	}

	public void plot(boolean modal)
	{
		Histogram2DPlot p = new Histogram2DPlot(this);
		p.setVisible(true);

		if (modal)
		{
			p.setLocation(new java.awt.Point(p.getLocation().x + p.getWidth(), p.getLocation().y));
			do {
				Waiter.pause(1000);} while (p.isVisible());
		}
	}

	public static Histogram2D filterResults(Histogram2D hsignal, Histogram2D herror,
		double err_reduct_fact, double pval)
	{
		Histogram2D hfiltered = new Histogram2D(hsignal.range);

		for (Bin bin : hsignal.bins.keySet())
		{
			if (hsignal.bins.get(bin) > 0)
			{
				if (herror.bins.containsKey(bin))
				{
					double err_ratio =
						(herror.bins.get(bin) * err_reduct_fact) / hsignal.bins.get(bin);

					if (err_ratio > pval) continue;
				}

				hfiltered.add(bin, hsignal.bins.get(bin));
			}
		}
		return hfiltered;
	}
}
