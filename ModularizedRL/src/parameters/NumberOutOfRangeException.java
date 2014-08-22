package parameters;

public class NumberOutOfRangeException extends Exception {

	public NumberOutOfRangeException(int num, int min, int max) {
		super(
				num
						+ " is out of the range. Please enter an integer value between "
						+ min + " - " + max);
	}

	public NumberOutOfRangeException(double num, double min, double max) {
		super(num
				+ " is out of the range. Please enter an double value between "
				+ min + " - " + max);
	}
}
