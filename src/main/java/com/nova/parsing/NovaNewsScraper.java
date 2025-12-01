package com.nova.parsing;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class NovaNewsScraper {
	public static Map<String,String> fetch() {
		Map<String,String> m = new HashMap<>();
		try {
			Document d = Jsoup.connect("https://portfolio-news-mock.vercel.app/article.html")
					.userAgent("Mozilla/5.0")
					.timeout(8000)
					.get();

			m.put("headline", d.selectFirst("h1").text());

			StringBuilder sb = new StringBuilder();
			for (Element p : d.select("p")) {
				if (sb.length() > 0) sb.append("\n\n");
				sb.append(p.text().trim());
			}
			m.put("content", sb.toString());

			Element time = d.selectFirst("time[datetime], time, meta[property=article:published_time], meta[name=date]");
			String dt = time.hasAttr("datetime") ? time.attr("datetime")
					: time.hasAttr("content") ? time.attr("content") : time.text();
			m.put("date", dt.trim());
		} catch (Exception ignored) {}
		return m;
	}
}
