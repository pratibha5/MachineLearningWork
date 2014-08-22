package structures.data.converters.output;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import data.dataset.Dataset;


public abstract class OutputDataset {
	protected Dataset data;

	protected PrintStream outPS;

	public OutputDataset(Dataset data, String outFileName) {
		this.data = data;
		try {
			outPS = new java.io.PrintStream(outFileName);
		} catch (FileNotFoundException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			outPS = System.out;
		}
	}

	public abstract void printDataset(boolean useDisc);
}
