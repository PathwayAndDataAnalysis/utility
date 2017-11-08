package org.panda.utility.statistics;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class XYPlot extends JPanel implements MouseListener, MouseMotionListener
{
	private static final Color SELECT_COLOR = new Color(39, 254, 253);
	private static final Color SELECTION_RECT_COLOR = new Color(220, 220, 220);

	Map<String, double[]> points;
	Map<String, Point> locs;
	Map<String, Color> colors;
	Set<String> labeled;
	Map<Set<String>, Color> highlightMap;
	Set<String> selected;

	double minX;
	double maxX;
	double minY;
	double maxY;
	int buffer;
	int pointRadius;

	Point press = null;
	Point drag = null;

	Color[] chartColors = new Color[]{
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

	public XYPlot(Map<String, double[]> points)
	{
		super(true);
		updatePoints(points);
		buffer = 20;
		pointRadius = 3;
		colors = new HashMap<>();
		highlightMap = new HashMap<>();
		selected = new HashSet<>();
		labeled = new HashSet<>();
		locs = new HashMap<>();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void updatePoints(Map<String, double[]> points)
	{
		this.points = points;
		this.minX = points.values().stream().map(d -> d[0]).min(Double::compareTo).get();
		this.maxX = points.values().stream().map(d -> d[0]).max(Double::compareTo).get();
		this.minY = points.values().stream().map(d -> d[1]).min(Double::compareTo).get();
		this.maxY = points.values().stream().map(d -> d[1]).max(Double::compareTo).get();
	}

	public void addPointColors(Set<String> names, int chartIndexForColor)
	{
		Color color = chartColors[chartIndexForColor % chartColors.length];
		addPointColors(names, color);
	}
	public void addPointColors(Set<String> names, Color color)
	{
		names.forEach(name -> colors.put(name, color));
	}

	public void setBuffer(int buffer)
	{
		this.buffer = buffer;
	}

	public void setPointRadius(int pointRadius)
	{
		this.pointRadius = pointRadius;
	}

	public void showLabels(Set<String> names)
	{
		labeled.addAll(names);
	}

	public void showLabel(String name)
	{
		labeled.add(name);
	}

	public void unhighlightAll()
	{
		highlightMap.clear();
	}

	public void deselectAll()
	{
		selected.clear();
	}

	public Set<String> getSelected()
	{
		return selected;
	}

	public void setHighlightMap(Map<Set<String>, Color> highlightMap)
	{
		this.highlightMap = highlightMap;
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

		// draw highlight
		for (Set<String> set : highlightMap.keySet())
		{
			g.setColor(highlightMap.get(set));
			for (String name : set)
			{
				double[] p = points.get(name);
				g.fillOval(transformX(p[0]) - pointRadius - 3, transformY(p[1]) - pointRadius - 3, (pointRadius * 2) + 7, (pointRadius * 2) + 7);
			}
		}

		// draw points
		for (String name : points.keySet())
		{
			Color color = (colors.containsKey(name)) ? colors.get(name) : Color.BLACK;

			g.setColor(color.brighter().brighter());
			double[] p = points.get(name);
			int x = transformX(p[0]);
			int y = transformY(p[1]);
			locs.put(name, new Point(x, y));

			g.fillOval(x - pointRadius, y - pointRadius, pointRadius * 2, pointRadius * 2);

			g.setColor(color.darker());
			g.drawOval(x - pointRadius, y - pointRadius, pointRadius * 2, pointRadius * 2);

			if (labeled.contains(name))
			{
				g.drawString(name, x + pointRadius + 1, y + pointRadius + 1);
			}

			if (selected.contains(name))
			{
				g.setColor(SELECT_COLOR);
				g.drawOval(x - pointRadius - 1, y - pointRadius - 1, (pointRadius * 2) + 2, (pointRadius * 2) + 2);
				g.drawOval(x - pointRadius - 2, y - pointRadius - 2, (pointRadius * 2) + 4, (pointRadius * 2) + 4);
			}

			if (drag != null && press != null)
			{
				g.setColor(SELECTION_RECT_COLOR);
				Rectangle r = getRectangle(press, drag);
				g.drawRect(r.x, r.y, r.width, r.height);
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

	Set<String> findDataAtLoc(Point loc)
	{
		Set<String> set = new HashSet<>();
		for (String name : locs.keySet())
		{
			Point p = locs.get(name);
			if (p.distance(loc) <= pointRadius + 5) set.add(name);
		}
		return set;
	}

	Set<String> findDataAtArea(Point p1, Point p2)
	{
		Rectangle rec = getRectangle(p1, p2);
		Set<String> set = new HashSet<>();
		for (String name : locs.keySet())
		{
			Point p = locs.get(name);
			if (rec.contains(p)) set.add(name);
		}
		return set;
	}

	private Rectangle getRectangle(Point p1, Point p2)
	{
		Point topLeft = new Point(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
		return new Rectangle(topLeft, new Dimension(Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y)));
	}


	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (!e.isControlDown()) selected.clear();

		Point click = e.getPoint();

		for (String name : findDataAtLoc(click))
		{
			if (selected.contains(name)) selected.remove(name);
			else selected.add(name);
		}
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		press = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		Point rel = e.getPoint();

		if (press != null && !rel.equals(press))
		{
			Set<String> set = findDataAtArea(press, rel);
			if (!e.isControlDown()) selected.clear();
			if (selected.containsAll(set))
			{
				selected.removeAll(set);
			}
			else
			{
				selected.addAll(set);
			}
			repaint();
		}

		press = null;
		drag = null;
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{

	}

	@Override
	public void mouseExited(MouseEvent e)
	{

	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (press != null)
		{
			drag = e.getPoint();
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{

	}
}
