package wordy.logic.runtime.execution;

import java.util.ArrayList;
import java.util.Arrays;

import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.nodes.ASTNode.NodeType;
import wordy.logic.compile.nodes.MethodCallNode;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Variable;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.StackComponent;
import wordy.logic.runtime.components.TypeInstance;
import wordy.logic.runtime.errors.InvocationException;
import wordy.logic.runtime.types.TypeDefinition;

/**
 * Represents a class constructor that when invoked
 * returns an instance of the TypeDefinition
 * it's declared in
 * @author Jose Guaro
 *
 */
public class ConstructorFunction extends FunctionMember{

  private TypeDefinition definition;
  
  public ConstructorFunction(String name, int argumentAmnt, 
                                  Statement[] statements, 
                                  TypeDefinition definition,
                                  FileInstance currentFile,
                                  WordyRuntime runtime) {
    super(name, argumentAmnt, runtime, currentFile, statements);
    //System.out.println("****CREATING CONSTRUCTOR: "+name+" | statements: "+statements.length);
    this.definition = definition;
  }
  
  public Instance call(GenVisitor visitor,  RuntimeTable table, Instance ... args) throws InvocationException{
    table = table.clone(false);
    table.addFuncMap(currentFile.getDefinition().getFunctions());
    visitor = new GenVisitor(table, currentFile, runtime);
    //System.out.println("-----CONSTRUCTOR!!!! "+definition.getName()+"------");

    Instance superInstance = null;

    RuntimeTable superTable = table.clone(false);
    superTable.clearLocalVars();

    GenVisitor superVisitor = new GenVisitor(superTable, currentFile, runtime);
    
    /*
     * The actual statements that we'll execute
     */
    ArrayList<Statement> actualFuncStatements = new ArrayList<>(Arrays.asList(statements));
    
    /*
     * Add all constructor parameters to symbol table
     */
    for(int i = 0; i < argAmnt; i++) {
      Variable rawParam = (Variable) statements[i];
      VariableMember param = new VariableMember(rawParam.getName().content(), args[0], null, rawParam.isConstant());
      superTable.placeLocalVar(param);
    }

    /*
     * Check the first statement. If it's a super constructor invocation, then
     * execute it. However, change reassign the "statements" variable to not include
     * that invocation.
     * 
     * If the first statement isn't a super constructor invocation,
     * then get the no-arg constructor of the parent and invoke that.
     */
    TypeDefinition parentDef = definition.getParent();
    //System.out.println("Constructor name: "+name+" | args: "+Arrays.toString(args)+" | "+statements.length);
    if (statements != null && (statements.length - argAmnt) >= 1) {
      Statement first = statements[argAmnt];
      //System.out.println("--constructor statement: "+Arrays.toString(first.getExpression().tokens()));
      if (first.getExpression().nodeType() == NodeType.FUNC_CALL) {
        MethodCallNode superInvoke = (MethodCallNode) first.getExpression();
        if (superInvoke.getName().content().equals("super")) {
          //System.out.println("----super call is first!");
          FunctionMember parentConstructor = parentDef.findConstructor(superInvoke.arguments().length);

          Instance [] parentConstArgs = new Instance[superInvoke.arguments().length];

          for(int i = 0 ; i < parentConstArgs.length; i++) {
            superVisitor.resetStack();
            superInvoke.arguments()[i].accept(superVisitor);
            StackComponent peeked = superVisitor.peekStack();
            if (peeked.isAnInstance()) {
              parentConstArgs[i] = (Instance) peeked;
            }
            else {
              VariableMember variable = (VariableMember) peeked;
              parentConstArgs[i] = variable.getValue();
            }
          }

          //reset for last argument
          superVisitor.resetStack();

          if (parentConstructor.argumentsCompatible(parentConstArgs)) {


            try {
              superInstance = parentConstructor.call(visitor, superTable, parentConstArgs);
            } catch (InvocationException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } 
          }
          else {
            throw new RuntimeException("Incompatible argument types at line "+superInvoke.tokens()[0].lineNumber());
          }
        }

        //remove this statement from the statement array
        actualFuncStatements.remove(argAmnt); //removes the super call
      }
      else {
        //invoke non-arg constructor
        FunctionMember parentConstructor = parentDef.findConstructor(0);
        //System.out.println("---NULL? "+parentConstructor+" | "+parentDef.getName());
        superInstance = parentConstructor.call(visitor, superTable, new Instance[0]);
        
      }
    }
    else {
      //empty constructor: either explicitly empty, or implicitly included
      //invoke non-arg constructor
      FunctionMember parentConstructor = parentDef.findConstructor(0);
      //System.out.println("---NULL? "+(parentConstructor == null)+" | "+parentDef.getName());
      //System.out.println(" ----constructors: "+parentDef.getConstructors().keySet());
     // System.out.println("---Which? "+statements.length+" | "+argAmnt);
      superInstance = parentConstructor.call(visitor, superTable, new Instance[0]);      
    }

    TypeInstance typeInstance = TypeInstance.newInstance(definition, superInstance);

    /*
     * Initialize the instance variables
     */
    for(VariableMember member: typeInstance.varMap().values()) {
      //System.out.println("!!!PLACING INSTANCE VAR: "+member.getName());
      table.placeLocalVar(member);
      if (member.getExpr() != null) {
        member.getExpr().accept(visitor);
        StackComponent result = visitor.peekStack();
        if (result.isAnInstance()) {
          member.setValue((Instance)result);
        }
        else {
          VariableMember variableMember = (VariableMember) result;
          //System.out.println("!- "+variableMember.getValue());
          member.setValue(variableMember.getValue());
        }
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
      visitor.resetStack();
      
      /*
       * Dummy function that will execute the only necessary statements 
       */
      FunctionMember member = new FunctionMember(name, 
          argAmnt, 
          runtime, 
          currentFile, 
          actualFuncStatements.toArray(new Statement[actualFuncStatements.size()]));
      member.call(superVisitor, table, args);

      //super.call(visitor, table, args);

    }
    /*
     * Then return a new instance based on the provided TypeDefinition
     */
    //System.out.println("----!!POST CONSTRUCTOR "+definition.getName()+"!!----");
    return typeInstance;
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
