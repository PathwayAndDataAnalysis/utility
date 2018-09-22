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
	Map<KernelDensityEstimation, String> kdeMap;
	protected JSlider deltaSlider;
	FunctionPlot fp;

	public KernelDensityPlot(KernelDensityEstimation kde) throws HeadlessException
	{
		this.kdeMap = new HashMap<>();
		kdeMap.put(kde, null);
		init();
	}

	public KernelDensityPlot(Map<KernelDensityEstimation, String> kdeMap) throws HeadlessException
	{
		this.kdeMap = kdeMap;
		init();
	}

	private void init()
	{
		double maxX = -Double.MAX_VALUE;
		double minX = Double.MAX_VALUE;
		for (KernelDensityEstimation kde : kdeMap.keySet())
		{
			double[] vals = kde.getVals();
			maxX = Math.max(Summary.max(vals), maxX);
			minX = Math.min(Summary.min(vals), minX);
		}

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle("Kernel Density Estimation Plot");

		this.setSize(600, 600);

		this.setLayout(new BorderLayout());
		fp = new FunctionPlot(new LinkedHashMap<>(kdeMap), minX, maxX);
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
			kdeMap.keySet().forEach(kde -> kde.setDelta(kde.initDeltaToDefault() * factor));
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

	public static void plotMap(String title, Map<KernelDensityEstimation, String> kdeMap)
	{
		KernelDensityPlot kdp = new KernelDensityPlot(kdeMap);
		if (title != null) kdp.setTitle(title);
		kdp.setSize(300, 300);
		kdp.setVisible(true);
		kdp.setLocation(new java.awt.Point(kdp.getLocation().x + kdp.getWidth(), kdp.getLocation().y));

		do
		{
			Waiter.pause(1000);
		}
		while (kdp.isVisible());
	}
}
