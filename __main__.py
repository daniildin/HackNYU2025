import logic.portfolioEffect as portfolioEffect

#TODO: create and call welcome.py for initial bootup, portfolio entry, API entry
#TODO: call the parsers after X time
#TODO: if parsers find new material, implement logic analysis

def main():
    print(portfolioEffectAnalysis)

def portfolioEffectAnalysis():
    portfolio = "AAPL, MSFT, GOOGL"  # Example portfolio
    newsTitle = "Apple Releases New iPhone"
    newsSubhead = "The latest model features advanced technology."
    newsContent = "Apple has announced the release of its latest iPhone model, which includes several new features and improvements over previous versions..."
    
    return portfolioEffect(newsTitle, newsSubhead, newsContent, portfolio)
    
if __name__ == "__main__":
    main()
    