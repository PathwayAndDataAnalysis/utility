package org.panda.utility.graph;

import org.panda.utility.ArrayUtil;
import org.panda.utility.FileUtil;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for preparing a SIF graph to visualize using ChiBE.
 *
 * @author Ozgun Babur
 */
public class SIFGenerator
{
	/**
	 * Background colors of nodes
	 */
	private Map<String, Color> nodeColorMap;

	/**
	 * Border colors of nodes
	 */
	private Map<String, Color> nodeBorderColorMap;

	/**
	 * Tooltip text of nodes
	 */
	private Map<String, String> nodeTooltipMap;

	/**
	 * Edges
	 */
	private Set<Edge> edges;

	/**
	 * Nodes, especially if they are disconnected
	 */
	private Set<String> nodes;

	/**
	 * Edge colors
	 */
	private Map<Edge, Color> edgeColorMap;

	/**
	 * Information boxes of nodes
	 */
	private Map<String, Set<InfoBox>> infoMap;

	/**
	 * The file name without .sif or .format extensions
	 */
	private String filenameWithoutExtension;

	/**
	 * Optional conversion map if some of hte node names are desired to be different than the provided in the final sif
	 * graph. Can be partial.
	 */
	private Map<String, String> nodeNameConversionMap;

	private int defaultEdgeWidth;
	private int defaultNodeBorderWidth;
	private Color defaultNodeColor;
	private Color defaultNodeBorderColor;
	private Color defaultTextColor;

	private Color defaultEdgeColor;

	public SIFGenerator()
	{
		defaultEdgeWidth = 1;
		defaultNodeBorderWidth = 1;
		defaultNodeColor = Color.WHITE;
		defaultNodeBorderColor = Color.BLACK;
		defaultTextColor = Color.BLACK;
	}

