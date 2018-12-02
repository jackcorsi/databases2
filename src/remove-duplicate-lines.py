inFilePath = "eventbrite-venues.txt"
outFilePath = "eventbrite-venues-filtered.txt"

inFile = open(inFilePath, "r")
outFile = open(outFilePath, "w")

prevLine = ""
lines = []

for line in inFile:
    lines.append(line)

inFile.close()

lines.sort()

prevLine = ""
for line in lines:
    if line != prevLine:
        outFile.write(line)
        prevLine = line

outFile.close()
