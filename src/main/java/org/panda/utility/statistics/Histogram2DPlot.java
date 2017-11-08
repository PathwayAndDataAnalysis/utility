package org.panda.utility.statistics;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Ozgun Babur
 */
public class Histogram2DPlot extends JFrame implements MouseListener, MouseMotionListener
{
	protected int binsPerTick = 10;
	protected double tickLength = 0.01;
	protected boolean preferGridToTick = true;

	protected int lightestColorNum = 200;
	protected int darkestColorNum = 0;

	protected int darkestValue = 3;
	protected JSlider darkestValSlider;
	protected JLabel darkestValLabel;
	protected JLabel totalValLabel;

	protected Color bgColor = Color.WHITE;
	protected Color axisColor = Color.BLACK;
	protected Color tickColor = Color.LIGHT_GRAY;

	protected Histogram2D histo;
	protected Histogram2D subset;
	protected double width_ratio;
	protected double height_ratio;
	protected double xmax;
	protected double xmin;
	protected double ymax;
	protected double ymin;

	protected java.util.List<double[]> points;
	protected java.util.List<double[]> lines;

	protected Color pointsColor = Color.RED;
	protected Color linesColor = Color.GREEN;

	protected double pointRadius = 0.003;

	public Histogram2DPlot(Histogram2D histo)
	{
		this(histo, null);
	}

	public Histogram2DPlot(Histogram2D histo, Histogram2D subset)
	{
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle("Density ModPrint");

		this.setSize(600, 700);

		this.setLayout(new BorderLayout());
		GraphPanel gp = new GraphPanel();
		this.getContentPane().add(gp, BorderLayout.CENTER);

		gp.addMouseListener(this);
		gp.addMouseMotionListener(this);

		initControlsPanel();

		loadHisto(histo);
		if (subset != null) loadSubset(subset);
	}

	protected void loadHisto(Histogram2D histo)
	{
		this.histo = histo;
		if (histo.getName() != null)
		{
			String title = this.getTitle();
			if (title.contains(" - ")) title = title.substring(0, title.indexOf(" - "));
			this.setTitle(title + " - " + histo.getName());
		}

		double[] limits = histo.getRealLimits();
		double r = histo.getRange();
		double l = r * 10;
		xmin = Math.min(limits[0]-r, -l);
		xmax = Math.max(limits[1]+r, l);
		ymin = Math.min(limits[2]-r, -l);
		ymax = Math.max(limits[3]+r, l);

		int maxval = histo.getMaxBinValue();
		darkestValSlider.setMaximum(maxval);
		darkestValue = maxval;
		darkestValLabel.setText("" + darkestValue);
		darkestValSlider.setValue(darkestValue);

		totalValLabel.setText("Total: " + histo.getTotal());
	}

	protected void loadSubset(Histogram2D subset)
	{
		assert histo != null;
		assert histo.getRange() == subset.getRange();

		this.subset = subset;
		totalValLabel.setText(totalValLabel.getText() + " - " + subset.getTotal());
	}

