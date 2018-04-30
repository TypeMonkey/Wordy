package wordy.logic.compile.structure;

import java.util.ArrayList;
import java.util.List;

import wordy.logic.compile.Token;

/**
 * Represents a function, and houses the statements declared inside the function
 * it represents
 * 
 * Two functions are equal if they have the same name, parameter amount and are both
 * constructors - or are both non-constructors
 * 
 * @author Jose Guaro
 *
 */
public class Function {
  
  private Token name;
  private Statement [] statements;
  private int paramAmnt;
  private boolean isConstructor;
  
  /**
   * Constructs a Function
   * @param name - name of the Function (as a Token)
   * @param statements - the Statements in this Function
   * @param paramAmnt - the amount of parameters this Function takes
   * @param isConstructor - true if this Function is a class constructor
   *                        false if else
   */
  public Function(Token name, Statement [] statements, int paramAmnt, boolean isConstructor) {
    this.name = name;
    this.statements = statements;
    this.paramAmnt = paramAmnt;
    this.isConstructor = isConstructor;
  }
  
  /**
   * Returns the amount of arguments this function accepts
   * @return the amount of arguments this function accepts
   */
  public int argAmount() {
    return paramAmnt;
  }
  
  /**
   * Returns the Statements inside this Function
   * @return the Statements inside this Function
   */
  public Statement [] getStatements() {
    return statements;
  }
  
  /**
   * Checks if this Function is a class constructor
   * @return true if this Function is a constructor
   *         false if not
   */
  public boolean isConstructor() {
    return isConstructor;
  }
  
  public boolean equals(Object object) {
    if (object instanceof Function) {
      Function function = (Function) object;
      return function.getName().content().equals(this.name.content()) && 
             function.paramAmnt == this.paramAmnt &&
             function.isConstructor == this.isConstructor;
    }
    return false;
  }
  
  public String toString() {
    return name.content()+" , args: "+paramAmnt+" | "+isConstructor;
  }
  
  /**
   * Returns the name of this Function (as a Token)
   * @return the name of this Function (as a Token)
   */
  public Token getName() {
    return name;
  }
  
  /**
   * A builder for Functions
   * @author Jose Guaro
   *
   */
  public static class FunctionBuilder{
    
    private Token name;
    private List<Statement> statements;
    private int paramAmnt;
    private boolean isConstructor;
    
    /**
     * Constructs a FunctionBuilder
     * @param name - the name for the Function being built (in Token form)
     */
    public FunctionBuilder(Token name) {
      this.name = name;
      statements = new ArrayList<>();
    }
    
    public FunctionBuilder() {
      this(null);
    }
    
    public FunctionBuilder setName(Token name) {
      this.name = name;
      return this;
    }
    
    public FunctionBuilder setAsConstructor(boolean constructor) {
      this.isConstructor = constructor;
      return this;
    }
    
    public FunctionBuilder addStatement(Statement statement, boolean isParam) {
      if (isParam) {
        paramAmnt++;
      }
      statements.add(statement);
      return this;
    }
    
    public Token getName() {
      return name;
    }

    public List<Statement> getStatements() {
      return statements;
    }
    
    public boolean isConstructor() {
      return isConstructor;
    }

    public Function build() {
      Statement [] states = statements.toArray(new Statement[statements.size()]);
      
      return new Function(name, states, paramAmnt, isConstructor);
    }
  }
}
