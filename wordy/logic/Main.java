package wordy.logic;

import java.util.Map;

import wordy.logic.compile.WordyCompiler;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.runtime.Constant;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.types.ValType;

public class Main {
  
  public static void main(String[] args) throws Exception {
    String [] sourceFile = {"src\\Sources\\Flood.w", "src\\Sources\\Some.w"};    


    WordyCompiler compiler = new WordyCompiler(sourceFile);
    Map<String, FileStructure> fileMap = compiler.compile();

    System.out.println("!!!!!!!!!!!!---EXECUTION---!!!!!!!!!!!!");
    
    WordyRuntime runtime = new WordyRuntime();
    runtime.initialize(fileMap);
    runtime.execute("Flood", 1, new Constant(ValType.STRING, "hello"));
  }
}
