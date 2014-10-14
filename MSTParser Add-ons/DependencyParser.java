package mstparser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import gnu.trove.*;

public class DependencyParser {

    public static String trainfile = null;
    public static String testfile = null;
    public static String trainforest = null;
    public static String testforest = null;
    public static boolean train = false;
    public static boolean eval = false;
    public static boolean test = false;
    public static String modelName = "dep.model";
    public static String lossType = "punc";
    public static boolean createForest = true;
    public static String decodeType = "proj";
    public static int numIters = 10;
    public static String outfile = "out.txt";
    public static String goldfile = null;
    public static int trainK = 1;
    public static int testK = 1;
    public static boolean secondOrder = false;


    private DependencyPipe pipe;
    private DependencyDecoder decoder;
    private Parameters params;

    public DependencyParser(DependencyPipe pipe) {
	this.pipe=pipe;
	// Set up arrays
	params = new Parameters(pipe.dataAlphabet.size());
	decoder = secondOrder ? new DependencyDecoder2O(pipe) : new DependencyDecoder(pipe);
    }

    public void train(DependencyInstance[] il, String trainfile, String train_forest) throws IOException {
		
	System.out.println("About to train");
	System.out.println("Num Feats: " + pipe.dataAlphabet.size());
		
	int i = 0;
	for(i = 0; i < numIters; i++) {
			
	    System.out.println("========================");
	    System.out.println("Iteration: " + i);
	    System.out.println("========================");
	    System.out.print("Processed: ");

	    long start = System.currentTimeMillis();

	    trainingIter(il,trainfile,train_forest,i+1);

	    long end = System.currentTimeMillis();
	    System.out.println("Training iter took: " + (end-start));
			
	}

	params.averageParams(i*il.length);
		
    }

