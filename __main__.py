import os
import logic.portfolioEffect as portfolioEffect
from dotenv import load_dotenv
from parsing.scraper import run_all
import time
import logic.welcome as welcome
from plyer import notification

PORTFOLIO_GLOBAL: list[str] = []  # tickers for this run


def config():
    load_dotenv(override=True)
    try:
        api_key = os.getenv("GEMINI_API_KEY")
        portfolio_str = os.getenv("PORTFOLIO")

        if not api_key or not portfolio_str:
            raise ValueError("GEMINI_API_KEY or PORTFOLIO missing in .env")

        portfolio_list = [ticker.strip() for ticker in portfolio_str.split(",") if ticker.strip()]
        PORTFOLIO_GLOBAL.clear()
        PORTFOLIO_GLOBAL.extend(portfolio_list)
    except Exception as e:
        print("Error loading configuration from .env file:", e)
        raise

    print("Loaded API key and portfolio:", api_key, PORTFOLIO_GLOBAL)


def show_stock_notification(title: str, message: str):
    # Fallback: just log to console, since plyer.notification has no backend on macOS
    print(f"[NOTIFICATION] {title}\n{message}\n")


def main():
    # Run the setup GUI once to create/update .env, then load config
    welcome.run_setup()
    config()

    # TODO: set sleep/scrape cycle
    output = portfolioEffectAnalysis()
    print("Portfolio Effect Analysis Output:")
    print(output)


def portfolioEffectAnalysis():
    portfolio = ", ".join(PORTFOLIO_GLOBAL) if PORTFOLIO_GLOBAL else "AAPL, MSFT, GOOGL"

    articles = run_all()

    nova_article = articles.get("NovaNews") if isinstance(articles, dict) else None
    cnbc_article = articles.get("CNBC") if isinstance(articles, dict) else None

    # Ensure we only treat dicts as articles
    if not isinstance(nova_article, dict):
        nova_article = None
    if not isinstance(cnbc_article, dict):
        cnbc_article = None

    if nova_article and cnbc_article:
        newsTitle = f"{nova_article.get('headline', 'NovaNews')} & {cnbc_article.get('headline', 'CNBC')}"
        newsContent = (
            f"NovaNews: {nova_article.get('content', '')}\n\n"
            f"CNBC: {cnbc_article.get('content', '')}"
        )
        publisher = "NovaNews & CNBC"

        # Normalize writers to lists before concatenating
        nova_writers = nova_article.get("writers") or nova_article.get("editors") or []
        cnbc_writers = cnbc_article.get("writers") or []

        if isinstance(nova_writers, str):
            nova_writers = [nova_writers]
        if isinstance(cnbc_writers, str):
            cnbc_writers = [cnbc_writers]

        writers = nova_writers + cnbc_writers
    elif nova_article or cnbc_article:
        article = nova_article or cnbc_article
        newsTitle = article.get("headline", "Latest Market News")
        newsContent = article.get("content", "")
        publisher = article.get("publisher", "Unknown")

        # Normalize single-article writers to list
        writers_value = article.get("writers") or article.get("editors") or ["Unknown"]
        if isinstance(writers_value, str):
            writers = [writers_value]
        else:
            writers = writers_value
    else:
        newsTitle = "Apple Releases New iPhone"
        newsContent = (
            "Apple has announced the release of its latest iPhone model, which includes "
            "several new features and improvements over previous versions..."
        )
        publisher = "DemoPublisher"
        writers = ["DemoWriter"]

    # Call Gemini once and reuse the result
    analysis_text = portfolioEffect.portfolioAnalysis(
        newsTitle,
        publisher,
        writers,
        newsContent,
        portfolio,
    )

    # Truncate for system notification if necessary
    if len(analysis_text) > 230:
        notif_msg = analysis_text[:230] + "..."
    else:
        notif_msg = analysis_text

    show_stock_notification(
        title="Portfolio update",
        message=notif_msg,
    )

    return analysis_text


if __name__ == "__main__":
    main()