/*
 * ArrayUtil.java
 *
 * Created on February 26, 2005, 1:15 AM
 */

package org.probe.util;

import java.util.Vector;

import org.probe.data.dataset.Attribute;
//import data.dataset.BayesAttribute;

/**
 * @author Jonathan
 */
public class Arrays {

	public static final double[] toDoubleArray(int[] array) {
		double[] result = new double[array.length];
		for (int i = 0; i < array.length; i++)
			result[i] = (double) array[i];
		return result;
	}
	
	public static int[] toIntArray(double[] array) {
		int[] result = new int[array.length];
		for (int i = 0; i < result.length; i++)
			result[i] = (int) array[i];
		return result;
	}

	public static int[][] toIntArray(double[][] matrix) {
		int[][] result = new int[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++)
				result[i][j] = (int) matrix[i][j];
		}

		return result;
	}

	public static int[] toIntArray(Integer[] array) {
		int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].intValue();
		}
		return result;
	}

	public static final Object toArray(Vector v) {
		Object result = new Object[v.size()];
		for (int i = 0; i < v.size(); i++)
			((Object[]) result)[i] = v.elementAt(i);
		return result;
	}

	public static final Double[] toDoubleObjectArray(Object[] a) {
		Double[] newArray = new Double[a.length];
		for (int i = 0; i < newArray.length; i++)
			newArray[i] = ((Double) a[i]);
		return newArray;
	}

	public static final double[] toDoubleArray(String[] array) {
		double[] nA = new double[array.length];
		for (int i = 0; i < array.length; i++)
			nA[i] = (Double.valueOf(array[i])).doubleValue();
		return nA;
	}

	public static final double[] toDoubleArray(Object[] array) {
		double[] newArray = new double[array.length];
		for (int i = 0; i < array.length; i++)
			newArray[i] = ((Double) array[i]).doubleValue();
		return newArray;
	}

	public static final String[] toStringArray(Object[] array) {
		String[] newArray = new String[array.length];
		for (int i = 0; i < array.length; i++)
			newArray[i] = (String) array[i];
		return newArray;
	}

	public static final int[] toIntArray(Object[] array) {
		int[] newArray = new int[array.length];
		for (int i = 0; i < array.length; i++)
			newArray[i] = ((Integer) array[i]).intValue();
		return newArray;
	}

	public static final String[] concatenate(String[] arr1, String[] arr2) {
		String[] result;
		if (arr1 == null && arr2 == null)
			return new String[0];
		else if (arr1 == null)
			return arr2;
		else if (arr2 == null)
			return arr1;
		else
			result = new String[arr1.length + arr2.length];

		System.arraycopy(arr1, 0, result, 0, arr1.length);
		System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
		return result;
	}

	public static double[] concatenate(double[] arr1, double[] arr2) {
		if (arr1 == null)
			return arr2;
		if (arr2 == null)
			return arr1;
		double[] result = new double[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, result, 0, arr1.length);
		System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
		return result;
	}

	public static int[] concatenate(int[] arr1, int[] arr2) {
		if (arr1 == null || arr1.length == 0)
			return arr2;
		if (arr2 == null || arr2.length == 0)
			return arr1;
		int[] narr = new int[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, narr, 0, arr1.length);
		System.arraycopy(arr2, 0, narr, arr1.length, arr2.length);
		return narr;
	}

	/*
	private static final Object concatenate(Object arr1, Object arr2) {
		Object nA = new Object[((Object[]) arr1).length + ((Object[]) arr2).length];
		System.arraycopy(arr1, 0, nA, 0, ((Object[]) arr1).length);
		System.arraycopy(arr2, 0, nA, ((Object[]) arr1).length,
				((Object[]) arr2).length);
		return nA;
	}
	 */

	public static final int[] insert(int[] array, int val, int pos) {
		int[] newArray = new int[array.length + 1];
		if (pos > array.length)
			return null;
		for (int i = 0; i < pos; i++)
			newArray[i] = array[i];
		newArray[pos] = val;
		for (int j = pos; j < array.length; j++)
			newArray[j + 1] = array[j];

		return newArray;

	}

	public static final String[] insert(String[] array, String val, int pos) {
		String[] newArray = new String[array.length + 1];
		if (pos > array.length)
			return null;
		System.arraycopy(array, 0, newArray, 0, pos);
		newArray[pos] = val;
		System.arraycopy(array, pos, newArray, pos + 1, array.length - pos);
		return newArray;

	}

	public static final double[] insert(double[] array, double value, int pos) {
		double[] newArray;
		if (array == null || array.length == 0)
			newArray = new double[1];
		else
			newArray = new double[array.length + 1];
		if (array != null) {
			if (pos > array.length)
				return null;
			for (int i = 0; i < pos; i++)
				newArray[i] = array[i];
		}
		System.out.println("Insterting at pos " + pos + ". newArray size is " + newArray.length);
		newArray[pos] = value;
		if (array != null) {
			for (int j = pos; j < array.length; j++)
				newArray[j + 1] = array[j];
		}
		return newArray;
	}

	/**
	 * @param matrix
	 *            Matrix to add incoming array to
	 * @param array
	 *            The Array To add
	 * @param pos
	 *            The Position to add it
	 * @return The new Matrix
	 */
	public static final double[][] insert(double[][] matrix, double[] array, int pos) {
		double[][] newMatrix = new double[matrix.length + 1][];
		if (pos > matrix.length)
			return null;
		for (int i = 0; i < pos; i++)
			newMatrix[i] = matrix[i];
		newMatrix[pos] = array;
		for (int j = pos; j < matrix.length; j++)
			newMatrix[j + 1] = matrix[j];

		return newMatrix;
	}

	public static final int[][] insert(int[][] matrix, int[] array, int pos) {
		int[][] newMatrix = new int[matrix.length + 1][];
		if (pos > matrix.length)
			return null;
		for (int i = 0; i < pos; i++)
			newMatrix[i] = matrix[i];
		newMatrix[pos] = array;
		for (int j = pos; j < matrix.length; j++)
			newMatrix[j + 1] = matrix[j];

		return newMatrix;
	}

	public static final Object[][] insert(Object[][] matrix, Object[] array, int pos) {
		Object[][] result = new Object[matrix.length + 1][];
		if (pos > matrix.length)
			return null;
		for (int i = 0; i < pos; i++)
			result[i] = matrix[i];
		result[pos] = array;
		for (int j = pos; j < matrix.length; j++)
			result[j + 1] = matrix[j];

		return result;
	}

	public static final double[] append(double[] array, double val) {
		double[] newArray = new double[array.length + 1];
		for (int i = 0; i < array.length; i++)
			newArray[i] = array[i];
		newArray[array.length] = val;
		return newArray;
	}

	public static final Object[] append(Object[] array, Object o) {
		Object[] nA = new Object[array.length + 1];
		System.arraycopy(array, 0, nA, 0, array.length);
		nA[array.length] = o;
		return nA;
	}

	public static final String[] append(String[] arr1, String[] arr2) {
		String[] newArray = new String[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, newArray, 0, arr1.length);
		System.arraycopy(arr2, 0, newArray, arr1.length, arr2.length);
		return newArray;
	}

	public static final String[] append(String[] array, String val) {
		String[] newArray = new String[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[array.length] = val;
		return newArray;
	}

	public static final Attribute[] append(Attribute[] array, Attribute pt) {
		Attribute[] result = new Attribute[array.length + 1];
		System.arraycopy(array, 0, result, 0, array.length);
		result[array.length] = pt;
		return result;
	}

	/*public static final BayesAttribute[] append(BayesAttribute[] array,
			BayesAttribute pt) {
		BayesAttribute[] newArray = new BayesAttribute[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[array.length] = pt;
		return newArray;
	}*/

	public static final int[] append(int[] array, int val) {
		int[] newArray = new int[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[array.length] = val;
		return newArray;
	}

	public static final Object append(Object array, Object toAdd) {
		Object result;
		if (array != null) {
			result = new Object[((Object[]) array).length + 1];
			System.arraycopy(array, 0, result, 0, ((Object[]) array).length);
		} else
			result = new Object[1];
		((Object[]) result)[((Object[]) result).length - 1] = toAdd;
		return result;
	}

	public static final Object append(Object array, Object toAdd,
			int pos) {
		Object neO = new Object[((Object[]) array).length + 1];
		System.arraycopy(array, 0, neO, 0, pos);
		((Object[]) neO)[pos] = toAdd;
		System.arraycopy(array, pos, neO, pos + 1, ((Object[]) array).length - pos);
		return neO;
	}

	public static final double[] remove(double[] array, int pos) {
		if (array.length == 1)
			return new double[0];
		double[] result = new double[array.length - 1];
		for (int i = 0; i < pos; i++)
			result[i] = array[i];
		for (int j = pos + 1; j < array.length; j++)
			result[j - 1] = array[j];
		return result;
	}

	/**
	 * Removes an integer from an int array at the specified position
	 * 
	 * @param array
	 *            The array to remove an item from
	 * @param pos
	 *            the position of the array
	 * @return A new array with the position specified removed (maintains the
	 *         old array)
	 */
	public static final int[] remove(int[] array, int pos) {
		if (array.length == 1)
			return new int[0];
		int[] result = new int[array.length - 1];
		for (int i = 0; i < pos; i++)
			result[i] = array[i];
		for (int j = pos + 1; j < array.length; j++)
			result[j - 1] = array[j];
		return result;
	}

	public static final Object[] remove(Object[] array, int pos) {
		if (array.length <= 1)
			return new Object[0];

		Object[] result = new Object[array.length - 1];
		for (int i = 0; i < pos; i++)
			result[i] = array[i];
		for (int j = pos + 1; j < array.length; j++)
			result[j - 1] = array[j];
		return result;
	}

	public static final String[] removeString(String[] array, int pos) {
		if (array.length <= 1)
			return new String[0];

		String[] result = new String[array.length - 1];
		System.arraycopy(array, 0, result, 0, pos);
		System.arraycopy(array, pos + 1, result, pos, array.length - 1 - pos);
		return result;
	}
	
	//public static double[] removeAll(double[] in, double value) {
	//	double[] out =
	//}
	
	protected void finalize() throws Throwable {
	}

	public static final Double[] toDoubleArray(double[] array) {
		Double[] a = new Double[array.length];
		for (int i = 0; i < array.length; i++)
			a[i] = Double.valueOf(array[i]);

		return a;
	}
	
	public static final Integer[] toIntegerArray(int[] array) {
		Integer[] a = new Integer[array.length];
		for (int i = 0; i < array.length; i++)
			a[i] = Integer.valueOf(array[i]);

		return a;
	}

	public static final double[] subArray(double[] array, int start, int end) {
		if (end > array.length || start < 0) {
			// Really we should throw an array index out of bounds exception here.
			System.out.println("Array index out of bounds!");
			System.exit(1);
		} else if (start == end)
			return new double[0];
		double[] result = new double[end - start];
		for (int i = start; i < end; i++)
			result[i - start] = array[i];
		return result;
	}

	public static final int[] subArray(int[] array, int start, int end) {
		if (end > array.length || start < 0) {
			System.out.println("Array index out of bounds!");
			System.exit(1);
		}
		int[] result = new int[end - start];
		for (int i = start; i < end; i++)
			result[i - start] = array[i];
		return result;
	}

	public static final Object[] subArray(Object[] array, int start, int end) {
		if (end > array.length || start < 0) {
			System.out.println("Array index out of bounds");
			System.exit(1);
		}
		Object[] result = new Object[end - start];
		System.arraycopy(array, start, result, 0, end - start);
		return result;
	}

	/**
	 * Returns a subset of the passed array from start to end-1 in the array
	 * 
	 * @param array
	 *            The string array
	 * @param start
	 *            The beginning of the copy
	 * @param end
	 *            The index which to end the copy right before
	 * @return the subset of the array
	 */
	public static final String[] subArray(String[] array, int start, int end) {
		if (end > array.length || start < 0) {
			System.out.println("Array index out of bounds");
			System.exit(1);
		}
		String[] result = new String[end - start];
		System.arraycopy(array, start, result, 0, end - start);
		return result;
	}

	public static int count(double[] array, double value) {
		int n = 0;
		for (int i = 0; i < array.length; i++) {
			if (Double.valueOf(array[i]).equals(Double.valueOf(value))) {
				// Use Double.equals() can compare NaN. 
				n++;
			}
		}
		return n;
	}
	
	public static double[] removeAll(double[] array, double value) {
		double[] temp = new double[array.length];
		int len = 0;
		for (int i = 0; i < array.length; i++) {
			if (!Double.valueOf(array[i]).equals(Double.valueOf(value))) {
				temp[len] = array[i];
				len++;
			}
		}
		double[] result = new double[len];
		System.arraycopy(temp, 0, result, 0, len);
		return result;
	}

	public static final double[][] append(double[][] matrix,
			double[] array) {
		double[][] result = new double[matrix.length + 1][];
		for (int i = 0; i < matrix.length; i++)
			result[i] = matrix[i];
		result[matrix.length] = array;
		return result;
	}

	public static final Object[][] append(Object[][] matrix,
			Object[] array) {
		Object[][] result = new Object[matrix.length + 1][];
		System.arraycopy(matrix, 0, result, 0, matrix.length);
		result[matrix.length] = array;
		return result;
	}

	public static final String[][] append(String[][] matrix,
			String[] array) {
		String[][] result = new String[matrix.length + 1][];
		System.arraycopy(matrix, 0, result, 0, matrix.length);
		result[matrix.length] = array;
		return result;
	}

	public static final int[][] append(int[][] matrix, int[] array) {
		int[][] result = new int[matrix.length + 1][];
		for (int i = 0; i < matrix.length; i++)
			result[i] = matrix[i];
		result[matrix.length] = array;
		return result;
	}

	public static final double[][] remove(double[][] matrix, int pos) {
		if (matrix.length <= 1)
			return new double[0][0];

		double[][] result = new double[matrix.length - 1][];
		for (int i = 0; i < pos; i++)
			result[i] = matrix[i];
		for (int j = pos + 1; j < matrix.length; j++)
			result[j - 1] = matrix[j];
		return result;

	}

	public static final String[][] remove(String[][] matrix, int pos) {
		if (matrix.length <= 1)
			return new String[0][0];
		String[][] result = new String[matrix.length - 1][];
		System.arraycopy(matrix, 0, result, 0, pos);
		System.arraycopy(matrix, pos + 1, result, pos, matrix.length - pos - 1);
		return result;
	}

	public static final int[][] remove(int[][] matrix, int pos) {
		if (matrix.length == 1)
			return new int[0][0];
		int[][] result = new int[matrix.length - 1][];
		for (int i = 0; i < pos; i++)
			result[i] = matrix[i];
		for (int j = pos + 1; j < matrix.length; j++)
			result[j - 1] = matrix[j];
		return result;
	}

	public static final Object[][] remove(Object[][] matrix, int pos) {
		if (matrix.length == 1)
			return new Object[0][0];
		Object[][] result = new Object[matrix.length - 1][];
		for (int i = 0; i < pos; i++)
			result[i] = matrix[i];
		for (int j = pos + 1; j < matrix.length; j++)
			result[j - 1] = matrix[j];
		return result;
	}
	
	/**
	 * Returns the transpose of a matrix of doubles
	 */
 	public static final double[][] transpose(double[][] matrix) {
		double[][] result;
		if (matrix.length > 0 && matrix[0].length > 0)
			result = new double[matrix[0].length][matrix.length];
		else
			return (new double[0][0]);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++)
				result[j][i] = matrix[i][j];
		}
		return result;
	}
	
	/**
	 * Returns the transpose of a matrix of ints
	 */
	public static final int[][] transpose(int[][] matrix) {
		int[][] result;
		if (matrix.length > 0 && matrix[0].length > 0)
			result = new int[matrix[0].length][matrix.length];
		else
			return (new int[0][0]);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++)
				result[j][i] = matrix[i][j];
		}
		return result;
	}

	/**
	 * Returns the transpose of a matrix of strings
	*/	
	public static final String[][] transpose(String[][] matrix) {
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
			return null;
		String[][] result = new String[matrix[0].length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			if (i == 25)  // Huh???
				System.out.print("");
			for (int j = 0; j < matrix[i].length; j++)
				result[j][i] = matrix[i][j];
		}
		return result;
	}
	
	/** 
	 * Returns the transpose of a matrix of objects 
	 */
	public static final Object[][] transpose(Object[][] matrix) {
		Object[][] result;
		if (matrix != null && matrix.length > 0 && matrix[0].length > 0)
			result = new Object[matrix[0].length][matrix.length];
		else
			return (new Object[0][0]);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++)
				result[j][i] = matrix[i][j];
		}
		return result;
	}
	
	/**
	 * Tests whether a double value is in the array
	 */
	public static final boolean contains(double[] array, double val) {
		if (array == null)
			return false;
		for (double d : array) {
			if (d == val || (Double.isNaN(d) && Double.isNaN(val))
					|| (Double.isInfinite(d) && Double.isInfinite(val)))
				return true;
		}
		return false;
	}

	/**
	 * Tests whether a int value is in the array
	 */
	public static final boolean contains(int[] array, int key) {
		for (int i : array) {
			if (i == key)
				return true;
		}
		return false;
	}

	/**
	 * Tests whether an object is in an array. If the array is an
	 * array of Attribute objectss and the object is a string, then this method compares the
	 * attribute name to the object
	 * 
	 * @param array
	 *            the Array of Objects
	 * @param key
	 *            the object to compare
	 * @return true if object is contained within the array
	 */
	public static final boolean contains(Object[] array, Object key) {
		if (array == null)
			return false;
		if (array[0] instanceof Attribute && key instanceof String) {
			for (Object o : array) {
				if (((Attribute) o).name().equals(key))
					return true;
			}
		} else {
			for (Object o : array) {
				if (o.equals(key))
					return true;
			}
		}
		return false;
	}

	/**
	 * Finds the first index of the double value key starting at index start
	 * 
	 * @param array
	 *            the array to search
	 * @param key
	 *            the double value for which to search the array with
	 * @param start
	 *            the starting index
	 */
	public static final int indexOf(double[] array, double key, int start) {
		if (array == null || array.length == 0 || start > array.length - 1)
			return -1;
		for (int i = start; i < array.length; i++) {
			if (array[i] == key || (Double.isNaN(array[i]) && Double.isNaN(key))
					|| (Double.isInfinite(array[i]) && Double.isInfinite(key)))
				return i;
		}
		return -1;
	}

	public static final int indexOf(Object[] arr, Object key) {
		if (arr[0] instanceof Attribute && key instanceof String) {
			for (int i = 0; i < arr.length; i++) {
				if ((((Attribute) arr[i]).name()).equals(key))
					return i;
			}
		} else {
			for (int i = 0; i < arr.length; i++) {
				if (arr[i].equals(key))
					return i;
			}
		}
		return -1;
	}

	public static final boolean containsOverlap(String[] arr1) {
		for (int i = 0; i < arr1.length - 1; i++) {
			if (contains(subArray(arr1, i, arr1.length - 1), arr1[i]))
				return false;
		}
		return true;
	}

	/**
	 * Copies a matrix of doubles.
	 *  @param matrix The double Matrix to be copied
	 * @return An exact copy of the original matrix
	 */
	public static final double[][] clone(double[][] matrix) {
		double[][] copy = null;
		try {
			copy = new double[matrix.length][matrix[0].length];
			for (int i = 0; i < matrix.length; i++)
				System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
		} catch (Exception e) {
		}
		return copy;
	}

	/**
	 * Copies a matrix of ints.
	 *  @param matrix The int Matrix to be copied @return
	 * An exact copy of the original matrix
	 */
	public static final int[][] clone(int[][] matrix) {
		int[][] copy = null;

		copy = new int[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++)
			System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
		return copy;
	}

	/**
	 * Copies a matrix of objects.
	 * @param matrix The String Matrix to be copied
	 * @return An exact copy of the original matrix
	 */
	public static final Object[][] clone(Object[][] matrix) {
		String[][] copy = null;
		try {
			copy = new String[matrix.length][matrix[0].length];
			for (int i = 0; i < matrix.length; i++)
				System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
		} catch (Exception e) {
		}
		return copy;
	}

	/**
	 * Combines two matrices of doubles by row. So you have elements: AAAAA and
	 * CCCCC AAAAA BBBBB DDDDD which goes to BBBBB CCCCC DDDDD
	 * 
	 * @param m1
	 *            the first matrix
	 * @param m2
	 *            the second matrix
	 * @return the combined matrix
	 */
	public static final double[][] concatVertically(double[][] m1,
			double[][] m2) {
		double[][] result = m1;
		for (double[] ar : m2)
			result = append(result, ar);
		return result;
	}

	/**
	 * Used to combine two matrices by row. So you have elements: AAAAA and
	 * CCCCC AAAAA BBBBB DDDDD which goes to BBBBB CCCCC DDDDD
	 * 
	 * @param m1
	 *            the first matrix
	 * @param m2
	 *            the second matrix
	 * @return the combined matrix
	 */
	public static final int[][] concatVertically(int[][] m1, int[][] m2) {
		int[][] result = m1;
		for (int[] ar : m2)
			result = append(result, ar);
		return result;
	}
	
	/**
	 * Combines two matrices by column. So you have elements: AAAAA and
	 * DDDDD AAAAADDDDD BBBBB EEEEE which goes to BBBBBEEEEE CCCCC FFFFF
	 * CCCCCFFFFF
	 * 
	 * @param m1
	 *            the first matrix
	 * @param m2
	 *            the second matrix
	 * @return the combined matrix
	 */
	public static final Object[][] concatHorizontally(Object[][] m1,
			Object[][] m2) {
		if (m1.length != m2.length) {
			System.err.println("Cannot concatenate the two matricies because they have different widths");
			return m1;
		}
		Object[][] result = new Object[m1.length][];
		for (int i = 0; i < m1.length; i++) {
			Object[] elem = new Object[m1[i].length + m2[i].length];
			System.arraycopy(m1[i], 0, elem, 0, m1[i].length);
			System.arraycopy(m2[i], 0, elem, m1[i].length, m2[i].length);
			result[i] = elem;
		}
		return result;
	}

	/**
	 * Combines two matrices by row. So you have elements: AAAAA and
	 * CCCCC AAAAA BBBBB DDDDD which goes to BBBBB CCCCC DDDDD
	 * 
	 * @param m1
	 *            the first matrix
	 * @param m2
	 *            the second matrix
	 * @return the combined matrix
	 */
	public static final Object[][] concatVertically(Object[][] m1,
			Object[][] m2) {
		if (m2 == null || m2.length == 0)
			return m1;
		Object[][] result = m1;
		for (int j = 0; j < m2.length; j++) {
			if (m2[j].length != m1[0].length) {
				System.err.println("Cannot concatenate the two matricies because they have different widths");
				return m1;
			}
		}
		for (Object[] o : m2)
			result = append(result, o);
		return result;
	}

	/**
	 * Checks whether a matrix is empty, that is if it has 0 columns.
	 * 
	 * @param matrix
	 *            the matrix
	 * @return True if it is empty, false if it is not
	 */
	public static final boolean isEmpty(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i] != null && matrix[i].length > 0)
				return false;
		}
		return true;
	}

	public static final String toString(Object[] array, String sep) {
		StringBuffer buf = new StringBuffer();
		if (array != null && array.length > 0) {
			buf.append(array[0].toString());
			for (int i = 1; i < array.length; i++) {
				buf.append(sep);
				buf.append(array[i].toString());
			}
		} else
			buf.append("");
		return buf.toString();
	}

	public static final String toString(double[] array, String sep) {
		StringBuffer buf = new StringBuffer();
		buf.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			buf.append(sep);
			buf.append(array[i]);
		}
		return buf.toString();
	}

	public static final String toString(int[] array, String sep) {
		StringBuffer buf = new StringBuffer();
		buf.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			buf.append(sep);
			buf.append(array[i]);
		}
		return buf.toString();
	}

	public static final String toString(String[] strings){
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < strings.length; i++ ) {
			buf.append(strings[i] + " ");
		}
		return buf.toString();
	}
	
	/**
	 * Sets all elements of an int array to the same value
	 * 
	 * @param array
	 *            the array to initialize
	 * @param val
	 *            the value to intialize the array to
	 * @return the array with the initialized values
	 */
	public static final int[] init(int[] array, int val) {
		for (int i = 0; i < array.length; i++)
			array[i] = val;

		return array;
	}

	/**
	 * Sets all elements of an int matrix the same value
	 * 
	 * @param array
	 *            the Matrix to initialize
	 * @param val
	 *            the value to intialize the array to
	 * @return the array with the initialized values
	 * 
	 * @see #init(int[], int)
	 */
	public static final int[][] init(int[][] array, int val) {
		for (int i = 0; i < array.length; i++)
			array[i] = init(array[i], val);

		return array;
	}

	/**
	 * Sets all elements of a double array to the same value
	 * 
	 * @param array
	 *            the Array to initialize
	 * @param val
	 *            the value to intialize the array to
	 * @return the array with the initialized values
	 */
	public static final double[] init(double[] array, double val) {
		for (int i = 0; i < array.length; i++)
			array[i] = val;

		return array;
	}

	/**
	 * Initializes the double matrix to the passed double value
	 * 
	 * @param array
	 *            the Matrix to initialize
	 * @param val
	 *            the value to intialize the array to
	 * @return the array with the initialized values
	 * 
	 * @see #init(double[], double)
	 */
	public static final double[][] init(double[][] array, double val) {
		for (int i = 0; i < array.length; i++)
			array[i] = init(array[i], val);

		return array;
	}
	
	public static final Attribute[] cloneAttrArray(Attribute[] array) {
		Attribute[] result = new Attribute[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = (Attribute) array[i].clone();
		}
		return result;
	}

	public static final boolean isAllSameVals(double[] array) {
		if (array == null || array.length == 0)
			return true;
		else {
			double val = array[0];
			for (double d : array) {
				if (d != val)
					return false;
			}
			return true;
		}
	}

	public static final boolean isAllSameType(Object[] array) {
		for (Object o: array) {
			if (o.getClass() != array[0].getClass())
				return false;
		}
		return true;
	}
}