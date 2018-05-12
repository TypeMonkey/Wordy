package wordy.logic.runtime.execution;

import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;
import wordy.logic.runtime.Constant;
import wordy.logic.runtime.types.ValType;

/**
 * A utility class for arithmetic (+,-,/,*)  operations on two
 * operands
 * @author Jose Guaro
 *
 */
public class Operator {
  
  /**
   * Adds, multiplies, subtracts and divides two operands
   * @param operand1 - the left operand this operator is operating on
   * @param operand2 - the right operand this operator is operating on
   * @param operator - the operator for the two operands
   * @return the resulting Value of the operator
   */
  public static Constant simpleArithmetic(Constant leftOperand, Constant rightOperand, Token operator) {
    if (leftOperand.getType().isChildOf(ValType.NUMBER) && 
        rightOperand.getType().isChildOf(ValType.NUMBER)) {
      Constant result = null;
      if (leftOperand.getType().equals(ValType.INTEGER) && 
          rightOperand.getType().equals(ValType.DOUBLE)) {
        int leftOp = (int) leftOperand.getValue();
        double rightOp = (double) rightOperand.getValue();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          result = new Constant(ValType.DOUBLE, leftOp + rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          result = new Constant(ValType.DOUBLE, leftOp - rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          result = new Constant(ValType.DOUBLE, leftOp * rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = new Constant(ValType.DOUBLE, leftOp / rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = new Constant(ValType.DOUBLE, leftOp % rightOp);
        }   
      }
      else if (leftOperand.getType().equals(ValType.DOUBLE) && 
          rightOperand.getType().equals(ValType.INTEGER)) {
        double leftOp = (double) leftOperand.getValue();
        int rightOp = (int) rightOperand.getValue();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          result = new Constant(ValType.DOUBLE, leftOp + rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          result = new Constant(ValType.DOUBLE, leftOp - rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          result = new Constant(ValType.DOUBLE, leftOp * rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = new Constant(ValType.DOUBLE, leftOp / rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = new Constant(ValType.DOUBLE, leftOp % rightOp);
        }   
      }
      else if (leftOperand.getType().equals(ValType.INTEGER) && 
          rightOperand.getType().equals(ValType.INTEGER)) {
        int leftOp = (int) leftOperand.getValue();
        int rightOp = (int) rightOperand.getValue();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          result = new Constant(ValType.INTEGER, leftOp + rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          result = new Constant(ValType.INTEGER, leftOp - rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          result = new Constant(ValType.INTEGER, leftOp * rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = new Constant(ValType.INTEGER, leftOp / rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = new Constant(ValType.INTEGER, leftOp % rightOp);
        }   
      }
      else if (leftOperand.getType().equals(ValType.DOUBLE) && 
          rightOperand.getType().equals(ValType.DOUBLE)) {
        double leftOp = (double) leftOperand.getValue();
        double rightOp = (double) rightOperand.getValue();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          result = new Constant(ValType.DOUBLE, leftOp + rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          result = new Constant(ValType.DOUBLE, leftOp - rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          result = new Constant(ValType.DOUBLE, leftOp * rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = new Constant(ValType.DOUBLE, leftOp / rightOp);
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = new Constant(ValType.DOUBLE, leftOp % rightOp);
        }    
      }
      return result;
    }
    if (leftOperand.getType().equals(ValType.STRING) || 
        rightOperand.getType().equals(ValType.STRING)) {
      String leftStr = leftOperand.getValue().toString();
      String rightStr = rightOperand.getValue().toString();
      if (!operator.content().equals(ReservedSymbols.PLUS)) {
        throw new RuntimeException("Invalid operation for string at line "+operator.lineNumber());
      }
      else {
        return new Constant(ValType.STRING, leftStr.concat(rightStr));        
      }
    }
    throw new RuntimeException("Invalid types for arithmetic operation at line "+operator.lineNumber());
  }
  
  /**
   * Compares two numbers
   * @param operand1 - the left operand this operator is operating on
   * @param operand2 - the right operand this operator is operating on
   * @param operator - the operator for the two operands
   * @return the resulting Value of the operator
   */
  public static Constant arithemticComparison(Constant leftOperand, Constant rightOperand, Token operator) {
    System.out.println("---LEFT OPERAND: "+leftOperand);
    if (leftOperand.getType().isChildOf(ValType.NUMBER) && 
        rightOperand.getType().isChildOf(ValType.NUMBER)) {
      Constant result = null;
      double left = (double) Double.valueOf(leftOperand.getValue().toString());
      double right = (double) Double.valueOf(rightOperand.getValue().toString());
      if (operator.content().equals(ReservedSymbols.GREAT)) {
        result = new Constant(ValType.BOOLEAN, left > right);
      }
      else if (operator.content().equals(ReservedSymbols.GREATE)) {
        result = new Constant(ValType.BOOLEAN, left >= right);
      }
      else if (operator.content().equals(ReservedSymbols.LESS)) {
        result = new Constant(ValType.BOOLEAN, left < right);
      }
      else if (operator.content().equals(ReservedSymbols.LESSE)) {
        result = new Constant(ValType.BOOLEAN, left <= right);
      }
      else if (operator.content().equals(ReservedSymbols.EQUAL_EQ)) {
        result = new Constant(ValType.BOOLEAN, left == right);
      }    
      return result;
    }
    throw new RuntimeException("Invalid types for arithmetic operation at line "+operator.lineNumber());
  }
  
  /**
   * Compares two booleans
   * @param operand1 - the left operand this operator is operating on
   * @param operand2 - the right operand this operator is operating on
   * @param operator - the operator for the two operands
   * @return the resulting Value of the operator
   */
  public static Constant booleanOperations(Constant leftOperand, Constant rightOperand, Token operator) {
    if (leftOperand.getType().equals(ValType.BOOLEAN) && 
        rightOperand.getType().equals(ValType.BOOLEAN)) {
      Constant result = null;
      boolean left = (boolean) leftOperand.getValue();
      boolean right = (boolean) rightOperand.getValue();
      if (operator.content().equals(ReservedSymbols.BOOL_AND)) {
        result = new Constant(ValType.BOOLEAN, left && right);
      }
      else if (operator.content().equals(ReservedSymbols.BOOL_OR)) {
        result = new Constant(ValType.BOOLEAN, left || right);
      }
      else if (operator.content().equals(ReservedSymbols.EQUAL_EQ)) {
        result = new Constant(ValType.BOOLEAN, left == right);
      }
      return result;
    }
    throw new RuntimeException("Invalid types for boolean operation at line "+operator.lineNumber());
  }
}
