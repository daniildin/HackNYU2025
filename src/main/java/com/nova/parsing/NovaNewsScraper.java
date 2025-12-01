package com.nova.parsing;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class NovaNewsScraper {
	@SuppressWarnings("UseSpecificCatch")
	public static Map<String,String> fetch() {
		Map<String,String> m = new HashMap<>();
		m.put("url","https://portfolio-news-mock.vercel.app/");
		m.put("headline",""); m.put("content",""); m.put("date","");

		try {
			Document d = Jsoup.connect(m.get("url"))
					.userAgent("Mozilla/5.0")
					.timeout(10000)
					.get();

			// First article block
			Element h3 = d.selectFirst("body > main > section > div > article:nth-child(1) > a > h3");
			Element dateP = d.selectFirst("body > main > section > div > article:nth-child(1) > p:nth-child(2)");
			Element publisherP = d.selectFirst("body > main > section > div > article:nth-child(1) > p:nth-child(3)");
			Element metaP = d.selectFirst("body > main > section > div > article:nth-child(1) > p:nth-child(4)");
			Element excerptP = d.selectFirst("body > main > section > div > article:nth-child(1) > p.excerpt");

			// headline
			if (h3 != null) m.put("headline", h3.text().trim());

			// date (assumes "Date: ..." format)
			if (dateP != null) {
				String raw = dateP.text().trim();
				int i = raw.toLowerCase().indexOf("date:");
				if (i >= 0) {
					String v = raw.substring(i + "date:".length()).trim();
					m.put("date", v);
				} else {
					m.put("date", raw);
				}
			}

			// publisher
			if (publisherP != null) {
				String raw = publisherP.text().trim();
				int i = raw.toLowerCase().indexOf("publisher:");
				if (i >= 0) {
					String v = raw.substring(i + "publisher:".length()).trim();
					m.put("publisher", v);
				} else {
					m.put("publisher", raw);
				}
			}

			// meta: Editors, Tickers, Sentiment (single line)
			if (metaP != null) {
				String raw = metaP.text().trim();
				putIfFound(m, "editors", raw, "Editors:");
				putIfFound(m, "tickers", raw, "Tickers:");
				putIfFound(m, "sentiment", raw, "Sentiment:");
			}

			// content: excerpt + any additional text lines from date/publisher/meta (excluding labels)
			StringBuilder content = new StringBuilder();
			if (excerptP != null) {
				String ex = excerptP.text().trim();
				if (!ex.isEmpty()) content.append(ex);
			}
			// append any trailing descriptive text inside meta paragraph that isn't a simple label
			if (metaP != null) {
				String t = metaP.text().trim();
				if (!t.isEmpty()) {
					if (content.length() > 0) content.append("\n\n");
					content.append(t);
				}
			}
			if (content.length() > 0) m.put("content", content.toString());

			System.out.println("[NovaNews] url=" + m.get("url")
					+ " headlineChars=" + (m.getOrDefault("headline","").length())
					+ " contentChars=" + (m.getOrDefault("content","").length())
					+ " date=" + m.getOrDefault("date",""));
		} catch (Exception e) {
			System.err.println("[NovaNews] fetch failed: " + e.getMessage());
		}
		return m;
	}

	private static void putIfFound(Map<String,String> m, String key, String raw, String label) {
		int i = raw.indexOf(label);
		if (i < 0) return;
		int start = i + label.length();
		int nextSep = raw.indexOf("·", start);
		String val = (nextSep >= 0 ? raw.substring(start, nextSep) : raw.substring(start)).trim();
		if (!val.isEmpty()) m.put(key, val);
	}
}
