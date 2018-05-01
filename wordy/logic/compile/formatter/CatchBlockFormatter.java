package wordy.logic.compile.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.structure.CatchBlock;
import wordy.logic.compile.structure.CatchBlock.ExceptionName;
import wordy.logic.compile.structure.StatementBlock;

public class CatchBlockFormatter {
  
  private List<Token> tokens;
  
  public CatchBlockFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  public CatchBlock formCatchBlock() {    
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.BLOCK_SIG));
    ListIterator<Token> iterator = tokens.listIterator();
    
    Token current = null;
    Token blockSig = null;
    
    Object [] closureResult = null;
    StatementBlock stateBlock = null;
    
    while (iterator.hasNext()) {
      current = iterator.next();
      if (expected.contains(current.type())) {
        if (current.type() == Type.BLOCK_SIG) {
          blockSig = current;
          expected.clear();
          expected.addAll(Arrays.asList(Type.LEFT_PAREN));
        }
        else if (current.type() == Type.LEFT_PAREN){
          //gather all the stuff enclosing the parenthesis
          List<Token> enclosed = Formatter.gatherEnclosingParanthesis(iterator, current.lineNumber());
          System.out.println("----ENCLOSED: "+enclosed);
          enclosed.remove(enclosed.size()-1);  //remove last parenthesis
          if (enclosed.isEmpty()) {
            //empty catch block conditional
            throw new ParseError("A catch block should be specified with exceptions to catch ", current.lineNumber());
          }
          
          closureResult = formNames(enclosed);
          
          expected.clear();
          expected.add(Type.OPEN_SCOPE);
        }
        else if (current.type() == Type.OPEN_SCOPE) {
          List<Token> scopeTokens = new ArrayList<>();
          scopeTokens.add(current);
          scopeTokens.addAll(Formatter.gatherBlock(iterator, current.lineNumber()));          
          System.out.println("---CATCH BLOCK BODY: "+scopeTokens);
          
          if (!scopeTokens.isEmpty()) {
            BlockFormatter formatter = new BlockFormatter(scopeTokens);
            stateBlock = formatter.formBlock();
          }
          
          expected.clear();
          expected.add(Type.NO_EXPECT);
        }
      }
      else {
        throw new ParseError("Misplaced token '"+current.content()+"' ", current.lineNumber());
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new ParseError("Missing tokens: "+expected, current.lineNumber());
    }
    
    CatchBlock catchBlock = new CatchBlock((Token) closureResult[1], (ExceptionName[])closureResult[0]);
    catchBlock.setBlockSig(blockSig);
    catchBlock.addStatements(stateBlock.getStatements());
    return catchBlock;
  }
  
  private Object[] formNames(List<Token> catchNames) {
    ArrayList<ExceptionName> names = new ArrayList<>();
    
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.IDENT));
    ListIterator<Token> iterator = catchNames.listIterator();
    
    ArrayList<Token> currentName = new ArrayList<>();
    
    Token varName = null;
    
    Token current = null;
    
    boolean nextIdenMayBeVar = false;
    
    while (iterator.hasNext()) {
      current = iterator.next();
      System.out.println("---CATCH CUR: "+current);
      if (expected.contains(current.type())) {
        if (current.type() == Type.IDENT) {
          if (nextIdenMayBeVar) {
            varName = current;
            expected.clear();
            expected.add(Type.NO_EXPECT);
          }
          else {
            currentName.add(current);
            nextIdenMayBeVar = true;
            expected.clear();
            expected.addAll(Arrays.asList(Type.DOT, Type.COMMA, Type.IDENT));
          }
        }
        else if (current.type() == Type.DOT){
          nextIdenMayBeVar = false;
          currentName.add(current);
          expected.clear();
          expected.addAll(Arrays.asList(Type.IDENT));
        }
        else if (current.type() == Type.COMMA) {
          nextIdenMayBeVar = false;
          names.add(new ExceptionName(currentName.toArray(new Token[currentName.size()])));
          currentName.clear();
          expected.clear();
          expected.addAll(Arrays.asList(Type.IDENT));
        }
      }
      else {
        throw new ParseError("Misplaced token '"+current.content()+"' ", current.lineNumber());
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new ParseError("Missing tokens: "+expected, current.lineNumber());
    }
    
    Object [] ret = {names.toArray(new ExceptionName[names.size()]), varName};
    return ret;
  }
  
}
