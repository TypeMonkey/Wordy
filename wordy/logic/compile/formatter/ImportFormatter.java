package wordy.logic.compile.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.structure.ImportedFile;

public class ImportFormatter {  
  private List<Token> tokens;
  
  /**
   * Constructs an ImportFormatter
   * @param tokens - the Tokens representing this import statement. Should have the semicolon clipped
   */
  public ImportFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  public ImportedFile formImport() {
    ListIterator<Token> iterator = tokens.listIterator();
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.IMPORT));
    
    Token importKey = null;
    String imported = "";
    Token alias = null;
    
    boolean expectAliasName = false;
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      System.out.println("---Import CURR: "+current+" | "+expectAliasName);
      if (expected.contains(current.type())) {
        if (current.type() == Type.IMPORT) {
          importKey = current;
          
          expected.clear();
          expected.add(Type.IDENT);
        }
        else if(current.type() == Type.IDENT){
         if (expectAliasName) {
            alias = current;
            expected.clear();
            expected.add(Type.NO_EXPECT);
          }
          else{
            imported += current.content();
            expected.clear();
            expected.addAll(Arrays.asList(Type.AS, Type.DOT, Type.NO_EXPECT));
          }          
        }
        else if (current.type() == Type.DOT) {
          imported += current.content();
          expected.clear();
          expected.add(Type.IDENT);
        }
        else if (current.type() == Type.AS) {
          expectAliasName = true;
          expected.clear();
          expected.add(Type.IDENT);
        }
      }
      else {
        throw new ParseError("Misplaced token '"+current.content(), current.lineNumber());
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new ParseError("Missing tokens: "+expected, current.lineNumber());
    }
    ImportedFile file = new ImportedFile(importKey, imported);
    file.setAlias(alias);
    return file;
  }
  
}
