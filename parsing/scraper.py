"""
scraper.py
Runs all individual scrapers and saves their output as JSON.
"""

import json
from .cnbc import scrape as scrape_cnbc
# from .novanews import scrape as scrape_nova

def run_all():
    return {
        "CNBC": scrape_cnbc(),
        # "NovaNews": scrape_nova()
    }

if __name__ == "__main__":
    articles = run_all()

    # Save the combined results for Connor
    with open("data/articles.json", "w") as f:
        json.dump(articles, f, indent=4)

    print("Scraping complete. Saved → data/articles.json")
