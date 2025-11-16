import os
from dotenv import load_dotenv

import logic.portfolioEffect as portfolioEffect
from logic.welcome import load_from_env, first_time_setup
from parsing.scraper import run_all

PORTFOLIO_GLOBAL: list[str] = []  # tickers for this run


def init_config() -> None:
    """Load API key and portfolio from .env or run first-time setup."""
    load_dotenv()
    api_key, portfolio_list = load_from_env()

    if not api_key or not portfolio_list:
        api_key, portfolio_list = first_time_setup()
        os.environ["GEMINI_API_KEY"] = api_key
        os.environ["PORTFOLIO_TICKERS"] = ",".join(portfolio_list)

    global PORTFOLIO_GLOBAL
    PORTFOLIO_GLOBAL = portfolio_list


def main() -> None:
    """Entry point for the app."""
    init_config()
    output = portfolioEffectAnalysis()
    print("Portfolio Effect Analysis Output:")
    print(output)


def portfolioEffectAnalysis():
    """Run scrapers and send combined news + portfolio to Gemini."""
    portfolio = ", ".join(PORTFOLIO_GLOBAL) if PORTFOLIO_GLOBAL else "AAPL, MSFT, GOOGL"

    articles = run_all()

    nova_article = articles.get("NovaNews") if isinstance(articles, dict) else None
    cnbc_article = articles.get("CNBC") if isinstance(articles, dict) else None

    if nova_article and cnbc_article:
        newsTitle = f"{nova_article.get('headline', 'NovaNews')} & {cnbc_article.get('headline', 'CNBC')}"
        newsContent = (
            f"NovaNews: {nova_article.get('content', '')}\n\n"
            f"CNBC: {cnbc_article.get('content', '')}"
        )
    elif nova_article or cnbc_article:
        article = nova_article or cnbc_article
        newsTitle = article.get("headline", "Latest Market News")
        newsContent = article.get("content", "")
    else:
        newsTitle = "Apple Releases New iPhone"
        newsContent = (
            "Apple has announced the release of its latest iPhone model, which includes "
            "several new features and improvements over previous versions..."
        )

    return portfolioEffect.portfolioAnalysis(newsTitle, newsContent, portfolio)


if __name__ == "__main__":
    main()