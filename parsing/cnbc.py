from selenium.webdriver.common.by import By
from .driver import get_driver
import time

def scrape():
    driver = get_driver()

    # CNBC latest news
    driver.get("https://www.cnbc.com/latest/")
    time.sleep(2)

    # Get first link
    first = driver.find_element(By.CSS_SELECTOR, "a.Card-title")
    url = first.get_attribute("href")

    # Open article
    driver.get(url)
    time.sleep(2)

    # Headline
    headline = driver.find_element(By.CSS_SELECTOR, "h1.ArticleHeader-headline").text

    # Subheader (optional)
    try:
        subheader = driver.find_element(By.CSS_SELECTOR, "div.ArticleHeader-subhead").text
    except:
        subheader = ""

    # Date
    try:
        date = driver.find_element(By.CSS_SELECTOR, "time").get_attribute("datetime")
    except:
        date = ""

    # Author
    try:
        author = driver.find_element(By.CSS_SELECTOR, "a.Byline-authorName").text
    except:
        author = "CNBC"

    # Body paragraphs
    paragraphs = driver.find_elements(By.CSS_SELECTOR, "div.ArticleBody-articleBody p")
    content = "\n".join([p.text for p in paragraphs if p.text.strip()])

    driver.quit()

    return {
        "headline": headline,
        "subheader": subheader,
        "date": date,
        "writers": [author],
        "content": content
    }
