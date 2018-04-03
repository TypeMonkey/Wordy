package wordy.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.Token;
import wordy.logic.compile.Tokenizer;
import wordy.logic.compile.formatter.Formatter;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.structure.Variable;
import wordy.logic.compile.verify.StructureVerifier;
import wordy.logic.runtime.Constant;
import wordy.logic.runtime.RuntimeBuilder;
import wordy.logic.runtime.RuntimeExecutor;
import wordy.logic.runtime.types.ValType;

public class Main {
  
  public static void main(String[] args) throws IOException {
    String sourceFile = "Sources\\Source1.w";    

    Tokenizer tokenizer = new Tokenizer(sourceFile);
    Token [] tokens = tokenizer.tokenize();

    Formatter formatter = new Formatter(Arrays.asList(tokens), sourceFile);
    FileStructure structure = formatter.formatSource();
    
    ArrayList<FunctionKey> sysFuncs = new ArrayList<>();
    sysFuncs.addAll(Arrays.asList(new FunctionKey("println", 1), new FunctionKey("print", 1)));

    StructureVerifier verifier = new StructureVerifier(structure, sysFuncs);
    verifier.verify();
    
    
    System.out.println("--------PRE--------");
    for(Variable variable: structure.getVariables()) {
      System.out.println("--NAME: "+variable.getName());
    }
    System.out.println("---------------POST------------");

    RuntimeBuilder builder = new RuntimeBuilder(structure);
    RuntimeExecutor executor = builder.build();   
    
    System.out.println("--------PROGRAM EXECUTION--------");
    executor.execute("main", 1, new Constant(ValType.STRING, "hello"));  
    
    
  }
  

  private static void announceError(Throwable throwable) {
    System.err.println(throwable.getMessage());
  }
}
