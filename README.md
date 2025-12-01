# HackNYU2025 - NOVA

Nova is a news analysis tool for tracking your trade portfolio as global events happen in real time. Simply enter your portfolio and our daemon runs in the background to analyze what is happening in the world and best warn you of any trends!
Nova is 100% human written as well. When it comes to finance, we believe human development and inginuity is a necessity!

### Developer Details
To activate the virtual environment and download the dependecnies, run the following commands in the terminal:
```bash
python -m venv venv
```

(activating the venv for Windows):
```bash
venv\Scripts\activate
```

(for Mac):
```bash
source venv/bin/activate
```

installing dependencies:
```bash
pip install -r requirements.txt
```


To update the dependencies with new libraries, write to requirements.txt with the following:
```bash
pip freeze > requirements.txt
```

## Converting this project to Java (concise guide)

Summary: Yes — you can convert this Python project to Java. Do it incrementally: set up a Maven/Gradle project, map modules to packages, pick Java libraries, port logic, then integrate and test.

Suggested steps:
1. Create a new Java project (Gradle or Maven) with standard layout: src/main/java, src/main/resources, src/test.
2. Recommended libraries:
   - HTTP / scraping: Jsoup (HTML parsing) and/or Selenium Java
   - Scheduling: ScheduledExecutorService or Quartz
   - Environment: System.getenv() or java-dotenv
   - JSON: Jackson or Gson
   - Google API / GenAI: use official Java client if available or call REST via java.net.http.HttpClient
   - GUI: JavaFX (preferred) or Swing
   - Notifications: JavaFX Tray / platform-specific native wrapper
