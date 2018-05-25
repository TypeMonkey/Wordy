package wordy.logic.compile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;

/**
 * Groups and transforms the characters in a source file 
 * into Tokens.
 * @author Jose Guaro
 *
 */
public class Tokenizer {
  
  /**
   * Tokenizes the content of a source file
   * @param sourceFile - the file path of the source file
   * @return an array of Tokens representing the source file's contents
   * @throws IOException - if an I/O error occurs
   */
  public static Token[] tokenize(String sourceFile) throws IOException {
    Token [] tokens = rawTokenize(sourceFile);    
    return tokens;
  }
  
  private static Token[] rawTokenize(String sourceFile) throws IOException{
    ArrayList<Token> tokens = new ArrayList<>();

    /*
     * Why a PushbackInputStream?
     * 
     * In the loop below, when we encounter a letter or digit or literal or operator,
     * We keep reading for that same type until we reach a character that is different
     * 
     * However, the we've read one extra character and so we need to push that back
     * to the next read operation
     */
    PushbackInputStream inputStream = new PushbackInputStream(new FileInputStream(sourceFile), 1);
    
    int lineNumber = 1;
    
    int read = 0;
    
    boolean dotRecieved = false;
    boolean doubleResolved = false;
        
    boolean currentComment = false;
    boolean commentIsBlock = true;
    
    String commentedString = "";
    
    while ((read = inputStream.read()) != -1) {
      /*
       * What we just read from the file.
       * It's stored as a String to make comparisons easier
       */
      String readByte = String.valueOf( (char) read);
      
      if (readByte.equals(ReservedSymbols.NEW_LN) || readByte.equals("\n")) {
        if (currentComment && !commentIsBlock) {
          currentComment = false;
        }
        commentedString = "";
        lineNumber++;
      }
      
      if (!currentComment) {
        System.out.println("---READ: "+readByte+" | "+currentComment+" "+lineNumber);
        if (Character.isDigit( (char) read)) {
          doubleResolved = false;
        }
        
        if (readByte.equals(ReservedSymbols.LEFT_PAREN)) {
          tokens.add(new Token(readByte, Type.LEFT_PAREN, lineNumber));
        }
        else if (readByte.equals(ReservedSymbols.RIGHT_PAREN)) {
          tokens.add(new Token(readByte, Type.RIGHT_PAREN, lineNumber));
        }
        else if (ReservedSymbols.isAnOperator(readByte)) {
          
          String operator = readByte;

          while ((read = inputStream.read()) != -1 &&
              ReservedSymbols.isAnOperator(String.valueOf((char) read))) {
            operator = operator.concat(String.valueOf((char)read));
            System.out.println("OP? "+operator);
            if (operator.equals(ReservedSymbols.LNE_COMMENT)) { 
              currentComment = true;
              commentIsBlock = false;
              break;
            }
            else if (operator.equals(ReservedSymbols.BLC_CMN_SRT)) {
              System.out.println("---BLOCK COMMENT");
              currentComment = true;
              commentIsBlock = true;
              break;
            }
            else if (ReservedSymbols.isAnOperator(operator) == false) {
              throw new ParseError("Unknown operator sequence: '"+operator+"'", lineNumber);
            }
          }
          
          if (read != -1) {
            inputStream.unread(read);
          }
          
          if (!currentComment) {
            tokens.add(new Token(operator, getType(operator), lineNumber));
          }
        }
        else if (Character.isDigit(readByte.charAt(0))) {
          //keep reading from string until non-numerical is found

          String number = readByte;
          while ((read = inputStream.read()) != -1 &&
              Character.isDigit((char) read)) {
            number = number.concat(String.valueOf((char)read));
          }
          
          if (read != -1) {
            inputStream.unread(read);
          }
          
          if (dotRecieved && !doubleResolved) {
            Token last = tokens.remove(tokens.size()-1);
            Token newNum = new Token(last.content()+number, Type.NUMBER, lineNumber);
            dotRecieved = false;
            doubleResolved = true;
            tokens.add(newNum);
          }
          else {
            tokens.add(new Token(number, Type.NUMBER, lineNumber));
          }
        }
        else if (readByte.equals(ReservedSymbols.DOUB_QUOTE)) {
          String literalContent = ""; //the content of the literal

          while ((read = inputStream.read()) != -1 &&
              String.valueOf((char) read).equals(ReservedSymbols.DOUB_QUOTE) == false) {
            literalContent = literalContent.concat(String.valueOf((char)read));
          }
                  
          if (read == -1 && String.valueOf((char) read).equals(ReservedSymbols.DOUB_QUOTE) == false) {
            throw new ParseError("Missing terminating '\"' for '"+literalContent+"' ", lineNumber);
          }
          
          tokens.add(new Token(literalContent, Type.LITERAL, lineNumber));
        }
        else if (Character.isLetter(readByte.charAt(0))) {
          String identStr = readByte; //the content of the literal
          
          while ((read = inputStream.read()) != -1 &&
              Character.isLetter((char) read)) {
            identStr = identStr.concat(String.valueOf((char)read));
          }
          
          if (read != -1) {
            inputStream.unread(read);
          }
          
          if (ReservedSymbols.isABlockSignifier(identStr)) {
            if (identStr.equals(ReservedSymbols.FUNC)) {
              tokens.add(new Token(identStr, Type.FUNCTION, lineNumber));
            }
            else {
              tokens.add(new Token(identStr, Type.BLOCK_SIG, lineNumber));
            }
          }
          else if (ReservedSymbols.isABooleanVal(identStr)) {
            tokens.add(new Token(identStr, Type.BOOL, lineNumber));
          }
          else if (ReservedSymbols.LET.equals(identStr)) {
            tokens.add(new Token(identStr, Type.LET, lineNumber));
          }
          else if (ReservedSymbols.RETURN.equals(identStr)) {
            tokens.add(new Token(identStr, Type.RETURN, lineNumber));
          }
          else if (ReservedSymbols.CONST.equals(identStr)) {
            tokens.add(new Token(identStr, Type.CONST, lineNumber));
          }
          else if (ReservedSymbols.CLASS.equals(identStr)) {
            tokens.add(new Token(identStr, Type.CLASS, lineNumber));
          }
          else if (ReservedSymbols.BREAK.equals(identStr)) {
            tokens.add(new Token(identStr, Type.BREAK, lineNumber));
          }
          else if (ReservedSymbols.CONTINUE.equals(identStr)) {
            tokens.add(new Token(identStr, Type.CONTINUE, lineNumber));
          }
          else if (ReservedSymbols.NULL.equals(identStr)) {
            tokens.add(new Token(identStr, Type.NULL, lineNumber));
          }
          else if (ReservedSymbols.IMPORT.equals(identStr)) {
            tokens.add(new Token(identStr, Type.IMPORT, lineNumber));
          }
          else if (ReservedSymbols.AS.equals(identStr)) {
            tokens.add(new Token(identStr, Type.AS, lineNumber));
          }
          else if (ReservedSymbols.IMPLNT.equals(identStr)) {
            tokens.add(new Token(identStr, Type.IMPLEMENT, lineNumber));
          }
          else {
            tokens.add(new Token(identStr, Type.IDENT, lineNumber));
          }
        }
        else if (readByte.equals(ReservedSymbols.SEMI_COLON)) {
          tokens.add(new Token(readByte, Type.STATE_END, lineNumber));
        }
        else if (readByte.equals(ReservedSymbols.COLON)) {
          tokens.add(new Token(readByte, Type.COLON, lineNumber));
        }
        else if (readByte.equals(ReservedSymbols.COMMA)) {
          tokens.add(new Token(readByte, Type.COMMA, lineNumber));
        }
        else if (readByte.equals(ReservedSymbols.DOT)) {
          dotRecieved = true;
          /*
           * Check last token and see if it's a number
           */
          if (tokens.isEmpty() == false && doubleResolved == false) {
            if (tokens.get(tokens.size()-1).type() == Type.NUMBER) {
              Token last = tokens.remove(tokens.size()-1);
              Token newNum = new Token(last.content()+readByte, Type.NUMBER, lineNumber);
              tokens.add(newNum);
            }
            else {
              dotRecieved = false;
              tokens.add(new Token(readByte, Type.DOT, lineNumber));
            }
            doubleResolved = false;
          }
          else {
            tokens.add(new Token(readByte, Type.DOT, lineNumber));
          }
        }
        else if (readByte.equals(ReservedSymbols.LEFT_CURLY)) {
          tokens.add(new Token(readByte, Type.OPEN_SCOPE, lineNumber));
        }
        else if (readByte.equals(ReservedSymbols.RIGHT_CURLY)) {
          tokens.add(new Token(readByte, Type.CLOSE_SCOPE, lineNumber));
        } 
      }
      else {
        //check for comment block end
        commentedString += readByte;
        commentedString = commentedString.trim();
        if (commentedString.equals(ReservedSymbols.BLC_CMN_END)) {
          if (currentComment == false) {
            throw new ParseError("Misplaced commend block symbol", lineNumber);
          }
          else {
            currentComment = false;
            commentIsBlock = false;
          }
        }
      }      
    }

    inputStream.close();
    return tokens.toArray(new Token[tokens.size()]);
  }
  
