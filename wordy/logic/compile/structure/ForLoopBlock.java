package wordy.logic.compile.structure;

import java.util.ArrayList;
import java.util.List;

import wordy.logic.compile.nodes.ASTNode;

public class ForLoopBlock extends StatementBlock{
  
  private Statement initialization;
  private Statement checkStatement;
  private Statement changeStatement;

  public ForLoopBlock(Statement init, Statement check, Statement change) {
    super(BlockType.FOR);
    this.initialization = init;
    this.checkStatement = check;
    this.changeStatement = change;
  }
  
  public ForLoopBlock(Statement init) {
    this(init, null, null);
  }
  
  public void setInitialization(Statement checkStatement) {
    this.checkStatement = checkStatement;
  }
  
  public void setCheckStatement(Statement checkStatement) {
    this.checkStatement = checkStatement;
  }

  public void setChangeStatement(Statement changeStatement) {
    this.changeStatement = changeStatement;
  }

  public Statement getInitialization() {
    return initialization;
  }
  
  public Statement getCheckStatement() {
    return checkStatement;
  }

  public Statement getChangeStatement() {
    return changeStatement;
  }

  public Object execute() {
    return null;
  }
  
  public String toString() {
    String x = "INIT: "+initialization+" | COMP: "+checkStatement+" | CHANGE: "+changeStatement;
    x += super.toString();
    return x;
  }
}
