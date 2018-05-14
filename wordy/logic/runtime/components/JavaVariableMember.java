package wordy.logic.runtime.components;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.types.JavaClassDefinition;

/**
 * Represents a Java variable.
 * 
 * Note: when retrieving or setting the value of a JavaVariableMember, 
 *       setTarget() must be called prior if the variable is an instance variable.
 *       If it's static, such call isn't required
 * @author Jose Guaro
 *
 */
public class JavaVariableMember extends VariableMember{

  private Field field;
  private Object target;
  
  public JavaVariableMember(Field field, JavaClassDefinition valType) {
    super(field.getName(), Modifier.isFinal(field.getModifiers()));
    this.field = field;
    this.type = valType;
  }
  
  public void setTarget(Object object) {
    this.target = object;
  }
  
  public void setValue(Instance constant) {
    if (isConstant) {
      throw new IllegalStateException("Can't change the value of a constant variable");
    }
    else if (constant instanceof JavaInstance == false) {
      throw new IllegalArgumentException("A Java variable can only be set to Java instances");
    }
    JavaInstance instance = (JavaInstance) constant;
    try {
      field.set(target, instance.getInstance());
      type = instance.getDefinition();
    } catch (Exception e) {
      throw new RuntimeException("Error occured while setting "+name+" : "+System.lineSeparator()+e.getMessage());
    }
  }
  
  public Instance getValue() {
    try {
      return JavaInstance.wrapInstance(field.get(target));
    } catch (Exception e) {
      throw new RuntimeException("Error occured while retrieving "+name+" : "+System.lineSeparator()+e.getMessage());
    }
  }
  
  public boolean isStatic() {
    return Modifier.isStatic(field.getModifiers());
  }
  
  public ASTNode getExpr() {
    return expr;
  }
  
  public VariableMember clone() {
    return new JavaVariableMember(field, (JavaClassDefinition) type);
  }
  
  public String toString() {
    return "VARIABLE: "+getName();
  }
  
  public Object getTarget() {
    return target;
  }

}
