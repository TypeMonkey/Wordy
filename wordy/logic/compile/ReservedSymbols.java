package wordy.logic.compile;

public final class ReservedSymbols {
  public static final String LET = "let";
  public static final String FUNC = "function";
  
  public static final String COMMA = ",";
  public static final String COLON = ":";
  public static final String SEMI_COLON = ";";
  
  public static final String PLUS = "+";
  public static final String MINUS = "-";
  public static final String MULT = "*";
  public static final String DIV = "/";
  public static final String MOD = "%";
  public static final String EQUALS = "=";
  public static final String DOT = ".";
  
  public static final String BANG = "!";
  public static final String BANG_EQUALS = "!=";
  public static final String LESS = "<";
  public static final String GREAT = ">";
  public static final String GREATE = ">=";
  public static final String LESSE = "<=";
  public static final String AND = "&";
  public static final String OR = "|";
  public static final String BOOL_AND = "&&";
  public static final String BOOL_OR = "||";
  
  public static final String INCREMENT = "++";
  public static final String DECREMENT = "--";
  public static final String EQUAL_EQ = "==";
  
  public static final String LEFT_PAREN = "(";
  public static final String RIGHT_PAREN = ")";
  public static final String LEFT_CURLY = "{";
  public static final String RIGHT_CURLY = "}";
  
  public static final String LNE_COMMENT = "//"; 
  public static final String BLC_CMN_SRT = "/*";
  public static final String BLC_CMN_END = "*/";

  
  public static final String FALSE = "false";
  public static final String TRUE = "true";
  
  public static final String TAB = "\t";
  public static final String NEW_LN = System.lineSeparator();
  public static final String DOUB_QUOTE = String.valueOf('"');
  public static final String SING_QUOTE = "'";
  
  public static final String IF = "if";
  public static final String FOR = "for";
  public static final String WHILE = "while";
  public static final String ELSE = "else";
  
  public static final String CONST = "const";
  public static final String THIS = "this";
  public static final String RETURN = "return";
  public static final String BREAK = "break";
  public static final String CONTINUE = "continue";
  
  public static final String THROW = "throw";
  public static final String TRY = "try";
  public static final String CATCH = "catch";
  
  public static final String NULL = "null";
  
  public static final String CLASS = "class";
  
  public static final String IMPORT = "import";
  public static final String AS = "as";
  public static final String IMPLNT = "implements";
  
  public static final String SUPER = "super";

 
  
  public static boolean isAnOperator(String potential) {
    return potential.equals(PLUS) || potential.equals(MINUS) || potential.equals(MULT)
        ||  potential.equals(DIV) || potential.equals(MOD) || potential.equals(EQUALS) 
        || potential.equals(BANG) || potential.equals(BANG_EQUALS) || potential.equals(LESS)
        ||  potential.equals(GREAT) || potential.equals(GREATE) || potential.equals(EQUAL_EQ) 
        ||  potential.equals(LESSE) || potential.equals(INCREMENT) || potential.equals(DECREMENT) 
        || potential.equals(AND) || potential.equals(OR) || potential.equals(BOOL_AND) || 
        potential.equals(BOOL_OR);
  }
  
  public static boolean isAComparisonOp(String potential) {
    return potential.equals(EQUAL_EQ) || potential.equals(LESS) || potential.equals(LESSE)
        ||  potential.equals(GREAT) || potential.equals(GREATE) || potential.equals(BANG_EQUALS) 
        || potential.equals(BANG);
  }
  
  public static boolean isABooleanOperator(String potential) {
    return potential.equals(BOOL_AND) || potential.equals(BOOL_OR);
  }
  
  public static boolean isABooleanVal(String potential) {
    return potential.equals(FALSE) || potential.equals(TRUE);
  }
  
  public static boolean isABlockSignifier(String potential) {
    return potential.equals(IF) || potential.equals(FOR) || potential.equals(WHILE)
        || potential.equals(ELSE) || potential.equals(FUNC) || potential.equals(TRY)
        || potential.equals(CATCH);
  }
}
