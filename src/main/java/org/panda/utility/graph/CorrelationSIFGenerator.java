package org.panda.utility.graph;

import org.panda.utility.ValToColor;
import org.panda.utility.statistics.Correlation;

import java.awt.*;
import java.util.Map;

/**
 * This class takes genes with value arrays, and produces a SIF graph where each edge means correlation.
 *
 * @author Ozgun Babur
 */
public class CorrelationSIFGenerator extends SIFGenerator
{
	/**
	 * Genes and corresponding values.
	 */
	private Map<String, double[]> geneMap;

	/**
	 * Minimum absolute value of correlation to generate an edge
	 */
	private double minCorrelation;

	/**
	 * Edge color for the maximum positive correlation
	 */
	private Color maxPosCorrColor;

	/**
	 * Edge color for the maximum negative correlation
	 */
	private Color maxNegCorrColor;

	/**
	 * The absolute value of the correlation that will have the maximum color.
	 */
	private double corrMaxOut;

	public static final String EDGE_TYPE = "correlates-with";

	public CorrelationSIFGenerator(Map<String, double[]> geneMap)
	{
		this.geneMap = geneMap;
		minCorrelation = 0.3;
		maxPosCorrColor = Color.BLUE;
		maxNegCorrColor = Color.RED;
		corrMaxOut = 1;
	}

	protected void prepare()
	{
		setDefaultEdgeWidth(2);

		ValToColor vtc = new ValToColor(
			new double[]{-corrMaxOut,     0,           corrMaxOut},
			new Color[]{ maxNegCorrColor, Color.WHITE, maxPosCorrColor});

		geneMap.keySet().forEach(g1 -> geneMap.keySet().stream().filter(g2 -> g1.compareTo(g2) < 0).forEach(g2 ->
		{
			double corr = Correlation.pearsonVal(geneMap.get(g1), geneMap.get(g2));
			if (Math.abs(corr) >= minCorrelation)
			{
				addEdge(g1, g2, EDGE_TYPE);
				addEdgeColor(g1, g2, EDGE_TYPE, vtc.getColor(corr));
			}
		}));
	}

	public void setMinCorrelation(double minCorrelation)
	{
		this.minCorrelation = Math.abs(minCorrelation);
	}

	public void setMaxPosCorrColor(Color maxPosCorrColor)
	{
		this.maxPosCorrColor = maxPosCorrColor;
	}

	public void setCorrMaxOut(double corrMaxOut)
	{
		this.corrMaxOut = Math.abs(corrMaxOut);
	}

	public void setMaxNegCorrColor(Color maxNegCorrColor)
	{
		this.maxNegCorrColor = maxNegCorrColor;
		System.out.println("Add");

	}
}
