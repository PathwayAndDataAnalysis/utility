package org.panda.utility.statistics;

import org.panda.utility.GUIUtil;
import org.panda.utility.Waiter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ozgun Babur
 */
public class TSNEPlot extends JFrame implements ActionListener
{
	protected JSlider perplexitySlider;
	int perplexity;
	protected JSlider thetaSlider;
	double theta;
	protected JSlider maxIterSlider;
	int maxIter;
	protected JSlider initDimsSlider;
	int initDims;

	JButton makeControlButton;
	JButton makeTestButton;
	JButton printGroupsButton;
	JButton sitchPlotButton;
	JButton copyWindowButton;
	JButton copyPlotButton;
	JButton nextColorButton;
	JButton prevColorButton;

	XYPlot plot;
	Map<String, double[]> data;

	Set<String> controlSet;
	Set<String> testSet;
	Color controlHighlightColor;
	Color testHighlightColor;

	JPanel parametersPanel;
	JPanel buttonsPanel;
	JPanel controlPanel;

	CurrentGraphType graphType;

	Map<String, Map<String, Color>> colorSeriesMap;
	List<String> colorNames;
	int colorIndex;

	public TSNEPlot(Map<String, double[]> data) throws HeadlessException
	{
		graphType = CurrentGraphType.PCA_GRAPH;

		this.data = data;
		perplexity = Math.max(1, (data.size()-1) / 6);
		maxIter = 10000;
		initDims = data.values().iterator().next().length;
		theta = 0.1;

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle("TSNE Plot");

		this.setSize(650, 650);

		this.setLayout(new BorderLayout());

		controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());

		parametersPanel = new JPanel();
		parametersPanel.setLayout(new GridBagLayout());

		GridBagConstraints con = new GridBagConstraints();
		con.fill = GridBagConstraints.BOTH;
		con.insets = new Insets(2, 2, 2, 2);

		con.gridy = 0;

		JLabel perpLabel = new JLabel("Perplexity");
		con.gridx = 0;
		parametersPanel.add(perpLabel, con);

		perplexitySlider = new JSlider(1, (data.size() - 1) / 3, perplexity);
		con.gridx = 1;
		parametersPanel.add(perplexitySlider, con);

		JTextField perplexityField = new JTextField(" " + perplexity);
		perplexityField.setEditable(false);
		con.gridx = 2;
		parametersPanel.add(perplexityField, con);

		con.gridy = 1;

		JLabel thetaLabel = new JLabel("Theta");
		con.gridx = 0;
		parametersPanel.add(thetaLabel, con);

		thetaSlider = new JSlider(0, 10, (int) (theta * 10));
		con.gridx = 1;
		parametersPanel.add(thetaSlider, con);

		JTextField thetaField = new JTextField("" + theta);
		thetaField.setEditable(false);
		con.gridx = 2;
		parametersPanel.add(thetaField, con);

		con.gridy = 2;

		JLabel iterLabel = new JLabel("Max iterations");
		con.gridx = 0;
		parametersPanel.add(iterLabel, con);

		maxIterSlider = new JSlider(1000, 20000, maxIter);
		con.gridx = 1;
		parametersPanel.add(maxIterSlider, con);

		JTextField iterField = new JTextField(maxIter + "");
		iterField.setEditable(false);
		con.gridx = 2;
		parametersPanel.add(iterField, con);

		con.gridy = 3;

		JLabel initDimsLabel = new JLabel("Initial dimensions");
		con.gridx = 0;
		parametersPanel.add(initDimsLabel, con);

		initDimsSlider = new JSlider(2, initDims, initDims);
		con.gridx = 1;
		parametersPanel.add(initDimsSlider, con);

		JTextField initDimsField = new JTextField("" + initDims);
		initDimsField.setEditable(false);
		con.gridx = 2;
		parametersPanel.add(initDimsField, con);

