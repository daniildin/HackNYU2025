from selenium.webdriver.common.by import By
from .driver import get_driver
import time
import json

def scrape():
    driver = get_driver()

    # Correct static article URL
    url = "https://portfolio-news-mock.vercel.app/article.html"
    driver.get(url)
    time.sleep(1.5)

    # Get the raw JSON metadata (<pre id="article-raw-meta">)
    try:
        meta_raw = driver.find_element(By.ID, "article-raw-meta").text
        meta = json.loads(meta_raw)
    except:
        # If meta JSON is missing
        meta = {
            "title": "(Untitled)",
            "date": "Unknown",
            "publisher": "Fahmy Macro Wire",
            "editors": "N/A",
            "tickers": "N/A",
            "sentiment": "N/A"
        }

    # Pull article content (<div id="article-content">)
    try:
        paragraphs = driver.find_elements(By.CSS_SELECTOR, "#article-content p")
        content = "\n".join(p.text for p in paragraphs if p.text.strip())
    except:
        content = "N/A"

    driver.quit()

    return {
        "headline": meta.get("title", ""),
        "subheader": "",
        "date": meta.get("date", ""),
        "publisher": meta.get("publisher", ""),
        "writers": [meta.get("editors", "")],
        "tickers": meta.get("tickers", ""),
        "sentiment": meta.get("sentiment", ""),
        "content": content
    }
