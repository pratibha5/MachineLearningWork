/*
 * CombineFileCheckAtt.java
 *
 * Created on January 24, 2007, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package util.file;

import java.io.*;
import java.util.*;

import structures.data.converters.input.TabCsvDataLoader;
import data.dataset.*;
import util.*;


/**
 * 
 * @author Jonathan
 */
public class CombineFileCheckAtt {
	private static Vector filesToMerge;
	private static Vector dataToMerge;
	private static String seperator;
	private static PrintWriter pr;
	private String[][] infoArray;
	private double[][] dInfoArray;
	private boolean[] isAttDouble;
	private String[][] sampleArray;
	private int numAtts;
	private int totalSamp;
	private int numSampsMissing;
	private int currSampMissing;
	private static final String FILE_PATH_SEP = System.getProperty("file.separator");

	public CombineFileCheckAtt() {
		this("\t");
	}

	public CombineFileCheckAtt(String sep) {
		seperator = sep;
		infoArray = new String[0][0];
		sampleArray = new String[0][0];
		dInfoArray = new double[0][0];
		isAttDouble = new boolean[0];
		filesToMerge = new Vector();
		dataToMerge = new Vector();
		//numAtts = 0;
		//totalSamp = 0;
		//numSampsMissing = 0;
		//currSampMissing = 0;
	}

