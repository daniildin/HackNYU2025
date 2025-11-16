"""
cnbc.py
Scrapes the most recent article from CNBC's Latest News page.

We extract:
- Headline
- Subheader 
- Publication date
- Writers
- Full article text
"""

from selenium.webdriver.common.by import By
from .driver import get_driver
import time

def scrape():
    driver = get_driver()

    # 1. Go to CNBC "Latest" feed
    driver.get("https://www.cnbc.com/latest/")
    time.sleep(2)

    # 2. Click the first article card
    first_article = driver.find_element(By.CSS_SELECTOR, "a.Card-title")
    article_url = first_article.get_attribute("href")

    driver.get(article_url)
    time.sleep(2)

    # 3. Scrape the required fields
    headline = driver.find_element(By.CSS_SELECTOR, "h1.ArticleHeader-headline").text

    # Subheader 
    try:
        subheader = driver.find_element(By.CSS_SELECTOR, ".ArticleHeader-dek").text
    except:
        subheader = ""

    date = driver.find_element(
        By.CSS_SELECTOR,
        "time[data-testid='published-timestamp']"
    ).get_attribute("datetime")

    # Gather all writer names
    writers = [
        el.text for el in driver.find_elements(By.CSS_SELECTOR, ".Author-authorName")
    ]
    if not writers:
        writers = ["CNBC Staff"]

    # Collect full article body
    paragraphs = driver.find_elements(By.CSS_SELECTOR, ".ArticleBody-articleBody p")
    content = "\n".join(p.text for p in paragraphs if p.text.strip())

    driver.quit()

    return {
        "headline": headline,
        "subheader": subheader,
        "date": date,
        "writers": writers,
        "content": content
    }
