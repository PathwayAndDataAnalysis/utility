package org.panda.utility.statistics;

import org.panda.utility.ArrayUtil;
import org.panda.utility.GUIUtil;
import org.panda.utility.Waiter;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ozgun Babur
 */
public class KernelDensityPlot extends JFrame
{
	KernelDensityEstimation kde;
	protected JSlider deltaSlider;
	FunctionPlot fp;

	public KernelDensityPlot(KernelDensityEstimation kde) throws HeadlessException
	{
		this.kde = kde;

		double[] vals = kde.getVals();
		double maxX = Summary.max(vals);
		double minX = Summary.min(vals);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle("Kernel Density Estimation Plot");

		this.setSize(600, 600);

		this.setLayout(new BorderLayout());
		fp = new FunctionPlot(kde, minX, maxX);
		this.getContentPane().add(fp, BorderLayout.CENTER);

		JPanel cPanel = new JPanel();
		cPanel.setLayout(new BorderLayout());

		JLabel bandwithLabel = new JLabel("Bandwidth");
		cPanel.add(bandwithLabel, BorderLayout.WEST);

		deltaSlider = new JSlider(JSlider.HORIZONTAL, -10, 10, 0);
		deltaSlider.addChangeListener(e ->
		{
			int value = deltaSlider.getValue();
			double factor = value == 0 ? 1 : value < 0 ? 1D / (-(value/2D) + 1) : (value/2D) + 1;
			kde.setDelta(kde.initDeltaToDefault() * factor);
			repaint();
		});

		cPanel.add(deltaSlider, BorderLayout.CENTER);

		JButton copyButton = new JButton("Copy");
		copyButton.addActionListener(e -> GUIUtil.saveImageToClipboard(fp));
		cPanel.add(copyButton, BorderLayout.EAST);

		this.add(cPanel, BorderLayout.NORTH);
	}

	public void label(String name, double xLoc)
	{
		fp.addXMark(name, xLoc);
	}

	public static void plot(String title, double[] vals)
	{
		Map<String, double[]> map = new HashMap<>();
		map.put("", vals);
		plot(title, map);
	}

	public static void plot(String title, Map<String, double[]> valMap)
	{
		List<Double> list = new ArrayList<>();
		for (double[] vals : valMap.values())
		{
			for (double val : vals)
			{
				list.add(val);
			}
		}
		double[] vals = ArrayUtil.convertToBasicDoubleArray(list);
		KernelDensityEstimation kde = new KernelDensityEstimation(vals);
		KernelDensityPlot kdp = new KernelDensityPlot(kde);
		if (title != null) kdp.setTitle(title);
		for (String name : valMap.keySet())
		{
			kdp.fp.addValsToPlace(name, valMap.get(name));
		}
		kdp.setVisible(true);
		kdp.setLocation(new java.awt.Point(kdp.getLocation().x + kdp.getWidth(), kdp.getLocation().y));

		do
		{
			Waiter.pause(1000);
		}
		while (kdp.isVisible());
	}
}
