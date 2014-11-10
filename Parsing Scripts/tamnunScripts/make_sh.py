#! /usr/bin/python

import sys;
import re;
import os

# Open Input File for Reading
# infile format is:
#    filename=
#    workdir=
#    command=
#    userEmail=
#    q=
#    LD_LIBRARY_PATH=
infile = open(sys.argv[1],'rt');
filename = workdir = command = userEmail = q = LD_PATH = ""; 
for line in infile:
    # skip blanks
    if re.match('^\s*$',line):
        continue
    # skip comments
    if re.match('^\s*#',line):
        continue
    # find matches
    matchObj = re.match('^\s*filename\s*=\s*(\S+)\s*$',line);
    if (matchObj):
         filename = matchObj.group(1);
         print("found filename '" + filename + "'");
         # fix file suffix if needed
         if (not re.match(".*\.sh$",filename)):
             filename = filename + ".sh";
             print("fixed filename to '" + filename + "'");
         continue
    matchObj = re.match('^\s*workdir\s*=\s*(\S+)\s*$',line);
    if (matchObj):
        workdir = matchObj.group(1);
        print("found workdir '" + workdir + "'");
         # fix workdir if needed
        if (not re.match("^\$HOME/",workdir)):
            workdir = "$HOME/" + workdir;
            print("fixed workdir to '" + workdir + "'");
        realWorkDir = os.environ["HOME"] + "/" + workdir[6:];
        if (not os.path.isdir(realWorkDir)):
            print("workdir '" + realWorkDir + "' does not exist, creating it");
            os.mkdir(realWorkDir);
        continue
    matchObj = re.match('^\s*command\s*=\s*(.*)\s*$',line);
    if (matchObj):
        command = matchObj.group(1);
        print("found command '" + command + "'"); 
        continue
    matchObj = re.match('^\s*userEmail\s*=\s*(\S+)\s*$',line);
    if (matchObj):
        userEmail = matchObj.group(1);
        print("found userEmail '" + userEmail + "'");
        continue
    matchObj = re.match('^\s*q\s*=\s*(\S+)\s*$',line);
    if (matchObj):
        q = matchObj.group(1);
        print("found q '" + q + "'");
        continue
    matchObj = re.match('^\s*LD_LIBRARY_PATH\s*=\s*(\S+)\s*$',line);
    if (matchObj):
        LD_PATH = matchObj.group(1);
        print("found LD_LIBRARY_PATH '" + LD_PATH + "'");
        continue
infile.close();

outlines = [];
outlines.append("#!/bin/sh\n");
outlines.append("#\n"); 
outlines.append("# Submit the job to the queue \"queue_name\"\n");
outlines.append("#---------------------------------------------------\n");
outlines.append("#PBS -q " + q + "\n");
outlines.append("#\n");
outlines.append("# resource limits: number and distribution of parallel processes\n");
outlines.append("#------------------------------------------------------------------\n");
outlines.append("#PBS -l select=1:ncpus=1\n");
outlines.append("#\n");
outlines.append("# comment: this select statement means: use M chunks (nodes),\n");
outlines.append("# use N (=< 12) CPUs for N mpi tasks on each of M nodes.\n");
outlines.append("# \"scatter\" will use exactly N CPUs from each node, while omitting\n");
outlines.append("# \"-l place\" statement will fill all available CPUs of M nodes\n");
outlines.append("#\n");
outlines.append("#PBS -M " + userEmail + "\n");
outlines.append("#\n");
outlines.append("#  specifying working directory\n");
outlines.append("#------------------------------------------------------\n");
outlines.append("PBS_O_WORKDIR=" + workdir + "\n");
outlines.append("cd $PBS_O_WORKDIR\n");
if (LD_PATH != ""):
    outlines.append("LD_LIBRARY_PATH=" + LD_PATH + ":$LD_LIBRARY_PATH");
outlines.append("#\n");
outlines.append("# running MPI executable with M*N processes\n");
outlines.append("#\n");
outlines.append("#------------------------------------------------------\n");
outlines.append(command + "\n");
outlines.append("# comment: the \"np\" must be equal the number of chunks multiplied by the number of \"ncpus\"\n");

outfile = open(filename,'w');
outfile.writelines(outlines);
outfile.close();
