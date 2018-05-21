package wordy.standard;

public class ArrayList {
  
  private int actualSize;
  private Array array;
  
  public ArrayList() {
    this(0);
  }
  
  public ArrayList(int initialSize) {
    this(new Array(initialSize));
  }
  
  public ArrayList(Array array) {
    this.array = array.clone();
    actualSize = array.length;
  }
  
  public Object get(int index) {
    return array.get(index);
  }
  
  public void add(Object element) {
    if (actualSize < array.length) {
      array.set(array.length-1, element);
    }
    else {
      Array old = array;
      array = new Array(old.length + 1);
      for(int i = 0; i < old.length; i++) {
        array.set(i, old.get(i));
      }
      array.set(old.length, element);
    }
    actualSize++;
  }
  
  public int size() {
    return actualSize;
  }
}
