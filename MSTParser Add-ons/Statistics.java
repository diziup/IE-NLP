package mstparser;

import java.io.*;
import java.nio.file;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import gnu.trove.*;

public class Statistics {

   private static Statistics statistics = new Statistics( );
   protected int modeFlag; 		    	//1 = Train, 2 = Test
   protected int hasCycle;				//1 = The sentence has a cycle, 0 = doesn't have
   protected String statsFile;			//The statistics file
   //Train data counters
   protected int trainCycleNum; 		//Counts the total # of cycles
   protected int trainSentenceNum; 	    //Counts the total # of sentences
   protected int trainCycleSentenceNum; //Counts the total # of sentences that have a cycle in them
   protected int trainManyCycleSentence;//Counts the total # of sentences that have more than 1 cycle in them
   protected double trainCycleAvg;		//Avg. cycles per sentence
   protected double trainTreeAvg;		//Avg. MST size - Currently not in use
   protected double trainCycleSizeAvg;	//Avg. nodes in a cycle   
   //Test data counters
   protected int testCycleNum; 			//Counts the total # of cycles
   protected int testSentenceNum; 	    //Counts the total # of sentences
   protected int testCycleSentenceNum;  //Counts the total # of sentences that have a cycle in them
   protected int testManyCycleSentence; //Counts the total # of sentences that have more than 1 cycle in them
   protected double testCycleAvg;		//Avg. cycles per sentence
   protected double testTreeAvg;		//Avg. MST size - Currently not in use
   protected double testCycleSizeAvg;	//Avg. nodes in a cycle

   
   /* A private Constructor prevents any other 
    * class from instantiating.
    */
   private Statistics(){ }
   
   /* Static 'instance' method */
   public static Statistics getInstance( ) {
      return statistics;
   }
   /* Other methods protected by Statistics-ness */
   protected static void addCycle( ) {
	  switch (statistics.modeFlag) {
            case 1:  statistics.trainCycleNum += 1;
                     break;
            case 2:  statistics.testCycleNum += 1;
                     break;
	  }		      
   }
   
   protected static void addCycleNodes( int val ) {
	  switch (statistics.modeFlag) {
            case 1:  statistics.trainCycleSizeAvg += val;
                     break;
            case 2:  statistics.testCycleSizeAvg += val;
                     break;
	  }		      
   }
   
   protected static void addSentence( ) {
	  switch (statistics.modeFlag) {
            case 1:  statistics.trainSentenceNum += 1;
                     break;
            case 2:  statistics.testSentenceNum += 1;
                     break;
	  }					       
   }
   
   protected static void setStatsFile( String val ) {
      statistics.statsFile = val; 
   } 
   
   protected static void setFlag( int val ) {
      statistics.modeFlag = val; 
   } 
   
   protected static void setHasCycle( int val ) {
      statistics.hasCycle = val; 
   }
   
   protected static void addCycleSentence( ) {
	  if (statistics.hasCycle == 1) { 
		  addManyCycle( ); 
		  setHasCycle(2);
	  }
	  if (statistics.hasCycle == 0) {	
		  switch (statistics.modeFlag) {
				case 1:  statistics.trainCycleSentenceNum += 1;
						 break;
				case 2:  statistics.testCycleSentenceNum += 1;
						 break;
				}
		 setHasCycle(1);			
		}
   } 
   
   protected static void addManyCycle( ) {
		  switch (statistics.modeFlag) {   
				case 1:  statistics.trainManyCycleSentence += 1;
						 break;
				case 2:  statistics.testManyCycleSentence += 1;
						 break;
				}
   }
	//Writes the input string s to the Stats file
   protected static void writeToStatsFile( String s ) {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(statistics.statsFile),"UTF8"))) {
			writer.write(s, 0, s.length());
			writer.write("\n");
			writer.close();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
   }  
   
   protected static void printStat( ) {
	  //First set the avg. cycles per sent. and avg. nodes per cycle
	  statistics.trainCycleAvg = (double)statistics.trainCycleNum / statistics.trainSentenceNum;
	  statistics.testCycleAvg = (double)statistics.testCycleNum / statistics.testSentenceNum;
	  statistics.trainCycleSizeAvg = (double)statistics.trainCycleSizeAvg / statistics.trainCycleNum;
	  statistics.testCycleSizeAvg = (double)statistics.testCycleSizeAvg / statistics.testCycleNum;	  
	  //Print statistics
      System.out.println("\nTrain data:");	  
	  System.out.println("Total # of cycles:" + statistics.trainCycleNum + "\n" + "Total # of sentences:" + statistics.trainSentenceNum + "\n" + 
	  "Total # of sentences that have a cycle in them:" + statistics.trainCycleSentenceNum + "\n" + "Total # of sentences with >1 cycles:" + statistics.trainManyCycleSentence + "\n" + 
	  "Avg. cycles per sentence:" + statistics.trainCycleAvg  + "\n" + "Avg. nodes in a cycle:" + statistics.trainCycleSizeAvg);
	  System.out.println("\n========================");
	  System.out.println("\nTest data:");
	  System.out.println("Total # of cycles:" + statistics.testCycleNum + "\n" + "Total # of sentences:" + statistics.testSentenceNum + "\n" + 
	  "Total # of sentences that have a cycle in them:" + statistics.testCycleSentenceNum + "\n" + "Total # of sentences with >1 cycles:" + statistics.testManyCycleSentence + "\n" + 
	  "Avg. cycles per sentence:" + statistics.testCycleAvg + "\n" + "Avg. nodes in a cycle:" + statistics.testCycleSizeAvg);
	  writeToStatsFile("The stats-file is made out of: Cycles lengths|Max cycle length|Number of cycles|Number of words in iter");	
   }        
}