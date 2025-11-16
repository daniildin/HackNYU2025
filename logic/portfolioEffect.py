from google import genai
import os
from dotenv import load_dotenv
import json

def portfolioAnalysis(newsTitle, newsContent, portfolio):
    print(portfolio)
    load_dotenv()
    apiKey=os.getenv("GEMINI_API_KEY")

    try:
        load_dotenv(verbose=True)
    except Exception as e:
        print("Error loading .env file:", e)
        
    client = genai.Client(api_key=apiKey)
        
    response = client.models.generate_content(
        model="gemini-2.5-pro",
        contents=f'''
        [SYSTEM PROMPT]Your sole purpose is to read news article information we provide you and generate the likelihood of portfolio affect as well as the sentiment. You will be provided with news article titles and text content. Provide a format as listed below with no other output.[END SYSTEM PROMPT]
                [PORTFOLIO] {portfolio} [END PORTFOLIO]
                [NEWS ARTICLE TITLE] {newsTitle} [END NEWS ARTICLE TITLE]
                [NEWS ARTICLE CONTENT] {newsContent} [END NEWS ARTICLE CONTENT]
                
                [OUTPUT FORMAT (in JSON)]
                "'stock_1_ticker'":{{
                    "likelihood": 'float from 0(no effect) to 1(direct effect)',
                    "sentiment": 'float -1(very negative) - 1(very positive)'
                }},
                "'stock_2_ticker'":{{
                    "likelihood": 'float 0(no effect) - 1(direct effect)',
                    "sentiment": 'float -1(very negative) - 1(very positive)'
                }},
                ...
                [END OUTPUT FORMAT]
        '''
    )

    return(response.text)