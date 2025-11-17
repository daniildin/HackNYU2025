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
        portfolio_tickers = os.getenv("PORTFOLIO")
        portfolio_list = [ticker.strip() for ticker in portfolio_tickers.split(",")]
    except Exception as e:
        print("Error loading configuration from .env file:", e)
        exit(1)
    print("config success")


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

def portfolioEffectAnalysis():
    print("bruh")

def ping():
    print("ping")


def main():
    #run the setup scripts
    #welcome.main()
    config()
    
    #schedule loop
    newNova, newCNBC = False, False
    schedule.every(10).seconds.do(parsing.novanews.scrape(), newNova)
    if newNova:
        print("Success")
        portfolioEffectAnalysis()
    schedule.every(1).hours.do(parsing.cnbc.scrape(), newCNBC)
    
    print("done")


if __name__ == "__main__":
    main()