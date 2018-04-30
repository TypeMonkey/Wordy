package wordy.logic.compile.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.structure.CatchBlock;
import wordy.logic.compile.structure.IfBlock;
import wordy.logic.compile.structure.StatementBlock;
import wordy.logic.compile.structure.TryBlock;
import wordy.logic.compile.structure.StatementBlock.BlockType;

public class BlockFormatter {
  
  private List<Token> tokens;
  
  public BlockFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  /**
   * 
   * @param addTo
   * @return
   */
  public StatementBlock formBlock() {
    Token blockSignifier = tokens.get(0);  
    //first parse header.
    switch (blockSignifier.content()) {
    case ReservedSymbols.IF:
      return new IfBlockFormatter(tokens).formIfBlock();
    case ReservedSymbols.WHILE:
      return new WhileLoopFormatter(tokens).formatWhileLoop();
    case ReservedSymbols.FOR:
      return new ForLoopFormatter(tokens).formForLoop();
    case ReservedSymbols.ELSE:
      /*
       * Assuming syntax correctness, if we remove the first element
       * of an else statement, we have either "if block", or
       * an just a general block.
       * 
       *  If we get an if block, then we can just add that block to addTo's
       *  else statements
       *  
       *  If we get just a general block, we can also just add that block to
       *  addTo's else statements
       */
      tokens.remove(0);
      if (tokens.get(0).content().equals(ReservedSymbols.IF)) {
        IfBlock ifBlock = new IfBlockFormatter(tokens).formIfBlock();
        ifBlock.setAsElseIf(true);
        ifBlock.setBlockSig(blockSignifier);
        return ifBlock;
      }
      else {
        IfBlock ifBlock = new IfBlock(null, true);
        ifBlock.addStatement(formatGeneralBlock());
        ifBlock.setBlockSig(blockSignifier);
        return ifBlock;
      }
    case ReservedSymbols.TRY:
      TryBlock block = new TryBlock(blockSignifier);
      tokens.remove(0);
      block.addStatement(formatGeneralBlock());
      return block;
    case ReservedSymbols.CATCH:
      CatchBlock catchBlock = new CatchBlockFormatter(tokens).formCatchBlock();
      return catchBlock;
    default:
      System.out.println("---DEFAULT");
      return formatGeneralBlock();
    }
    
  }
 
  private StatementBlock formatGeneralBlock() {
    //Remove the bounding braces
    tokens.remove(0);
    tokens.remove(tokens.size()-1);
    
    ListIterator<Token> iterator = tokens.listIterator();
        
    StatementBlock block = new StatementBlock(BlockType.GENERAL);
    ArrayList<Token> tempStatement = new ArrayList<>();
    
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      System.out.println("---BL: "+current);
      tempStatement.add(current);
      if (current.type() == Type.STATE_END) {
        tempStatement.remove(tempStatement.size()-1);
        
        System.out.println("GENERAL: "+tempStatement);
        
        StatementFormatter statementFormatter = new StatementFormatter(tempStatement);
        block.addStatement(statementFormatter.formatStatements());
        System.out.println("---POST BLOCK: "+block.getStatements().size());
        tempStatement = new ArrayList<>();
      }
      else if (current.type() == Type.BLOCK_SIG ) {
        boolean headerEndReached = false;
        while (iterator.hasNext()) {
          Token header = iterator.next();
          tempStatement.add(header);
          if (header.type() == Type.OPEN_SCOPE) {
            headerEndReached = true;
            break;
          }
        }

        if (headerEndReached == false) {
          throw new RuntimeException("Missing '{' for scope declaration at line "+current.lineNumber());
        }

        tempStatement.addAll(Formatter.gatherBlock(iterator, current.lineNumber()));

        if (!tempStatement.isEmpty()) {
          BlockFormatter formatter = new BlockFormatter(tempStatement);
          block.addStatement(formatter.formBlock());
        }

        tempStatement = new ArrayList<>();
      }
      else if (current.type() == Type.OPEN_SCOPE) {
        //these are for general scopes/blocks
        tempStatement.addAll(Formatter.gatherBlock(iterator, current.lineNumber()));
        
        for(Token g: tempStatement) {
          System.out.println("-GENERAL: "+g);
        }
        
        BlockFormatter formatter = new BlockFormatter(tempStatement);
        block.addStatement(formatter.formBlock());
        
        tempStatement = new ArrayList<>();
      }
    }
    
    //the temp statement isn't empty which means a statement delimiter 
    // - be it a semicolon, or a opening curly brace - hasn't been encountered.
    //This means we have dangling statements
    if (!tempStatement.isEmpty()) {
      throw new ParseError("Dangling statemen or block declaration", current.lineNumber());
    }
    return block;
  }
}
