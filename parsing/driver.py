"""
driver.py
This file creates and returns a Chrome browser using Selenium.
We keep this separate so all scrapers can reuse the same browser setup.
"""

from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

def get_driver():
    # Set up browser options
    options = webdriver.ChromeOptions()
    options.add_argument("--headless=new")  # Run without opening a window
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_argument("--window-size=1920,1080")

    # Create a Chrome browser that Selenium can control
    driver = webdriver.Chrome(
        service=Service(ChromeDriverManager().install()),
        options=options
    )

    return driver
