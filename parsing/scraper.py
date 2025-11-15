#Scraping the data from news articles, collecting libraries
import json 
from fox import scrape as scrape_fox
from apnews import scrape as scrape_apnews
from bbc import scrape as scrape_bbc
from cnn import scrape as scrape_cnn
from marketwatch import scrape as scrape_marketwatch
from cnbc import scrape as scrape_cnbc

#Final function to run all subscrape_news functions 
def run_all():
    return {
        "FOX": scrape_fox(),
        "APNEWS": scrape_apnews(),
        "BBC": scrape_bbc(),
        "CNN": scrape_cnn(),
        "MARKETWATCH": scrape_marketwatch(),
        "CNBC": scrape_cnbc(),
    }

if __name__ == "__main__":
    articles = run_all()

    #For Connor -> save to JSON file, no strings
    with open("../data/article.json", "w") as f:
        json.dump(articles, f, indent = 4)

    print("Scraping complete. See output in data/article.json")

