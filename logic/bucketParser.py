import json
import os

def bucketParser(meta):
    print("BUCKET PARSER \ndata:")
    print(meta)
    headline = meta.get("title")
    print(headline)
<<<<<<< HEAD
    data = ''
    try:
        with open('backlog.json', "r+") as log:
            data = json.load(log)
    except FileNotFoundError:
        print("ERROR: File not found")
    except json.JSONDecodeError:
        print("ERROR: failed to decode")
    
    print(log)

    for key,value in log():
=======
    with open('.\\data\\backlog.json', "a+") as log:
        data = json.load(log)

    for key, value in data.items():
>>>>>>> parent of 2477850 (test)
        if headline == key:
            return False
        else:
            log.seek(0)
            log.write(json.dumps({**data, **{headline: meta}}, indent=4))
            return True