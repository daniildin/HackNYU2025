import json
import os

def bucketParser(meta):
    headline = meta.get("headline")
    print(headline)
    with open('.\\data\\backlog.json', "a+") as log:
        data = json.load(log)

    for key, value in data.items():
        if headline == key:
            return False
        else:
            log.seek(0)
            log.write(json.dumps({**data, **{headline: meta}}, indent=4))
            return True