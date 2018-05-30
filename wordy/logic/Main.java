package wordy.logic;

import java.util.Map;

import wordy.logic.compile.WordyCompiler;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.JavaInstance;

public class Main {  
  
  public static void main(String[] args) throws Exception {
    String [] sourceFiles = { "src\\Sources\\Dry.w", "src\\Sources\\Flood.w"};    
           
    printCompilerIntro(sourceFiles);
    WordyCompiler compiler = new WordyCompiler(sourceFiles);
    Map<String, FileStructure> fileMap = compiler.compile();
     
       
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
    
    
    System.out.println("      --Form completed. Interpreting now...--      ");
    WordyRuntime runtime = new WordyRuntime();
    System.out.println("      --Initializing runtime environment--      ");
    runtime.initialize(fileMap);
    String mainSourceFile = "Flood";
    System.out.println("      --Invoking main function in "+mainSourceFile+" --      ");
    System.out.println();
    //runtime.execute("Flood", 1, JavaInstance.wrapInstance("hello"));
    
  }
  
  private static void printCompilerIntro(String [] sources) {
    System.out.println("      WORDY: Vers. "+WordyCompiler.WORDY_VERSION+"      ");
    System.out.println("--Target source files: ");
    for(String x : sources) {
      System.out.println("* "+x);
    }
    System.out.println("      FORMING SOURCES........");
    System.out.println();
  }
}
