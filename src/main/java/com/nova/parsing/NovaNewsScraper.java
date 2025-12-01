package com.nova.parsing;

import java.util.HashMap;
import java.util.Map;

/*
 Reflection-based NovaNews scraper: uses Jsoup at runtime if available.
 If Jsoup isn't on the classpath (IDE/editor), this returns a safe placeholder.
*/
public class NovaNewsScraper {
    public static Map<String, String> fetch() {
        Map<String, String> out = new HashMap<>();
        // Placeholder defaults
        out.put("headline", "Sample NovaNews Headline");
        out.put("content", "Sample NovaNews content (no content extracted).");

        try {
            // Check for Jsoup
            Class<?> jsoup = Class.forName("org.jsoup.Jsoup");
            // connect(url).userAgent(...).timeout(...).get()
            java.lang.reflect.Method connectM = jsoup.getMethod("connect", String.class);
            Object conn = connectM.invoke(null, "https://portfolio-news-mock.vercel.app/article.html");
            conn = conn.getClass().getMethod("userAgent", String.class).invoke(conn, "Mozilla/5.0 (compatible; NovaBot/1.0)");
            conn = conn.getClass().getMethod("timeout", int.class).invoke(conn, 10000);
            Object doc = conn.getClass().getMethod("get").invoke(conn);

            // Try headline via selectFirst
            java.lang.reflect.Method selectFirst = doc.getClass().getMethod("selectFirst", String.class);
            Object h = selectFirst.invoke(doc, "h1, .headline, .article-title");
            if (h != null) {
                java.lang.reflect.Method textM = h.getClass().getMethod("text");
                String headline = (String) textM.invoke(h);
                if (headline != null && !headline.isBlank()) out.put("headline", headline.trim());
            } else {
                // try #article-raw-meta element text for title
                Object metaEl = selectFirst.invoke(doc, "#article-raw-meta");
                if (metaEl != null) {
                    java.lang.reflect.Method textM = metaEl.getClass().getMethod("text");
                    String metaRaw = (String) textM.invoke(metaEl);
                    if (metaRaw != null) {
                        int idx = metaRaw.indexOf("\"title\"");
                        if (idx >= 0) {
                            int colon = metaRaw.indexOf(":", idx);
                            if (colon >= 0) {
                                int start = metaRaw.indexOf("\"", colon);
                                int end = metaRaw.indexOf("\"", start + 1);
                                if (start >= 0 && end > start) {
                                    out.put("headline", metaRaw.substring(start + 1, end));
                                }
                            }
                        }
                    }
                }
            }

            // Extract paragraphs: select "#article-content p, article p, .article-body p" then fallback to "p"
            java.lang.reflect.Method selectM = doc.getClass().getMethod("select", String.class);
            Object paras = selectM.invoke(doc, "#article-content p, article p, .article-body p");
            java.lang.reflect.Method sizeM = paras.getClass().getMethod("size");
            int size = (Integer) sizeM.invoke(paras);
            if (size == 0) {
                paras = selectM.invoke(doc, "p");
                size = (Integer) sizeM.invoke(paras);
            }
            StringBuilder content = new StringBuilder();
            if (size > 0) {
                java.lang.reflect.Method getM = paras.getClass().getMethod("get", int.class);
                java.lang.reflect.Method textM = null;
                for (int i = 0; i < size; i++) {
                    Object p = getM.invoke(paras, i);
                    if (textM == null) textM = p.getClass().getMethod("text");
                    String t = (String) textM.invoke(p);
                    if (t != null && !t.isBlank()) {
                        content.append(t.trim()).append("\n\n");
                    }
                }
            }
            if (content.length() > 0) out.put("content", content.toString().trim());

        } catch (ClassNotFoundException cnf) {
            // Jsoup not available — return placeholder (already set)
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Log concise error (avoids long stack traces in editor)
            System.err.println("NovaNewsScraper error: " + e.getMessage());
        }
        return out;
    }
}
