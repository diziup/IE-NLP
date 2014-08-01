#! /usr/bin/python
#See READMEconll2mst file for credits and instructions
#This script takes conll format and transfers it to MstParser format
import sys;

# Open Input File for Reading
infile = open(sys.argv[1],'rt');
# Open Output File for Writing
outfile = open(sys.argv[2],'w');
wrds = ""; pos = ""; labs = ""; par = "";

for line in infile:
    
    sent = line.split();

    if len(sent) > 0:
        wrds += sent[1] + "\t";
        pos += sent[4] + "\t";
        labs += sent[7] + "\t";
        par += sent[6] + "\t";
    else:
        outfile.write(wrds); outfile.write('\n'); wrds = "";
        outfile.write(pos); outfile.write('\n');  pos = "";
        outfile.write(labs); outfile.write('\n'); labs = "";
        outfile.write(par); outfile.write('\n');  par = "";
        outfile.write('\n');
		
# Close Input and Output Files
infile.close();
outfile.close();
