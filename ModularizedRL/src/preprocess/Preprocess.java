/*
 * Preprocess.java
 *
 * Created on February 9, 2005, 2:51 PM
 */

package preprocess;

import parameters.DataParameters;
import parameters.LearnerParameters;
import structures.data.converters.input.CsvTabConvert;
import structures.data.converters.input.TabCsvDataLoader;
import structures.data.converters.output.OutputArff;
import structures.data.converters.output.OutputDataset;
import structures.data.converters.output.OutputTabOrCSV;
import data.dataset.*;
//import structures.learner.attribute.VHierarchyNode;
//import util.ArrayUtil;
//import util.HierarchyConstructor;
import util.Transpose;
import util.file.*;
import parameters.ProcessParams;
import preprocess.Remove;
import parameters.PreProcessParameters;
import data.discretize.Discretizers;
//import preprocess.math.*;

import java.io.*;

/**
 * @author Jonathan Lustgarten
 */
public class Preprocess {
	private Scaler scaler;
	private ChiTrim chiTrimmer;
	private Dataset trainData;
	private Dataset testData; // Test set given by the user as a separate file
	private Dataset indepTestData; // Test set created as a split from the training set
	private Dataset sourceData; // Data set for transfer of learned models or selected attributes
	private Discretizers discretizer;
	private CsvTabConvert csvConverter;
	private Remove removalFilter;
	private Transpose transposer;
	private DataParameters dataParams;
	private PreProcessParameters preProcParams;
	private static final String FILE_PATH_SEP = System.getProperty("file.separator");

	public Preprocess() {
		this(null, new Dataset());
	}

	// This is the constructor that RBL.run() calls 
	public Preprocess(DataParameters dp,
			PreProcessParameters ppp, LearnerParameters lp) {
		this(ppp, new Dataset());
		dataParams = dp;
	}

	/*
	public Preprocess(DataParameters dataParam,
			PreProcessParameters preProcParam,
			RuleLearnerParameters learningParam, boolean recordSets) {
		this(dataParam, preProcParam, learningParam);
	}
	*/

	/* This constructor is not used --PG2009 
	public Preprocess(PreProcessParameters ppp, Dataset[] dataSets) {
		this(ppp, dataSets[0]);
		if (dataSets.length >= 2)
			testData = dataSets[1];
	}
	 */
	
	public Preprocess(PreProcessParameters ppp, Dataset trainData) {
		discretizer = new Discretizers();
		csvConverter = new CsvTabConvert();
		preProcParams = ppp;
		this.trainData = trainData;
	}

	private boolean createOutDir(String name) {
		if (name != null && !name.equalsIgnoreCase("")) {
			File dir = new File(name);
			if (dir.exists() || dir.mkdir())
				return true;
		}
		return false;
	}

	private Dataset loadFile(String fileName, String sep) throws Exception {
		TabCsvDataLoader tcsvl = new TabCsvDataLoader(fileName, 
				dataParams.getXMLAttributeFile());
			Dataset d = tcsvl.loadData(sep);
			if (preProcParams != null)
				d.iRepAtt = preProcParams.getReplicateAttIndex();
			return d;
	}

	/* Need to modify to join two files then separate them */
	private void scaleData() throws AttributeDoesNotExistException {
		scaler = new Scaler(trainData, testData, sourceData);
		scaler.run(preProcParams.getScaleParams());
		scaler = null;
	}

