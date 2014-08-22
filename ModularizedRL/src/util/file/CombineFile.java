/*
 * CombineFile.java
 *
 * Created on February 22, 2005, 7:50 PM
 */

package util.file;

import java.io.*;
import java.util.*;

import structures.data.converters.input.TabCsvDataLoader;
import data.dataset.*;
import util.*;

/**
 * 
 * @author Jonathan Lustgarten
 */
public class CombineFile {
	private static Vector filesToMerge;
	private static Vector dataToMerge;
	private static String dataSeparator;
	private static PrintWriter pr;
	private String[][] instanceArray;
	private static boolean addInstances;
	private final static String FILE_PATH_SEP = System.getProperty("file.separator");
	
	/** Creates a new instance of CombineFile */
	public CombineFile() {
		this("\t");
	}

	public CombineFile(String sep) {
		this(sep, true);
	}

	public CombineFile(String sep, boolean aInstances) {
		dataSeparator = sep;
		dataToMerge = new Vector();
		filesToMerge = new Vector();
		addInstances = aInstances;
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
		/*		
 		FilenameFilter csvFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		};
		FilenameFilter tabFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".tsv");
			}
		};
 		*/
		FileFilter fileFilter = new FileFilter() {
			public boolean accept (File f) {
				return f.isFile();
			}
		};
		File[] files;
		/*		
		 if (dataSepearator.equalsIgnoreCase(","))
			fileNames = dir.list(csvFilter);
		else
			fileNames = dir.list(tabFilter);
		 */
		files = dir.listFiles(fileFilter);
		Vector vFilesToMerge = new Vector();
		for (File f :  files) {
			vFilesToMerge.add(f.getAbsolutePath());
		}
		return vFilesToMerge;
	}

