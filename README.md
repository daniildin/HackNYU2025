# NovaNews Portfolio Sentiment

Simple Java app that:
- Scrapes the latest article from your Nova site and CNBC
- Sends the combined text to AI (Gemini) or uses a tiny local heuristic
- Gets per-ticker sentiment in [0..1] (1 = most optimistic, 0 = most pessimistic)
- Prints a BUY/NO ACTION based on the highest sentiment and saves results to data/

## Tech stack

- Java 11+ (JDK)
- Gradle (application plugin)
- Jsoup 1.17.2 (HTML fetch + parsing)
- java.net.http.HttpClient (calls Gemini API)
- No heavy JSON libs; uses simple string building/parsing

## Requirements

- JDK 11+ installed (java -version)
- Gradle wrapper generated (run `gradle wrapper` once if missing), or use a local Gradle
- Internet access (scraping + optional AI)

## Run

Interactive (prompts for tickers and optional API key):
- macOS/Linux:
  ./gradlew run
- Windows (PowerShell):
  .\gradlew.bat run

What to enter when prompted:
- Tickers (comma-separated), e.g.: NVDA,PAL
- API key (optional): your Google Generative Language (Gemini) API key

Non-interactive example:
./gradlew run --args "NVDA,PAL YOUR_GEMINI_API_KEY"

## What it does

1) Scrape
- NovaNews (homepage first article card):
  - Headline: body > main > section > div > article:nth-child(1) > a > h3
  - Date: p:nth-child(2) (reads after "Date:")
  - Publisher: p:nth-child(3) (reads after "Publisher:")
  - Meta: p:nth-child(4) (extracts Editors, Tickers, Sentiment labels)
  - Content: p.excerpt + meta text
- CNBC:
  - Finds latest article link on https://www.cnbc.com/latest/
  - Fetches article page, extracts headline (h1), content (p), and date (time/meta)

2) Analyze
- With API key: sends prompt to Gemini (model: gemini-1.5-flash) asking for ONLY a JSON map:
  {"AAPL":0.73,"MSFT":0.61}
- Without API key: local heuristic:
  - Tiny positive/negative word list determines a base score in [0..1]
  - Mentioned tickers get base; unmentioned tickers get neutral 0.5

3) Decide
- If the highest sentiment >= 0.6 → BUY that ticker
- Else → NO ACTION

4) Save
- data/articles.json: the scraped snapshot (simple JSON string)
- data/analysis.json: the sentiment result JSON
- .env: convenience (GEMINI_API_KEY, PORTFOLIO_TICKERS)

## Project layout

- com.nova.App
  - Main; prompts, scrapes, calls analyzer, saves files, prints decision
- com.nova.parsing.Scraper
  - Orchestrates NovaNewsScraper + CNBCScraper; writes data/articles.json
- com.nova.parsing.NovaNewsScraper
  - Uses your CSS selectors to read the first article card from Nova homepage
- com.nova.parsing.CNBCScraper
  - Picks a likely latest article, then extracts h1/body/date
- com.nova.logic.PortfolioEffect
  - analyze(...) → Gemini if API key, otherwise local word-list heuristic
- com.nova.logic.Welcome
  - saveEnv(...) writes .env (API key + tickers)

## Example run

- Input:
  - Tickers: NVDA,PAL
  - API key: <your key> (or blank to use local)
- Output (console):
  {"NVDA":0.82,"PAL":0.91}
  === NOTIFICATION: BUY NVDA ===

Files created:
- data/articles.json
- data/analysis.json
- .env

## Troubleshooting

- “Neutral 0.5” scores:
  - Means Gemini wasn’t used (no/invalid API key) or ticker not mentioned (local heuristic).
- Nova headline/content empty:
  - Ensure the homepage has the elements the scraper targets; it doesn’t execute JavaScript.
- CNBC changed layout:
  - Update selectors in CNBCScraper if headlines/content are empty.
- Gradle/JDK issues:
  - Ensure Java 11+ is on PATH. Run `java -version`.
  - If wrapper missing, run `gradle wrapper` once, then use `./gradlew`.

## Notes

- Sentiment is about stock price outlook (0..1), not ethics/morality.
- Keep the API key private; it’s stored in .env for convenience.
- Improve later: full-article Nova fetch, better JSON parsing, retries, tests, richer sentiment model.