	private void runMDL() throws AttributeDoesNotExistException {
		discretizer.setData(trainData);
		discretizer.setMehtod(preProcParams.getDiscMethod(), preProcParams.getDiscMethodVal());
		double[][] cutPoints = discretizer.discretizeMDL();
		trainData.setDiscretization(cutPoints);
		if (testData != null) {
			testData.setDiscretization(cutPoints, trainData);
		}
		if (indepTestData != null)
			indepTestData.setDiscretization(cutPoints, trainData);

		//if (sourceData != null) {
		//	// Here we can do union transfer:
		//	// If there are any attributes that are in the source but not in the target 
		//	// data set, then add the closest ones from the target data set, with 
		//	// discretization cut points taken from the source data set.
		//
		// Impose train discretization on the source data after BSS and CV on the 
		// source data, just before learning the prior rule model -- not here!
		//	System.out.println("Setting source data discretization");
		//	sourceData.setDiscretization(cutPoints, trainData);
		//}
		
		if (!preProcParams.shouldRemoveAttribute()) {
			try {
				String fileName = util.Util.replaceFileNameSuffix(trainData.getFileName(), "atts.xml");
				SaveAttributesXML saxml = new SaveAttributesXML(trainData,
						new File(dataParams.getOutDirName() + FILE_PATH_SEP + fileName),
								trainData.getFileName());
				saxml.save();
			} catch (Exception e) {
				System.err.println("Cannot save attributes:\n" + e.getStackTrace());
			}
		}
	}

	/**
	 * Removes the attributes that have no cut points.
	 */
	public void removeTrivialAtts() {
		if (testData != null)
			removalFilter = new Remove(trainData, testData);
		else
			removalFilter = new Remove(trainData);

		removalFilter.removeAttributes();
		trainData = removalFilter.getTrainSet();
		if (testData != null)
			testData = removalFilter.getTestSet();
		if (indepTestData != null) {
			removalFilter = new Remove(indepTestData);
			removalFilter.removeAttributes();
			indepTestData = removalFilter.getTrainSet();
		}
		if (sourceData != null) {
			removalFilter = new Remove(sourceData);
			removalFilter.removeAttributes();
			sourceData = removalFilter.getTrainSet();
		}
		try {
			SaveAttributesXML saxml = new SaveAttributesXML(trainData, 
					new File(
						dataParams.outDirName + FILE_PATH_SEP
						+ util.Util.replaceFileNameSuffix(trainData.getFileName(),"atts.xml")), 
					trainData.getFileName());
			saxml.save();
		} catch (Exception e) {
			System.err.println("Cannot save attributes: " + e.getMessage());
		}
	}
	
	public void writeFiles(String suff, boolean useDisc) {
		if (dataParams.outputFormat == DataParameters.CSV
				|| dataParams.outputFormat == DataParameters.TSV) {
			String sep = (dataParams.outputFormat == DataParameters.CSV) ? "," : "\t";
			System.out.println("Writing training file...");
			writeCSVFile(trainData, sep, useDisc, suff);
			if (dataParams.hasTestSet && testData != null) {
				System.out.println("Writing test file...");
				writeCSVFile(testData, sep, useDisc, suff);
			}
			if (indepTestData != null) {
				System.out.println("Writing independent test file...");
				writeCSVFile(indepTestData, sep, useDisc, suff);
			}
			if (sourceData != null) {
				System.out.println("Writing source file...");
				writeCSVFile(sourceData, sep, useDisc, suff);
			}
		} else if (dataParams.outputFormat == DataParameters.ARFF) {
			System.out.println("Writing training file...");
			writeArffFile(trainData, useDisc, suff);
			if (dataParams.hasTestSet) {
				System.out.println("Writing test file...");
				writeArffFile(testData, useDisc, suff);
			}
			if (indepTestData != null) {
				System.out.println("Writing independent test file...");
				writeArffFile(indepTestData, useDisc, suff);
			}
			if (sourceData != null) {
				System.out.println("Writing source file...");
				writeArffFile(sourceData, useDisc, suff);
			}
			if (sourceData != null) {
				System.out.println("Writing source file...");
				writeArffFile(sourceData, useDisc, suff);
			}
		}
	}

	private void writeCSVFile(Dataset data, String sep, boolean useDisc, String suffix) {
		String fileName = util.Util.appendFileNameSuffix(data.getFileName(), suffix);
		if (useDisc)
			fileName = util.Util.appendFileNameSuffix(data.getFileName(), "d");
		OutputDataset od = new OutputTabOrCSV(data, 
				dataParams.getOutDirName() + FILE_PATH_SEP + fileName, sep);
		od.printDataset(useDisc);		
	}
	
