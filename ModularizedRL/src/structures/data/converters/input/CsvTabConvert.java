/*
 * TabtoCSV.java
 *
 * Created on February 11, 2005, 8:22 PM
 */

package structures.data.converters.input;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;

/**
 * 
 * @author Jonathan Lustgarten
 */
public class CsvTabConvert {
	private static PrintStream pr;
	private static BufferedReader br;
	private static BufferedReader stdin;
	private static Vector filesToRead;
	private static char replaceType;
	private static char stringType;

	public CsvTabConvert() {
		filesToRead = new Vector();
		replaceType = '\t';
		stringType = ',';
	}

	public static void setReplaceType(char type) {
		replaceType = type;
	}

	public static void setStringType(char type) {
		stringType = type;
	}

	public static void guessFileType(String fileName) {
		if (fileName.indexOf(".csv") == -1) {
			setReplaceType(',');
			setStringType('\t');
		} else {
			setReplaceType('\t');
			setStringType(',');
		}
	}

	public static String replaceString(String I) {
		String NewS = I.replace(stringType, replaceType);
		return NewS;
	}

	public static void loadAndConvert(String inFileName) throws IOException {
		FileReader fr = new FileReader(inFileName);
		br = new BufferedReader(fr);
		guessFileType(inFileName);
		String inExt = (stringType == ',' ? ".csv" : ".tsv");
		String outExt = (stringType == ',' ? ".tsv" : ".csv");
		String outFileName = inFileName.substring(0, inFileName.indexOf(inExt)) 
					+ "_c" + outExt;
		FileOutputStream fileOut = new FileOutputStream(outFileName,  false);
		pr = new PrintStream(fileOut);
		String Data = "";
		while ((Data = br.readLine()) != null)
			pr.println(replaceString(Data));
		pr.close();
	}

	public static void setInputVector(Vector inputFiles) {
		filesToRead = inputFiles;
	}

	public static void acquireFiles(String inputFileList) throws IOException {
		String line;
		BufferedReader iR;
		if (inputFileList.length() == 0) {
			do {
				System.out.print(
						"Please enter the names of files to convert, one per line. Type 'done' to begin conversion: ");
				line = stdin.readLine();
				if (! line.equalsIgnoreCase("done"))
					filesToRead.add(line);
			} while (line.equalsIgnoreCase("done"));
		} else {
			iR = new BufferedReader(new FileReader(inputFileList));
			while ((line = iR.readLine()) != null)
				filesToRead.add(line);
		}
	}

	public static void setSingleFile(String file) {
		filesToRead.add(file);
	}

	public static void runFiles() {
		for (int i = 0; i < filesToRead.size(); i++) {
			try {
				String fileName = (String) filesToRead.elementAt(i);
				guessFileType(fileName);
				System.out.println("Writing Results to : c"
								+ (fileName.substring(0, fileName.indexOf("."
										+ (stringType == ',' ? "csv" : "tsv"))) 
										+ (stringType == ',' ? ".tsv" : ".csv")));
				loadAndConvert(fileName);
			} catch (Exception e) {
				System.out.println("Cannot read file "
						+ (String) filesToRead.elementAt(i));
				System.out.println("Moving onto next file");
			}
		}
	}

	public static void main(String args[]) {
		CsvTabConvert nCTC = new CsvTabConvert();
		stdin = new BufferedReader(new InputStreamReader(System.in));
		if (args.length > 0) {
			try {
				if (args[0].charAt(0) == '-') {
					switch (args[0].charAt(1)) {
					case 'F':
						nCTC.setSingleFile(args[1]);
						nCTC.guessFileType(args[1]);
						break;
					case 'L':
						nCTC.acquireFiles(args[1]);
						break;
					default:
						throw new IOException();
					}
				} else
					throw new IOException();
				nCTC.runFiles();
			} catch (Exception e) {
				System.out.println("Error in input. Three choices of input:");
				System.out.println(
						"java CsvTabConvert [-F SingleFileName.(csv/tsv)] [-L ListOfFiles.tsv]");
				System.exit(1);
			}
		} else {
			try {
				nCTC.acquireFiles("");
				nCTC.runFiles();
			} catch (Exception e) {
				System.out.println("Cannot convert file");
			}
		}
	}
}
