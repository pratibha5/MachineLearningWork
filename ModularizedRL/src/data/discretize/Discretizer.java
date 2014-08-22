/**
 * 
 */
package data.discretize;

/**
 * @author Jonathan
 * 
 */
public abstract class Discretizer {

	/**
	 * A required function for Discretization
	 * 
	 * @param values
	 *            A sorted list from least to greatest of the double values
	 * @param classCounts
	 *            a list of ints representing the class counts
	 * @param numClass
	 *            The Number of Classes
	 * @param numBins
	 *            The Maximum number of bins (only used for some methods)
	 * @return the cutpoints for every attribute
	 */
	public abstract double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins);
}
