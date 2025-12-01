package com.nova.parsing;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CNBCScraper {
	public static Map<String,String> fetch() {
		Map<String,String> m = new HashMap<>();
		try {
			Document list = Jsoup.connect("https://www.cnbc.com/latest/")
					.userAgent("Mozilla/5.0")
					.timeout(8000)
					.get();

			Element first = list.selectFirst("a[href*=\"/202\"], a.Card-title[href], a[href*=\"/news/\"]");
			String url = first.absUrl("href");

			Document art = Jsoup.connect(url)
					.userAgent("Mozilla/5.0")
					.timeout(8000)
					.get();

			m.put("headline", art.selectFirst("h1").text());

			StringBuilder sb = new StringBuilder();
			for (Element p : art.select("p")) {
				if (sb.length()>0) sb.append("\n\n");
				sb.append(p.text().trim());
			}
			m.put("content", sb.toString());

			Element time = art.selectFirst("time[datetime], time, meta[property=article:published_time], meta[name=date]");
			String dt = time.hasAttr("datetime") ? time.attr("datetime")
					: time.hasAttr("content") ? time.attr("content") : time.text();
			m.put("date", dt.trim());
		} catch (Exception ignored) {}
		return m;
	}
}
