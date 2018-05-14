package wordy.logic.runtime.execution;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;

public class JavaCallable extends FunctionMember{

  private Method method;
  private Constructor<?> constructor;
  
  public JavaCallable(Method method) {
    super(method.getName(), method.getParameters().length, null);
    this.method = method;
  }
  
  public JavaCallable(Constructor<?> constructor) {
    super(constructor.getDeclaringClass().getName(), constructor.getParameterCount(), null);
    this.constructor = constructor;
  }

  /**
   * Invokes this Java method
   * @param visitor - the GenVisitor to use when evaluating the statements of this method
   *             (this parameter is ignored when calling a Java Method)
   * @param table - the Runtime table to use when identifying function and variable names
   *             (this parameter is ignored when calling a Java Method)
   * @param args - the arguments to pass to this method. All arguments must be JavaInstances
   *           (NOTE: if the java method is an instance method, the first Instance is interpreted
   *                  to be a JavaInstance that this method is being invoked on._
   * @return
   */
  @Override
  public Instance call(GenVisitor visitor, RuntimeTable table, Instance ... args) {
    Object result = null;
    
    Object [] realArgs = new Object[args.length];
    for(int i = 0; i < realArgs.length; i++) {
      if (args[i] instanceof JavaInstance == false) {
        throw new RuntimeException("All arguments to a Java method must be JavaInstances");
      }
      else {
        JavaInstance jArgs = (JavaInstance) args[i];
        realArgs[i] = jArgs.getInstance();
      }
    }
    
    if (constructor != null) {
      //this callable is a function
      try {
        result = constructor.newInstance(realArgs);
      } catch (Exception e) {
        throw new RuntimeException("An exception was thrown when calling the constructor for"+method.getName()+": "
                                    +System.lineSeparator()+
                                     e);
      }
    }
    else {
      if (Modifier.isStatic(method.getModifiers())) {
        try {
          result = method.invoke(null, realArgs);
        } catch (Exception e) {
          throw new RuntimeException("An exception was thrown when calling "+method.getName()+": "+System.lineSeparator()+
                                     e);
        }
      }
      else {
        try {
          result = method.invoke(realArgs[0], Arrays.copyOfRange(realArgs, 1, args.length));
        } catch (Exception e) {
          throw new RuntimeException("An exception was thrown when calling "+method.getName()+": "+System.lineSeparator()+
                                     e);
        }
      }
    }
    return JavaInstance.wrapInstance(result);
  }

  public boolean isStatic() {
    return Modifier.isStatic(method.getModifiers());
  }
  
  @Override
  public boolean isAConstructor() {  
    return constructor != null;
  }

}
