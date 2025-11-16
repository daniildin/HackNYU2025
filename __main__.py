import logic.portfolioEffect as portfolioEffect

#TODO: create and call welcome.py for initial bootup, portfolio entry, API entry
#TODO: call the parsers after X time
#TODO: if parsers find new material, implement logic analysis

def main():
    output = portfolioEffectAnalysis()
    print("Portfolio Effect Analysis Output:")
    print(output)

def portfolioEffectAnalysis():
    portfolio = "AAPL, MSFT, GOOGL"
    newsTitle = "Apple Releases New iPhone"
    newsContent = "Apple has announced the release of its latest iPhone model, which includes several new features and improvements over previous versions..."
    
    return portfolioEffect.portfolioAnalysis(newsTitle, newsContent, portfolio)
    
if __name__ == "__main__":
    print("NOVA")
    main()