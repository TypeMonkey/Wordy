package wordy.standard;

public class Array {
  
  private Object[] array;
  public final int length;
  
  public Array() {
    this(0);
  }
  
  public Array(int size) {
    array = new Object[size];
    length = size;
  }
  
  public Object get(int index) {
    return array[index];
  }
  
  public Object set(int index, Object object) {
    Object prev = array[index]; 
    array[index] = object;
    return prev;
  }
}
