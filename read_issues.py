import json
data = json.load(open('issues.json'))
for i in data.get('issues', []):
    print(f"{i['component']}: {i['message']} (Rule: {i['rule']})")
