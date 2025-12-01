package com.nova.logic;

public class PortfolioEffect {
	public static String analyze(String newsTitle, String publisher, String[] writers, String newsContent, String portfolio, String apiKey) {
		String title = newsTitle == null ? "" : newsTitle;
		String content = newsContent == null ? "" : newsContent;
		String all = (title + " " + content).toLowerCase();

		// tiny lexicon → base sentiment in [0,1]
		String[] posWords = {"up","gain","gains","rise","rose","surge","beat","beats","strong","growth","profit","record","upgrade"};
		String[] negWords = {"down","loss","losses","drop","fell","miss","missed","weak","decline","slump","downgrade"};
		int pos=0, neg=0;
		for (String w : all.split("\\s+")) {
			for (String pw : posWords) if (w.equals(pw)) pos++;
			for (String nw : negWords) if (w.equals(nw)) neg++;
		}
		double base = 0.5;
		int total = pos + neg;
		if (total > 0) base = 0.5 + 0.5 * ((double)(pos - neg) / total);
		if (base < 0) base = 0;
		if (base > 1) base = 1;

		StringBuilder out = new StringBuilder();
		out.append("{");
		if (portfolio != null && !portfolio.isBlank()) {
			String[] toks = portfolio.split(",");
			boolean first = true;
			for (String raw : toks) {
				String t = raw.trim().toUpperCase();
				if (t.isEmpty()) continue;
				boolean present = all.contains(t.toLowerCase()) || all.contains("$" + t.toLowerCase());
				double s = present ? base : 0.5 + (base - 0.5) * 0.5; // pull toward neutral if not mentioned
				// round to 2 decimals
				double rounded = Math.round(s * 100.0) / 100.0;
				if (!first) out.append(",");
				out.append("\"").append(t).append("\":").append(rounded);
				first = false;
			}
		}
		out.append("}");
		return out.toString();
	}
}
