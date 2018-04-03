package wordy.logic.compile.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.parser.Parser;
import wordy.logic.compile.structure.IfBlock;
import wordy.logic.compile.structure.Statement;

public class IfBlockFormatter {
  
  private List<Token> tokens;
  
  public IfBlockFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  public IfBlock formIfBlock() {
    ListIterator<Token> iterator = tokens.listIterator();
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.BLOCK_SIG));
    
    System.out.println("-----IF STATE------");
    for(Token token: tokens) {
      System.out.println("----> "+token);
    }
    System.out.println("-----IF STATE-END------");
    
    Statement actCond = null;
    List<Statement> statements = new ArrayList<>();
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      System.out.println("--IF: "+current+" | ");
      if (expected.contains(current.type())) {
        if (current.type() == Type.BLOCK_SIG) {
          expected.clear();
          expected.add(Type.LEFT_PAREN);
        }
        else if (current.type() == Type.LEFT_PAREN) {
          List<Token> conditional = Formatter.gatherEnclosingParanthesis(iterator, current.lineNumber());
          conditional.remove(conditional.size() - 1);
          
          if (conditional.isEmpty()) {
            throw new RuntimeException("Empty conditional at line "+current.lineNumber());
          }
                    
          StatementFormatter condFormatter = new StatementFormatter(conditional);
          actCond = condFormatter.formatStatements();
          
          expected.clear();
          expected.add(Type.OPEN_SCOPE);
        }
        else if (current.type() == Type.OPEN_SCOPE) {
          List<Token> body = new ArrayList<>();
          body.addAll(Formatter.gatherBlock(iterator, current.lineNumber()));
          body.remove(body.size()-1);
          
          //form those statements
          if (!body.isEmpty()) {
            statements.addAll(formBody(body));
          }
          
          expected.clear();
          expected.add(Type.NO_EXPECT);
        }
      }
      else {
        throw new RuntimeException("Misplaced token '"+current.content()+"' in line "+current.lineNumber()+" | "+expected);
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new RuntimeException("Missing tokens: "+expected+" at line "+current.lineNumber());
    }
    
    IfBlock ifBlock = new IfBlock(actCond, false);
    ifBlock.addStatements(statements);
    return ifBlock;    
  }
  
  private List<Statement> formBody(List<Token> tokens){
    ListIterator<Token> iterator = tokens.listIterator();
    ArrayList<Statement> statements = new ArrayList<>();
    
    ArrayList<Token> tempStatement = new ArrayList<>();
    while (iterator.hasNext()) {
      Token current = iterator.next();
      tempStatement.add(current);
      if (current.type() == Type.STATE_END) {
        tempStatement.remove(tempStatement.size()-1);
        
        StatementFormatter stateForm = new StatementFormatter(tempStatement);
        statements.add(stateForm.formatStatements());
        
        tempStatement = new ArrayList<>();
      }
      else if (current.type() == Type.BLOCK_SIG) {
        boolean headerEndReached = false;
        while (iterator.hasNext()) {
          Token header = iterator.next();
          tempStatement.add(header);
          if (header.type() == Type.OPEN_SCOPE) {
            headerEndReached = true;
            break;
          }
        }
        
        tempStatement.addAll(Formatter.gatherBlock(iterator, current.lineNumber()));

        if (headerEndReached == false) {
          throw new RuntimeException("Missing '{' for scope declaration at line "+current.lineNumber());
        }

        for(Token g: tempStatement) {
          System.out.println("GIVING: "+g);
        }

        BlockFormatter blockForm = new BlockFormatter(tempStatement);
        statements.add(blockForm.formBlock());

        tempStatement = new ArrayList<>();
      }
      else if (current.type() == Type.OPEN_SCOPE) {
        //these are for general scopes/blocks
        tempStatement.addAll(Formatter.gatherBlock(iterator, current.lineNumber()));
        
        for(Token g: tempStatement) {
          System.out.println("-GENERAL: "+g);
        }
        
        BlockFormatter formatter = new BlockFormatter(tempStatement);
        statements.add(formatter.formBlock());
        
        tempStatement = new ArrayList<>();
      }
    }
    
    return statements;
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}
