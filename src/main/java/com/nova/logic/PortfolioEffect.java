// package com.nova.logic
// // // import java.time.LocalDate;
// // import java.util.Arrays;
// // import java.util.HashSet;
// // import java.util.Set;

// /*
//  Replaced network GenAI call with a local free heuristic analyzer.

//  Behavior:
//  - summary: short one-line summary from title/content
//  - affectedStocks: for each ticker in portfolio:
//      - likelihood: high if ticker text appears in title/content, low otherwise
//      - sentiment: simple lexicon-based score between -1 and 1
//  - writers: preserved as provided
// */
// public class PortfolioEffect {
//     // private static final ObjectMapper M = new ObjectMapper();

//     // small positive / negative lexicons (can be extended)
//     private static final Set<String> POS = new HashSet<>(Arrays.asList(
//             "gain","gains","up","rose","rises","surge","surged","positive","beat","beats","beats","strong","growth","profit",
//             "outperform","optimistic","upgrade","record","higher","advance","advances","improve","improves","improved"
//     ));
//     private static final Set<String> NEG = new HashSet<>(Arrays.asList(
//             "loss","losses","down","drop","drops","fall","falls","fell","weak","weakness","decline","declines","slump",
//             "cut","cuts","warning","warn","risk","negative","miss","missed","lower","falling","reduction","reduced"
//     ));

//     // Signature unchanged for compatibility; apiKey is ignored by local analyzer
//     public static String analyze(String newsTitle, String publisher, String[] writers, String newsContent, String portfolio, String apiKey) {
//         try {
//             String title = (newsTitle != null && !newsTitle.isBlank()) ? newsTitle.trim() : "";
//             String content = (newsContent != null && !newsContent.isBlank()) ? newsContent.trim() : "";

//             // Build a short summary: prefer title, otherwise first ~30 words of content
//             String summary;
//             if (!title.isEmpty()) {
//                 summary = title;
//             } else if (!content.isEmpty()) {
//                 String[] words = content.split("\\s+");
//                 int n = Math.min(words.length, 30);
//                 StringBuilder sb = new StringBuilder();
//                 for (int i = 0; i < n; i++) {
//                     if (i > 0) sb.append(" ");
//                     sb.append(words[i]);
//                 }
//                 summary = sb.toString().trim();
//                 if (words.length > n) summary += "...";
//             } else {
//                 summary = "No article content available.";
//             }

//             // Simple tokenization for sentiment scoring
//             String combined = (title + " " + content).toLowerCase();
//             String[] tokens = combined.replaceAll("[^a-z0-9$#_,\\s]", " ").split("\\s+");

//             int posCount = 0;
//             int negCount = 0;
//             int matchCount = 0;
//             for (String t : tokens) {
//                 if (t == null || t.isBlank()) continue;
//                 String w = t.trim();
//                 if (POS.contains(w)) { posCount++; matchCount++; }
//                 else if (NEG.contains(w)) { negCount++; matchCount++; }
//             }

//             double sentimentScore = 0.0;
//             if (matchCount > 0) {
//                 sentimentScore = (double)(posCount - negCount) / (double) Math.max(1, matchCount);
//                 // clamp to -1..1
//                 if (sentimentScore > 1.0) sentimentScore = 1.0;
//                 if (sentimentScore < -1.0) sentimentScore = -1.0;
//             }

//             // Build affectedStocks from portfolio tickers (comma-separated)
//             ObjectNode root = M.createObjectNode();
//             root.put("summary", summary);
//             root.put("publisher", publisher != null ? publisher : "");
//             root.put("date", LocalDate.now().toString());
//             root.put("portfolio", portfolio != null ? portfolio : "");

//             ObjectNode affected = M.createObjectNode();
//             if (portfolio != null && !portfolio.isBlank()) {
//                 String[] tickers = portfolio.split(",");
//                 for (String raw : tickers) {
//                     String t = raw.trim().toUpperCase();
//                     if (t.isEmpty()) continue;

//                     // Determine likelihood: check if ticker appears as standalone token or with $ prefix
//                     double likelihood = 0.05;
//                     String look1 = t.toLowerCase();
//                     String look2 = ("$" + t).toLowerCase();
//                     boolean present = false;
//                     for (String tok : tokens) {
//                         if (tok == null) continue;
//                         String tk = tok.trim().toLowerCase();
//                         if (tk.equalsIgnoreCase(look1) || tk.equalsIgnoreCase(look2)) {
//                             present = true;
//                             break;
//                         }
//                     }
//                     if (present) likelihood = 0.9;
//                     else {
//                         // also boost likelihood if the company name (ticker as substring in headline/content)
//                         if (combined.contains(look1)) likelihood = 0.5;
//                     }

//                     ObjectNode entry = M.createObjectNode();
//                     entry.put("likelihood", Math.round(likelihood * 100.0) / 100.0);
//                     // sentiment per ticker: use computed sentimentScore
//                     entry.put("sentiment", Math.round(sentimentScore * 100.0) / 100.0);
//                     affected.set(t, entry);
//                 }
//             } else {
//                 ObjectNode entry = M.createObjectNode();
//                 entry.put("likelihood", 0.0);
//                 entry.put("sentiment", 0.0);
//                 affected.set("NONE", entry);
//             }

//             root.set("affectedStocks", affected);
//             root.putPOJO("writers", Arrays.asList(writers));

//             return M.writerWithDefaultPrettyPrinter().writeValueAsString(root);
//         } catch (Exception e) {
//             // fallback JSON on error
//             e.printStackTrace();
//             return "{\"error\":\"analysis failed\"}";
//         }
//     }
// }
