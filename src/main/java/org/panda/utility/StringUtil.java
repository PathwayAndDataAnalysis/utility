package org.panda.utility;

/**
 * Created by babur on 4/13/16.
 */
public class StringUtil
{
	public static String fetch(String from, String detect, String leftBound, String rightBound)
	{
		int m = from.indexOf(detect);
		if (m < 0) return null;
		String s = from.substring(0, m);
		int l = s.lastIndexOf(leftBound);
		if (l < 0) return null;
		s = from.substring(m);
		int r = s.indexOf(rightBound);
		if (r < 0) return null;
		r += m;

		return from.substring(l + leftBound.length(), r);
	}

	public static void main(String[] args)
	{
		String s = "<tr><td valign=\"top\"><img src=\"/icons/compressed.gif\" alt=\"[   ]\"></td><td><a href=\"gdac.broadinstitute.org_SKCM-TM.MutSigNozzleReport2CV.Level_4.2015082100.0.0.tar.gz\">gdac.broadinstitute.org_SKCM-";
		System.out.println(fetch(s, ".MutSigNozzleReport2CV.Level_4.", "\"", "\""));
	}
}
