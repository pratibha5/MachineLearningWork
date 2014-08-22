package util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
//import java.util.Vector;

public class Transpose {
	private PrintStream pFile;
	private static BufferedReader br;
	private String[][] matrix;
	private StringBuffer[] toOutput;

	public Transpose(String[][] iM) {
		matrix = iM;
		toOutput = null;
	}

	public Transpose(String OutputFile) throws IOException {
		pFile = new PrintStream(new FileOutputStream(OutputFile));
		matrix = null;
		toOutput = null;
	}

	public void setInputFile(String inputFile) throws IOException {
		System.out.println("Loading " + inputFile + " to transpose");
		br = new BufferedReader(new FileReader(inputFile));
	}

	public void setMatrix(String[][] m) {
		matrix = m;
	}

	public void loadFile(String sep) throws IOException {
		//String input = "";
		String mat;
		int numCols = 0;
		int numRows = 1;
		toOutput = new StringBuffer[numRows];
		toOutput[0] = new StringBuffer();
		while ((mat = br.readLine()) != null) {
			String[] arr = mat.split("\t");
			if (arr.length > numRows) {
				StringBuffer[] nBuf = new StringBuffer[arr.length];
				System.arraycopy(toOutput, 0, nBuf, 0, toOutput.length);
				for (int j = toOutput.length; j < arr.length; j++) {
					nBuf[j] = new StringBuffer();
					for (int c = 0; c < numCols; c++)
						nBuf[j].append("\t");
				}
				numRows = nBuf.length;
				toOutput = nBuf;
			}

			for (int i = 0; i < arr.length; i++) {
				if (numCols > 0)
					toOutput[i].append("\t");
				toOutput[i].append(arr[i]);
			}
			numCols++;
		}
		for (int sbc = 0; sbc < toOutput.length; sbc++)
			toOutput[sbc].trimToSize();
	}

	public void printTMatrix() throws IOException {
		System.out.println("Transposing file");
		if (matrix != null) {
			for (int i = 0; i < matrix[0].length; i++) {
				int c = 0;
				for (c = 0; c < matrix.length - 1; c++)
					pFile.print(matrix[c][i] + "\t");
				pFile.println(matrix[c][i]);
			}
		} else if (toOutput != null) {
			for (int sbi = 0; sbi < toOutput.length; sbi++) {
				pFile.println(toOutput[sbi].toString());
				toOutput[sbi] = null;
			}
		}
	}

	public String[][] getMatrix() {
		return matrix;
	}

	public void main(String args[]) throws IOException {
		//String FileDir = "";
		//boolean ALS = true;
		//double TH = 0;

		if (args.length < 2)
			System.err
					.println("Missing key components.  \nUsage: transpose [InputFile] [OutputFile]");
		else {
			Transpose t = new Transpose(args[1]);
			t.setInputFile(args[0]);
			t.loadFile("\t");
			t.printTMatrix();
		}
	}
}