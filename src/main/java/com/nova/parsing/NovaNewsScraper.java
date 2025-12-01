package com.nova.parsing;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class NovaNewsScraper {
	public static Map<String,String> fetch() {
		Map<String,String> m = new HashMap<>();
		m.put("url","https://portfolio-news-mock.vercel.app/");
		m.put("headline",""); m.put("content",""); m.put("date","");

		try {
			Document d = Jsoup.connect(m.get("url"))
					.userAgent("Mozilla/5.0")
					.timeout(10000)
					.get();

			Element h3 = d.selectFirst("body > main > section > div > article:nth-child(1) > a > h3");
			Element dateP = d.selectFirst("body > main > section > div > article:nth-child(1) > p:nth-child(2)");
			Element publisherP = d.selectFirst("body > main > section > div > article:nth-child(1) > p:nth-child(3)");
			Element metaP = d.selectFirst("body > main > section > div > article:nth-child(1) > p:nth-child(4)");
			Element excerptP = d.selectFirst("body > main > section > div > article:nth-child(1) > p.excerpt");

			if (h3 != null) m.put("headline", h3.text().trim());

			if (dateP != null) {
				String raw = dateP.text().trim();
				int i = raw.toLowerCase().indexOf("date:");
				m.put("date", i >= 0 ? raw.substring(i + "date:".length()).trim() : raw);
			}

			if (publisherP != null) {
				String raw = publisherP.text().trim();
				int i = raw.toLowerCase().indexOf("publisher:");
				m.put("publisher", i >= 0 ? raw.substring(i + "publisher:".length()).trim() : raw);
			}

			if (metaP != null) {
				String raw = metaP.text().trim();
				putIfFound(m, "editors", raw, "Editors:");
				putIfFound(m, "tickers", raw, "Tickers:");
				putIfFound(m, "sentiment", raw, "Sentiment:");
			}

			StringBuilder content = new StringBuilder();
			if (excerptP != null) {
				String ex = excerptP.text().trim();
				if (!ex.isEmpty()) content.append(ex);
			}
			if (metaP != null) {
				String t = metaP.text().trim();
				if (!t.isEmpty()) {
					if (content.length() > 0) content.append("\n\n");
					content.append(t);
				}
			}
			if (content.length() > 0) m.put("content", content.toString());
		} catch (Exception ignored) {}
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
