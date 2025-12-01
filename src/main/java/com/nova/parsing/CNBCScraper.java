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
					.timeout(10000)
					.get();

			Element first = list.selectFirst("a.Card-title[href]");
			if (first == null) first = list.selectFirst("a[href*=\"/202\"], a[href*=\"/news/\"]");
			String url = (first != null) ? first.absUrl("href") : "https://www.cnbc.com/latest/";
			m.put("url", url);

			Document art = Jsoup.connect(url)
					.userAgent("Mozilla/5.0")
					.timeout(10000)
					.get();

			Element h = art.selectFirst("h1.ArticleHeader-headline, h1");
			if (h != null) m.put("headline", h.text().trim());

			StringBuilder sb = new StringBuilder();
			for (Element p : art.select("div.ArticleBody-articleBody p, article p, p")) {
				String t = p.text().trim();
				if (!t.isEmpty()) {
					if (sb.length()>0) sb.append("\n\n");
					sb.append(t);
				}
			}
			if (sb.length()>0) m.put("content", sb.toString());

			Element time = art.selectFirst("time[datetime], time, meta[property=article:published_time], meta[name=date]");
			if (time != null) {
				String dt = time.hasAttr("datetime") ? time.attr("datetime")
						: time.hasAttr("content") ? time.attr("content") : time.text();
				if (dt != null && !dt.isBlank()) m.put("date", dt.trim());
			}
		} catch (Exception ignored) {}
		return m;
	}
}