	/**
	 * Returns the list of files (CSV or TAB delimited) based on the type of
	 * seperator that was selected. default is tab delimited
	 * 
	 * @param dir
	 *            The directory of files to search
	 * @return A vector of files of the seperator type
	 */
	private static Vector getFileList(File dir) throws IOException {
		FilenameFilter csvFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".csv") || name.endsWith(".CSV"));
			}
		};
		FilenameFilter tabFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String n = name.toLowerCase();
				return (n.endsWith(".txt") || n.endsWith(".tsv"));
			}
		};
		String[] fileNames;
		if (seperator.equalsIgnoreCase(","))
			fileNames = dir.list(csvFilter);
		else
			fileNames = dir.list(tabFilter);
		Vector vFilesToMerge = new Vector();
		for (int c = 0; c < fileNames.length; c++)
			vFilesToMerge.add(dir.getAbsolutePath() + FILE_PATH_SEP  + fileNames[c]);

		return vFilesToMerge;
	}

	/**
	 * Returns a list of files from the directory specified the type of file
	 * retreived is defined by the seperator: , = CSV, \t = .txt or .tsv
	 * 
	 * @param dir
	 *            the dirctory of the file
	 * @return A vector of files within the directory
	 */
	public static Vector addFilesToMegre(File dir) throws IOException {
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		Vector TempV = getFileList(dir);
		File[] PossDirect = dir.listFiles(fileFilter);
		if (PossDirect.length < 1) {
			if (TempV.size() < 1)
				return null;
			else
				return TempV;
		} else {
			Vector tV = new Vector();
			for (int i = 0; i < PossDirect.length; i++) {
				tV = addFilesToMegre(PossDirect[i]);
				if (tV != null && tV.size() > 0)
					TempV.addAll(tV);
			}
			if (TempV.size() < 1)
				return null;
			else
				return TempV;
		}
	}

	private static void initReaders(File dir) throws IOException {
		Vector vFilesToMerge = new Vector();
		vFilesToMerge = addFilesToMegre(dir);
		if (vFilesToMerge == null || vFilesToMerge.size() < 1) {
			System.err.println("The directory is empty: "
					+ dir.getAbsolutePath() + ".");
			System.exit(1);
		}
		Collections.sort(vFilesToMerge);
		Vector vDataToMerge = new Vector();
		File F = null;
		//boolean success = false;
		boolean addToVect = true;
		String sFile = null;
		for (int i = 0; i < vFilesToMerge.size(); i++) {
			//success = false;
			sFile = (String) vFilesToMerge.elementAt(i);
			try {
				F = new File(sFile);
				addToVect = true;
			} catch (Exception e) {
				System.out.println("Error in " + vFilesToMerge.elementAt(i)
						+ ". Cannot read the file.");
				addToVect = false;
			}
			if (addToVect)
				vDataToMerge.add(F);
		}
		dataToMerge = vDataToMerge;
		filesToMerge = vFilesToMerge;
	}

	private String[][] growMatrix(String[][] oldA) {
		String[][] nA = new String[oldA.length * 2][totalSamp + 1];
		System.arraycopy(oldA, 0, nA, 0, oldA.length);
		boolean[] nBa = new boolean[infoArray.length * 2];
		System.arraycopy(isAttDouble, 0, nBa, 0, isAttDouble.length);
		isAttDouble = nBa;
		double[][] nDa = new double[dInfoArray.length * 2][totalSamp];
		System.arraycopy(dInfoArray, 0, nDa, 0, dInfoArray.length);
		dInfoArray = nDa;
		return nA;
	}

	private String[] growArray(String[] oldA) {
		String[] nA = new String[oldA.length * 2];
		System.arraycopy(oldA, 0, nA, 0, oldA.length);
		return nA;
	}

	private void insertAtt(String I, String V, int pos, int currS) {
		String[] nD = new String[0];
		double[] nDa = new double[0];
		if (pos == infoArray.length && pos != 0) {
			nD = new String[totalSamp + 1];
			nDa = new double[totalSamp];
			for (int i = 1; i < currS - 1; i++) {
				nD[i] = "?";
				nDa[i - 1] = Double.NaN;
			}
			nD[0] = I;
			nDa[currS] = (isDoubleVal(V) ? (new Double(V)).doubleValue()
					: Double.NaN);
			nD[currS] = V;
		}

		if (numAtts >= infoArray.length - 1)
			infoArray = growMatrix(infoArray);
		if (pos < numAtts) {
			for (int i = numAtts; i >= pos; i--) {
				dInfoArray[i + 1] = dInfoArray[i];
				infoArray[i + 1] = infoArray[i];
				isAttDouble[i + 1] = isAttDouble[i];
			}
		}
		if (isDoubleVal(V)) {
			isAttDouble[pos] = true;
			dInfoArray[pos][currS] = (new Double(V)).doubleValue();
			infoArray[pos][0] = I;
		} else
			infoArray[pos] = nD;
		numAtts++;
		if (numAtts >= infoArray.length - 1)
			infoArray = growMatrix(infoArray);
	}

	/*private Double safeDoubleVal(String val) {
		try {
			return new Double(val).doubleValue();
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}
	*/

	private boolean isDoubleVal(String sval) {
		try {
			new Double(sval);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void loadFirstFile(File inFile) throws IOException {
		int numEntries = 0;
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		infoArray = new String[1][totalSamp + 1];
		dInfoArray = new double[1][totalSamp];
		isAttDouble = new boolean[1];
		String line;
		while ((line = br.readLine()) != null) {
			String[] IV = line.split(seperator);
			if (!IV[0].equalsIgnoreCase("M/Z")) {
				if (numEntries >= infoArray.length - 1) {
					infoArray = growMatrix(infoArray);
				}
				if (isDoubleVal(IV[1])) {
					isAttDouble[numEntries] = true;
					dInfoArray[numEntries][0] = (new Double(IV[1])).doubleValue();
					infoArray[numEntries][0] = IV[0];
				} else {
					System.arraycopy(IV, 0, infoArray[numEntries], 0, IV.length);
				}
				numEntries++;
			}
		}
		numAtts = numEntries;
	}

	/**
	 * Loads the file iterating down the array, growing it and inserting as
	 * needed
	 * 
	 * @param inFile
	 *            The Sample File to read
	 * @param currSamp
	 *            The current sample number
	 */
	private void loadFile(File inFile, int currSamp) throws IOException {
		String line = "";
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		int currAtt = 0;
		boolean missVal = false;
		int numMissVal = 0;
		while ((line = br.readLine()) != null) {
			String[] IV = line.split(seperator);
			if (!IV[0].equalsIgnoreCase("M/Z")) {
				if (currAtt > numAtts - 1) {
					if (currAtt == infoArray.length)
						infoArray = growMatrix(infoArray);
					System.out.println("Inserted attribtue: " + IV[0]);
					insertAtt(IV[0], IV[1], currAtt, currSamp);
				} else if (infoArray[currAtt][0].compareTo(IV[0]) == 0) {
					if (isAttDouble[currAtt])
						dInfoArray[currAtt][currSamp - 1] = (new Double(IV[1]))
								.doubleValue();
					else
						infoArray[currAtt][currSamp] = IV[1];
				} else if (infoArray[currAtt][0].compareTo(IV[0]) > 0) {
					int newAttPos = currAtt;
					for (int i = currAtt; i < numAtts; i++) {
						if (infoArray[i][0].compareTo(IV[0]) == 0) {
							currAtt = i;
							break;
						} else if (infoArray[currAtt][0].compareTo(IV[0]) < 0) {
							System.out.println("Inserted attribtue: " + IV[0]);
							insertAtt(IV[0], IV[1], i, currSamp);
							currAtt = i;
							break;
						} else {
							missVal = true;
							if (isAttDouble[i])
								dInfoArray[i][currSamp - 1] = Double.NaN;
							else
								infoArray[i][currSamp] = "?";
							newAttPos = i;
							numMissVal++;
						}
					}
					if (newAttPos == numAtts - 1)
						break;
					infoArray[currAtt][currSamp] = IV[1];
				}
				currAtt++;
			}
		}
		if (missVal) {
			numSampsMissing++;
			currSampMissing = numMissVal;
			// System.out.println("\nSample "+sampleArray[currSamp-1][0]+" is
			// missing "+numMissVal+" attributes");
		}
		br.close();
		br = null;
	}

	/**
	 * Warning permenanently shrinks the array. This creates large overhead if
	 * done multiple times when trying to add files!
	 */
	private void trimInfoArr() {
		String[][] nA = new String[numAtts][totalSamp];
		System.arraycopy(infoArray, 0, nA, 0, numAtts);
		infoArray = nA;
	}

	private void printData(String outDir) throws FileNotFoundException {
		PrintWriter pr = new PrintWriter(new FileOutputStream(outDir + ".tsv"), true);
		try {
			pr.print("#Sample\t@Class");
			for (int h = 0; h < infoArray.length; h++)
				pr.print("\t" + infoArray[h][0]);
			pr.println();
			for (int i = 0; i < sampleArray.length; i++) {
				pr.print("" + sampleArray[i][0] + "\t" + sampleArray[i][1]);
				for (int a = 0; a < infoArray.length; a++) {
					if (isAttDouble[a]) {
						if (Double.isNaN(dInfoArray[a][i]))
							pr.print("\t?");
						else
							pr.print("\t" + dInfoArray[a][i]);
					} else
						pr.print("\t" + infoArray[a][i + 1]);
				}
				pr.println();
			}
			pr.close();
		} catch (Exception e) {
			System.err.println("Error while writing to file!");
			pr.close();
			System.exit(1);
		}
	}

	private int getIndexOfEnd(String name) {
		String n = name.toLowerCase();
		if (seperator.equalsIgnoreCase(",")) {
			return n.lastIndexOf(".csv");
		} else {
			int i = n.lastIndexOf(".txt");
			return
				i >= 0 ? 
					i :
					n.lastIndexOf(".tsv");
		}
	}

	public void writeFiles(String sourceDirName) {
		File sourceDir;
		try {
			PrintWriter missPR = new PrintWriter(new FileOutputStream(sourceDirName
					+ "-MissingValueSamples.txt"), true);
			File missAttDir = new File(sourceDirName + "-MissingAttSampleDir");
			missAttDir.mkdir();
			missPR.println("Samples removed due to > 25% missing data");
			sourceDir = new File(sourceDirName);
			initReaders(sourceDir);
			sampleArray = new String[filesToMerge.size()][2];
			totalSamp = filesToMerge.size();
			System.out.print("Combining instance files into one data file...");
			//String SampleName, SampleClass, 
			String fileName;
			for (int i = 0; i < dataToMerge.size(); i++) {
				if (dataToMerge.size() < 10 || i % (dataToMerge.size() / 10) == 1) {
					System.out.print(".");
					System.out.flush();
				}
				fileName = (String) filesToMerge.elementAt(i);
				sampleArray[i][0] = fileName.substring(
						fileName.lastIndexOf(FILE_PATH_SEP) + 1, getIndexOfEnd(fileName));
				sampleArray[i][1] = fileName.substring(fileName.indexOf(FILE_PATH_SEP, 
						fileName.indexOf(sourceDirName)) + 1, fileName.lastIndexOf(FILE_PATH_SEP));
				if (i == 0)
					loadFirstFile((File) dataToMerge.elementAt(i));
				else
					loadFile((File) dataToMerge.elementAt(i), i + 1);
				// If the sample has more than 25% of the attributes missing
				if ((currSampMissing) / (numAtts * 1.0) > .25) {
					dataToMerge.removeElementAt(i);
					System.gc();
					missPR.println(sampleArray[i][0]);
					File file = new File((String) filesToMerge.elementAt(i));
					File newMissSubDir = new File(missAttDir, fileName.substring(
							fileName.indexOf(FILE_PATH_SEP, fileName.indexOf(sourceDirName)) + 1,
							fileName.lastIndexOf(FILE_PATH_SEP)));
					if (!newMissSubDir.exists())
						newMissSubDir.mkdir();
					if (!file.renameTo(new File(newMissSubDir, fileName.substring(
							fileName.lastIndexOf(FILE_PATH_SEP) + 1))))
						System.out.println("Error moving file " + file.getName());
					filesToMerge.removeElementAt(i);
					sampleArray = util.Arrays.remove(sampleArray, i);
					for (int a = 0; a < dInfoArray.length; a++) {
						dInfoArray[a] = util.Arrays.remove(dInfoArray[a], i);
						infoArray[a] = util.Arrays.removeString(infoArray[a], i + 1);
					}
					i--;
					totalSamp--;
				}
				currSampMissing = 0;
				// System.out.println("added Sample: "+SampleName+" with class:
				// "+SampleClass);
			}
			System.out.println("  Done");
			missPR.close();
			missPR = null;
			trimInfoArr();
			if (missAttDir.list().length == 0) {
				missAttDir.delete();
				(new File(sourceDirName + "-MissingValueSamples.txt")).delete();
			}
			System.out.println("\nWriting the data to the file.");
			printData(sourceDirName);
		} catch (IOException e) {
			System.out.println("Cannot open source directory.");
			System.out.println(e.getLocalizedMessage());
			System.exit(1);
		}
	}

	public DataModel mergeFiles(String sourceDirName) throws Exception {
		//File SD;
		writeFiles(sourceDirName);
		TabCsvDataLoader ltscv = new TabCsvDataLoader(sourceDirName + ".tsv");
		return ltscv.loadData();
	}

	public static void main(String[] args) {
		CombineFileCheckAtt cmb = new CombineFileCheckAtt("\t");
		cmb.writeFiles("TestCmbf");
	}
}