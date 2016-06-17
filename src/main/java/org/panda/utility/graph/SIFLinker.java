package org.panda.utility.graph;

import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class SIFLinker
{
	protected Map<String, Map<String, Set<String>>> sif;
	public Graph graph;
	
	public boolean load(String filename)
	{
		try
		{
			return load(new FileInputStream(filename), "controls-state-change-of", "controls-expression-of");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean load(Graph g)
	{
		if (graph == null) graph = new Graph();
		graph.merge(g);

		if (sif == null) sif = new HashMap<>();

		for (String up : g.getSymbols())
		{
			for (String dw : g.getDownstream(up))
			{
				if (!sif.containsKey(up))
					sif.put(up, new HashMap<>());

				if (!sif.get(up).containsKey(g.getEdgeType()))
					sif.get(up).put(g.getEdgeType(), new HashSet<>());

				sif.get(up).get(g.getEdgeType()).add(dw);
			}
		}
		return true;
	}

	public boolean load(InputStream is, String... directedTypes)
	{
		try
		{
			byte[] content = getBytes(is);

			if (graph == null) graph = new Graph();
			graph.load(new ByteArrayInputStream(content), new HashSet<String>(),
				new HashSet<String>(Arrays.asList(directedTypes)));

			if (sif == null) sif = new HashMap<String, Map<String, Set<String>>>();
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(content)));
	
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				
				if (!sif.containsKey(token[0])) 
					sif.put(token[0], new HashMap<String, Set<String>>());
				
				if (!sif.get(token[0]).containsKey(token[1]))
					sif.get(token[0]).put(token[1], new HashSet<String>());
				
				sif.get(token[0]).get(token[1]).add(token[2]);
			}
	
			reader.close();
		}
		catch (IOException e) { e.printStackTrace(); return false; } return true;
	}

	private byte[] getBytes(InputStream is) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n;
		while ((n = is.read(buf)) >= 0) baos.write(buf, 0, n);
		return baos.toByteArray();
	}

	public List<String> linkMinimal(Set<String> set, int limit)
	{
		List<String> rels = new ArrayList<String>();
		Set<String> linked = new HashSet<String>();

		for (int i = 0; i <= limit && linked.size() < set.size(); i++)
		{
			for (String s1 : set)
			{
				for (String s2 : set)
				{
					if (s1.equals(s2)) continue;
					if (linked.contains(s1) && linked.contains(s2)) continue;

					List<String> rel = link(Collections.singleton(s1), Collections.singleton(s2), i);
					
					if (!rel.isEmpty())
					{
						rels.addAll(rel);
						linked.add(s1);
						linked.add(s2);
					}
				}
			}
		}
		return rels;
	}
	
	public List<String> linkProgressive(Set<String> from, Set<String> to, int limit)
	{
		List<String> rels = new ArrayList<String>();

		for (String s1 : from)
		{
			for (String s2 : to)
			{
				for (int i = 0; i <= limit; i++)
				{
					if (s1.equals(s2)) continue;

					List<String> rel = link(Collections.singleton(s1), Collections.singleton(s2), i);

					if (!rel.isEmpty())
					{
						for (String s : rel) if (!rels.contains(s)) rels.add(s);
						break;
					}
				}
			}
		}
		return rels;
	}

	public List<String> link(Set<String> from, Set<String> to, int limit)
	{
		List<String> rels = new ArrayList<String>();

		for (String s : from)
		{
			Set<String> eles = graph.getPathElements(s, to, limit);
			eles.add(s);
			for (String ele : eles)
			{
				if (sif.containsKey(ele))
				for (String type : sif.get(ele).keySet())
				{
					for (String tar : sif.get(ele).get(type))
					{
						if (eles.contains(tar) && !tar.equals(ele))
						{
							String rel = ele + "\t" + type + "\t" + tar;
							if (!rels.contains(rel)) rels.add(rel);
						}
					}
				}
			}
		}
		return rels;
	}

	public List<String> linkCommonDownstream(Set<String> seed, int limit)
	{
		List<String> rels = new ArrayList<String>();

		List<Graph.CommPoint> points = graph.getCommonDownstream(seed, limit);
		
		Set<Graph.CommPoint> select = new HashSet<Graph.CommPoint>();
		Set<String> covered = new HashSet<String>();

//		while(covered.size() < seed.size())
		{
			for (Graph.CommPoint p : points)
			{
//				if (!covered.containsAll(p.upstr))
				{
					select.add(p);
					covered.addAll(p.upstr);
				}
			}
		}


		for (Graph.CommPoint p : select)
		{
			List<String> link = linkProgressive(p.upstr, Collections.singleton(p.s), p.dist);
			for (String l : link)
			{
				if (!rels.contains(l)) rels.add(l);
			}
		}

		return rels;
	}
	
	public static void main(String[] args)
	{
		SIFLinker linker = new SIFLinker();
		linker.load("/home/ozgun/Desktop/SIF.txt");
//		linker.load("C:/Users/ozgun/Downloads/SIF.txt");
		HashSet<String> set = new HashSet<String>(Arrays.asList("IFNA2  TP53".split("  ")));
		List<String> rels = linker.link(Collections.singleton("IFNA2"), Collections.singleton("TP53"), 3);
		for (String rel : rels)
		{
//			System.out.println(rel.replaceAll("STATE_CHANGE","-->").replaceAll("BINDS_TO","---").replaceAll("TRANSCRIPTION","-t>").replaceAll("DEGRADATION","-d>"));
			System.out.println(rel);
		}
	}
}