	private void writeArffFile(Dataset data, boolean useDisc, String suffix) {
		String fileName = util.Util.replaceFileNameSuffix(data.getFileName(), suffix);
		OutputDataset od = new OutputArff(data, 
				dataParams.getOutDirName() + FILE_PATH_SEP + fileName + ".arff");
		od.printDataset(useDisc);		
	}
	
	/**
	 * Generates an independent test set
	 * 
	 * @param percent
	 *            the percent to leave out
	 */
	private void createTestSet(int percent) {
		Dataset trainSet;
		System.out.println("Creating an independent test set using approximately "
						+ percent + "% of the training set");
		trainSet = trainData.trainSplit(percent);
		OutputDataset od = new OutputTabOrCSV(trainSet, dataParams.getOutDirName() 
				+ FILE_PATH_SEP + trainData.getFileName(), "\t");
		od.printDataset(true);
		indepTestData = trainData.testSplit(percent);
		od = new OutputTabOrCSV(indepTestData, dataParams.getOutDirName() + FILE_PATH_SEP
				+ indepTestData.getFileName(), "\t");
		od.printDataset(true);
		trainData = trainSet;
	}

	/*
	private void getInputDirFiles(String dirName, boolean isTestSet) throws Exception {
		CombineFile cf = new CombineFile((dataParams.trainFileSep));
		// If inputCSV is true, then dir contains CSV files
		if (isTestSet)
			testData = cf.mergeFiles(dirName);
		else
			trainData = cf.mergeFiles(dirName);
	}
	*/
	
	private void chiSquareTrim() throws AttributeDoesNotExistException {
		if (!preProcParams.shouldDiscretize()) {
			discretizer.setData(trainData);
			discretizer.setMehtod(Discretizers.EBD, 1);
			double[][] cutPts = discretizer.discretizeMDL();
			trainData.setDiscretization(cutPts);
			if (testData != null)
				testData.setDiscretization(cutPts, trainData);
			if (indepTestData != null)
				indepTestData.setDiscretization(cutPts, trainData);
		}
		chiTrimmer = new ChiTrim(trainData, preProcParams.getChiSqTrimThreshold());
		int[] attsTR = chiTrimmer.trim();
		Remove remAtt = new Remove(trainData, testData);
		remAtt.removeAttributes(attsTR, false);
		trainData = remAtt.getTrainSet();
		testData = remAtt.getTestSet();
		remAtt = null;
		chiTrimmer = null;
		if (indepTestData != null) {
			remAtt = new Remove(indepTestData);
			remAtt.removeAttributes(attsTR, false);
			indepTestData = remAtt.getTrainSet();
		}
	}

	private void addToNames(String suffix) {
		trainData.setFileName(util.Util.appendFileNameSuffix(trainData.getFileName(), suffix));
		if (testData != null)
			testData.setFileName(util.Util.appendFileNameSuffix(testData.getFileName(), suffix));
		if (indepTestData != null)
			indepTestData.setFileName(util.Util.appendFileNameSuffix(indepTestData.getFileName(), suffix));
		if (sourceData != null)
			sourceData.setFileName(util.Util.appendFileNameSuffix(sourceData.getFileName(), suffix));
	}

	@SuppressWarnings("static-access")
	private void createFolds() throws AttributeDoesNotExistException {
		int numFolds = preProcParams.getNumFolds();
		Dataset origDat = trainData;
		Dataset origTstDat = testData;
		Dataset origValDat = indepTestData;
		boolean hadTestSet = dataParams.hasTestSet;
		for (int i = 0; i < numFolds; i++) {
			if (preProcParams.shouldDoRRV()) {
				trainData = origDat.rrvTrainSplit(100 - preProcParams.getRRVPercent());
				testData = origDat.rrvTestSplit(preProcParams.getRRVPercent());
			} else {
				try {
					trainData = origDat.trainCV(numFolds, i);
				} catch (NullDatasetException e) {
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
					System.exit(1);
				}
				try {
					testData = origDat.testCV(numFolds, i);
				} catch (NullDatasetException e) {
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
					System.exit(1);
				}
			}
			dataParams.hasTestSet = true;
			processDatasets();
		}
		dataParams.hasTestSet = hadTestSet;
		testData = origTstDat;
		indepTestData = origValDat;
		trainData = origDat;
	}

