package org.panda.utility.statistics;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class FunctionPlot extends JPanel
{
	Function func;
	double minX;
	double maxX;
	double minY;
	double maxY;
	int buffer;

	Map<String, Double> xMarks;
	Color[] labelColors = new Color[]{
		new Color(0, 69, 134),
		new Color(255, 66, 14),
		new Color(255, 211, 32),
		new Color(87, 157, 28),
		new Color(126, 0, 33),
		new Color(131, 202, 255),
		new Color(49, 64, 4),
		new Color(174, 207, 0),
		new Color(75, 31, 111),
		new Color(255, 149, 14),
		new Color(197, 0, 11),
		new Color(0, 132, 209),
		new Color(114, 159, 207),
		new Color(52, 101, 164),
		new Color(221, 72, 20),
		new Color(174, 167, 159),
		new Color(51, 51, 51),
		new Color(119, 33, 111),
	};

	List<XSet> valsToPlace;

	public FunctionPlot(Function func, double min, double max)
	{
		super(true);
		this.func = func;
		this.minX = min;
		this.maxX = max;
		buffer = 10;
		xMarks = new HashMap<>();
		valsToPlace = new ArrayList<>();
	}

	public void addValsToPlace(String name, double[] vals)
	{
		Color color = labelColors[valsToPlace.size() % labelColors.length];
		valsToPlace.add(new XSet(name, vals, color));
	}

	public void addXMark(String name, double x)
	{
		if (x < minX || x > maxX)
		{
			System.err.println("Given x coordinate is out of range. x = " + x + ". Ignoring");
		}
		else
		{
			xMarks.put(name, x);
		}
	}

	public void setBuffer(int buffer)
	{
		this.buffer = buffer;
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		g.setColor(Color.WHITE);
		g.fillRect(2, 2, getWidth()-4, getHeight()-4);
		g.setColor(Color.BLACK);
		g.drawRect(1, 1, getWidth()-3, getHeight()-3);

		if (g instanceof Graphics2D)
		{
			Map<RenderingHints.Key, Object> m = new HashMap<>();
			m.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			((Graphics2D) g).setRenderingHints(m);
		}

		int[] xx = new int[getGraphWidth()];
		double[] y = new double[xx.length];
		int[] yy = new int[xx.length];

		double range = (maxX - minX) / (getGraphWidth() - 1);

		int i = 0;
		for (double x = minX; x <= maxX + (range / 10); x += range)
		{
			xx[i] = transformX(x);
			y[i++] = func.value(x);
		}
		minY = Summary.min(y);
		maxY = Summary.max(y);

		for (int j = 0; j < xx.length; j++)
		{
			yy[j] = transformY(y[j]);
		}

		for (int j = 1; j < xx.length; j++)
		{
			g.drawLine(xx[j-1], yy[j-1], xx[j], yy[j]);
		}

		i = 0;
		for (String name : xMarks.keySet())
		{
			int x = transformX(xMarks.get(name));
			g.setColor(labelColors[i++ % labelColors.length]);
			g.drawLine(x, buffer, x, getGraphHeight() + buffer);
			g.drawString(name, x+1, buffer * (i+1));
		}

		g.setColor(Color.BLACK);

		Random r = new Random();
		for (XSet xSet : valsToPlace)
		{
			g.setColor(xSet.color);
			for (int j = 0; j < xSet.vals.length; j++)
			{
				int x = transformX(xSet.vals[j]);
				g.drawOval(x - 2, getGraphHeight() - r.nextInt(2*buffer), 4, 4);

				if (j == 0)
				{
					x = transformX(Summary.mean(xSet.vals));
					g.drawString(xSet.name, x, getGraphHeight() - 3*buffer);
				}
			}
		}
	}

	int getGraphWidth()
	{
		return Math.max(this.getWidth() - (2 * buffer), 0);
	}

	int getGraphHeight()
	{
		return Math.max(this.getHeight() - (2 * buffer), 0);
	}

	int transformX(double v)
	{
		double range = maxX - minX;
		double ratio = (v - minX) / range;

		int width = getGraphWidth();
		return buffer + (int) Math.round(width * ratio);
	}

	int transformY(double v)
	{
		double range = maxY - minY;
		double ratio = (v - minY) / range;

		int height = getGraphHeight();
		return buffer + (int) Math.round(height * (1-ratio));
	}

	class XSet
	{
		String name;
		double[] vals;
		Color color;

		public XSet(String name, double[] vals, Color color)
		{
			this.name = name;
			this.vals = vals;
			this.color = color;
		}
	}
}