    private void trainingIter(DependencyInstance[] il, String trainfile, String train_forest, int iter) throws IOException {

	int numUpd = 0;
	ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));
	boolean evaluateI = true;

	for(int i = 0; i < il.length; i++) {
	    if((i+1) % 500 == 0)
		System.out.println("  "+(i+1)+" instances");

	    DependencyInstance inst = il[i];
		
	    int length = inst.length;

	    // Get production crap.
	    FeatureVector[][][] fvs = new FeatureVector[length][length][2];
	    double[][][] probs = new double[length][length][2];
	    FeatureVector[][][][] nt_fvs = new FeatureVector[length][pipe.types.length][2][2];
	    double[][][][] nt_probs = new double[length][pipe.types.length][2][2];
	    FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
	    double[][][] probs_trips = new double[length][length][length];
	    FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
	    double[][][] probs_sibs = new double[length][length][2];

	    if(secondOrder)
		inst = ((DependencyPipe2O)pipe).getFeatureVector(in,inst,fvs,probs,
								 fvs_trips,probs_trips,
								 fvs_sibs,probs_sibs,
								 nt_fvs,nt_probs,params);
	    else
		inst = pipe.getFeatureVector(in,inst,fvs,probs,nt_fvs,nt_probs,params);

	    double upd = (double)(numIters*il.length - (il.length*(iter-1)+(i+1)) + 1);
	    int K=trainK;
	    Object[][] d = null;
	    if(decodeType.equals("proj")) {
		if(secondOrder)
		    d = ((DependencyDecoder2O)decoder).decodeProjective(inst,fvs,probs,
									fvs_trips,probs_trips,
									fvs_sibs,probs_sibs,
									nt_fvs,nt_probs,K);
		else
		    d = decoder.decodeProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
	    }
	    if(decodeType.equals("non-proj")) {
		if(secondOrder)
		    d = ((DependencyDecoder2O)decoder).decodeNonProjective(inst,fvs,probs,
								       fvs_trips,probs_trips,
								       fvs_sibs,probs_sibs,
								       nt_fvs,nt_probs,K);
		else
			{
			//stat. object Counting the number of sentences
			Statistics stat = Statistics.getInstance( );
			stat.addSentence( );				
		    d = decoder.decodeNonProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
			}
	    }
	    params.updateParamsMIRA(inst,d,upd);

	}
	System.out.println("");
	
	System.out.println("  "+il.length+" instances");
		
	in.close();

    }

    ///////////////////////////////////////////////////////
    // Saving and loading models
    ///////////////////////////////////////////////////////
    public void saveModel(String file) throws IOException {
	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
	out.writeObject(params.parameters);
	out.writeObject(pipe.dataAlphabet);
	out.writeObject(pipe.typeAlphabet);
	out.close();
    }

    public void loadModel(String file) throws Exception {
	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
	params.parameters = (double[])in.readObject();
	pipe.dataAlphabet = (Alphabet)in.readObject();
	pipe.typeAlphabet = (Alphabet)in.readObject();
	in.close();
	pipe.closeAlphabets();
    }

    //////////////////////////////////////////////////////
    // Get Best Parses ///////////////////////////////////
    //////////////////////////////////////////////////////
    public void outputParses(String tFile, String file)
	throws IOException {

	long start = System.currentTimeMillis();

	BufferedWriter pred = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF8"));

	BufferedReader in =
	    new BufferedReader(new InputStreamReader(new FileInputStream(tFile),"UTF8"));
	System.out.print("Processing Sentence: ");
	DependencyInstance il = pipe.createInstance(in);
	int cnt = 0;
	while(il != null) {
	    cnt++;
	    System.out.print(cnt+" ");
	    String[] toks = il.sentence;
			
	    int length = toks.length;

	    FeatureVector[][][] fvs = new FeatureVector[toks.length][toks.length][2];
	    double[][][] probs = new double[toks.length][toks.length][2];
	    FeatureVector[][][][] nt_fvs = new FeatureVector[toks.length][pipe.types.length][2][2];
	    double[][][][] nt_probs = new double[toks.length][pipe.types.length][2][2];
	    FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
	    double[][][] probs_trips = new double[length][length][length];
	    FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
	    double[][][] probs_sibs = new double[length][length][2];
	    if(secondOrder)
		((DependencyPipe2O)pipe).getFeatureVector(il,fvs,probs,
							  fvs_trips,probs_trips,
							  fvs_sibs,probs_sibs,
							  nt_fvs,nt_probs,params);
	    else
		pipe.getFeatureVector(il,fvs,probs,nt_fvs,nt_probs,params);

	    int K = testK;
	    Object[][] d = null;
	    if(decodeType.equals("proj")) {
		if(secondOrder)
		    d = ((DependencyDecoder2O)decoder).decodeProjective(il,fvs,probs,
									fvs_trips,probs_trips,
									fvs_sibs,probs_sibs,
									nt_fvs,nt_probs,K);
		else
		    d = decoder.decodeProjective(il,fvs,probs,nt_fvs,nt_probs,K);
	    }
	    if(decodeType.equals("non-proj")) {
		if(secondOrder)
		    d = ((DependencyDecoder2O)decoder).decodeNonProjective(il,fvs,probs,
								       fvs_trips,probs_trips,
								       fvs_sibs,probs_sibs,
								       nt_fvs,nt_probs,K);
		else
		  {
			//stat. object Counting the number of sentences
			Statistics stat = Statistics.getInstance( );
			stat.addSentence( );		
		    d = decoder.decodeNonProjective(il,fvs,probs,nt_fvs,nt_probs,K);
		  }
	    }

	    String[] res = ((String)d[0][1]).split(" ");
	    String[] sent = il.sentence;
	    String[] pos = il.pos;
	    String line1 = ""; String line2 = ""; String line3 = ""; String line4 = "";
	    for(int j = 1; j < pos.length; j++) {
		String[] trip = res[j-1].split("[\\|:]");
		line1+= sent[j] + "\t"; line2 += pos[j] + "\t";
		line4 += trip[0] + "\t"; line3 += pipe.types[Integer.parseInt(trip[2])] + "\t";
	    }
	    pred.write(line1.trim() + "\n" + line2.trim() + "\n"
		       + (pipe.labeled ? line3.trim() + "\n" : "")
		       + line4.trim() + "\n\n");
	    
	    il = pipe.createInstance(in);
	}
	System.out.println();
		
	pred.close();
	in.close();
		
	long end = System.currentTimeMillis();
	System.out.println("Took: " + (end-start));

    }

    /////////////////////////////////////////////////////
    // RUNNING THE PARSER
    ////////////////////////////////////////////////////
    public static void main (String[] args) throws FileNotFoundException, Exception
    {

		
	processArguments(args);
	//stat. object for later use
	Statistics stat = Statistics.getInstance( );
			
	if(train) {
		
	    DependencyPipe pipe =
		secondOrder ? new DependencyPipe2O (createForest) : new DependencyPipe (createForest);
			
	    pipe.setLabeled(trainfile);

	    DependencyInstance[] trainingData = pipe.createInstances(trainfile,trainforest);
			
	    pipe.closeAlphabets();
			
	    DependencyParser dp = new DependencyParser(pipe);

	    int numFeats = pipe.dataAlphabet.size();
	    int numTypes = pipe.typeAlphabet.size();
	    System.out.println("Num Feats: " + numFeats);	
	    System.out.println("Num Edge Labels: " + numTypes);
		//Set stat. flag to Train and write to file
		stat.setFlag(1);
		stat.writeToStatsFile("Train data for " + outfile + " :");
	    dp.train(trainingData,trainfile,trainforest);
	
	    System.out.print("Saving model ... ");
	    dp.saveModel(modelName);
	    System.out.println("done.");
			
	}
		
	if (test) {
	    DependencyPipe pipe =
		secondOrder ? new DependencyPipe2O (true) : new DependencyPipe (true);
	    pipe.setLabeled(testfile);
	    DependencyParser dp = new DependencyParser(pipe);

	    System.out.println("\nLoading model ... ");
	    dp.loadModel(modelName);
	    System.out.println("done.");

	    pipe.closeAlphabets();
		//Set stat. flag to Test and write to file
		stat.setFlag(2);
		stat.writeToStatsFile("Test data for " + outfile + " :");
	    dp.outputParses(testfile,outfile);
	}
		
	if(eval) {
	    System.out.println("\nEVALUATION PERFORMANCE:");
	    DependencyEvaluator.evaluate(goldfile,outfile);
	}
		//Print the statistics
		stat.printStat();	
    }

    public static void processArguments(String[] args) {
	//stat. object for the stats-file
	Statistics stat = Statistics.getInstance( );
	for(int i = 0; i < args.length; i++) {
	    String[] pair = args[i].split(":");
	    if(pair[0].equals("train")) {
		train = true;
	    }
	    if(pair[0].equals("eval")) {
		eval = true;
	    }
	    if(pair[0].equals("test")) {
		test = true;
	    }
	    if(pair[0].equals("iters")) {
		numIters = Integer.parseInt(pair[1]);
	    }
	    if(pair[0].equals("output-file")) {
		outfile = pair[1];
	    }
	    if(pair[0].equals("gold-file")) {
		goldfile = pair[1];
	    }
	    if(pair[0].equals("train-file")) {
		trainfile = pair[1];
	    }
	    if(pair[0].equals("test-file")) {
		testfile = pair[1];
	    }
	    if(pair[0].equals("model-name")) {
		modelName = pair[1];
	    }
	    if(pair[0].equals("training-k")) {
		trainK = Integer.parseInt(pair[1]);
	    }
	    if(pair[0].equals("loss-type")) {
		lossType = pair[1];
	    }			
	    if(pair[0].equals("order") && pair[1].equals("2")) {
		secondOrder = true;
	    }			
	    if(pair[0].equals("create-forest")) {
		createForest = pair[1].equals("true") ? true : false;
	    }			
	    if(pair[0].equals("decode-type")) {
		decodeType = pair[1];
	    }	
	    if(pair[0].equals("stats-file")) {
		//stat. object for setting the statistics output file
		stat.setStatsFile(pair[1]);
	    }		
	}
	trainforest = trainfile == null ? null : trainfile+".forest";
	testforest = testfile == null ? null : testfile+".forest";
	
	System.out.println("------\nFLAGS\n------");
	System.out.println("train-file: " + trainfile);
	System.out.println("test-file: " + testfile);
	System.out.println("gold-file: " + goldfile);
	System.out.println("output-file: " + outfile);
	System.out.println("stats-file: " + stat.getStatsFile());
	System.out.println("model-name: " + modelName);
	System.out.println("train: " + train);
	System.out.println("test: " + test);
	System.out.println("eval: " + eval);
	System.out.println("loss-type: " + lossType);
	System.out.println("second-order: " + secondOrder);
	System.out.println("training-iterations: " + numIters);
	System.out.println("training-k: " + trainK);
	System.out.println("decode-type: " + decodeType);
	System.out.println("create-forest: " + createForest);
	System.out.println("------\n");
    }
}