  private static Type getType(String cont) {
    Type type = null;
    switch (cont) {
      case ReservedSymbols.PLUS:
        type = Type.PLUS;
        break;
      case ReservedSymbols.MINUS:
        type = Type.MINUS;
        break;
      case ReservedSymbols.MULT:
        type = Type.MULT;
        break;
      case ReservedSymbols.MOD:
        type = Type.MOD;
        break;
      case ReservedSymbols.DIV:
        type = Type.DIVI;
        break;
      case ReservedSymbols.BANG:
        type = Type.BANG;
        break;
      case ReservedSymbols.BANG_EQUALS:
        type = Type.BANG_EQUAL;
        break;
      case ReservedSymbols.LESS:
        type = Type.LESS_THAN;
        break;
      case ReservedSymbols.LESSE:
        type = Type.LESSE;
        break;
      case ReservedSymbols.GREAT:
        type = Type.GREATER_THAN;
        break;
      case ReservedSymbols.GREATE:
        type = Type.GREATERE;
        break;
      case ReservedSymbols.EQUAL_EQ:
        type = Type.EQUAL_EQUAL;
        break;
      case ReservedSymbols.EQUALS:
        type = Type.EQUALS;
        break;
      case ReservedSymbols.INCREMENT:
        type = Type.INCREMENT;
        break;
      case ReservedSymbols.DECREMENT:
        type = Type.DECREMENT;
        break;
      case ReservedSymbols.OR:
        type = Type.OR;
        break;
      case ReservedSymbols.AND:
        type = Type.AND;
        break;
      case ReservedSymbols.BOOL_OR:
        type = Type.BOOL_OR;
        break;
      case ReservedSymbols.BOOL_AND:
        type = Type.BOOL_AND;
        break;
      default:
        break;
    }
    return type;
  }
  
}