	private void createSplit() throws AttributeDoesNotExistException {
		Dataset origDat = trainData;
		Dataset origTstDat = testData;
		Dataset origValDat = indepTestData;
		int percent = preProcParams.getPercentSplit();
		trainData = origDat.trainSplit(percent);
		testData = origDat.testSplit(percent);
		processDatasets();
		testData = origTstDat;
		indepTestData = origValDat;
		trainData = origDat;
	}

	private void averageReplicates() 
	throws AttributeDoesNotExistException, ValueNotFoundException, IncompatibleDatatypeException {
		int i = preProcParams.getReplicateAttIndex();

		String s = (i >= 0) ? 
				(" specified in attribute " + trainData.attribute(i).name() + "(" + i +")") 
				: "";
		System.out.print("Combining technical replicates" + s + "...");
		
		AverageInstances ctr = new AverageInstances(trainData, i);
		trainData = ctr.averageReplicates();
		if (testData != null) {
			ctr = new AverageInstances(testData, i);
			testData = ctr.averageReplicates();
		}
		if (indepTestData != null) {
			ctr = new AverageInstances(indepTestData, i);
			indepTestData = ctr.averageReplicates();
		}
		if (sourceData != null) {
			ctr = new AverageInstances(sourceData, i);
			sourceData = ctr.averageReplicates();
		}
		System.out.println("Done.");
	}
	
	private void processDatasets() throws AttributeDoesNotExistException {
		if (preProcParams.shouldScale()) {
			scaleData();
			if (preProcParams.shouldWriteInterData())
				writeFiles("sc", false);
			addToNames("sc");
		}
		if (preProcParams.shouldDiscretize()) {
			runMDL();
			if (preProcParams.shouldWriteInterData())
				writeFiles(Discretizers.getMethodName(preProcParams.getDiscMethod(), 
						preProcParams.getDiscMethodVal()), true);
			addToNames(Discretizers.getMethodName(preProcParams.getDiscMethod(), 
					preProcParams.getDiscMethodVal()));
		}
		if (preProcParams.shouldRemoveAttribute()) {
			if (preProcParams.shouldDiscretize())
				removeTrivialAtts();
			else {
				runMDL();
				removeTrivialAtts();
			}
			if (preProcParams.shouldWriteInterData())
				writeFiles("trim", true);
			addToNames("trim");
		}
		if (preProcParams.shouldChiSqTrim()) {
			chiSquareTrim();
			if (preProcParams.shouldWriteInterData())
				writeFiles("chi", true);
			addToNames("chi");
		}
		// If we don't save intermediate data, save the final pre-processed data
		if (!preProcParams.shouldWriteInterData()) {
			writeFiles(null, preProcParams.shouldDiscretize());
		}
	}
	
