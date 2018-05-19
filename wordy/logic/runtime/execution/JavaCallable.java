package wordy.logic.runtime.execution;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import wordy.logic.runtime.PrimitiveTypeChecks;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;

public class JavaCallable extends FunctionMember{

  private Method method;
  private Constructor<?> constructor;
  
  public JavaCallable(Method method) {
    super(method.getName(), method.getParameters().length + 1, null, null, null);
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
   *           (NOTE: if the java method is an instance or static method, the first Instance is interpreted
   *                  to be a JavaInstance that this method is being invoked on.
   *                  If it's a static method, the static rep. of the Java class is seen as that instance)
   * @return the result of 
   */
  @Override
  public Instance call(GenVisitor visitor, RuntimeTable table, Instance ... args) {
    //System.out.println("     !!----CALLING: "+name+"----!! ");
    Object result = null;

    Object [] realArgs = new Object[args.length];

    Class<?> [] types = null;
    if (method != null) {
      if (Modifier.isStatic(method.getModifiers()) == false) {
        types = new Class[method.getParameterCount() + 1];
        types[0] = method.getDeclaringClass();
        System.arraycopy(method.getParameterTypes(), 0, types, 1, method.getParameterCount());
      }
      else {
        types = method.getParameterTypes();
      }
    }
    else {
      types = constructor.getParameterTypes();
    }

    for(int i = 0; i < types.length; i++) {
      Instance current = args[i];
      //System.out.println("---CURRENT INSTANCE!!! "+current);
      if (types[i].isAssignableFrom(Instance.class)) {
        //java method actually wants an Instance
        realArgs[i] = current;
      }
      else {
        //java method wants a Java Object
        if (current instanceof JavaInstance) {
          Object actualInstance = ((JavaInstance) current).getInstance();
          if (actualInstance != null && !PrimitiveTypeChecks.isCompatible(types[i], actualInstance.getClass())) {
            System.out.println("---cur type: "+types[i].getName()+" | "+actualInstance.getClass().getName()+
                " | "+types[i].isInstance(actualInstance)+" | "+(actualInstance != null));
            throw new IllegalArgumentException("Method '"+method.getName()+"' was given the wrong argument types");
          }
          realArgs[i] = actualInstance;
        }
        else {
          throw new IllegalArgumentException("Method '"+method.getName()+"' was given the wrong argument types");
        }
      }
      //System.out.println("---CURRENT INSTANCE ** FINAL !!! "+realArgs[i]+" | "+i);

    }

    //System.out.println("**** FIRST: "+realArgs[0]);

    if (constructor != null) {
      try {
        result = constructor.newInstance(realArgs);
      } catch (Exception e) {
        //System.out.println(" first arg type: "+realArgs[0].getClass());
        //System.out.println("---ARG TYPES: "+Arrays.toString(constructor.getParameterTypes())+" | "+realArgs.length);
        throw new RuntimeException("An exception was thrown when calling the constructor for "+constructor.getDeclaringClass().getName()+": "
            +System.lineSeparator()+e);
      }
    }
    else {
      try {
        result = method.invoke(realArgs[0], Arrays.copyOfRange(realArgs, 1, args.length));
        System.out.println("    !CALL FIRST ARG: "+realArgs[0].getClass().getName()+" | "+realArgs.length);
      } catch (Exception e) {         
        //System.out.println(" first arg type: | "+name+" | "+(realArgs[0] == null)+" | "+Arrays.copyOfRange(realArgs, 1, args.length).length);
        //System.out.println("---ARG TYPES: "+Arrays.toString(method.getParameterTypes())+" | "+realArgs.length);

        System.err.println("An exception was thrown when calling "+name+": "+System.lineSeparator());
        e.printStackTrace();
      }
    }
    //System.out.println("---CALLED: "+name);


    if (result instanceof Instance) {
      return (Instance) result;
    }
    
    System.out.println("    *WRAP: "+result.getClass());  
    return JavaInstance.wrapInstance(result);
  }
  
  public boolean argumentsCompatible(Instance ... args) {
    if (super.argumentsCompatible(args)) {
      if (method != null) {
        Class<?> [] paramTypes = new Class[method.getParameterCount() + 1];
        paramTypes[0] = method.getDeclaringClass();
        System.arraycopy(method.getParameterTypes(), 0, paramTypes, 1, method.getParameterCount());
        
        System.out.println("---CHECK: "+name+" | "+args.length);
        for(int i = 0; i < paramTypes.length; i++) {
          if (Instance.class.isAssignableFrom(paramTypes[i])) {
            if (args[i] != null && !paramTypes[i].isInstance(args[i])) {
              System.out.println("---BAD INSTANCE");
              return false;
            }
          }
          else {
            if (args[i] instanceof JavaInstance) {
              JavaInstance currentIns = (JavaInstance) args[i];
              Object instance = currentIns.getInstance();
              if (instance != null && !PrimitiveTypeChecks.isCompatible(paramTypes[i], instance.getClass())) {
                System.out.println("---NO INSTANCE "+paramTypes[i]+" | "+instance.getClass()+" | "+
                                          paramTypes[i].isAssignableFrom(instance.getClass()));
                return false;
              }
            }
            else {
              System.out.println("---NOT JAVA INSTANCE");
              return false;
            }
          }
        }
        return true;
      }
      else {
        //this is a constructor
        Class<?> [] paramTypes = constructor.getParameterTypes();
        for(int i = 0; i < paramTypes.length; i++) {
          if (Instance.class.isAssignableFrom(paramTypes[i])) {
            if (args[i] != null && !paramTypes[i].isInstance(args[i])) {
              System.out.println("---BAD INSTANCE C");
              return false;
            }
          }
          else {
            if (args[i] instanceof JavaInstance) {
              JavaInstance currentIns = (JavaInstance) args[i];
              Object instance = currentIns.getInstance();
              if (instance != null && !PrimitiveTypeChecks.isCompatible(paramTypes[i], instance.getClass())) {
                System.out.println("---BAD INSTANCE CC "+paramTypes[i].getName()+" | "+instance.getClass());
                return false;
              }
            }
            else {
              System.out.println("---BAD INSTANCE CD");
              return false;
            }
          }
        }
        return true;
      }    
    }
    return false;
  }
  
  public boolean isStatic() {
    return Modifier.isStatic(method.getModifiers());
  }
  
  @Override
  public boolean isAConstructor() {  
    return constructor != null;
  }

}