	private void initControlsPanel()
	{
		JPanel cPanel = new JPanel();
		darkestValSlider = new JSlider(JSlider.HORIZONTAL, 1, darkestValue, darkestValue);

		darkestValLabel = new JLabel("" + darkestValue);
		totalValLabel = new JLabel("Total: 1000");

		darkestValSlider.addChangeListener(e -> {
			if (darkestValue != darkestValSlider.getValue())
			{
				darkestValue = darkestValSlider.getValue();
				darkestValLabel.setText("" + darkestValue);
				Histogram2DPlot.this.repaint();
			}
		});

		JButton zoomInButton = new JButton("+");
		zoomInButton.setSize(20, 20);
		zoomInButton.addActionListener(e -> {
			double w = xmax - xmin;
			xmax -= w * ZOOM_STEP;
			xmin += w * ZOOM_STEP;
			double h = ymax - ymin;
			ymax -= h * ZOOM_STEP;
			ymin += h * ZOOM_STEP;
			Histogram2DPlot.this.repaint();
		});

		JButton zoomOutButton = new JButton("-");
		zoomOutButton.setSize(20, 20);
		zoomOutButton.addActionListener(e -> {
			double w = xmax - xmin;
			xmax += w * ZOOM_STEP;
			xmin -= w * ZOOM_STEP;
			double h = ymax - ymin;
			ymax += h * ZOOM_STEP;
			ymin -= h * ZOOM_STEP;
			Histogram2DPlot.this.repaint();
		});

		JLabel buf1 = new JLabel("    ");
		JLabel buf2 = new JLabel("    ");

		cPanel.add(darkestValSlider);
		cPanel.add(darkestValLabel);
		cPanel.add(buf1);
		cPanel.add(totalValLabel);
		cPanel.add(buf2);
		cPanel.add(zoomInButton);
		cPanel.add(zoomOutButton);
		this.getContentPane().add(cPanel, BorderLayout.NORTH);
	}

	private void ensurePointsInside()
	{
		if (points != null)
		{
			for (double[] p : points)
			{
				if (xmin > p[0]) xmin = p[0] - histo.getRange();
				else if (xmax < p[0]) xmax = p[0] + histo.getRange();

				if (ymin > p[1]) ymin = p[1] - histo.getRange();
				else if (ymax < p[1]) ymax = p[1] + histo.getRange();
			}
		}
	}

	public double getXmax()
	{
		return xmax;
	}

	public void setXmax(double xmax)
	{
		this.xmax = xmax;
	}

	public double getXmin()
	{
		return xmin;
	}

	public void setXmin(double xmin)
	{
		this.xmin = xmin;
	}

	public double getYmax()
	{
		return ymax;
	}

	public void setYmax(double ymax)
	{
		this.ymax = ymax;
	}

	public double getYmin()
	{
		return ymin;
	}

	public void setYmin(double ymin)
	{
		this.ymin = ymin;
	}

	public int getBinsPerTick()
	{
		return binsPerTick;
	}

	public void setBinsPerTick(int binsPerTick)
	{
		this.binsPerTick = binsPerTick;
	}

	public double getTickLength()
	{
		return tickLength;
	}

	public void setTickLength(double tickLength)
	{
		this.tickLength = tickLength;
	}

	public int getLightestColorNum()
	{
		return lightestColorNum;
	}

	public void setLightestColorNum(int lightestColorNum)
	{
		this.lightestColorNum = lightestColorNum;
	}

	public int getDarkestColorNum()
	{
		return darkestColorNum;
	}

	public void setDarkestColorNum(int darkestColorNum)
	{
		this.darkestColorNum = darkestColorNum;
	}

	public int getDarkestValue()
	{
		return darkestValue;
	}

	public void setDarkestValue(int darkestValue)
	{
		this.darkestValue = darkestValue;
	}

	public Color getBgColor()
	{
		return bgColor;
	}

	public void setBgColor(Color bgColor)
	{
		this.bgColor = bgColor;
	}

	public Color getAxisColor()
	{
		return axisColor;
	}

	public void setAxisColor(Color axisColor)
	{
		this.axisColor = axisColor;
	}

	public Color getTickColor()
	{
		return tickColor;
	}

	public void setTickColor(Color tickColor)
	{
		this.tickColor = tickColor;
	}

	public void setPoints(java.util.List<double[]> points)
	{
		for (double[] point : points)
		{
			if (Double.isNaN(point[0]) || Double.isNaN(point[1]))
			{
				throw new IllegalArgumentException("A point cannot be NaN on histogram plot.");
			}
		}
		this.points = points;
		ensurePointsInside();
	}

	public void setLines(java.util.List<double[]> lines)
	{
		this.lines = lines;
	}

	public void setPointsColor(Color pointsColor)
	{
		if (pointsColor != null) this.pointsColor = pointsColor;
	}

