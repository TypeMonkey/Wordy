package wordy.logic.compile.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.nodes.BinaryOpNode;
import wordy.logic.compile.nodes.UnaryNode;
import wordy.logic.compile.nodes.ASTNode.NodeType;
import wordy.logic.compile.parser.Parser;
import wordy.logic.compile.structure.ForLoopBlock;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.StatementBlock;

public class ForLoopFormatter {
  
  private List<Token> tokens;
  private Token forBlockSig;
  
  public ForLoopFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  public ForLoopBlock formForLoop() {
    ListIterator<Token> iterator = tokens.listIterator();
    
    //get the initialization statement
    Statement init = extractInit(iterator);
    
    //at this point, the next call to next() should be the beginning
    //of the comparison statement
    List<Token> comp = Formatter.gatherStatement(iterator, tokens.get(iterator.previousIndex()).lineNumber());
    comp.remove(comp.size()-1);
    Statement comparison = null;
    if (!comp.isEmpty()) {
      comparison = new StatementFormatter(comp).formatStatements();
      ASTNode compExpr = comparison.getExpression();
      if (compExpr.nodeType() == NodeType.UNARY) {
        UnaryNode unaryNode = (UnaryNode) compExpr;
        if (unaryNode.unaryType() != Type.BANG) {
          throw new ParseError("For loop comparison expression doesn't evaluate to a boolean", forBlockSig.lineNumber());
        }
      }
      else if (compExpr.nodeType() == NodeType.OPERATOR) {
        BinaryOpNode opNode = (BinaryOpNode) compExpr;
        if (ReservedSymbols.isABooleanOperator(opNode.getOperator()) == false) {
          throw new ParseError("For loop comparison expression doesn't evaluate to a boolean", forBlockSig.lineNumber());
        }
      }
    } 
    
    //We can use gatherEnclosingParanthesis() as it technically doesn't care about the LEFT_PAREN
    //of the initial statement, only nested parenthesis groupings.
    //All we have to do is remove the very last token as it contains the RIGHT_PAREN of our loop
    List<Token> change = Formatter.gatherEnclosingParanthesis(iterator, tokens.get(iterator.previousIndex()).lineNumber());
    change.remove(change.size()-1);
    Statement changeState = null;
    if (!change.isEmpty()) {
      changeState = new StatementFormatter(change).formatStatements();
    }
    
    List<Token> body = new ArrayList<>();
    if (iterator.hasNext()) {
      Token next = iterator.next();
      body.add(next);
      if (next.type() != Type.OPEN_SCOPE) {
        throw new RuntimeException("Misplaced token at line "+next.lineNumber());
      }
    }
    else {
      throw new RuntimeException("Malformed for loop at line "+tokens.get(iterator.previousIndex()));
    }
   
    
    //next call to next() should return an opening curly brace
    //So, we can just call buildBlock() in Formatter
    body.addAll(Formatter.gatherBlock(iterator, tokens.get(iterator.previousIndex()).lineNumber()));
    //since we initially called buildBlock with '{' being the firts thing the iterator
    //gave at next(), and since we know that buildBlock returns the list with ending brace
    //We can remove the first and last tokens and just get the bare tokens of the body
    
    System.out.println("---FOR LOOP: ");
    for(Token token: body) {
      System.out.println("token: "+token);
    }
    System.out.println("---FOR LOOP END");
    
    if (body.size() > 2) {
      BlockFormatter blockFormatter = new BlockFormatter(body);
      StatementBlock loopBlock = blockFormatter.formBlock();
      
      ForLoopBlock forLoopBlock = new ForLoopBlock(init, comparison, changeState);
      forLoopBlock.setBlockSig(forBlockSig);
      forLoopBlock.addStatements(loopBlock.getStatements());
      
      return forLoopBlock;
    }
    else {
      //block is empty
      return new ForLoopBlock(init, comparison, changeState);
    }
  }
  
  /**
   * Forms the initialization statement of a for-loop
   * @param iterator - ListIterator to use to traverse the list of tokens
   * @return the initialization Statement, or null if there's none
   */
  private Statement extractInit(ListIterator<Token> iterator) {
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.BLOCK_SIG));
    Statement init = null;
    
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      if (expected.contains(current.type())) {
        if (current.type() == Type.BLOCK_SIG) {
          forBlockSig = current;
          expected.clear();
          expected.add(Type.LEFT_PAREN);
        }
        else if (current.type() == Type.LEFT_PAREN) {
          //gather initialization statement tokens
          List<Token> initState = Formatter.gatherStatement(iterator, current.lineNumber());
          initState.remove(initState.size()-1);
          
          //Only parse if not an empty statement
          if (!initState.isEmpty()) {
            StatementFormatter statement = new StatementFormatter(initState);
            init = statement.formatStatements();
          }
          
          expected.clear();
          expected.add(Type.NO_EXPECT);
          break;
        }
      }
      else {
        throw new RuntimeException("Misplaced token '"+current.content()+"' at line "+current.lineNumber());
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new RuntimeException("Malformed statement at "+current.lineNumber());
    }
    
    return init;
  }
}
