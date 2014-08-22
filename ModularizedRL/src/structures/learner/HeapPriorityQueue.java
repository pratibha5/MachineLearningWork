/*
 * @(#)HeapPriorityQueue.java
 *
 * Title: JavaRL<br>
 * Description: rule induction for knowledge discovery<br>
 * Copyright: Copyright (c) 2002<br>
 * Company: Intelligent Systems Laboratory at University of Pittsburgh<br>
 */

package structures.learner;

//import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * An implementation of the PriorityQueue interface using an array-based binary
 * heap. The queue grows to twice its capacity and then it is trimmed. This
 * class does not extend ArrayList in order to keep the queue private.
 * 
 * @author Eric Williams
 * @version 0.8
 * @since 0.8
 */
public class HeapPriorityQueue implements PriorityQueue {
	private ArrayList heapArray;
	private final int maxSize, capacity;
	private final Comparator comp;
	//private PrintStream pr;

	/**
	 * Constructs a priority queue with capacity <code>cap</code> and
	 * comparator <code>comparator</code>. The queue's maximum size is
	 * initialized as <code>2 * initialCapacity</code>, and is trimmed when
	 * needed.
	 * 
	 * @param comparator
	 *            a comparator used for sorting
	 * @param cap
	 *            the desired queue length
	 */
	public HeapPriorityQueue(Comparator comparator, int capacity) {
		this.capacity = capacity;
		maxSize = 2 * capacity;
		comp = comparator;
		heapArray = new ArrayList(); // Will be trimmed to capacity when
		// needed
		/*
		 * try{ pr = new PrintStream(new
		 * FileOutputStream("BeamRealTime.txt",true)); }catch(Exception e){
		 * System.out.println("Can't initialize text for recording
		 * beam....error!"); System.exit(1); }
		 */
	}

	public void clear() {
		heapArray.clear();
	}

	/**
	 * Returns the index of the left child of the specified node
	 * 
	 * @param index
	 *            the index of the node whose left child to get
	 * @return the index of the left child of the node at <code>index</code>
	 */
	private int left(int index) {
		return 2 * (index + 1) - 1;
	}

	/**
	 * Returns the index of the right child of the specified node.
	 * 
	 * @param index
	 *            the index of the node whose right child to get
	 * @return the index of the right child of the node at <code>index</code>
	 */
	private int right(int index) {
		return 2 * (index + 1);
	}

	/**
	 * Returns the index of the parent of the specified node.
	 * 
	 * @param index
	 *            the index of the node whose parent to get
	 * @return index of the parent of the node at <code>index</code>
	 */
	private int parent(int index) {
		return ((index + 1) / 2) - 1;
	}

	/**
	 * Restores the heap property for the tree rooted at <code>index</code>
	 * 
	 * @param index
	 *            the root of the tree for which to restore the heap property
	 */
	private void heapify(int index) {
		int l = left(index);
		int r = right(index);
		Object temp;

		int largest;
		if (l < size()
				&& comp.compare(heapArray.get(l), heapArray.get(index)) == -1)
			largest = l;
		else
			largest = index;
		if (r < size()
				&& comp.compare(heapArray.get(r), heapArray.get(largest)) == -1)
			largest = r;
		if (largest != index) {
			temp = new Object();
			temp = heapArray.get(index);
			heapArray.set(index, heapArray.get(largest));
			heapArray.set(largest, temp);
			heapify(largest);
		}
	}