3. File / package mapping (example):
   - parsing.scraper -> package parsing; class Scraper with methods runAll()
   - logic.portfolioEffect -> package logic; class PortfolioEffect with portfolioAnalysis(...)
   - logic.bucketParser -> package logic; class BucketParser
   - __main__.py -> class App (with main) that wires services and scheduler
   - data/*.json -> resources or external config files under src/main/resources/data
4. Port strategy:
   - Implement POJOs for article & portfolio models.
   - Port scraping to return the same model objects.
   - Recreate prompt-building and API calls; test responses and JSON parsing.
   - Replace plyer notifications with JavaFX/system tray notifications.
   - Add logging and unit tests.
5. Build and run:
   - Use Gradle/Maven to declare dependencies and produce a runnable jar.
   - Run with proper environment variables (GEMINI_API_KEY, PORTFOLIO_TICKERS).

Minimal code notes:
- Keep the same overall architecture: separate parsing, logic, and UI.
- For the Google/GenAI call, if no official Java client fits, call the HTTP API directly and parse JSON with Jackson.
- Start by converting portfolioEffect logic and its inputs/outputs so other components can be tested against it.

This README note should help you plan and execute the port. If you want, I can generate an initial Gradle build file and Java class skeletons mapped from your Python modules.

## Converting this project to Java with Gradle (quick guide)

Summary: Yes — you can convert this folder into a Java Gradle project in-place. The plan: add Gradle build files, create src/main/java, and implement Java classes that mirror your Python packages (parsing, logic, etc.).

Quick steps:
1. Add Gradle files (build.gradle, settings.gradle) at project root.
2. Create standard layout: src/main/java/com/nova/{parsing,logic} and put skeleton classes there.
3. Use these recommended libraries:
   - HTML parsing/scraping: org.jsoup:jsoup
   - Browser automation: org.seleniumhq.selenium:selenium-java
   - Env vars: io.github.cdimascio:java-dotenv or System.getenv()
   - JSON: com.fasterxml.jackson.core:jackson-databind
4. Build & run:
   - On Unix/mac: ./gradlew run
   - On Windows: gradlew.bat run
5. Iterative porting: start by porting core logic (portfolio analysis), then scraping, then scheduler/GUI.

If you want, I can add a Gradle wrapper and more Java skeletons for other modules.

## Files safe to delete after Java conversion

The following Python files are deprecated because the project was ported to Java/Gradle. Make a backup branch first, then remove them with git rm:

- __main__.py
- __init__.py
- logic/welcome.py
- logic/portfolioEffect.py
- logic/bucketParser.py
- logic/__init__.py

Example commands:
```bash
git checkout -b backup/python-archive
git rm __main__.py __init__.py logic/welcome.py logic/portfolioEffect.py logic/bucketParser.py logic/__init__.py
git commit -m "Remove deprecated Python files after Java port"
git push origin HEAD
```

## Parsing folder (Java migration)

Plan:
1. Create com.nova.parsing package under src/main/java.
2. Add an Article POJO and one scraper class per source (NovaNews, CNBC).
3. Implement site scrapers using Jsoup (or Selenium if JS rendering required).
4. Keep Scraper.runAll() as the aggregator returning Map<String, Map<String,String>> so the existing App.java can consume it.

Minimal implementation details:
- Article fields: headline, content, publisher, writers[], date.
- Per-site scrapers: implement a fetch() method that returns a Map with keys "headline" and "content".
- Use selectors in Jsoup to extract title and content; wrap HTTP calls with retries and timeouts.

Example commands:
- Build: ./gradlew build
- Run: ./gradlew run

See src/main/java/com/nova/parsing for skeletons.

## Will this Java port work?

Short answer: Yes — but you must complete a few items first.

Prerequisites
- Java 17 installed and on PATH.
- Gradle (or add the Gradle wrapper files) to run ./gradlew.
- Network access for scraping (or provide data/articles.json).
- A valid .env with GEMINI_API_KEY and PORTFOLIO_TICKERS (or set environment variables).

Required follow-ups
1. Add Gradle wrapper (optional): ./gradle wrapper or run with your system Gradle.
2. Implement real scraping selectors:
   - Replace placeholder code in src/main/java/com/nova/parsing/NovaNewsScraper.java and CNBCScraper.java with Jsoup selectors (or Selenium if JS required).
3. Implement GenAI call:
   - Replace the placeholder in src/main/java/com/nova/logic/PortfolioEffect.java with an HTTP client call to your GenAI endpoint (include API key from .env) or use the official Java client.
4. Decide on Python files:
   - Python scrapers (parsing/*.py) are still in the repo but are not used by the Java app. Archive or delete them once you confirm Java scrapers work.
5. Test:
   - Run: ./gradlew run
   - Verify console output and that analysis JSON looks correct.

Common issues
- Missing Gradle wrapper → add it or install Gradle.
- Sites that require JS → use Selenium or a headless browser.
- Rate limits / scraping blocks → add retries, timeouts, and respectful delays.

If you want, I can:
- Add a Gradle wrapper.
- Convert one Python scraper (novanews or cnbc) into a concrete Jsoup implementation.
- Implement the GenAI HTTP request in PortfolioEffect (need API endpoint and auth details).

## Automate install & replace (backup + build)

If you want to back up the old Python files, create the Gradle wrapper (if gradle is available), build and run the Java port, run the included script for your platform:

- Unix / macOS:
```bash
chmod +x ./install_and_replace.sh
./install_and_replace.sh
```

- Windows (Command Prompt):
```cmd
install_and_replace.bat
```

Notes:
- The scripts move Python files into backup_python/ (safe restore).
- If you don't have Gradle installed the scripts will ask you to install it or run `gradle wrapper` manually.
- After the script completes the Java app runs via `./gradlew run`. Check console output for the analysis.

## Fix for missing Java 17 (copy & paste for macOS)

If you see Gradle errors about "Cannot find a Java installation matching: {languageVersion=17}", run these commands:

# 1) Install OpenJDK 17 with Homebrew (if you have Homebrew)
brew update
brew install openjdk@17

# 2) Add JDK 17 to your shell environment (zsh)
echo "export PATH=\"$(brew --prefix openjdk@17)/bin:\$PATH\"" >> ~/.zshrc
echo "export JAVA_HOME=\"$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home\"" >> ~/.zshrc
source ~/.zshrc

# 3) Verify java
java -version

# 4) Re-run installer
chmod +x ./install_and_replace.sh
./install_and_replace.sh

If you prefer the manual route instead of using the installer script, you can:
gradle wrapper         # if you have system gradle
chmod +x ./gradlew
./gradlew build
./gradlew run

## Run the app (interactive)

This simplified version runs once: it scrapes NovaNews and CNBC, analyzes impact for your tickers, prints the analysis and a simple terminal notification.

Recommended (interactive, preserves stdin):
```bash
# build and run the interactive launcher (you'll be prompted for API key and tickers)
./gradlew --no-daemon clean installDist
./build/install/nova/bin/nova
```

Non-interactive (provide credentials via JVM props and run once):
```bash
./gradlew run --no-daemon -Dgemini.key="YOUR_API_KEY" -Dportfolio.tickers="AAPL,MSFT"
```

Notes:
- The app will not save your API key unless you explicitly use the Welcome.saveEnv helper.
- The analyzer is local and free by default; to use a remote GenAI provider, set GENAI_API_URL and credentials and update PortfolioEffect if needed.
