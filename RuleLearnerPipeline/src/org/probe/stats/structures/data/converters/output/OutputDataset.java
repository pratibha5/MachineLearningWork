package structures.data.converters.output;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import data.dataset.DataModel;


public abstract class OutputDataModel {
	protected DataModel data;

	protected PrintStream outPS;

	public OutputDataModel(DataModel data, String outFileName) {
		this.data = data;
		try {
			outPS = new java.io.PrintStream(outFileName);
		} catch (FileNotFoundException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			outPS = System.out;
		}
	}

	public abstract void printDataModel(boolean useDisc);
}
