package org.panda.utility;

public class SeqAlign
{
	private String s1;
	private String s2;

	private String as1;
	private String as2;

	private int[][] matrix;
	private Direction[][] arrow;

	private int matchScore = 1;
	private int mismatchPenalty = -1;
	private int gapPenalty = -1;

	private char GAP = '-';

	private int[] map1;
	private int[] map2;

	private boolean fragmentSearch = false;

	public SeqAlign(String s1, String s2)
	{
		this.s1 = s1;
		this.s2 = s2;

		this.setFragmentSearch(s1.length() > (2 * s2.length()));
	}

	public void setFragmentSearch(boolean fragmentSearch)
	{
		this.fragmentSearch = fragmentSearch;
	}

	public void align()
	{
		matrix = new int[s1.length()+1][s2.length()+ 1];
		arrow = new Direction[s1.length()+1][s2.length() + 1];

		matrix[0][0] = 0;
		for (int i = 1; i <= s1.length(); i++)
		{
			matrix[i][0] = matrix[i-1][0] + (fragmentSearch ? 0 : gapPenalty);
			arrow[i][0] = Direction.VERT;
		}
		for (int j = 1; j <= s2.length(); j++)
		{
			matrix[0][j] = matrix[0][j-1] + gapPenalty;
			arrow[0][j] = Direction.HORI;
		}

		for (int i = 1; i <= s1.length(); i++)
		{
			for (int j = 1; j <= s2.length(); j++)
			{
				boolean match = s1.charAt(i-1) == s2.charAt(j-1);
				boolean bothGaps = s1.charAt(i-1) == GAP && s2.charAt(j-1) == GAP;
				int diagScore = matrix[i-1][j-1] + (bothGaps ? 0 : match ? matchScore : mismatchPenalty);
				int vertScore = matrix[i-1][j] + (s1.charAt(i-1) == GAP || (j == s2.length() && fragmentSearch) ? 0 : gapPenalty);
				int horiScore = matrix[i][j-1] + (s2.charAt(j-1) == GAP ? 0 : gapPenalty);

				if (diagScore >= vertScore && diagScore >= horiScore)
				{
					matrix[i][j] = diagScore;
					arrow[i][j] = Direction.DIAG;
				}
				else if (vertScore >= horiScore)
				{
					matrix[i][j] = vertScore;
					arrow[i][j] = Direction.VERT;
				}
				else
				{
					matrix[i][j] = horiScore;
					arrow[i][j] = Direction.HORI;
				}
			}
		}

		String[] ss = getAlignedStrings();
		as1 = ss[0];
		as2 = ss[1];

		map1 = new int[s1.length()];
		map2 = new int[s2.length()];

		int i = -1;
		int j = -1;
		for (int k = 0; k < as1.length(); k++)
		{
			boolean b1 = s1.length() > i+1 && as1.charAt(k) == s1.charAt(i+1);
			boolean b2 = s2.length() > j+1 && as2.charAt(k) == s2.charAt(j+1);
			if (b1) ++i;
			if (b2) ++j;
			if (b1 && b2)
			{
				map1[i] = j;
				map2[j] = i;
			}
			else if (b1) map1[i] = -1;
			else if (b2) map2[j] = -1;
		}
	}

	public String[] getAlignedStrings()
	{
		StringBuilder as1 = new StringBuilder();
		StringBuilder as2 = new StringBuilder();

		int i = s1.length();
		int j = s2.length();

		while (i > 0 || j > 0)
		{
			switch (arrow[i][j])
			{
				case DIAG:
					as1.insert(0, s1.charAt(--i));
					as2.insert(0, s2.charAt(--j));
					break;
				case VERT:
					as1.insert(0, s1.charAt(--i));
					as2.insert(0, GAP);
					break;
				case HORI:
					as1.insert(0, GAP);
					as2.insert(0, s2.charAt(--j));
					break;
			}
		}
		return new String[]{as1.toString(), as2.toString()};
	}

	public int getMappedIndexS1ToS2(int ind)
	{
		return map1[ind];
	}

	public int getMappedIndexS2ToS1(int ind)
	{
		if (ind >= map2.length) return -1;
		return map2[ind];
	}

	public double getS2MatchScore()
	{
		double sum = 0;
		int cnt = 0;
		for (int i = 0; i < map2.length; i++)
		{
			if (s2.charAt(i) != GAP)
			{
				cnt++;
				if (map2[i] >= 0 && s1.charAt(map2[i]) == s2.charAt(i)) sum++;
			}
		}
		return sum / cnt;
	}

	public void print()
	{
		System.out.println(as1);
		System.out.println(as2);
	}

	enum Direction
	{
		VERT,
		HORI,
		DIAG;
	}

	public static void main(String[] args)
	{
		String s1 = "MEEPQSDPSVEPPLSQETFSDLWKLLPENNVLSPLPSQAMDDLMLSPDDIEQWFTEDPGPDEAPRMPEAAPPVAPAPAAPTPAAPAPAPSWPLSSSVPSQKTYQGSYGFRLGFLHSGTAKSVTCTYSPALNKMFCQLAKTCPVQLWVDSTPPPGTRVRAMAIYKQSQHMTEVVRRCPHHERCSDSDGLAPPQHLIRVEGNLRVEYLDDRNTFRHSVVVPYEPPEVGSDCTTIHYNYMCNSSCMGGMNRRPILTIITLEDSSGNLLGRNSFEVRVCACPGRDRRTEEENLRKKGEPHHELPPGSTKRALPNNTSSSPQPKKKPLDGEYFTLQIRGRERFEMFRELNEALELKDAQAGKEPGGSRAHSSHLKSKKGQSTSRHKKLMFKTEGPDSD";
		String s2 = "APPVAPA-----------------PAAPTP";

		SeqAlign sa = new SeqAlign(s1, s2);
		sa.align();
		sa.print();
	}
}
