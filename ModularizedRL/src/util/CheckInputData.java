package util;

public class CheckInputData {
	private String trnFile;

	private String tstFile;

	private String sep;

	public CheckInputData(String trn) {
		trnFile = trn;
		tstFile = null;
		sep = "\t";
	}

	public CheckInputData(String trn, String tst) {
		trnFile = trn;
		tstFile = tst;
		sep = "\t";
	}

	public void setSeperator(String seperator) {
		sep = seperator;
	}
}
