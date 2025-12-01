// package main.java.com.nova.parsing;

// import java.util.HashMap;
// import java.util.Map;

// /*
//  Reflection-based CNBC scraper. If Jsoup isn't present, returns a safe placeholder.
// */
// public class CNBCScraper {
//     public static Map<String, String> fetch() {
//         Map<String, String> out = new HashMap<>();
//         out.put("headline", "No CNBC headline (Jsoup missing)");
//         out.put("content", "No CNBC content (Jsoup missing)");

//         try {
//             Class<?> jsoup = Class.forName("org.jsoup.Jsoup");
//             java.lang.reflect.Method connectM = jsoup.getMethod("connect", String.class);
//             Object conn = connectM.invoke(null, "https://www.cnbc.com/latest/");
//             conn = conn.getClass().getMethod("userAgent", String.class).invoke(conn, "Mozilla/5.0 (compatible; NovaBot/1.0)");
//             conn = conn.getClass().getMethod("timeout", int.class).invoke(conn, 10000);
//             Object listDoc = conn.getClass().getMethod("get").invoke(conn);

//             // try common card selector
//             java.lang.reflect.Method selectFirst = listDoc.getClass().getMethod("selectFirst", String.class);
//             Object firstLink = selectFirst.invoke(listDoc, "a.Card-title[href]");
//             if (firstLink == null) {
//                 java.lang.reflect.Method selectM = listDoc.getClass().getMethod("select", String.class);
//                 Object anchors = selectM.invoke(listDoc, "a[href*=\"/202\"] , a[href*=\"/news/\"] , a[href*=\"cnbc.com/\"]");
//                 java.lang.reflect.Method sizeM = anchors.getClass().getMethod("size");
//                 int anchorsSize = (Integer) sizeM.invoke(anchors);
//                 if (anchorsSize > 0) {
//                     java.lang.reflect.Method getM = anchors.getClass().getMethod("get", int.class);
//                     for (int i = 0; i < anchorsSize; i++) {
//                         Object a = getM.invoke(anchors, i);
//                         java.lang.reflect.Method absUrlM = a.getClass().getMethod("absUrl", String.class);
//                         String href = (String) absUrlM.invoke(a, "href");
//                         if (href != null && href.contains("cnbc.com")) {
//                             firstLink = a;
//                             break;
//                         }
//                     }
//                 }
//             }

//             String articleUrl = null;
//             if (firstLink != null) {
//                 java.lang.reflect.Method absUrlM = firstLink.getClass().getMethod("absUrl", String.class);
//                 articleUrl = (String) absUrlM.invoke(firstLink, "href");
//             }
//             if (articleUrl == null || articleUrl.isBlank()) articleUrl = "https://www.cnbc.com/latest/";

//             // fetch article page
//             Object artDocConn = connectM.invoke(null, articleUrl);
//             artDocConn = artDocConn.getClass().getMethod("userAgent", String.class).invoke(artDocConn, "Mozilla/5.0 (compatible; NovaBot/1.0)");
//             artDocConn = artDocConn.getClass().getMethod("timeout", int.class).invoke(artDocConn, 10000);
//             Object artDoc = artDocConn.getClass().getMethod("get").invoke(artDocConn);

//             // headline
//             java.lang.reflect.Method selectFirstDoc = artDoc.getClass().getMethod("selectFirst", String.class);
//             Object h = selectFirstDoc.invoke(artDoc, "h1.ArticleHeader-headline");
//             if (h == null) h = selectFirstDoc.invoke(artDoc, "h1");
//             if (h != null) {
//                 java.lang.reflect.Method textM = h.getClass().getMethod("text");
//                 String headline = (String) textM.invoke(h);
//                 if (headline != null && !headline.isBlank()) out.put("headline", headline.trim());
//             } else {
//                 out.put("headline", artDoc.getClass().getMethod("title").invoke(artDoc).toString());
//             }

//             // content paragraphs
//             java.lang.reflect.Method selectM = artDoc.getClass().getMethod("select", String.class);
//             Object paras = selectM.invoke(artDoc, "div.ArticleBody-articleBody p, div.ArticleBody p, article p");
//             java.lang.reflect.Method sizeM = paras.getClass().getMethod("size");
//             int size = (Integer) sizeM.invoke(paras);
//             if (size == 0) {
//                 paras = selectM.invoke(artDoc, "p");
//                 size = (Integer) sizeM.invoke(paras);
//             }
//             StringBuilder content = new StringBuilder();
//             if (size > 0) {
//                 java.lang.reflect.Method getM = paras.getClass().getMethod("get", int.class);
//                 java.lang.reflect.Method textM = null;
//                 for (int i = 0; i < size; i++) {
//                     Object p = getM.invoke(paras, i);
//                     if (textM == null) textM = p.getClass().getMethod("text");
//                     String t = (String) textM.invoke(p);
//                     if (t != null && !t.isBlank()) {
//                         content.append(t.trim()).append("\n\n");
//                     }
//                 }
//             }
//             if (content.length() > 0) out.put("content", content.toString().trim());

//         } catch (ClassNotFoundException cnf) {
//             // Jsoup missing — return placeholder already set
//         } catch (ReflectiveOperationException | RuntimeException e) {
//             System.err.println("CNBCScraper error: " + e.getMessage());
//         }

//         return out;
//     }
// }
