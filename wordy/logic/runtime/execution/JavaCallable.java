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
    super(method.getName(), method.getParameters().length, null, null, null);
    this.method = method;
  }
  
  public JavaCallable(Constructor<?> constructor) {
    super(constructor.getDeclaringClass().getSimpleName(), constructor.getParameterCount(), null,  null, null, null);
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
   *                  to be a JavaInstance that this method is being invoked on.
   *                  Otherwise, all arguments are passed as a normal call)
   * @return the result of 
   */
  @Override
  public Instance call(GenVisitor visitor, RuntimeTable table, Instance ... args) {
    Object result = null;
    
    Object [] realArgs = new Object[args.length];
    for(int i = 0; i < realArgs.length; i++) {
      System.out.println("*PASSED TYPE: "+args[i].getDefinition());
      if (args[i] instanceof JavaInstance) {
        JavaInstance jArgs = (JavaInstance) args[i];
        realArgs[i] = jArgs.getInstance();
      }
      else {
        realArgs[i] = args[i];
      }
    }
    
    if (constructor != null) {
      try {
        result = constructor.newInstance(realArgs);
      } catch (Exception e) {
        System.out.println(" first arg type: "+realArgs[0].getClass());
        System.out.println("---ARG TYPES: "+Arrays.toString(constructor.getParameterTypes())+" | "+realArgs.length);
        throw new RuntimeException("An exception was thrown when calling the constructor for "+constructor.getDeclaringClass().getName()+": "
                                    +System.lineSeparator()+e);
      }
    }
    else {
      if (Modifier.isStatic(method.getModifiers())) {
        try {
          result = method.invoke(null, realArgs);
        } catch (Exception e) {
          throw new RuntimeException("An exception was thrown when calling "+name+": "+System.lineSeparator()+
                                     e);
        }
      }
      else {
        try {
          result = method.invoke(realArgs[0], Arrays.copyOfRange(realArgs, 1, args.length));
        } catch (Exception e) {         
          //System.out.println(" first arg type: "+realArgs[1].getClass());
          //System.out.println("---ARG TYPES: "+Arrays.toString(method.getParameterTypes())+" | "+realArgs.length);
          throw new RuntimeException("An exception was thrown when calling "+name+": "+System.lineSeparator()+
                                     e);
        }
      }
    }
    //System.out.println("---CALLED: "+name);
    
    if (result instanceof Instance) {
      return (Instance) result;
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
