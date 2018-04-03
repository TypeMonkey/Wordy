package wordy.logic.compile.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import wordy.logic.compile.Token;
import wordy.logic.compile.nodes.ASTNode;

public class Function {
  
  private Token name;
  private Statement [] statements;
  private int paramAmnt;
  private boolean isConstructor;
  
  public Function(Token name, Statement [] statements, int paramAmnt, boolean isConstructor) {
    this.name = name;
    this.statements = statements;
    this.paramAmnt = paramAmnt;
    this.isConstructor = isConstructor;
  }
  
  public int argAmount() {
    return paramAmnt;
  }
  
  public Statement [] getStatements() {
    return statements;
  }
  
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
  
  public Token getName() {
    return name;
  }
  
  public static class FunctionBuilder{
    
    private Token name;
    private List<Statement> statements;
    private int paramAmnt;
    private boolean isConstructor;
    
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
