package wordy.logic.runtime.execution;

import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Variable;
import wordy.logic.runtime.Constant;
import wordy.logic.runtime.RuntimeExecutor;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.types.TypeDefinition;
import wordy.logic.runtime.types.TypeInstance;

/**
 * Represents a class constructor that when invoked
 * returns an instance of the TypeDefinition
 * it's declared in
 * @author Jose Guaro
 *
 */
public class Constructor extends FunctionMember{

  private TypeDefinition definition;
  
  public Constructor(String name, int argumentAmnt, Statement[] statements, TypeDefinition definition) {
    super(name, argumentAmnt, statements);
    this.definition = definition;
  }
  
  public Constant call(GenVisitor visitor, RuntimeExecutor executor, Constant ... args) {
    executor = executor.clone();
    System.out.println("-----CONSTRUCTOR!!!! "+definition.getName()+"------");
    visitor = new GenVisitor(executor);
    /*
     * Initialize the instance variables
     */
    TypeInstance typeInstance = new TypeInstance(definition);
    for(Variable member: definition.getVariables()) {
      VariableMember varMem = new VariableMember(member.getName().content(), member.isConstant());
      System.out.println("----PLACING VAR: "+varMem.getName());
      executor.placeLocalVar(varMem);
      if (member.getExpression() != null) {
        member.getExpression().visit(visitor);
        varMem.setValue(visitor.peekStack(), visitor.peekStack().getType());
      }
      typeInstance.placeVariable(varMem);
    }
    
    VariableMember thisVar = new VariableMember("this", true);
    thisVar.setValue(typeInstance, typeInstance.getDefinition().getType());
    executor.placeLocalVar(thisVar);
    /*
     * Then execute the actual statements of this constructor
     */
    if (statements != null) {
      /*
       * Null check as the default constructor has no statements, other than
       * initializations.
       */
      super.call(visitor, executor, args);  
    }
    /*
     * Then return a new instance based on the provided TypeDefinition
     */
    System.out.println("----!!POST COMNSTRUCTOR "+definition.getName()+"!!----");
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
