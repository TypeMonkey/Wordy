package wordy.logic.compile.parser;

import java.util.ArrayList;
import java.util.List;

import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.nodes.BinaryOpNode;
import wordy.logic.compile.nodes.ConstantNode;
import wordy.logic.compile.nodes.IdentifierNode;
import wordy.logic.compile.nodes.LiteralNode;
import wordy.logic.compile.nodes.MemberAccessNode;
import wordy.logic.compile.nodes.MethodCallNode;
import wordy.logic.compile.nodes.UnaryNode;
import wordy.logic.compile.nodes.ASTNode.NodeType;
import wordy.logic.compile.parser.FuncArgumentParser.ParseReturn;

public class FuncArgumentParser {
  
  private List<Token> tokens;
  private int index;
    
  public FuncArgumentParser(List<Token> tokens, int index) {
    this.tokens = tokens;
    this.index = index;
  }
  
  public ParseReturn parse() {
    ASTNode result = expression();
    
    ParseReturn parseReturn = new ParseReturn();
    parseReturn.arg = result;
    parseReturn.index = index;
    return parseReturn;
  }
  
  private ASTNode expression() {
    System.out.println("FUNCTION PARSE");
    return equality();
  }
  
  private ASTNode equality() {
    ASTNode node = comparison();
        
    while (match(Type.BANG_EQUAL, Type.EQUAL_EQUAL)) {
      Token operator = previous();
      ASTNode right = comparison();
      node = new BinaryOpNode(operator, node, right);
    }
    
    return node;
  }
  
  private ASTNode comparison() {
    ASTNode node = addition();
    
    while (match(Type.GREATER_THAN, Type.GREATERE, 
                 Type.LESS_THAN, Type.LESS_THAN)) {
      Token operator = previous();
      ASTNode right = addition();
      node = new BinaryOpNode(operator, node, right);
    }
    
    return node;
  }
  
  private ASTNode addition() {
    ASTNode node = multiplication();
    
    while (match(Type.MINUS, Type.PLUS)) {
      Token operator = previous();
      ASTNode right = multiplication();
      node = new BinaryOpNode(operator, node, right);
    }
    

    return node;
  }
  
  private ASTNode multiplication() {
    ASTNode node = unary();
        
    while (match(Type.DIVI, Type.MULT, Type.MOD)) {
      Token operator = previous();
      ASTNode right = unary();
      node = new BinaryOpNode(operator, node, right);
    }

    return node;
  }
  
  private ASTNode unary() {
    if (match(Type.BANG, Type.MINUS)) {
      Token operator = previous();
      ASTNode node = unary();
      return new UnaryNode(operator, node);
    }
    
    return call();
  }
  
  private ASTNode call() {
    ASTNode node = primary();
    
    while (true) {
      if (match(Type.LEFT_PAREN)) {
        if (node.nodeType() == NodeType.MEM_ACCESS) {
          MemberAccessNode old = (MemberAccessNode) node;
          System.out.println("**** SEETING FOR FUNC: "+old.getMemberName());
          old.setForFunction(true);
        }
        else {
          System.out.println("***** NOT MEMACCESSS "+node.nodeType()+" "+node.tokens()[0]);
        }
        node = finishCall(node);                
      }
      else if (match(Type.DOT)) {
        System.out.println("DOT-F!!!");
        Token name = consume(Type.IDENT, "Expected identifier after '.'");
        node = new MemberAccessNode(node, name);
      }
      else {
        break;
      }
    }
    
    return node;
  }
  
  private MethodCallNode finishCall(ASTNode node) {
    List<ASTNode> args = new ArrayList<>();
    if (!check(Type.RIGHT_PAREN)) {
      do {
        FuncArgumentParser argParser = new FuncArgumentParser(tokens, index);
        ParseReturn parseReturn = argParser.parse();
        
        index = parseReturn.index;
        
        args.add(parseReturn.arg);
      } while(match(Type.COMMA));
    }
    
    Token paren = consume(Type.RIGHT_PAREN, " ')' expected after expression");
    
    Token funcName = null;
    if (node.nodeType() == NodeType.IDENTIFIER) {
      IdentifierNode identifierNode = (IdentifierNode) node;
      funcName = identifierNode.getTokenName();
    }
    else if (node.nodeType() == NodeType.MEM_ACCESS) {
      MemberAccessNode memberAccessNode = (MemberAccessNode) node;
      funcName = memberAccessNode.getMemberName();
    }
    
    return new MethodCallNode(node, paren, funcName, args.toArray(new ASTNode[args.size()]));
  }
  
  private ASTNode primary() {
    if (match(Type.BOOL, Type.NUMBER, Type.NULL)) {
      System.out.println("PRIMARY-F: "+previous().content());
      return new ConstantNode(previous());
    }
    else if (match(Type.LITERAL)) {
      System.out.println("PRIMARY-F: "+previous().content());
      return new LiteralNode(previous());
    }
    else if (match(Type.IDENT)) {
      System.out.println("PRIMARY-F: "+previous().content()); 
      return new IdentifierNode(previous()); 
    }
    
    
    if (match(Type.LEFT_PAREN)) {
      System.out.println("NESTED-F!!!");
      ASTNode expr = expression();
      consume(Type.RIGHT_PAREN, " ')' expected after expression");
      return expr;
    }
    
    throw new RuntimeException("Unknown token '"+previous()+"'");
  }
  
  private Token consume(Type type, String message) {
    if (check(type)) {
      return advance();
    }
    
    throw new RuntimeException("Error at: '"+previous().content()+"' : "+message);
  }
  
  private boolean match(Token.Type ... types) {
    for(Token.Type type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }
  
  private boolean check(Token.Type type) {
    if (isAtEnd()) {
      return false;
    }
    return peek().type() == type;
  }
  
  private Token advance() {
    if (isAtEnd() == false) {
      index++;
    }
    return previous();
  }
  
  private boolean isAtEnd() {
    return index == tokens.size();
  }
  
  private Token peek() {
    return tokens.get(index);
  }
  
  private Token previous() {
    return tokens.get(index - 1);
  }
  
  static class ParseReturn{  
    public int index;
    public ASTNode arg;
  }
}