	/**
	 * Loads the data sets specified in the command line parameters
	 * @throws Exception 
	 */
	private void loadData() throws Exception {
		CombineFile fileCombiner;
		if (dataParams.isTrainDir()) {
			fileCombiner = new CombineFile(dataParams.trainFileSep);
			trainData = fileCombiner.mergeFiles(dataParams.getTrainDirName());
		} else {
			trainData = loadFile(dataParams.trainDataFileName, dataParams.trainFileSep);
			//loadFile(dataParams.getTrainFileName(), false, "\t");
		}
		if (dataParams.isTestDir()) {
			System.out.println("Loading test data from directory " + dataParams.sourceDataFileName + "...");
			fileCombiner = new CombineFile(dataParams.testFileSep);
			testData = fileCombiner.mergeFiles(dataParams.getInputTestDir());
		} else if (dataParams.hasTestSet) {
			System.out.println("Loading test data from file " + dataParams.sourceDataFileName + "...");
			testData = loadFile(dataParams.getTestFileName(), dataParams.testFileSep);
			//loadFile(dataParams.getTestFileName(), true, "\t");
		}
		if (dataParams.sourceDataFileName != null) {
			if (dataParams.isSourceDir()) {
				System.out.println("Loading source data from directory " + dataParams.sourceDataFileName + "...");
				// Assume that the source file and the target training file have the same column separator
				fileCombiner = new CombineFile(dataParams.trainFileSep);
				sourceData = fileCombiner.mergeFiles(dataParams.sourceDataFileName);
			} else {
				System.out.println("Loading source data from file " + dataParams.sourceDataFileName + "...");				
				sourceData = loadFile(dataParams.sourceDataFileName, dataParams.trainFileSep);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void run() throws Exception {
		if (dataParams.shouldConvertTabCSV()) {
			System.out.println("Converting " + dataParams.trainDataFileName);
			try {
				csvConverter.loadAndConvert(dataParams.trainDataFileName);
			} catch (IOException e) {
				System.err.println("Cannot convert file.");
				e.printStackTrace();
			}
		} else if (dataParams.shouldTransposeOnly()) {
			// System.out.println("Transposing file " + InputTransposeFile);
			try {
				transposer = new Transpose("t" + dataParams.trainDataFileName);
				transposer.setInputFile(dataParams.trainDataFileName);
				transposer.loadFile(dataParams.trainFileSep);
				transposer.printTMatrix();
			} catch (IOException e) {
				System.out.println("Could not tranpose file: " + dataParams.trainDataFileName);
				e.printStackTrace();
			}
		} else if (dataParams.shouldCombineSamples()) {
			CombineFileCheckAtt cf;
			cf = new CombineFileCheckAtt(dataParams.trainFileSep);
			cf.writeFiles(dataParams.getCombineDirName());
		} else if (dataParams.shouldCombineAtts()) {
			CombineFile cf;
			cf = new CombineFile(dataParams.trainFileSep, false);
			cf.printFiles(dataParams.getCombineDirName());
		} else {
			createOutDir(dataParams.getOutDirName());
			loadData();
			if (dataParams.outputFormat == DataParameters.ARFF) {
				OutputDataset od = new OutputArff(trainData, 
						util.Util.replaceFileNameSuffix(trainData.getFileName(), ".arff"));
				od.printDataset(false);
			}
			if (preProcParams != null) {
				if (preProcParams.shouldCombineTechReplicates()) {
					averageReplicates();
				}
				processDatasets();
			}
			if (preProcParams != null && preProcParams.shouldCreateFolds()) {
				createFolds();
			} else if (preProcParams != null && preProcParams.shouldSplitData()) {
				createSplit();
			}
		}
	}

	public Dataset getTrainSet() {
		return trainData;
	}

	public Dataset getTestSet() {
		return testData;
	}

	public Dataset getITestSet() {
		return indepTestData;
	}

	public Dataset getSourceData() {
		return sourceData;
	}

	/**
	 * Runs on sets that have already been loaded into
	 * memory. The main goal is to make sure that you can use this for the
	 * experimenter or any extra stuff.
	 * @throws AttributeDoesNotExistException 
	 * @throws IncompatibleDatatypeException 
	 * @throws ValueNotFoundException 
	 */
	public void runLoadedDataSets() throws AttributeDoesNotExistException, ValueNotFoundException, IncompatibleDatatypeException {
			averageReplicates();
		    processDatasets();
	}

	/**
	 * @param args
	 *            the command line arguments
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Preprocess test;
		ProcessParams pp;
		if (args.length < 1) {
			System.out.println("Need an input file.  The format of the options is:");
			System.out.println("[-c] [-tpf] [-d] [-s X] [-t] [-icsv] [-ocsv] [-r] [-[d]tst TestFile/TestDir] [-of OutputFile] [-odir OutputDirectory] [-dtr] TrainFile");
			System.exit(1);
		} else if (args.length > 0) {
			pp = new ProcessParams(args);
			test = new Preprocess(pp.getDataParams(), pp.getPreProcParams(),
					new LearnerParameters());
			test.run();
		}
	}
}