	public void write(String fileNameWithoutExtension)
	{
		this.filenameWithoutExtension = fileNameWithoutExtension;
		prepare();

		if (edges == null) return;

		try
		{
			BufferedWriter writer1 = Files.newBufferedWriter(Paths.get(fileNameWithoutExtension + ".sif"));
			edges.forEach(e -> FileUtil.writeln(e.toString(), writer1));
			Set<String> nodesInEdges = edges.stream().map(e -> new String[]{e.source, e.target}).flatMap(Arrays::stream)
				.collect(Collectors.toSet());
			if (nodes != null) nodes.stream().filter(n -> !nodesInEdges.contains(n)).forEach(n ->
				FileUtil.writeln(convert(n), writer1));
			writer1.close();

			BufferedWriter writer2 = Files.newBufferedWriter(Paths.get(fileNameWithoutExtension + ".format"));
			writer2.write("node\tall-nodes\tcolor\t" + colorString(defaultNodeColor) + "\n");
			writer2.write("node\tall-nodes\tbordercolor\t" + colorString(defaultNodeBorderColor) + "\n");
			writer2.write("node\tall-nodes\ttextcolor\t" + colorString(defaultTextColor) + "\n");
			writer2.write("node\tall-nodes\tborderwidth\t" + defaultNodeBorderWidth + "\n");
			if (defaultEdgeColor != null) writer2.write("edge\tall-edges\tcolor\t" + colorString(defaultEdgeColor) + "\n");
			writer2.write("edge\tall-edges\twidth\t" + defaultEdgeWidth + "\n");

			if (nodeColorMap != null)
			{
				nodeColorMap.keySet().forEach(n ->
					FileUtil.writeln("node\t" + convert(n) + "\tcolor\t" + colorString(nodeColorMap.get(n)), writer2));
			}

			if (nodeBorderColorMap != null)
			{
				nodeBorderColorMap.keySet().forEach(n ->
					FileUtil.writeln("node\t" + convert(n) + "\tbordercolor\t" + colorString(nodeBorderColorMap.get(n)),
						writer2));
			}

			if (nodeTooltipMap != null)
			{
				nodeTooltipMap.keySet().forEach(n ->
					FileUtil.writeln("node\t" + convert(n) + "\ttooltip\t" + nodeTooltipMap.get(n), writer2));
			}

			if (edgeColorMap != null)
			{
				edgeColorMap.keySet().forEach(e ->
					FileUtil.writeln("edge\t" + ArrayUtil.getString(" ", convert(e.source), e.type, convert(e.target)) +
						"\tcolor\t" + colorString(edgeColorMap.get(e)), writer2));
			}

			if (infoMap != null)
			{
				infoMap.keySet().forEach(n ->
					infoMap.get(n).forEach(b ->
						FileUtil.writeln("node\t" + convert(n) + "\trppasite\t" + b.toString(), writer2)));
			}

			writer2.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private String convert(String molecule)
	{
		if (nodeNameConversionMap == null || !nodeNameConversionMap.containsKey(molecule)) return molecule;
		return nodeNameConversionMap.get(molecule);
	}

	/**
	 * Subclasses can overwrite this method when they want to configure the graph before writing to a file.
	 */
	protected void prepare()
	{
	}

	public void addNodeColor(String node, Color background)
	{
		if (nodeColorMap == null) nodeColorMap = new HashMap<>();
		nodeColorMap.put(node, background);
	}

	public boolean hasNodeColor(String node)
	{
		return nodeColorMap != null && nodeColorMap.containsKey(node);
	}

	public void addNodeBorderColor(String node, Color border)
	{
		if (nodeBorderColorMap == null) nodeBorderColorMap = new HashMap<>();
		nodeBorderColorMap.put(node, border);
	}

	public void addNodeTooltip(String node, String tooltip)
	{
		if (nodeTooltipMap == null) nodeTooltipMap = new HashMap<>();
		nodeTooltipMap.put(node, tooltip);
	}

	/**
	 * This method is for adding disconnected nodes. Connected nodes are automatically included when the edge is added.
	 */
	public void addNode(String node)
	{
		if (nodes == null) nodes = new HashSet<>();
		nodes.add(node);
	}

	public void addEdge(String source, String target, String type)
	{
		addEdge(source, target, type, null);
	}
	public void addEdge(String source, String target, String type, String mediators)
	{
		if (edges == null) edges = new HashSet<>();
		edges.add(new Edge(source, target, type, mediators));
	}

	public void addEdgeColor(String source, String target, String type, Color c)
	{
		if (edgeColorMap == null) edgeColorMap = new HashMap<>();
		edgeColorMap.put(new Edge(source, target, type, null), c);
	}

	public void addInfoBox(String node, String letter, String tooltip, Color background, Color border)
	{
		if (infoMap == null) infoMap = new HashMap<>();
		if (!infoMap.containsKey(node)) infoMap.put(node, new HashSet<>());
		infoMap.get(node).add(new InfoBox(letter, tooltip, background, border));
	}

	public void setDefaultEdgeWidth(int defaultEdgeWidth)
	{
		this.defaultEdgeWidth = defaultEdgeWidth;
	}

	public void setDefaultNodeBorderWidth(int defaultNodeBorderWidth)
	{
		this.defaultNodeBorderWidth = defaultNodeBorderWidth;
	}

	public void setDefaultNodeColor(Color defaultNodeColor)
	{
		this.defaultNodeColor = defaultNodeColor;
	}

	public void setDefaultNodeBorderColor(Color defaultNodeBorderColor)
	{
		this.defaultNodeBorderColor = defaultNodeBorderColor;
	}

	public void setDefaultTextColor(Color defaultTextColor)
	{
		this.defaultTextColor = defaultTextColor;
	}

	public void setDefaultEdgeColor(Color defaultEdgeColor)
	{
		this.defaultEdgeColor = defaultEdgeColor;
	}

	public String colorString(Color c)
	{
		return c.getRed() + " " + c.getGreen() + " " + c.getBlue();
	}

	public void setNodeNameConversionMap(Map<String, String> nodeNameConversionMap)
	{
		this.nodeNameConversionMap = nodeNameConversionMap;
	}

	public String getFilenameWithoutExtension()
	{
		return filenameWithoutExtension;
	}

	class Edge
	{
		String source;
		String target;
		String type;
		String mediators;

		public Edge(String source, String target, String type, String mediators)
		{
			this.source = source;
			this.target = target;
			this.type = type;
			this.mediators = mediators;
		}

		@Override
		public int hashCode()
		{
			return source.hashCode() + target.hashCode() + type.hashCode();
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof Edge &&
				((Edge) o).source.equals(source) && ((Edge) o).target.equals(target) && ((Edge) o).type.equals(type);
		}

		@Override
		public String toString()
		{
			String s = convert(source) + "\t" + type + "\t" + convert(target);
			if (mediators != null) s += "\t" + mediators;
			return s;
		}
	}

	class InfoBox
	{
		String letter;
		String tooltip;
		Color background;
		Color border;

		public InfoBox(String letter, String tooltip, Color background, Color border)
		{
			this.letter = letter;
			this.tooltip = tooltip;
			this.background = background;
			this.border = border;
		}

		@Override
		public String toString()
		{
			return ArrayUtil.getString("|", tooltip, letter, colorString(background), colorString(border));
		}
	}
}
