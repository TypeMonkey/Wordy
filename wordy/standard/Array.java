package wordy.standard;

/**
 * A fixed sized array.
 * @author Jose Guaro
 *
 */
public class Array {
  
  private Object[] array;
  public final int length;
  
  /**
   * Constructs an array of size 0
   */
  public Array() {
    this(0);
  }
  
  /**
   * Constructs an array of custom size
   * @param size - how big the array should be
   */
  public Array(int size) {
    array = new Object[size];
    length = size;
  }
  
  /**
   * Retrieves an element in the array
   * @param index - the index on the array to retrieve
   */
  public Object get(int index) {
    return array[index];
  }
  
  /**
   * Sets the given index placement of the array to a new element
   * @param index - the index to which the operation is to occur
   * @param object - the new element to place at that index
   * @return the object that was formerly in the given index, or null if there was no object
   */
  public Object set(int index, Object object) {
    Object prev = array[index]; 
    array[index] = object;
    return prev;
  }
  
  public Array clone() {
    Array clone = new Array(array.length);
    clone.array = this.array.clone();
    return clone;
  }
}
