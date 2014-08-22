//Things that need to be done:
//More robust method of reading files
//More options - Skipping first line of file(default right now)

package preprocess;

import java.io.*;
import java.util.*;
//import java.lang.*;

public class FileCombiner {
	private static PrintStream pFile;
	public static BufferedReader stdin;
	private static Vector filesToMerge;
	private static Vector dataToMerge;
	private static String outputFile;
	private static String sSD;
	private static HashMap fileToHeader;
	private static String seperator;
	private static final String FILE_PATH_SEP = System.getProperty("file.separator");

	public FileCombiner(String header, String title, String dirName, String sep)
			throws IOException {
		if (title.equals(""))
			title = dirName + "_comb.tsv";
		sSD = dirName;
		FileOutputStream fileOut = new FileOutputStream(title);
		stdin = new BufferedReader(new InputStreamReader(System.in));
		pFile = new PrintStream(fileOut);
		seperator = sep;
		File sourceDir = new File(dirName);
		initReaders(sourceDir);
		fileToHeader = new HashMap();
		loadHeader(header);
	}

	public FileCombiner(String title, String dirName, String sep) throws IOException {
		this("", title, dirName, sep);
	}

	private static void loadHeader(String header) throws IOException {
		if (!header.equals("")) {
			BufferedReader hRead = new BufferedReader(new FileReader(header));
			String Input;
			while ((Input = hRead.readLine()) != null) {
				String[] sArray = Input.split("\t");
				fileToHeader.put(sArray[0], sArray[1]);
			}
		} else {
			for (int i = 0; i < filesToMerge.size(); i++)
				fileToHeader.put(((String) filesToMerge.elementAt(i)), "");
		}
	}

