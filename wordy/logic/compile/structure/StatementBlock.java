package wordy.logic.compile.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import wordy.logic.compile.Token;
import wordy.logic.compile.nodes.ASTNode;

/**
 * A group of statements that belong in a single block/scope.
 * 
 * Visual example:
 * A StatementBlock is practically any group of statements between
 * opening and closing parenthesis
 * 
 * {
 *    Statement;
 *    Statement;
 *    .....
 * }
 * 
 * So, this includes loops (for and while) and conditional blocks.
 * 
 * @author Jose Guaro
 *
 */
public class StatementBlock extends Statement{

  public enum BlockType{
    FOR,
    WHILE,
    IF,
    GENERAL; //For blocks that are just in a separate scope
  }
  
  protected List<Statement> statements;
  protected final BlockType type;
  protected Token blockSig;
  
  public StatementBlock(BlockType type) {
    statements = new ArrayList<>();
    this.type = type;
    isABlock = true;
    isVarDec = false;
  }

  public void addStatement(Statement statement) {
    statements.add(statement);
  }
  
  public void addStatements(Collection<Statement> states) {
    statements.addAll(states);
  }
  
  public void setBlockSig(Token token) {
    this.blockSig = token;
  }
  
  public BlockType blockType() {
    return type;
  }
  
  public List<Statement> getStatements() {
    return statements;
  } 
  
  public Token getBlockSig() {
    return blockSig;
  }
  
  public String toString() {
    String string = "";
    for(Statement statement: statements) {
      string += statement+System.lineSeparator();
    }
    return string;
  }
}
