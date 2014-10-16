#! /usr/bin/python
from __future__ import division;
import sys;


# Open File
f = open(sys.argv[1],'rt');
train = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];
test = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];
state ="";i = 0;totaltrain = 0;totaltest = 0;
for line in f:
#Set the state we are in
	ln = line.split();	
	if ln[0] == "Train":
 	 state = "train";
	 continue;
	if ln[0] == "Test":
	 state = "test";
	 continue;
	if ln[0] == "The":
	 continue;
	sent = line.split('|');	
#Get the data	
	if state == "train":
	 if len(sent) > 0:
	  i = int(sent[1]);
	  if i >= 0:   
	   train[i] += 1;
	   totaltrain += 1;
	if	state == "test":
	 if len(sent) > 0:
	  i = int(sent[0]);
	  if i >= 0:
	   test[i] += 1;
	   totaltest += 1;

if totaltrain > 0:	   
	print "\nTrain data for ",
	print totaltrain,
	print "cycles:";   
	for j in range(len(train)):
	 if train[j] == 0:
	  continue;
	 print "Cycle length:",
	 print j,
	 print "Percentage: ",
	 per = train[j] / totaltrain;
	 print per,
	 print "Weight: ", 
	 print (j * per);

if totaltest > 0:	 
	print "\nTest data for ",
	print totaltest,
	print "cycles:";  
	for j in range(len(test)):
	 if test[j] == 0:
	  continue;
	 print "Cycle length:",
	 print j,
	 print "Percentage: ",
	 per = test[j] / totaltest;
	 print per,
	 print "Weight: ", 
	 print (j * per);
 
f.close();