	public void setLinesColor(Color linesColor)
	{
		if (linesColor != null) this.linesColor = linesColor;
	}

	private int x;
	private int y;

	private double rev_tw(int width)
	{
		return width / width_ratio;
	}

	private double rev_th(int height)
	{
		return height / height_ratio;
	}

	public void mouseDragged(MouseEvent e)
	{
		int dx = e.getX() - x;
		int dy = e.getY() - y;

		xmin -= rev_tw(dx);
		xmax -= rev_tw(dx);
		ymin += rev_th(dy);
		ymax += rev_th(dy);

		x = e.getX();
		y = e.getY();

		repaint();
	}

	public void mouseMoved(MouseEvent e)
	{
	}

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
		x = e.getX();
		y = e.getY();
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	private class GraphPanel extends JPanel
	{
		public void paint(Graphics g)
		{
			ensureRatio();
			if (g instanceof Graphics2D)
			{
				Map m = new HashMap();
				m.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D) g).setRenderingHints(m);
			}
			width_ratio = this.getWidth() / (xmax - xmin);
			height_ratio = this.getHeight() / (ymax - ymin);
			drawBackground(g);
			drawBins(g);
			drawPoints(g);
			drawAxis(g);
			drawLines(g);
		}

		private void drawBins(Graphics g)
		{
			double r = histo.getRange();
			double h_r = r / 2;
			java.util.List<double[]> occupiedPoints = histo.getOccupiedPoints();
			for (double[] p : occupiedPoints)
			{
				Color c = (subset == null) ? getColor(histo.getValue(p[0], p[1])) :
					getColor(histo.getValue(p[0], p[1]), subset.getValue(p[0], p[1]));

				g.setColor(c);
				g.fillRect(tx(p[0] - h_r), ty(p[1] + h_r), tw(r), th(r));
			}
		}

		private Color getColor(int v)
		{
			if (v == 0) return Color.WHITE;

			if (v >= darkestValue)
			{
				return new Color(darkestColorNum, darkestColorNum, darkestColorNum);
			}

			int i = (int) (lightestColorNum - (((lightestColorNum - (double) darkestColorNum) /
				(darkestValue - 1)) * (v - 1)));

			return new Color(i, i, i);
//			return new Color(Color.HSBtoRGB(i / 256.0f, 1f, 1f));
		}

		private Color getColor(int v, int s)
		{
			assert s <= v : "Second histo must be a subset of the first";

			if (v == 0) return Color.WHITE;

			double expectedRat = subset.getTotal() / (double) histo.getTotal();
			double observedRat = (double) s / v;

//			if (observedRat > expectedRat) return Color.RED;
//			if (observedRat < expectedRat) return Color.BLUE;

			int g;
			if (v >= darkestValue)
			{
				g = darkestColorNum;
			}
			else
			{
				g = (int) (lightestColorNum - (((lightestColorNum - (double) darkestColorNum) /
					(darkestValue - 1)) * (v - 1)));
			}

			int r = g, b = g;

			if (observedRat > expectedRat)
			{
				double rat = (observedRat - expectedRat) / (1 - expectedRat);
				r = (int) (((255 - g) * rat) + g);
			}
			else
			{
				double rat = (expectedRat - observedRat) / expectedRat;
				b = (int) (((255 - g) * rat) + g);
			}

			return new Color(r, g, b);
//			return new Color(Color.HSBtoRGB(i / 256.0f, 1f, 1f));
		}

		private void drawBackground(Graphics g)
		{
			super.paint(g);
			g.setColor(bgColor);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, this.getWidth(), this.getHeight());
		}

		private void drawAxis(Graphics g)
		{
			drawTicksOrGrid(g);
			g.setColor(axisColor);
			g.drawLine(tx(0), ty(ymax), tx(0), ty(ymin));
			g.drawLine(tx(xmin), ty(0), tx(xmax), ty(0));
		}

		private void drawTicksOrGrid(Graphics g)
		{
			g.setColor(tickColor);
			double range = histo.getRange();
			double tick_range = range * binsPerTick;

			double min =  Math.ceil(xmin / tick_range) * tick_range;
			double halfTick = (tickLength / 2);
			for (double d = min; d < xmax; d += tick_range)
			{
				d = fixAccuracy(d);
				if (preferGridToTick)
				{
					g.drawLine(tx(d), ty(ymax), tx(d), ty(ymin));
				}
				else
				{
					g.drawLine(tx(d), ty(halfTick), tx(d), ty(-halfTick));
				}

				g.drawString("" + d, tx(d + halfTick), ty(halfTick));
			}

			min =  Math.ceil(ymin / (tick_range)) * tick_range;
			for (double d = min; d < ymax; d += tick_range)
			{
				d = fixAccuracy(d);
				if (preferGridToTick)
				{
					g.drawLine(tx(xmin), ty(d), tx(xmax), ty(d));
				}
				else
				{
					g.drawLine(tx(-halfTick), ty(d), tx(halfTick), ty(d));
				}

				g.drawString("" + d, tx(halfTick), ty(d + halfTick));
			}
		}

		private double fixAccuracy(double d)
		{
			if (d == 0) d = +0;
			d *= 1E6;
			d = Math.round(d);
			d /= 1E6;
			return d;
		}

		private void drawLines(Graphics g)
		{
			if (lines != null)
			{
				g.setColor(linesColor);

				for (double[] param : lines)
				{
					drawLine(g, param[0], param[1]);
				}
			}
		}

		private void drawPoints(Graphics g)
		{
			if (points != null)
			{
				g.setColor(pointsColor);

				for (double[] coor : points)
				{
					drawPoint(g, coor[0], coor[1], pointRadius);
				}
			}
		}

		private void ensureRatio()
		{
			double ratioW = this.getWidth() / ((double) this.getHeight());

			double widthG = xmax - xmin;
			double heightG = ymax - ymin;
			double ratioG = widthG / heightG;

			double eps = 0.00001;
			if (ratioG + eps > ratioW && ratioG - eps < ratioW) return;

			if (ratioW > ratioG)
			{
				double idealWidth = heightG * ratioW;
				double dif = (idealWidth - widthG) / 2;
				xmax += dif;
				xmin -= dif;
			}
			else if (ratioW < ratioG)
			{
				double idealHeight = widthG / ratioW;
				double dif = (idealHeight - heightG) / 2;
				ymax += dif;
				ymin -= dif;
			}
		}

		/**
		 * Transforms x point
		 */
		private int tx(double v)
		{
			return (int) Math.round((v - xmin) * width_ratio);
		}

		/**
		 * Transforms y point
		 */
		private int ty(double v)
		{
			return (int) Math.round((ymax - v) * height_ratio);
		}

		private int tw(double width)
		{
			return (int) Math.ceil(width * width_ratio);
		}

		private int th(double height)
		{
			return (int) Math.ceil(height * height_ratio);
		}

		/**
		 * Draws line y = ax + b
		 * @param a
		 * @param b
		 */
		private void drawLine(Graphics g, double a, double b)
		{
			g.drawLine(tx(xmin), ty((xmin * a) + b), tx(xmax), ty((xmax * a) + b));
		}

		private void drawPoint(Graphics g, double x, double y, double radius)
		{
			g.fillOval(tx(x - radius), ty(y + radius), tw(radius * 2), th(radius * 2));
		}
	}

	public static final double ZOOM_STEP = 0.1;

	public static void main(String[] args)
	{
		Random r = new Random();
		Histogram2D h = new Histogram2D(0.1);
		Histogram2D s = new Histogram2D(0.1);

		for (int i = 0; i < 100000; i++)
		{
			double x = r.nextGaussian();
			double y = r.nextGaussian();
			h.count(x, y);
			if (x + y + r.nextGaussian() > 0) s.count(x, y);

		}

		Histogram2DPlot p = new Histogram2DPlot(h, s);
		p.setVisible(true);
	}
}