	/**
	 * Returns this priority queue as a sorted <code>ArrayList<code>.
	 *
	 * @return a fully sorted ArrayList representation of this queue
	 * @throws Exception if this queue is empty
	 * @see ArrayList
	 */
	public ArrayList toSortedArrayList() throws Exception {
		ArrayList sortedList = new ArrayList();
		ArrayList tempList = new ArrayList(heapArray);

		// Why not?! -PG200221114
		/*
		 * if(isEmpty()) { throw new Exception("cannot export empty heap to
		 * ArrayList"); }
		 */

		tempList.trimToSize();

		while (!isEmpty()) {
			try {
				sortedList.add(dequeue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		heapArray = tempList;

		return sortedList;
	}

	public void makeQueue(ArrayList newList) {
		heapArray = newList;
		for (int i = newList.size() / 2; i >= 0; i--) {
			heapify(i);
		}
	}

	/**
	 * Trims this queue to the capacity given in the constructor
	 */
	public void trimToCapacity() {
		int currentSize = size();
		ArrayList sortedList = new ArrayList();

		if (currentSize > capacity) {
			try {
				sortedList = toSortedArrayList();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// sortedList.removeRange(capacity, maxSize);
			if (currentSize > capacity) {
				for (int i = 0; i < (currentSize - capacity); i++) {
					sortedList.remove(capacity);
				}
			}
			makeQueue(sortedList);
			/*
			 * if(currentSize > capacity) { for(int i = 0; i < (currentSize -
			 * capacity); i++) { heapArray.remove(capacity); } }
			 */
		}
	}

	/**
	 * Returns the length of this queue
	 * 
	 * @return the length of this queue
	 */
	public int size() {
		return heapArray.size();
	}

	/**
	 * Returns <code>true</code> if this queue is empty and <code>false</code>
	 * otherwise
	 * 
	 * @return <code>true</code> if this queue is empty, <code>false</code>
	 *         otherwise
	 */
	public boolean isEmpty() {
		return heapArray.isEmpty();
	}

	/**
	 * Returns <code>true</code> if this queue is full and <code>false</code>
	 * otherwise
	 * 
	 * @return <code>true</code> if this queue has exactly
	 *         <code>capacity</code> elemetns, <code>false</code> otherwise
	 * @see #capacity
	 */
	public boolean isFull() {
		return size() == capacity;
	}

	/**
	 * Removes the element with the highest priority from this queue
	 * 
	 * @return the element with the highest priority
	 * @throws Exception
	 *             if this queue is empty
	 */
	public Object dequeue() throws Exception {
		if (isEmpty())
			throw new Exception("heap underflow");

		Object max = new Object();
		max = heapArray.get(0);
		heapArray.set(0, heapArray.get(size() - 1));
		heapArray.remove(size() - 1);
		heapify(0);

		return max;
	}

	/*
	private String replaceAllParens(String val) {
		String newVal = new String();
		int start = 0;
		while (val.indexOf(", ((", start) > -1) {
			int i = val.indexOf(", ((", start);
			newVal = newVal + val.substring(start, i);
			newVal = newVal + "\n";
			start = i + 2;
		}
		newVal = newVal + val.substring(start);
		return newVal;
	}
	 */

	/**
	 * Adds a new element to this queue
	 * 
	 * @param newNode
	 *            the <code>Object</code> to be added
	 */
	public void enqueue(Object newNode) {
		// pr.println("//---------------------------------- New Node
		// #"+(heapArray.size()+1)+"-----------------------");
		heapArray.add(newNode);
		int i = size() - 1;
		int level = 1;
		// pr.println("Level 1");
		// pr.println(replaceAllParens(heapArray.toString())+"\n");
		while (i > 0 && (comp.compare(heapArray.get(parent(i)), newNode) == 1)) {
			level++;
			// pr.println("Level "+level+" --> Parent("+i+") = "+parent(i)+"
			// with the
			// comparison:\n\t"+newNode.toString()+"\nand\n\t"+heapArray.get(parent(i)).toString()+"\n");
			heapArray.set(i, heapArray.get(parent(i)));
			// pr.println(replaceAllParens(heapArray.toString())+"\n");
			// if (i > 0)
			i = parent(i);
		}
		// pr.println("Final Array: ");
		heapArray.set(i, newNode);
		// pr.println(replaceAllParens(heapArray.toString())+"\n");
		if (size() == maxSize) {
			// FIXME: BUG!!!! only checks size for strict equality, so it will not trim if size > maxSize
			// Actually it should never trim
			trimToCapacity();
		}
	}

	/**
	 * Peeks at the highest priority element without removing it
	 * 
	 * @return a reference to the element with the highest priority
	 * @throws Exception
	 *             if this queue is empty
	 */
	public Object front() throws Exception {
		if (isEmpty())
			throw new Exception("heap underflow");

		Object max = heapArray.get(0);
		return max;
	}

	public ArrayList toArrayList() {
		return heapArray;
	}

	public Object get(int index) throws Exception {
		if (heapArray.size() < index)
			throw new Exception(
			"HeapPriorityQueue index out of range exception");

		return heapArray.get(index);
	}
}