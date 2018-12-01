import urllib.request
import re
import json
citiesFile = open("eventbrite-cities.txt", "r")
html = citiesFile.read()
citiesFile.close()

cities = re.findall(r"<a.*>(.*)<\/a>", html)

eventsFile = open("eventbrite-events.txt", "w")
venuesFile = open("eventbrite-venues.txt", "w")

numberOfEvents = 0
yetToBeDetermined = 0

for city in cities:
    city = re.sub(r" ", "-", city) #URL formatting - remove spaces tolower
    city = city.lower()
    page = 1
    while True:
        if page > 100: #For safety!
            break
        
        url = "https://www.eventbrite.co.uk/d/united-kingdom--"
        url += city
        url += "/all-events/?page="
        url += str(page)
        url += "&tags=Party%2CChristmas"
        print(url)
        try:
            html = urllib.request.urlopen(url).read().decode("utf-8")
        except Exception:
            print("HTTP FAIL")
            page = 500 #Lol hack
            continue
        
        jsonStrings = re.findall(r"<script type=\"application\/ld\+json\">\s*(.*)\s*<\/script>", html, flags=re.MULTILINE)
        events = json.loads(jsonStrings[0])
        
        if len(events) == 0: #Run out of events for this city
            break
        
        for event in events:
            eventsFile.write(event["name"] + "\n")
            location = event["location"]
            locName = location["name"]
            
            if locName != None:
                if "address" in location:
                    address = location["address"]
                    if "addressLocality" in address:
                        addressLocality = address["addressLocality"]
                        if addressLocality != None:
                            locName += ", " + address["addressLocality"]
                        
                venuesFile.write(locName + "\n")
                        
            numberOfEvents += 1

        page += 1
        
    print("EVENTS")
    print(numberOfEvents)
        
eventsFile.close()
venuesFile.close()