		controlPanel.add(parametersPanel, BorderLayout.CENTER);

		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridBagLayout());

		con.gridx = 0;
		con.gridy = 0;
		makeControlButton = new JButton("Make control");
		makeControlButton.addActionListener(this);
		buttonsPanel.add(makeControlButton, con);
		con.gridy = 1;
		makeTestButton = new JButton("Make test");
		makeTestButton.addActionListener(this);
		buttonsPanel.add(makeTestButton, con);
		con.gridy = 2;
		printGroupsButton = new JButton("Print groups");
		printGroupsButton.addActionListener(this);
		buttonsPanel.add(printGroupsButton, con);
		con.gridy = 3;
		sitchPlotButton = new JButton("Plot TSNE");
		sitchPlotButton.addActionListener(this);
		buttonsPanel.add(sitchPlotButton, con);
		con.gridx = 1;
		con.gridy = 0;
		copyWindowButton = new JButton("Copy window");
		copyWindowButton.addActionListener(this);
		buttonsPanel.add(copyWindowButton, con);
		con.gridy = 1;
		copyPlotButton = new JButton("Copy plot");
		copyPlotButton.addActionListener(this);
		buttonsPanel.add(copyPlotButton, con);
		con.gridy = 2;
		nextColorButton = new JButton();
		nextColorButton.addActionListener(this);
		buttonsPanel.add(nextColorButton, con);
		con.gridy = 3;
		prevColorButton = new JButton();
		prevColorButton.addActionListener(this);
		buttonsPanel.add(prevColorButton, con);

		controlPanel.add(buttonsPanel, BorderLayout.EAST);

		this.getContentPane().add(controlPanel, BorderLayout.NORTH);

