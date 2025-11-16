"""
driver.py
This file creates and returns a Chrome browser using Selenium.
We keep this separate so all scrapers can reuse the same browser setup.
"""

from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

# I put all the Selenium config in this helper so every scraper
# can just call get_driver() and not worry about the setup details.

def get_driver():
    
    options = webdriver.ChromeOptions()
    options.add_argument("--headless=new")  
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_argument("--window-size=1920,1080")


    driver = webdriver.Chrome(
        service=Service(ChromeDriverManager().install()),
        options=options
    )

    return driver
