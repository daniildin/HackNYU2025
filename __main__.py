import os
import logic.portfolioEffect as portfolioEffect
from dotenv import load_dotenv
from parsing.scraper import run_all
import time
import schedule
import logic.welcome as welcome
from plyer import notification
import parsing.novanews, parsing.cnbc

def config():
    load_dotenv(override=True)
    try:
        api_key = os.getenv("GEMINI_API_KEY")
        portfolio_tickers = os.getenv("PORTFOLIO_TICKERS")
        portfolio_list = [ticker.strip() for ticker in portfolio_tickers.split(",")]
    except Exception as e:
        print("Error loading configuration from .env file:", e)
        exit(1)


def show_stock_notification(title: str, message: str):
    notification.notify(
        title=title,
        message=message,
        timeout=5, 
    )


def show_stock_notification(title: str, message: str):
    notification.notify(
        title=title,
        message=message,
        timeout=5, 
    )


def main():
    #run the setup scripts
    #welcome.main()
    config()
    
    #schedule loop
    newNova, newCNBC = False, False
    schedule.every(10).seconds.do(parsing.novanews.scrape, newNova=newNova)
    if newNova:
        print("Success")
    schedule.every(1).hours.do(parsing.cnbc.scrape, newNova=newCNBC)
    
    print("Portfolio Effect Analysis Output:")
    print(portfolioEffectAnalysis())


def portfolioEffectAnalysis():
    load_dotenv()
    portfolio = ", ".join(os.getenv("PORTFOLIO_TICKERS"))

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

    show_stock_notification(
        title="Portfolio update",
        message=(portfolioEffect.portfolioAnalysis(newsTitle, newsContent, portfolio)[:230] + "...") if len(portfolioEffect.portfolioAnalysis(newsTitle, newsContent, portfolio)) > 230 
            else portfolioEffect.portfolioAnalysis(newsTitle, newsContent, portfolio),
    )

    return portfolioEffect.portfolioAnalysis(newsTitle, newsContent, portfolio)


if __name__ == "__main__":
    main()