	public static Vector getFileList(File dir) throws IOException {
		FilenameFilter filterCSV = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		};
		FilenameFilter filterT = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".tsv");
			}
		};
		String[] fileNames = 
			seperator.equals(",") ? 
					dir.list(filterCSV) : 
						dir.list(filterT);
		Vector vFilesToMerge = new Vector();
		for (int c = 0; c < fileNames.length; c++)
			vFilesToMerge.add(dir.getAbsolutePath() + FILE_PATH_SEP + fileNames[c]);

		return vFilesToMerge;
	}

	public static Vector addFilesToMegre(File dir) throws IOException {

		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		Vector vector = getFileList(dir);
		File[] PossDirect = dir.listFiles(fileFilter);
		if (PossDirect.length < 1) {
			if (vector.size() < 1)
				return null;
			else
				return vector;
		} else {
			Vector v = new Vector();
			for (int i = 0; i < PossDirect.length; i++) {
				v = addFilesToMegre(PossDirect[i]);
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
			System.err.println("No Files Exist in Directory: "
					+ sourceDir.getAbsolutePath() + ".");
			System.exit(1);
		}
		Collections.sort(vFilesToMerge);
		Vector vDataToMerge = new Vector();
		FileReader reader = null;
		boolean success = false;
		boolean addToVect = true;
		String fileName = null;
		for (int i = 0; i < vFilesToMerge.size(); i++) {
			success = false;
			fileName = (String) vFilesToMerge.elementAt(i);
			while (!success) {
				try {
					reader = new FileReader(fileName);
					success = true;
				} catch (Exception e) {
					System.out.println("Cannot read file " + vFilesToMerge.elementAt(i));
					System.out.println("Enter the name if the file to use or \"none\" to skip this file: ");
					fileName = stdin.readLine();
					if (fileName == null || fileName.toLowerCase().equals("none")) {
						addToVect = false;
						vFilesToMerge.remove(i);
						success = true;
					} else
						success = false;
				}
			}
			if (reader != null && addToVect)
				vDataToMerge.add(new BufferedReader(reader));
		}
		dataToMerge = vDataToMerge;
		filesToMerge = vFilesToMerge;
	}

	public static void mergeFiles() throws IOException {
		System.out.println("Merging files...");
		PrintHeader();
		String inputD = "";
		int i = 0;
		if (dataToMerge.size() == 0) {
			System.out.println("No readers left after trying to load all the files!");
			System.exit(1);
		}
		try {
			while ((inputD = ((BufferedReader) dataToMerge.elementAt(0)).readLine()) != null) {
				// System.out.println(inputD);
				if (inputD.indexOf(seperator) == -1
						|| inputD.indexOf("sample") != -1
						|| inputD.indexOf(":") != -1
						|| inputD.indexOf("MZ") != -1
						|| inputD.indexOf("M/Z") != -1
						|| inputD.indexOf("intensity") != -1) {
					for (int r = 0; r < dataToMerge.size(); r++)
						((BufferedReader) dataToMerge.elementAt(r)).readLine();
				} else {
					String[] inArray = inputD.split(seperator);
					pFile.print(inArray[0] + "\t" + inArray[1]);
					for (i = 1; i < dataToMerge.size(); i++) {
						String temp = ((BufferedReader) dataToMerge
								.elementAt(i)).readLine();
						if (temp == null) {
							System.out.println("File: "
									+ (String) filesToMerge.elementAt(i)
									+ " ended before other files.");
							throw new IOException();
						}
						inArray = temp.split(seperator);
						pFile.print("\t" + inArray[1]);
					}
					pFile.println();
				}
			}
		} catch (IOException e) {
			System.out
					.println("A file in the directory terminated prematurely. Finishing file then closing.");
			if (i < dataToMerge.size())
				for (int j = i; j < dataToMerge.size(); j++)
					pFile.print("\t" + " ");
			pFile.println("");
		}
		pFile.close();
		System.out.println("Done merging files");
	}

	private static void PrintHeader() throws IOException {
		pFile.print("Sample");
		String EndFile = ".tsv";
		if (seperator.equals(","))
			EndFile = ".csv";
		for (int i = 0; i < filesToMerge.size(); i++)
			pFile.print("\t" 
					+ ((String) filesToMerge.elementAt(i)).substring(
							((String) filesToMerge.elementAt(i)).lastIndexOf(FILE_PATH_SEP) + 1,
							((String) filesToMerge.elementAt(i)).indexOf(EndFile)));
		pFile.println("");
		pFile.print("CLASS");
		for (int j = 0; j < filesToMerge.size(); j++)
			pFile.print("\t"
					+ fileToHeader.get(((String) filesToMerge.elementAt(j))
							.substring(((String) filesToMerge.elementAt(j))
									.lastIndexOf(FILE_PATH_SEP) + 1,
									((String) filesToMerge.elementAt(j)).indexOf(EndFile))));
		pFile.println("");
	}

	public static void errorStart() {
		System.err.println("Error in input");
		System.err.println("Format must be in: [-h headerFile] [-t title] [-c] directory");
		System.exit(1);
	}

	public static FileCombiner processInput(String args[]) throws IOException {
		String dir = "";
		String header = "";
		String title = "";
		String sep = "\t";
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-h")) {
				if ((args.length < i + 2) || (args[i + 1].equals("-t")))
					errorStart();
				header = args[i + 1];
				i++;
			} else if (args[i].equals("-t")) {
				if ((args.length < i + 2))
					errorStart();
				title = args[i + 1];
			} else if (args[i].equals("-c"))
				sep = ",";
			else
				dir = args[i];
		}
		if (dir.equals("-t") || dir.equals(""))
			errorStart();
		System.out.println("Seperator in file used is: " + sep);
		FileCombiner nC = new FileCombiner(header, title, dir, sep);
		return nC;
	}

	public static void main(String args[]) throws IOException {
		// -h File that has header
		if (args.length < 1)
			errorStart();
		else {
			FileCombiner combiner = processInput(args);
			combiner.mergeFiles();
		}
	}
}