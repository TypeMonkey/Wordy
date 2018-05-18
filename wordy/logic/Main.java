package wordy.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import wordy.logic.compile.WordyCompiler;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;

public class Main {
  
  public static int changeMe = 10;
  
  public static void main(String[] args) throws Exception {
    String [] sourceFile = {"src\\Sources\\Flood.w", "src\\Sources\\Dry.w"};    
       /* 
    WordyCompiler compiler = new WordyCompiler(sourceFile);
    Map<String, FileStructure> fileMap = compiler.compile();
     */
       
    /*
    System.out.println(Operator.simpleArithmetic(JavaInstance.wrapInstance(10), 
                                                     JavaInstance.wrapInstance(10), 
                                                     new Token("+", Type.PLUS, 0)).getInstance());
    */
    
    /*
    JavaInstance instance = JavaInstance.createStaticInstance(Main.class);
    VariableMember variableMember = instance.retrieveVariable("changeMe");
    variableMember.setValue(JavaInstance.wrapInstance(50));
    System.out.println("RESULT: "+changeMe);
    
    
    JavaInstance secondInstance = JavaInstance.createStaticInstance(Main.class);
    VariableMember member = secondInstance.retrieveVariable("changeMe");
    System.out.println("---RETRIEVE: "+ member.getValue());
    
    System.out.println(instance.retrieveVariable("changeMe").getValue());
    instance.retrieveVariable("changeMe").setValue(JavaInstance.wrapInstance(10));
    System.out.println("---RETRIEVE 2: "+ secondInstance.retrieveVariable("changeMe").getValue());
    */
    
    /*
    System.out.println("!!!!!!!!!!!----------EXECUTE----------!!!!!!!!!!!");
    
    WordyRuntime runtime = new WordyRuntime();
    runtime.initialize(fileMap);
    runtime.execute("Flood", 1, JavaInstance.wrapInstance("hello"));
    //System.out.println(changeMe);
    */
    
    System.out.println(Object.class.isAssignableFrom(FileInstance.class));
  }
}
