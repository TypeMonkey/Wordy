package wordy.logic.compile;

public class Token {
  
  public enum Type{
    IMPORT,
    LET,
    FUNCTION,
    DOT,
    NUMBER,
    IDENT,
    CLASS,
    LITERAL,
    BOOL,
    BREAK,
    NULL,
    CONTINUE,
    LEFT_PAREN,
    RIGHT_PAREN,
    OPEN_SCOPE,
    CLOSE_SCOPE,
    CONST,
    PLUS,
    MINUS,
    MULT,
    DIVI,
    MOD,
    EQUALS,
    TRY,
    CATCH,
    OR,
    AND,
    BOOL_OR,
    BOOL_AND,
    BANG,
    BANG_EQUAL,
    LESSE,
    LESS_THAN,
    GREATERE,
    GREATER_THAN,
    EQUAL_EQUAL,
    INCREMENT,
    DECREMENT,
    COMMA,
    RETURN,
    BLOCK_SIG, //like if, for, while, else, function
    STATE_END,//semicolon
    NO_EXPECT;  //for the formatter to check if there's no tokens expected
  }
  
  private final String content;
  private final Type type;
  private final int lineNumber;
  
  public Token(String content, Type type, int lineNumber) {
    this.content = content;
    this.type = type;
    this.lineNumber = lineNumber;
  }
  
  public String content() {
    return content;
  }
  
  public Type type() {
    return type;
  }
  
  public int lineNumber() {
    return lineNumber;
  }
  
  public String toString() {
    return "["+content+" | "+type+" | ln:"+lineNumber+"]";
  }
}
