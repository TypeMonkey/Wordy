package wordy.logic.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.Variable;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.Constructor;
import wordy.logic.runtime.execution.FunctionMember;
import wordy.logic.runtime.execution.GenVisitor;
import wordy.logic.runtime.types.TypeDefinition;
import wordy.logic.runtime.types.ValType;

/**
 * Builds the runtime environment for executing the code
 * interpreted from the source files.
 * 
 * Note: Runtime building start from the first 
 * 
 * @author Jose Guaro
 *
 */
public class RuntimeBuilder {
  
  private FileStructure structure;
  private List<Callable> callables;
  private List<VariableMember> fileVars;
  private List<SystemFunction> sysFuncs;
  
  public RuntimeBuilder(FileStructure fileStructure) {
    this.structure = fileStructure;
    callables = new ArrayList<>();
    fileVars = new ArrayList<>();
    sysFuncs = new ArrayList<>();
  }
  
  public RuntimeTable build() {    
    addSysFunctions();
    /*
     * Add class constructors to callable map
     */
    for(ClassStruct struct: structure.getClasses()) {
      TypeDefinition structDef = TypeDefinition.constructDefinition(struct);
      boolean constructorFound = false;
      for(FunctionMember funcMem : structDef.getFunctions()) {
        System.out.println("**CYCLING FUNCS FOR "+structDef.getName()+" "+funcMem.isAConstructor()+" | "+funcMem.getName());
        if (funcMem.isAConstructor()) {
          constructorFound = true;
          System.out.println("****ADDING CONSTRUCTOR: "+funcMem.getName());
          callables.add(funcMem);
        }
      }
      
      if (constructorFound == false) {
        /*
         * Insert default constructor
         */
        Constructor defaultCons = new Constructor(struct.getName().content(), 0, null, structDef);
        System.out.println("****ADDING CONSTRUCTOR DEF: "+defaultCons.getName());
        callables.add(defaultCons);
      }
    }
      
    /*
     * Initializes functions first
     */   
    List<Function> structFuncs = structure.getFunctions();
    for(Function function: structFuncs) {
      FunctionMember functionMember = new FunctionMember(function.getName().content(), 
                                                         function.argAmount(), 
                                                         function.getStatements());
      callables.add(functionMember);
    }
        
    /*
     * Construct the VariableMembers 
     */
    List<Variable> structVars = structure.getVariables();
    for(Variable variable: structVars) {
      VariableMember variableMember = new VariableMember(variable.getName().content(), 
                                                         variable.getExpression(),
                                                         variable.isConstant());
      fileVars.add(variableMember);
    }    
    
    RuntimeTable executor = new RuntimeTable();
    executor.initialize(null,null,fileVars, callables, sysFuncs);
    
    GenVisitor genVisitor = new GenVisitor(executor);
    
    /*
     * initialize file variables
     */
    for(VariableMember member: fileVars) {
      ASTNode expression = member.getExpr();
      
      //null expressions are for when the variable isn't initialized
      if (expression != null) {
        expression.accept(genVisitor);
      }   
      genVisitor.resetStack();
    }
      
    return executor;
  }
  
  public void addSysFunctions() {
    SystemFunction println = new SystemFunction("println", 1, ValType.VOID) {
      public Constant call(GenVisitor visitor, RuntimeTable executor, Constant... args) {
        System.out.println(args[0].getValue());
        return Constant.VOID;
      }
    };
    
    SystemFunction print = new SystemFunction("print", 1, ValType.VOID) {
      public Constant call(GenVisitor visitor, RuntimeTable executor, Constant... args) {
        System.out.print(args[0].getValue());
        return Constant.VOID;
      }
    };
    
    /*
     * Add DEBUG functions below
     */
    
    sysFuncs.addAll(Arrays.asList(print, println));
  }
}
