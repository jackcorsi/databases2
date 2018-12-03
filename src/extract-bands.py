import urllib.request
import re
import json

citiesFile = open("eventbrite-cities.txt", "r")
html = citiesFile.read()
citiesFile.close()

cities = re.findall(r"<a.*>(.*)<\/a>", html)

actsFile = open("eventbrite-bands.txt", "w")

numberOfEvents = 0
yetToBeDetermined = 0

for city in cities:
    city = re.sub(r" ", "-", city)  # URL formatting - remove spaces tolower
    city = city.lower()
    page = 1
    while True:
        if page > 100:  # For safety!
            break

        url = "https://www.eventbrite.co.uk/d/united-kingdom--"
        url += city
        url += "/performances/?page="
        url += str(page)
        url += "&tags=Band"
        print(url)
        try:
            html = urllib.request.urlopen(url).read().decode("utf-8")
        except Exception:
            print("HTTP FAIL")
            page = 500  # Lol hack
            continue

        jsonStrings = re.findall(r"<script type=\"application\/ld\+json\">\s*(.*)\s*<\/script>", html,
                                 flags=re.MULTILINE)
        events = json.loads(jsonStrings[0])

        if len(events) == 0:  # Run out of events for this city
            break

        for event in events:
            actsFile.write(event["name"] + "\n")

            numberOfEvents += 1

        page += 1

    print("EVENTS")
    print(numberOfEvents)
    if numberOfEvents > 1000:
        break

actsFile.close()
