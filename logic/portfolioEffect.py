from google import genai
import os
from dotenv import load_dotenv
import json

def portfolioAnalysis(newsTitle, newsSubhead, newsContent, portfolio):
    with open('portfolio.json') as p:
        portfolios = json.load(p)

    print(portfolios)

    load_dotenv()
    api_key = os.getenv("GENAI_API_KEY")

    client = genai.Client(api_key="GENAI_API_KEY")

    response = client.models.generate_content(
        model="gemini-2.5-pro",
        contents=f'''[SYSTEM PROMPT]Your sole purpose is to read news article information we provide you and generate the likelihood of portfolio affect as well as the sentiment. You will be provided with news article titles and text content. Provide a format as listed below with no other output.[END SYSTEM PROMPT]
        [PORTFOLIO] {portfolio} [END PORTFOLIO]
        [NEWS ARTICLE TITLE] {newsTitle} [END NEWS ARTICLE TITLE]
        [NEWS ARTICLE CONTENT] {newsContent} [END NEWS ARTICLE CONTENT]
        
        [OUTPUT FORMAT]
        "<stock_1_ticker>":{
            "likelihood": '<float 0(no effect) - 1(direct effect)>,
            "sentiment": '<float -1(very negative) - 1(very positive)>
        },
        "<stock_2_ticker>":{
            "likelihood": '<float 0(no effect) - 1(direct effect)>,
            "sentiment": '<float -1(very negative) - 1(very positive)>
        },
        ...
        [END OUTPUT FORMAT]''',
    )

    print(response.content)
    return(response.text)