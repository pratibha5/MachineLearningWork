/*
 * DataParameters.java
 *
 * Created on July 6, 2005, 2:23 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package parameters;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

import util.Util;

/**
 * @author Jonathan Lustgarten
 */
public class DataParameters {
	public final static int TSV = -1,
										CSV = 0,
										ARFF = 1,
										DIR_CSV = 2;
	public String trainFileSep, testFileSep;
	public boolean hasTestSet;
	public boolean isTrainDir, isTestDir, isSourceDir, bInputCV; // Input booleans 
	public int outputFormat; // One of TSV, CSV, ARFF
	public boolean bTransposeInputData; // Modifying data booleans
	private boolean bCombineAtts, bCombineSamples, bTranspose, bConvertTabCSV,
			bVGForm; // Complex command booleans
	public boolean bLoadXMLAttribute;
	public String sourceDataFileName, trainDataFileName;
	private String trainDirName, testFileName,
			testDirName, sourceDirName, combineDirName, inputXMLFileName;
	public String outDirName; // Directory where output files will be written
	public String outFileSuffix = ".tsv";

	/** Creates a new instance of DataParameters */
	public DataParameters() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		outDirName = "BRL_run_" + df.format(new Date());
		outputFormat = TSV;
	}

	public DataParameters(String[] args) {
		this();
		processArgs(args);
	}

	/** Sets whether there is a test set */
	public void setTestFileName(String name) {
		hasTestSet = true;
		testFileName = name;
	}

	public boolean isTrainDir() {
		return isTrainDir;
	}

	public boolean isTestDir() {
		return isTestDir;
	}

	public boolean isSourceDir() {
		return isSourceDir;
	}
	
	public void setTestDirName(String name) {
		isTestDir = true;
		hasTestSet = true;
		testDirName = name;
		testFileName = name + ".tsv";
	}

	public String getXMLAttributeFile() {
		return (bLoadXMLAttribute ? inputXMLFileName : null);
	}

	public void setTestSetIsDir(boolean b) {
		isTestDir = b;
	}

	public String getTrainDirName() {
		return (isTrainDir ? trainDirName : null);
	}

	public void setTrainDirName(String name) {
		isTrainDir = true;
		trainDirName = name;
		trainDataFileName = name + ".tsv";
	}

	public String getTestFileName() {
		return (hasTestSet ? testFileName : null);
	}

	public String getInputTestDir() {
		return (isTestDir ? testDirName : null);
	}

	public void setCombineDirName(String name) {
		combineDirName = name;
		trainDataFileName = name + ".tsv";
	}

	public String getCombineDirName() {
		return combineDirName;
	}

	public boolean getLoadCVFiles() {
		return bInputCV;
	}

	public void setLoadCVFiles(boolean b) {
		bInputCV = b;
	}

	public boolean shouldTransposeOnly() {
		return bTranspose;
	}

	public boolean getTransposeInputFile() {
		return bTransposeInputData;
	}

	public void setTransposeInputFile(boolean b) {
		bTransposeInputData = b;
	}

	public void setWorkingDirName(String name) {
		outDirName = name;
	}

	public String getOutDirName() {
		return outDirName;
	}

	public String getOutFileName() {
		return outFileSuffix;
	}

	public boolean shouldConvertTabCSV() {
		return bConvertTabCSV;
	}

	public boolean shouldCombineSamples() {
		return bCombineSamples;
	}

	public boolean shouldCombineAtts() {
		return bCombineAtts;
	}

	public void processArgs(String[] args) {
		trainDataFileName = args[args.length - 1];
		try {
			File trainFile = new File(trainDataFileName);
			if (trainFile.isDirectory()) {
				trainDataFileName = Util.trimDirName(trainDataFileName);
				trainDirName = trainDataFileName;
				isTrainDir = true;
			}
		} catch (ArrayIndexOutOfBoundsException x) {
			System.err.println("Error: expected a train file or directory");
		}
		
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equalsIgnoreCase("-o")) {
				if (args[i + 1].indexOf("-") == -1) {
					if (args[i + 1].equalsIgnoreCase("arff")) {
						outputFormat = ARFF;
						outFileSuffix = ".arff";
					} else {
						outputFormat = CSV;
						outFileSuffix = ".csv";
					}
				} else {
					outputFormat = CSV;
					outFileSuffix = ".csv";
				}
			} else if (args[i].equalsIgnoreCase("-icfv")) {
				bInputCV = true;
			} else if (args[i].equalsIgnoreCase("-itrncsv"))
				trainFileSep = ",";
			else if (args[i].equalsIgnoreCase("-itstcsv"))
				testFileSep = ",";
			else if (args[i].equalsIgnoreCase("-t"))
				bTransposeInputData = true;
			else if (i + 1 < args.length) {
				if (args[i].equalsIgnoreCase("-dtr")) {
					isTrainDir = true;
					trainDirName = Util.trimDirName(args[i + 1]);
					i++;
				} else if (args[i].equalsIgnoreCase("-dtst")
						|| args[i].equalsIgnoreCase("-tstd")) {
					isTestDir = true;
					testDirName = Util.trimDirName(args[i + 1]);
					i++;
					hasTestSet = true;
				} else if (args[i].equalsIgnoreCase("-tst")) {
					testFileName = args[i + 1];
					i++;
					hasTestSet = true;
					try {
						File testFile = new File(testFileName);
						if (testFile.isDirectory()) {
							testFileName = Util.trimDirName(testFileName);
							testDirName = testFileName;
							isTestDir = true;
						}
					} catch (NullPointerException x) {
						System.err.println("BRL: Error: you did not specify a test file or directory!");
					}
				} else if (args[i].equalsIgnoreCase("-src")) {
					sourceDataFileName = args[i+1];
					i++;
					try {
						File sourceFile = new File(sourceDataFileName);
						if (sourceFile.isDirectory()) {
							sourceDataFileName = Util.trimDirName(sourceDataFileName);
							sourceDirName = sourceDataFileName;
							isSourceDir = true;
						}
					} catch (NullPointerException x) {
						System.err.println("BRL: Error: you did not specify a test file or directory!");
					}
				} else if (args[i].equalsIgnoreCase("-odir")
						|| args[i].equalsIgnoreCase("-od")
						|| args[i].equalsIgnoreCase("-outdir")) {
					outDirName = args[i + 1];
					i++;
				} else if (args[i].equalsIgnoreCase("-c")) {
					bConvertTabCSV = true;
					trainDataFileName = args[i + 1];
					i++;
				} else if (args[i].equalsIgnoreCase("-of")) {
					outFileSuffix = args[i + 1];
					i++;
				} else if (args[i].equalsIgnoreCase("-tpf")) {
					bTranspose = true;
					trainDataFileName = args[i + 1];
					i++;
				} else if (args[i].equalsIgnoreCase("-cmbf")) {
					bCombineSamples = true;
					combineDirName = args[i + 1];
					i++;
				} else if (args[i].equalsIgnoreCase("-cmbAtts")) {
					bCombineAtts = true;
					combineDirName = args[i + 1];
					i++;
				} else if (args[i].equalsIgnoreCase("-AttFile")) {
					bLoadXMLAttribute = true;
					inputXMLFileName = args[i + 1];
					i++;
				} else if (args[i].equalsIgnoreCase("-VGF")) {
					bVGForm = true;
				}
			}
		}
	}

	public boolean doVGForm() {
		return bVGForm;
	}

	public String getVGFormFile() {
		return trainDataFileName;
	}

	public static final String getHelpString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Parameters that allow to learn a rule model:\n");
		sb	.append("  -ocsv\t\tFormat output files with comma-seperated values (CSV)\n");
		sb	.append("  -itrncsv\tThe training file is a CSV\n");
		sb	.append("  -itstcsv\tThe testing file is a CSV\n");
		sb	.append("  -tpf\t\tTranspose the input files (files have attributes as columns and\n");
		sb.append("\t\tsamples as rows)\n");
		sb	.append("  -tst test_file\n");
		sb.append("  -dtst dir\tDirectory containing test files (one sample per a file)\n");
		sb	.append("  -odir out_dir\tDirectory where to output all the files.  If out_dir does not\n");
		sb.append("\t\texist, create it.\n");
		sb.append("\t\tIf not specified, creates directory with current time and date\n");
		sb	.append("  -AttFile file\tThe attribute-value hierarchy file. Useful for loading discrete\n");
		sb.append("\t\tvalue hierarchies\n");
		sb	.append("  -dtr in_dir\tDirectory contianing training files (one sample per a file)\n");
		sb	.append("  -icfv\t\tAllows to input several related cross-fold validation datasets.\n");
		sb.append("\t\tTheir file names must contain 'train' and 'test' respectively.\n");
		sb	.append("  train_file\tThe last parameter is the training file if -dtr is not used. \n");
		sb	.append("\n");
		sb	.append("Parameters that do not allow to learn a rule model:\n");
		sb	.append("  -c\t\tConvert the file from tab seperated to CSV or vice-versa\n");
		sb.append("  -tpf\t\tTranspose the file\n");
		sb	.append("  -cmbf dir\tCombine the files contained in directory dir. Files with similar\n");
		sb.append("\t\tclassification should be in the same subdirectory. Each file is\n");
		sb.append("\t\ta sample.\n");
		sb	.append("  -cmbAtts dir\tCombine the files contained in directory dir. Each file is an\n");
		sb.append("\t\tattribute\n");
		return sb.toString();
	}
}