	/**
	 * Returns a list of files from the directory specified the type of file
	 * retreived is defined by the seperator: , = CSV, \t = .txt or .tsv
	 * 
	 * @param directory
	 *            the dirctory
	 * @return A vector of files within the directory
	 */
	public static Vector addFilesToMegre(File directory) throws IOException {
		FileFilter dirsFilter = new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory();
			}
		};
		Vector vector = getFileList(directory);
		File[] subdirs = directory.listFiles(dirsFilter);
		if (subdirs.length < 1) {
			if (vector.size() < 1) {
				return null;
			} else {
				return vector;
			}
		} else {	// "directory" has subdirectories denoting classes of instances
			Vector v = new Vector();
			for (File f : subdirs) {
				v = addFilesToMegre(f);
				if (v.size() > 0)
					vector.addAll(v);
			}
			if (vector.size() < 1)
				return null;
			else
				return vector;
		}
	}

	private static void initReaders(File sourceDir) throws IOException {
		Vector vFilesToMerge = new Vector();
		vFilesToMerge = addFilesToMegre(sourceDir);
		if (vFilesToMerge == null || vFilesToMerge.size() < 1) {
			System.err.println("Directory " + sourceDir.getAbsolutePath() + " is empty.");
			System.exit(1);
		}
		Collections.sort(vFilesToMerge);
		Vector vDataToMerge = new Vector();
		File f = null;
		boolean success = false;
		boolean addToVect = true;
		String fileName = null;
		for (int i = 0; i < vFilesToMerge.size(); i++) {
			success = false;
			fileName = (String) vFilesToMerge.elementAt(i);
			try {
				f = new File(fileName);
				addToVect = true;
			} catch (Exception e) {
				System.err.println("Cannot read the file " + vFilesToMerge.elementAt(i));
				addToVect = false;
			}
			if (addToVect)
				vDataToMerge.add(f);
		}
		dataToMerge = vDataToMerge;
		filesToMerge = vFilesToMerge;
	}

	private void getSampleHeader(File fd) throws IOException {
		BufferedReader fin = new BufferedReader(new FileReader(fd));
		String Input;
		int i = 0;
		fin.readLine();
		while ((Input = fin.readLine()) != null) {
			String[] lin = Input.split(dataSeparator);
			if (lin.length == 2)
				pr.print("\t" + lin[0]);
		}
		pr.println();
	}

	private boolean containsSameCharAtIndex(int index) {
		for (int i = 1; i < filesToMerge.size(); i++) {
			if (((String) filesToMerge.elementAt(i)).charAt(index) 
						!= ((String) filesToMerge.elementAt(0)).charAt(index))
				return false;
		}
		return true;
	}

	private String[] createAttPrefix() {
		String[] AttPreFix = new String[filesToMerge.size()];
		int startIndex = 0, amountOfStr = 0;
		while (containsSameCharAtIndex(startIndex))
			++startIndex;

		do {
			++amountOfStr;
			for (int r = 0; r < filesToMerge.size(); r++)
				AttPreFix[r] = ((String) filesToMerge.elementAt(r)).substring(
						startIndex, amountOfStr);
		} while (!util.Arrays.containsOverlap(AttPreFix));
		return AttPreFix;
	}

	private void getAttrHeader() throws IOException {
		Vector header = new Vector();
		String[] AttPreFix = createAttPrefix();
		int amountOfStr = 0;

		for (int i = 0; i < dataToMerge.size(); i++) {
			String[] head = (new String(((new BufferedReader(new FileReader(
					(File) dataToMerge.elementAt(i)))).readLine())))
					.split(dataSeparator);
			for (int j = 2; j < head.length; j++)
				header.add(AttPreFix[i] + head[j]);
		}
		pr.print("#Sample\t@Class");
		for (int c = 0; c < header.size(); c++) {
			pr.print("\t");
			pr.print(header.elementAt(c));
			if (header.size() > 100 && c % (header.size() / 100) == 0)
				pr.flush();
		}
		pr.println();
	}

	private void readSampleFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] words = line.split(dataSeparator);
			if (words.length == 2) {
				pr.print("\t" + words[1]);
			}
		}
		pr.println();
	}

	private void readAttributeFiles() throws IOException {
		Vector buffReaders = new Vector(dataToMerge.size());
		for (int i = 0; i < dataToMerge.size(); i++)
			buffReaders.add(new BufferedReader(new FileReader(
					(File) dataToMerge.elementAt(i))));

		boolean nullIndicator = true;
		String temp;
		for (int i = 0; i < buffReaders.size(); i++)
			temp = ((BufferedReader) buffReaders.elementAt(i)).readLine();

		while (nullIndicator) {
			temp = ((BufferedReader) buffReaders.elementAt(0)).readLine();
			if (temp == null)
				break;
			pr.print(temp);
			for (int c = 1; c < buffReaders.size(); c++) {
				temp = (((BufferedReader) buffReaders.elementAt(c)).readLine());
				String[] Line = temp.split(dataSeparator);
				pr.print(Line[2]);
				for (int j = 3; j < Line.length; j++) {
					pr.print("\t");
					pr.print(Line[j]);
				}
			}
			pr.println();
		}
	}

	public void printFiles(String sourceDirName) {
		File dir;
		try {
			pr = new PrintWriter(new FileOutputStream(sourceDirName + ".tsv"), true);
			pr.print("#Sample\t@Class");
			dir = new File(sourceDirName);
			initReaders(dir);
			if (addInstances) {
				System.out.print("Combining instance files from directory " + 
						sourceDirName + " into one data file...");
				// Assume that all the files have the same separator
				dataSeparator = Util.guessSeparator((File) dataToMerge.elementAt(0));
				/*
				String fileName = (String) filesToMerge.elementAt(0);
				int end = fileName.lastIndexOf(".");
				if (end < 0) {
					end = fileName.length();
				}
				//String sampleName = fileName.substring(fileName.lastIndexOf(fileSep, 
				//		fileName.indexOf(sourceDirName)) + 1, 
				//		//fileS.indexOf((dataSepearator.equalsIgnoreCase(",") ? ".csv" : ".tsv")));
				//		end);
				String sampleName = fileName.substring(fileName.lastIndexOf(fileSep) +1, 
						end);
				//String sampleClass = fileName.substring(fileName.indexOf(fileSep, 
				//		fileName.indexOf(sourceDirName)) + 1, fileName.lastIndexOf(fileSep));
				String sampleClass = fileName.substring(0, fileName.lastIndexOf(fileSep));
				sampleClass = sampleClass.substring(sampleClass.lastIndexOf(fileSep) +1);
				System.out.print(sampleName + ":" + sampleClass + "\t");
				*/
				getSampleHeader((File) dataToMerge.elementAt(0));
				for (int i = 0; i < dataToMerge.size(); i++) {
					String fileName = (String) filesToMerge.elementAt(i);
					int end = fileName.lastIndexOf(".");
					if (end < 0) {
						end = fileName.length();
					}
					String sampleName = fileName.substring(fileName.lastIndexOf(FILE_PATH_SEP) +1, end);
					String sampleClass = fileName.substring(0, fileName.lastIndexOf(FILE_PATH_SEP));
					sampleClass = sampleClass.substring(sampleClass.lastIndexOf(FILE_PATH_SEP) +1);
					pr.print(sampleName + "\t" + sampleClass);
					readSampleFile((File) dataToMerge.elementAt(i));
				}
				System.out.println("  Done");
			} else {
				getAttrHeader();
				readAttributeFiles();
			}
			pr.close();
		} catch (IOException e) {
			System.err.println("Cannot open source directory, " + sourceDirName);
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}
	}

	public Dataset mergeFiles(String sourceDirName) throws Exception {
		// Remove any trailing "/" or "\"
		while (sourceDirName.endsWith(FILE_PATH_SEP)) {
			sourceDirName = sourceDirName.substring(0, sourceDirName.lastIndexOf(FILE_PATH_SEP));
		}
		printFiles(sourceDirName);
		//Dataset nSet = new Dataset();
		TabCsvDataLoader loader = new TabCsvDataLoader(sourceDirName + ".tsv");
		return loader.loadData();
	}
}