package wordy.logic.compile.structure;

import wordy.logic.compile.Token;

public class CatchBlock extends StatementBlock{
  
  private ExceptionName [] exceptionTypes;
  private Token variableName;
  
  public CatchBlock(Token variableName, ExceptionName [] errorTypes) {
    super(BlockType.CATCH);
    this.variableName = variableName;
    this.exceptionTypes = errorTypes;
  }

  public ExceptionName[] getExceptionTypes() {
    return exceptionTypes;
  }

  public Token getVariableName() {
    return variableName;
  }
  
  public static class ExceptionName{
    
    private Token [] nameArr;
    
    public ExceptionName(Token ... nameArr) {
      this.nameArr = nameArr;
    }   
    
    public String getName() {
      String x = "";
      for(Token token : nameArr) {
        x += token.content();
      }
      
      return x;
    }
    
    public Token [] getNameArray() {
      return nameArr;
    }
  }
}