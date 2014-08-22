package data.dataset;

public class InstanceNotFoundException extends Exception {

	public InstanceNotFoundException(int inst) {
		super("Sample " + inst + " does not exist: ");
	}

	public InstanceNotFoundException(String inst) {
		super("Sample " + inst + " does not exist");
	}

	public InstanceNotFoundException(int missingSamp, int totalSamps) {
		super("Sample " + missingSamp + " does not exist. There are only "
				+ totalSamps + " samples");
	}
}
