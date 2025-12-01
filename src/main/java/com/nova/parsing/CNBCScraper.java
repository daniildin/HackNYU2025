package com.nova.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public class CNBCScraper {
	public static Map<String,String> fetch() {
		Map<String,String> m = new HashMap<>();
		m.put("headline","No CNBC headline");
		m.put("content","No CNBC content");
		m.put("date","No CNBC date");
		try {
			Document list = Jsoup.connect("https://www.cnbc.com/latest/")
					.userAgent("Mozilla/5.0")
					.timeout(8000)
					.get();
			Element first = list.selectFirst("a[href*=\"/202\"], a.Card-title[href], a[href*=\"/news/\"]");
			String url = first != null ? first.absUrl("href") : "https://www.cnbc.com/latest/";
			Document art = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(8000).get();
			Element h = art.selectFirst("h1");
			if (h != null) m.put("headline", h.text());
			StringBuilder sb = new StringBuilder();
			for (Element p : art.select("p")) {
				String t = p.text().trim();
				if (!t.isEmpty()) {
					if (sb.length()>0) sb.append("\n\n");
					sb.append(t);
				}
			}
			if (sb.length()>0) m.put("content", sb.toString());

			// simple date extraction
			Element time = art.selectFirst("time[datetime], time, meta[property=article:published_time], meta[name=date]");
			if (time != null) {
				String dt = time.hasAttr("datetime") ? time.attr("datetime") : time.hasAttr("content") ? time.attr("content") : time.text();
				if (dt != null && !dt.isBlank()) m.put("date", dt.trim());
			}
		} catch (Exception ignored) {}
		return m;
	}
}
