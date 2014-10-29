#! /usr/bin/python

import sys;
import re;
import math;
import random;

# Open Input File for Reading
infile = open(sys.argv[1],'rt');
# Open Output File for Writing
outfile = open(sys.argv[2],'w');

numSentences = sys.argv[3];

allSentences = [];
currSentence = [];
for line in infile:
    if re.match('^\s*$',line):
        continue
    # new sentence
    if re.match('^\s*1\s+',line):
        # if we had a sentence in the pipe
        if (len(currSentence) > 0):
            allSentences.append(currSentence)
            currSentence = [];
    currSentence.append(line)
# finish last sentence
if (len(currSentence) > 0):
    allSentences.append(currSentence)

sentenceCount = len(allSentences)

# convert % to num
matchObj = re.match('(\d+\.?\d+?)%',numSentences)
if (matchObj):
    print('converting percent to num...')
    percent = matchObj.group(1)
    numSentences = math.floor(float(percent) * sentenceCount / 100)
    print('converted ' + str(percent) + '% to ' + str(numSentences) + "\n")

numSentences = int(numSentences)

print('sampling ' + str(numSentences) + ' out of ' + str(sentenceCount) + ' sentences...')
allSentecesIndices = [];
while (len(allSentecesIndices) < numSentences):
    randNumber = int(math.floor(random.random() * sentenceCount))
    print('rand val is ' + str(randNumber))
    if (allSentecesIndices.count(randNumber) == 0):
	print('adding to allSentences, new len is ' + str(len(allSentecesIndices)) )
        allSentecesIndices.append(randNumber) 

print('sampled indices ' + str(allSentecesIndices))

# write output file
for index1 in allSentecesIndices:
    for line in allSentences[index1]:
    	outfile.write(line)
    outfile.write("\n")

# Close Input and Output Files
infile.close();
outfile.close();
