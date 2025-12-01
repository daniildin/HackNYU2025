package com.nova.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public class NovaNewsScraper {
	public static Map<String,String> fetch() {
		Map<String,String> m = new HashMap<>();
		m.put("headline","Sample NovaNews Headline");
		m.put("content","Sample NovaNews content.");
		try {
			Document d = Jsoup.connect("https://portfolio-news-mock.vercel.app/article.html")
					.userAgent("Mozilla/5.0")
					.timeout(8000)
					.get();
			Element h = d.selectFirst("h1");
			if (h != null) m.put("headline", h.text());
			StringBuilder sb = new StringBuilder();
			for (Element p : d.select("p")) {
				String t = p.text().trim();
				if (!t.isEmpty()) {
					if (sb.length() > 0) sb.append("\n\n");
					sb.append(t);
				}
			}
			if (sb.length() > 0) m.put("content", sb.toString());

			// simple date extraction
			Element time = d.selectFirst("time[datetime], time, meta[property=article:published_time], meta[name=date]");
			if (time != null) {
				String dt = time.hasAttr("datetime") ? time.attr("datetime") : time.hasAttr("content") ? time.attr("content") : time.text();
				if (dt != null && !dt.isBlank()) m.put("date", dt.trim());
			}
		} catch (Exception ignored) {}
		return m;
	}
}
