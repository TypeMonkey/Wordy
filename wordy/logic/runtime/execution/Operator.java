package wordy.logic.runtime.execution;

import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.types.JavaClassDefinition;

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
  public static JavaInstance simpleArithmetic(JavaInstance leftOperand, JavaInstance rightOperand, Token operator) {
    if (leftOperand.getDefinition().isChildOf(JavaClassDefinition.defineClass(Number.class)) && 
        rightOperand.getDefinition().isChildOf(JavaClassDefinition.defineClass(Number.class))) {
      JavaInstance result = null;
      if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class)) && 
          rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Double.class))) {
        Integer leftOp = (Integer) leftOperand.getInstance();
        Double rightOp = (Double) rightOperand.getInstance();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          result = JavaInstance.wrapInstance(new Double(leftOp + rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          result = JavaInstance.wrapInstance(new Double(leftOp - rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          result = JavaInstance.wrapInstance(new Double(leftOp * rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = JavaInstance.wrapInstance(new Double(leftOp / rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = JavaInstance.wrapInstance(new Double(leftOp % rightOp));
        }   
      }
      else if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Double.class)) && 
          rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class))) {
        Double leftOp = (Double) leftOperand.getInstance();
        Integer rightOp = (Integer) rightOperand.getInstance();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          result = JavaInstance.wrapInstance(new Double(leftOp + rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          result = JavaInstance.wrapInstance(new Double(leftOp - rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          result = JavaInstance.wrapInstance(new Double(leftOp * rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = JavaInstance.wrapInstance(new Double(leftOp / rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = JavaInstance.wrapInstance(new Double(leftOp % rightOp));
        }   
      }
      else if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class)) && 
          rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class))) {
        Integer leftOp = (Integer) leftOperand.getInstance();
        Integer rightOp = (Integer) rightOperand.getInstance();
        System.out.println("---OPE: "+leftOperand.getInstance()+" | "+rightOperand.getInstance());
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          result = JavaInstance.wrapInstance(new Integer(leftOp + rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          result = JavaInstance.wrapInstance(new Integer(leftOp - rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          result = JavaInstance.wrapInstance(new Integer(leftOp * rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = JavaInstance.wrapInstance(new Integer(leftOp / rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = JavaInstance.wrapInstance(new Integer(leftOp % rightOp));
        }   
      }
      else if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Double.class)) && 
          rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Double.class))) {
        Double leftOp = (Double) leftOperand.getInstance();
        Double rightOp = (Double) rightOperand.getInstance();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          result = JavaInstance.wrapInstance(new Double(leftOp + rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          result = JavaInstance.wrapInstance(new Double(leftOp - rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          result = JavaInstance.wrapInstance(new Double(leftOp * rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = JavaInstance.wrapInstance(new Double(leftOp / rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = JavaInstance.wrapInstance(new Double(leftOp % rightOp));
        }   
      }
      return result;
    }
    if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(String.class)) || 
        rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(String.class))) {
      String leftStr = leftOperand.getInstance().toString();
      String rightStr = rightOperand.getInstance().toString();
      if (!operator.content().equals(ReservedSymbols.PLUS)) {
        throw new RuntimeException("Invalid operation for string at line "+operator.lineNumber());
      }
      else {
        return JavaInstance.wrapInstance(leftStr.concat(rightStr));        
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
  public static JavaInstance arithemticComparison(JavaInstance leftOperand, JavaInstance rightOperand, Token operator) {
    System.out.println("---LEFT OPERAND: "+leftOperand);
    if (leftOperand.getDefinition().isChildOf(JavaClassDefinition.defineClass(Number.class)) && 
        rightOperand.getDefinition().isChildOf(JavaClassDefinition.defineClass(Number.class))) {
      JavaInstance result = null;
      Double left = new Double(leftOperand.getInstance().toString());
      Double right = new Double(rightOperand.getInstance().toString());
      if (operator.content().equals(ReservedSymbols.GREAT)) {
        result = JavaInstance.wrapInstance(left > right);
      }
      else if (operator.content().equals(ReservedSymbols.GREATE)) {
        result = JavaInstance.wrapInstance(left >= right);
      }
      else if (operator.content().equals(ReservedSymbols.LESS)) {
        result = JavaInstance.wrapInstance(left < right);
      }
      else if (operator.content().equals(ReservedSymbols.LESSE)) {
        result = JavaInstance.wrapInstance(left <= right);
      }
      else if (operator.content().equals(ReservedSymbols.EQUAL_EQ)) {
        result = JavaInstance.wrapInstance(left == right);
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
  public static JavaInstance booleanOperations(JavaInstance leftOperand, JavaInstance rightOperand, Token operator) {
    if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Boolean.class)) && 
        rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Boolean.class))) {
      JavaInstance result = null;
      Boolean left = (Boolean) leftOperand.getInstance();
      Boolean right = (Boolean) rightOperand.getInstance();
      if (operator.content().equals(ReservedSymbols.BOOL_AND)) {
        result = JavaInstance.wrapInstance(left && right);
      }
      else if (operator.content().equals(ReservedSymbols.BOOL_OR)) {
        result = JavaInstance.wrapInstance( left || right);
      }
      else if (operator.content().equals(ReservedSymbols.EQUAL_EQ)) {
        result = JavaInstance.wrapInstance(left == right);
      }
      return result;
    }
    throw new RuntimeException("Invalid types for boolean operation at line "+operator.lineNumber());
  }
}