//		Map<String, double[]> points = TSNE.run(data, perplexity, theta, maxIter, initDims);
		Map<String, double[]> points = PCA.run(data);
		plot = new XYPlot(points);

		this.getContentPane().add(plot, BorderLayout.CENTER);

		perplexitySlider.addChangeListener(e -> perplexityField.setText("" + perplexitySlider.getValue()));
		thetaSlider.addChangeListener(e -> thetaField.setText("" + thetaSlider.getValue() / 10D));
		maxIterSlider.addChangeListener(e -> iterField.setText("" + maxIterSlider.getValue()));
		initDimsSlider.addChangeListener(e -> initDimsField.setText("" + initDimsSlider.getValue()));

		MouseAdapter listener = new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				perplexity = perplexitySlider.getValue();
				theta = thetaSlider.getValue() / 10D;
				maxIter = maxIterSlider.getValue();
				initDims = initDimsSlider.getValue();

				if (initDims < initDimsSlider.getMaximum() && initDims > data.size())
				{
					initDims = data.size();
					initDimsSlider.setValue(initDims);
				}

				updateGraph();
			}
		};

		perplexitySlider.addMouseListener(listener);
		thetaSlider.addMouseListener(listener);
		maxIterSlider.addMouseListener(listener);
		initDimsSlider.addMouseListener(listener);

		controlSet = new HashSet<>();
		testSet = new HashSet<>();
		controlHighlightColor = new Color(254, 255, 0);
		testHighlightColor = new Color(3, 253, 6);
	}

	public void setColorGorups(Set<String>... colSets)
	{
		int i = 0;
		for (Set<String> colSet : colSets)
		{
			plot.addPointColors(colSet, i++);
		}
	}

	public void setPointColors(Map<String, Color> colorMap)
	{
		plot.addPointColors(colorMap);
	}

	public void setPointColorSeries(Map<String, Map<String, Color>> colorSeriesMap)
	{
		this.colorSeriesMap = colorSeriesMap;
		colorNames = new ArrayList<>(colorSeriesMap.keySet());
		nextColorButton.setText("Next color");
		prevColorButton.setText("Prev color");
		colorIndex = 0;
		updateColors();
		updateColorNameDisplay(colorNames.get(colorIndex));
	}

	private void updateColors()
	{
		plot.addPointColors(colorSeriesMap.get(colorNames.get(colorIndex)));
	}

	private void updateColorNameDisplay(String colorName)
	{
		String title = getTitle();
		String indicator = " -- color: ";
		if (title.contains(indicator))
		{
			title = title.substring(0, title.indexOf(indicator));
		}
		title += indicator + colorName;
		setTitle(title);
	}

	protected void updateGraph()
	{
		Map<String, double[]> points = graphType == CurrentGraphType.TSNE_GRAPH ?
			TSNE.run(data, perplexity, theta, maxIter, initDims) :
			PCA.run(data);

		plot.updatePoints(points);
		plot.repaint();
	}

	public static void plot(String title, Map<String, double[]> data, Set<String>... colGroups)
	{
		TSNEPlot plot = new TSNEPlot(data);
		plot.setTitle(title);
		plot.setColorGorups(colGroups);
		plot.setVisible(true);
		plot.setLocation(new Point(plot.getLocation().x + plot.getWidth(), plot.getLocation().y));

		do
		{
			Waiter.pause(1000);
		}
		while (plot.isVisible());
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == makeControlButton || e.getSource() == makeTestButton)
		{
			Set<String> selected = plot.getSelected();

			if (e.getSource() == makeControlButton)
			{
				if (controlSet.containsAll(selected)) controlSet.removeAll(selected);
				else
				{
					controlSet.addAll(selected);
					testSet.removeAll(selected);
				}
			}
			else if (e.getSource() == makeTestButton)
			{
				if (testSet.containsAll(selected)) testSet.removeAll(selected);
				else
				{
					testSet.addAll(selected);
					controlSet.removeAll(selected);
				}
			}

			Map<Set<String>, Color> map = new HashMap<>();
			map.put(controlSet, controlHighlightColor);
			map.put(testSet, testHighlightColor);
			plot.setHighlightMap(map);
			plot.deselectAll();
			plot.repaint();
		}
		else if (e.getSource() == printGroupsButton)
		{
			for (String name : controlSet)
			{
				System.out.println("control-value-column = " + name);
			}
			for (String name : testSet)
			{
				System.out.println("test-value-column = " + name);
			}
			Set<String> selected = plot.getSelected();
			if (!selected.isEmpty())
			{
				System.out.println("Selected:");
				selected.forEach(System.out::println);
			}
		}
		else if (e.getSource() == sitchPlotButton)
		{
			if (this.graphType == CurrentGraphType.PCA_GRAPH)
			{
				this.graphType = CurrentGraphType.TSNE_GRAPH;
				sitchPlotButton.setText("Show PCA");
			}
			else
			{
				this.graphType = CurrentGraphType.PCA_GRAPH;
				sitchPlotButton.setText("Show TSNE");
			}
			updateGraph();
		}
		else if (e.getSource() == copyWindowButton)
		{
			buttonsPanel.setVisible(false);
			GUIUtil.saveImageToClipboard(this);
			buttonsPanel.setVisible(true);
		}
		else if (e.getSource() == copyPlotButton)
		{
			GUIUtil.saveImageToClipboard(plot);
		}
		else if (e.getSource() == nextColorButton && colorSeriesMap != null)
		{
			colorIndex = (colorIndex + 1) % colorNames.size();
			updateColors();
			updateColorNameDisplay(colorNames.get(colorIndex));
			updateGraph();
		}
		else if (e.getSource() == prevColorButton && colorSeriesMap != null)
		{
			colorIndex = colorIndex == 0 ? colorNames.size() - 1 : colorIndex - 1;
			updateColors();
			updateColorNameDisplay(colorNames.get(colorIndex));
			updateGraph();
		}
	}

	enum CurrentGraphType
	{
		PCA_GRAPH,
		TSNE_GRAPH
	}
}
