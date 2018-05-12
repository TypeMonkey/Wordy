package wordy.logic.runtime.execution;

import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Variable;
import wordy.logic.runtime.Constant;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.types.TypeDefinition;
import wordy.logic.runtime.types.TypeInstance;
import wordy.logic.runtime.types.Instance;

/**
 * Represents a class constructor that when invoked
 * returns an instance of the TypeDefinition
 * it's declared in
 * @author Jose Guaro
 *
 */
public class Constructor extends FunctionMember{

  private TypeDefinition definition;
  
  public Constructor(String name, int argumentAmnt, 
                                  Statement[] statements, 
                                  TypeDefinition definition, 
                                  WordyRuntime runtime) {
    super(name, argumentAmnt, runtime, statements);
    this.definition = definition;
  }
  
  public Constant call(GenVisitor visitor, RuntimeTable table, Constant ... args) {
    table = table.clone();
    visitor = new GenVisitor(table, runtime);
    System.out.println("-----CONSTRUCTOR!!!! "+definition.getName()+"------");
    
    
    TypeInstance typeInstance = new TypeInstance(definition);
    typeInstance.copyInstanceVars();
    
    /*
     * Initialize the instance variables
     */
    for(VariableMember member: definition.getVariables().values()) {
      System.out.println("----PLACING INSTANCE VAR: "+member.getName());
      if (member.getExpr() != null) {
        member.getExpr().accept(visitor);
        VariableMember result = visitor.peekStack();
        member.setValue(result, result.getType());
      }
    }
        
    /*
     * Then execute the actual statements of this constructor
     */
    if (statements != null) {
      /*
       * Null check as the default constructor has no statements, other than
       * initializations.
       */
      super.call(visitor, table, args);  
    }
    /*
     * Then return a new instance based on the provided TypeDefinition
     */
    System.out.println("----!!POST CONSTRUCTOR "+definition.getName()+"!!----");
    return new Constant(typeInstance.getDefinition().getType(), typeInstance);
  }

  /**
   * Checks if this FunctionMember is a class constructor
   * @return false if this FunctionMember isn't a constructor,
   *         true if this is a FunctionMember is a constructor
   */
  public boolean isAConstructor() {
    return true;
  }

}
