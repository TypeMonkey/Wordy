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
import wordy.logic.compile.structure.ForLoopBlock;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.StatementBlock;
import wordy.logic.compile.structure.WhileLoopBlock;

public class WhileLoopFormatter {

  private List<Token> tokens;
  private Token blockSig;

  public WhileLoopFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  public WhileLoopBlock formatWhileLoop() {
    ListIterator<Token> iterator = tokens.listIterator();
    Statement conditional = formConditional(iterator);
    ASTNode compExpr = conditional.getExpression();
    if (compExpr.nodeType() == NodeType.UNARY) {
      UnaryNode unaryNode = (UnaryNode) compExpr;
      if (unaryNode.unaryType() != Type.BANG) {
        throw new ParseError("While loop conditional expression doesn't evaluate to a boolean", blockSig.lineNumber());
      }
    }
    else if (compExpr.nodeType() == NodeType.OPERATOR) {
      BinaryOpNode opNode = (BinaryOpNode) compExpr;
      if (!ReservedSymbols.isABooleanOperator(opNode.getOperator()) && 
          !ReservedSymbols.isAComparisonOp(opNode.getOperator())) {
        System.out.println(opNode.getOperator());
        throw new ParseError("While loop conditional expression doesn't evaluate to a boolean", blockSig.lineNumber());
      }
    }
    
    //next call to next() should give '{'
    //with that, we then just call buildBlock()
    List<Token> loopBody = new ArrayList<>();
    loopBody.add(iterator.next());
    loopBody.addAll(Formatter.gatherBlock(iterator, tokens.get(iterator.previousIndex()).lineNumber()));
    
    if (!loopBody.isEmpty()) {
      BlockFormatter blockFormatter = new BlockFormatter(loopBody);
      StatementBlock formattedBody = blockFormatter.formBlock();
      
      WhileLoopBlock whileLoop = new WhileLoopBlock(conditional, false);
      whileLoop.setBlockSig(blockSig);
      whileLoop.addStatement(formattedBody);
      return whileLoop;
    }
    else {
      //block is empty
      WhileLoopBlock whileLoop = new WhileLoopBlock(conditional, false);
      whileLoop.setBlockSig(blockSig);
      return whileLoop;
    }   
  }
  
  private Statement formConditional(ListIterator<Token> iterator) {
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.BLOCK_SIG));
    
    Token current = null;
    ArrayList<Token> conditional = new ArrayList<>();
    while (iterator.hasNext()) {
      current = iterator.next();
      System.out.println("WHILE_LOOP: "+current);
      if (expected.contains(current.type())) {
        if (current.type() == Type.BLOCK_SIG) {
          blockSig = current;
          expected.clear();
          expected.add(Type.LEFT_PAREN);
        }
        else if (current.type() == Type.LEFT_PAREN) {
          conditional.addAll(Formatter.gatherEnclosingParanthesis(iterator, current.lineNumber()));
          conditional.remove(conditional.size()-1);
          System.out.println("--WHILE-LOOP CONDITION: "+conditional);
          
          //can't have empty conditionals
          if(conditional.isEmpty()) {
            throw new RuntimeException("Empty conditional for while-loop at line "+current.lineNumber());
          }
          
          expected.clear();
          expected.add(Type.NO_EXPECT);
          break;
        }
      }
      else {
        throw new RuntimeException("Misplaced token '"+current.content()+"' in line "+current.lineNumber()+" | "+expected);
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new RuntimeException("Malformed while loop at line "+current.lineNumber());
    }
    
    System.out.println("---POST COND: "+conditional);
    StatementFormatter formatter = new StatementFormatter(conditional);
    return formatter.formatStatements();
  }
}
