import os
from dotenv import load_dotenv

import logic.portfolioEffect as portfolioEffect
from logic.welcome import load_from_env, first_time_setup
from parsing.scraper import run_all

#TODO: create and call welcome.py for initial bootup, portfolio entry, API entry
#TODO: call the parsers after X time
#TODO: if parsers find new material, implement logic analysis

PORTFOLIO_GLOBAL: list[str] = []  # holds the user portfolio tickers for this run


def init_config() -> None:
    """Initialize configuration from .env or run first-time setup.
  Sets up GEMINI_API_KEY and PORTFOLIO_GLOBAL.
    """
    load_dotenv()
    api_key, portfolio_list = load_from_env()

    # If we don't have an API key or a portfolio yet, ask the user for them.
    if not api_key or not portfolio_list:
        api_key, portfolio_list = first_time_setup()
        # Make sure these are available to the rest of the app (and to Gemini client).
        os.environ["GEMINI_API_KEY"] = api_key
        os.environ["PORTFOLIO_TICKERS"] = ",".join(portfolio_list)

    # Save the final portfolio (from env or from the wizard) into a global variable.
    global PORTFOLIO_GLOBAL
    PORTFOLIO_GLOBAL = portfolio_list


def main() -> None:
    """Entry point for the whole app.

    Right now this is basically:
      make sure config is ready
      run the portfolio effect analysis once
      rint whatever Gemini returns

    Later we can add loops / scheduling here if we want this to run continuously.
    """
    init_config()
    output = portfolioEffectAnalysis()
    print("Portfolio Effect Analysis Output:")
    print(output)


def portfolioEffectAnalysis():
    portfolio = "AAPL, MSFT, GOOGL"
    newsTitle = "Apple Releases New iPhone"
    newsContent = "Apple has announced the release of its latest iPhone model, which includes several new features and improvements over previous versions..."
    
    return portfolioEffect.portfolioAnalysis(newsTitle, newsContent, portfolio)


if __name__ == "__main__":
    main()