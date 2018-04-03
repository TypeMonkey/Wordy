package wordy.logic.compile.parser;

import java.util.ArrayList;
import java.util.List;


import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
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

public class Parser {
  
  private List<Token> tokens;
  private int index;
  
  private boolean binaryOpNeeded;
  
  public Parser(List<Token> tokens) {
    this.tokens = tokens;
    index = 0;
    binaryOpNeeded = true;
  }
  
  public ASTNode parse() {
    System.out.println("----TO PARSE: "+tokens);
    ASTNode result = expression();

    if (index < tokens.size()) {
      System.out.println("INDEX: "+index+" | size: "+tokens.size()+" | what? "+tokens.get(index));
      throw new ParseError("Extra token: "+tokens.get(index).content(), tokens.get(index).lineNumber());
    }

    return result;
  }
  
  private ASTNode expression() {
    return assignment();
  }
  
  private ASTNode assignment() {
    ASTNode node = or();
    
    System.out.println("-----TOKEN? "+node.nodeType()+node.tokens()[0]);
    
    if (match(Type.EQUALS)) {
      System.out.println("-----TOKEN-M? "+node.nodeType()+node.tokens()[0]);
      Token equals = previous();
      ASTNode value = assignment();
      
      if (node.nodeType() == NodeType.IDENTIFIER) {
        IdentifierNode identifierNode = (IdentifierNode) node;
        return new BinaryOpNode(equals, identifierNode, value);
      }
      else if (node.nodeType() == NodeType.MEM_ACCESS) {
        MemberAccessNode accessNode = (MemberAccessNode) node;
        return new BinaryOpNode(equals, accessNode, value);
      }
      
      throw new ParseError("Unknown assignment target for '=' ", equals.lineNumber());
    }
    
    return node;
  }
  
  private ASTNode or() {
    ASTNode node = and();
    
    while (match(Type.BOOL_OR)) {
      Token operator = previous();
      ASTNode right = and();
      node = new BinaryOpNode(operator, node, right);
    }
    
    return node;
  }
  
  private ASTNode and() {
    ASTNode node = equality();
    
    while (match(Type.BOOL_AND)) {
      Token operator = previous();
      ASTNode right = equality();
      node = new BinaryOpNode(operator, node, right);
    }
    
    return node;
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
                 Type.LESS_THAN, Type.LESSE)) {
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
      binaryOpNeeded = false;      
    }
    
    if (binaryOpNeeded) {
      Token pre = previous();
      throw new ParseError("Invalid token '"+pre.content()+"' ", pre.lineNumber());
    }
    else {
      binaryOpNeeded = true;
    }

    return node;
  }
  
  private ASTNode multiplication() {
    ASTNode node = unary();
        
    while (match(Type.DIVI, Type.MULT, Type.MOD)) {
      Token operator = previous();
      ASTNode right = unary();
      node = new BinaryOpNode(operator, node, right);
      binaryOpNeeded = false;
    }
    
    return node;
  }
  
  private ASTNode unary() {
    if (match(Type.BANG, Type.MINUS)) {
      Token operator = previous();
      ASTNode node = unary();
      return new UnaryNode(operator, node);
    }
    
    binaryOpNeeded = true;
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
        System.out.println("---FINISHED CALL: "+((MethodCallNode) node).getName());
      }
      else if (match(Type.DOT)) {
        Token name = consume(Type.IDENT, "Expected identifier after '.'");
        System.out.println("---FINISHED DOT: "+name.content());
        node = new MemberAccessNode(node, name);
      }
      else {
        break;
      }
    }
    
    binaryOpNeeded = false;
    return node;
  }
  
  private MethodCallNode finishCall(ASTNode node) {
    List<ASTNode> args = new ArrayList<>();
    if (!check(Type.RIGHT_PAREN)) {
      do {
        binaryOpNeeded = false;
        
        FuncArgumentParser argParser = new FuncArgumentParser(tokens, index);
        ParseReturn parseReturn = argParser.parse();
        
        index = parseReturn.index;
        args.add(parseReturn.arg);
        binaryOpNeeded = false;
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
      return new ConstantNode(previous());
    }
    else if (match(Type.LITERAL)) {
      return new LiteralNode(previous());
    }
    else if (match(Type.IDENT)) {
      return new IdentifierNode(previous()); 
    }
    
    
    if (match(Type.LEFT_PAREN)) {
      binaryOpNeeded = false;
      ASTNode expr = expression();
      consume(Type.RIGHT_PAREN, " ')' expected after expression");
      binaryOpNeeded = false;
      return expr;
    }
    
    Token pre = previous();
    throw new ParseError("Unknown token '"+pre.content()+"' ", pre.lineNumber());
  }
  
  private Token consume(Type type, String message) {
    if (check(type)) {
      return advance();
    }
    
    Token pre = previous();
    throw new ParseError("Error at: '"+pre.content()+"' : "+message, pre.lineNumber());
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
}
