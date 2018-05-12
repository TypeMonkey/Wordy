package wordy.logic.runtime.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValType {
  
  public static final ValType OBJECT = new ValType("Object");
  
  static {
    //to prevent an infinite recursive type check when checking Object
    OBJECT.parents.clear();
  }
  public static final ValType PRIMITVE = new ValType("Primitve");
  public static final ValType NUMBER = new ValType("Number", PRIMITVE);
  public static final ValType DOUBLE = new ValType("double", NUMBER); 
  public static final ValType INTEGER = new ValType("int", NUMBER); 
  public static final ValType BOOLEAN = new ValType("boolean", PRIMITVE); 
  public static final ValType STRING = new ValType("boolean", OBJECT);  
  public static final ValType VOID = new ValType("void");

  private String typeName;
  private List<ValType> parents;
  
  public ValType(String typeName) {
    this(typeName, OBJECT);
  }
  
  public ValType(String typeName, ValType ... parentTypes) {
    this.typeName = typeName;
    this.parents = new ArrayList<>();
    parents.addAll(Arrays.asList(parentTypes));
  }
  
  public List<ValType> parents() {
    return parents;
  }
  
  public String getTypeName() {
    return typeName;
  }
  
  public boolean equals(Object type) {
    if (type instanceof ValType) {
      ValType check = (ValType) type;
      return typeName.equals(check.typeName);
    }
    return false;
  }
  
  /**
   * Checks if the given type is the parent of this current
   * type
   * @param type - the Type to check
   * @return true if type is a parent of this current type, false if else
   */
  public boolean isChildOf(ValType type) {
    if (parents.contains(type)) {
      return true;
    }
    else {
      for(ValType par: parents) {
        System.out.println("TYPE: "+par.typeName);
        if (par.isChildOf(type)) {
          return true;
        }
      }
      return false;
    }
  }
}
