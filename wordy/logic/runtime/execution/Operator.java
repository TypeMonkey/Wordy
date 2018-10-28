package wordy.logic.runtime.execution;

import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.nodes.UnaryNode;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.types.JavaClassDefinition;

/**
 * A utility class for arithmetic (+,-,/,*)  operations on two
 * operands, along with unary operations
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
      if ( leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Double.class)) && 
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
      else if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class)) &&
               rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class))) {
        Integer leftOp = (Integer) leftOperand.getInstance();
        Integer rightOp = (Integer) rightOperand.getInstance();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          try {
            result = JavaInstance.wrapInstance(new Integer(Math.addExact(leftOp, rightOp)));
          } catch (ArithmeticException e) {
            result = JavaInstance.wrapInstance(new Long(leftOp + rightOp));
          }
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          try {
            result = JavaInstance.wrapInstance(new Integer(Math.subtractExact(leftOp, rightOp)));
          } catch (ArithmeticException e) {
            result = JavaInstance.wrapInstance(new Long(leftOp - rightOp));
          }
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          try {
            result = JavaInstance.wrapInstance(new Integer(Math.multiplyExact(leftOp, rightOp)));
          } catch (ArithmeticException e) {
            result = JavaInstance.wrapInstance(new Long(leftOp * rightOp));
          }
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = JavaInstance.wrapInstance(new Integer(leftOp / rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = JavaInstance.wrapInstance(new Integer(leftOp % rightOp));
        }   
      }
      else if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Long.class)) &&
               rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Long.class))) {
        Long leftOp = (Long) leftOperand.getInstance();
        Long rightOp = (Long) rightOperand.getInstance();
        if (operator.content().equals(ReservedSymbols.PLUS)) {
          try {
            result = JavaInstance.wrapInstance(new Long(Math.addExact(leftOp, rightOp)));
          } catch (ArithmeticException e) {
            throw e;
          }
        }
        else if (operator.content().equals(ReservedSymbols.MINUS)) {
          try {
            result = JavaInstance.wrapInstance(new Long(Math.subtractExact(leftOp, rightOp)));
          } catch (ArithmeticException e) {
            throw e;
          }
        }
        else if (operator.content().equals(ReservedSymbols.MULT)) {
          try {
            result = JavaInstance.wrapInstance(new Long(Math.multiplyExact(leftOp, rightOp)));
          } catch (ArithmeticException e) {
            throw e;
          }
        }
        else if (operator.content().equals(ReservedSymbols.DIV)) {
          result = JavaInstance.wrapInstance(new Long(leftOp / rightOp));
        }
        else if (operator.content().equals(ReservedSymbols.MOD)) {
          result = JavaInstance.wrapInstance(new Long(leftOp % rightOp));
        }   
      }
      else {
        result = mixedOperandNumberTypes(leftOperand, rightOperand, operator);
      }
      return result;
    }
    if (leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(String.class)) || 
        rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(String.class))) {
      String leftStr = leftOperand.toString();
      String rightStr = rightOperand.toString();
      if (!operator.content().equals(ReservedSymbols.PLUS)) {
        throw new RuntimeException("Invalid operation for string at line "+operator.lineNumber());
      }
      else {
        return JavaInstance.wrapInstance(leftStr.concat(rightStr));        
      }
    }
    
    System.out.println("LEFT TYPE: "+leftOperand.getDefinition().getName()+
                       " | RIGHT TYPE: "+rightOperand.getDefinition().getName());
    
    throw new RuntimeException("Invalid types for arithmetic operation at line "+operator.lineNumber());
  }
  
  /**
   * Adds, multiplies, divides and mods two numbers of different types
   * @param leftOperand - the left operand
   * @param rightOperand - the right operand
   * @return JavaInstance that holds the result of the arithmetic operation
   */
  private static JavaInstance mixedOperandNumberTypes(JavaInstance leftOperand, JavaInstance rightOperand, Token operator) {
    if (leftOperand.getDefinition().isChildOf(JavaClassDefinition.defineClass(Number.class)) && 
        rightOperand.getDefinition().isChildOf(JavaClassDefinition.defineClass(Number.class))) {
      JavaInstance result = null;
      if ( leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Double.class)) ) {
        Double left = (Double) leftOperand.getInstance();
        if (rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class))) {
          Integer right = (Integer) rightOperand.getInstance();
          result = JavaInstance.wrapInstance(new Double(left + right));     
        }
        else if (rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Long.class))) {
          Long right = (Long) rightOperand.getInstance();
          result = JavaInstance.wrapInstance(new Double(left + right));     
        }
      }
      else if ( leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class)) ) {
        Integer left = (Integer) leftOperand.getInstance();
        if (rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Double.class))) {
          Double right = (Double) rightOperand.getInstance();
          result = JavaInstance.wrapInstance(new Double(left + right));     
        }
        else if (rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Long.class))) {
          Long right = (Long) rightOperand.getInstance();
          result = JavaInstance.wrapInstance(new Long(left + right));     
        }       
      }
      else if ( leftOperand.getDefinition().equals(JavaClassDefinition.defineClass(Long.class)) ) {
        Long left = (Long) leftOperand.getInstance();
        if (rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Integer.class))) {
          Integer right = (Integer) rightOperand.getInstance();
          result = JavaInstance.wrapInstance(new Long(left + right));     
        }
        else if (rightOperand.getDefinition().equals(JavaClassDefinition.defineClass(Double.class))) {
          Double right = (Double) rightOperand.getInstance();
          result = JavaInstance.wrapInstance(new Double(left + right));     
        }       
      }
      return result;
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
    //System.out.println("---LEFT OPERAND: "+leftOperand);
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
  
  public static JavaInstance performUnaryOperation(JavaInstance instance, Token operator) {
    JavaInstance result = null;
    if (operator.type() == Type.BANG) {
      Boolean bool = (Boolean) instance.getInstance();
      result = JavaInstance.wrapInstance(!bool);
    }
    else if (operator.type() == Type.MINUS) {
      Double doubleVal = new Double(instance.getInstance().toString());
      result = JavaInstance.wrapInstance(-doubleVal);
    }
    return result;
  }
}
