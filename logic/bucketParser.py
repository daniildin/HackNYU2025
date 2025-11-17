import json
import os


def bucketParser(meta):
    headline = meta.get("headline")
    print(headline)

    # Build absolute path to data/backlog.json (cross-platform)
    backlog_path = os.path.join(os.path.dirname(__file__), "..", "data", "backlog.json")
    backlog_path = os.path.abspath(backlog_path)

    # Ensure file exists and is at least an empty JSON object
    if not os.path.exists(backlog_path):
        with open(backlog_path, "w") as f:
            f.write("{}")

    # Safely load JSON content
    try:
        with open(backlog_path, "r") as log:
            content = log.read().strip()
            data = json.loads(content) if content else {}
    except json.JSONDecodeError:
        data = {}

    # If headline already exists, do nothing
    if headline in data:
        return False

    # Add new headline and write back
    data[headline] = meta
    with open(backlog_path, "w") as log:
        json.dump(data, log, indent=4)

    return True