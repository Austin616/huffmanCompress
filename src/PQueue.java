/*  Student information for assignment:
 *
 *  On <OUR> honor, <Austin> and <Micayla>, this programming assignment is <OUR> own work
 *  and <WE> have not provided this code to any other student.
 *
 *  Number of slip days used:
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: AAT3377
 *  email address: austintran616@gmail.com
 *  Grader name: Sai
 *
 *  Student 2: Micayla Bustillos
 *  UTEID: mvb567
 *  email address: micayla.bustillos@utexas.edu
 *
 */

import java.util.LinkedList;

public class PQueue<E extends Comparable<E>> {
    private LinkedList<E> myCon;
    public PQueue() {
        myCon = new LinkedList<>();
    }
    // Adds the value to the PQueue
    public void enqueue(E value) {
        int index = 0;
        while (index < myCon.size() && value.compareTo(myCon.get(index)) >= 0) {
            index++;
        }
        myCon.add(index, value);
    }

    // Removes the first value in "line"
    public E dequeue() {
        if (size() == 0) {
            throw new IllegalStateException("Queue is empty");
        }
        return myCon.poll();
    }


    // Returns the size of the queue
    public int size() {
        return myCon.size();
    }
}
