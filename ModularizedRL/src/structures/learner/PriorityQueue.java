package structures.learner;

import java.util.ArrayList;

/**
 * PriorityQueue.java
 * 
 * <p>
 * Title: JavaRL
 * </p>
 * <p>
 * Description: rule induction for knowledge discovery
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Intelligent Systems Laboratory at University of Pittsburgh
 * </p>
 * 
 * @author Eric Williams
 * @version 0.8 (new version numbering system)
 * @since 0.8
 */

public interface PriorityQueue {

	Object dequeue() throws Exception;

	void enqueue(Object obj);

	Object front() throws Exception;

	boolean isEmpty();

	boolean isFull();

	int size();

	Object get(int index) throws Exception;

	void trimToCapacity();

	ArrayList toSortedArrayList() throws Exception;

	void clear();
}
