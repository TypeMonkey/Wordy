package wordy.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.Token;
import wordy.logic.compile.Tokenizer;
import wordy.logic.compile.formatter.Formatter;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.verify.StructureVerifier;

/**
 * Entry point for the compilation of Wordy source files.
 * 
 * Note: Compilation is used here to describe the formation of 
 *       constructs (i.e classes, function and variable declarations/definitions) 
 *       prior to runtime. Since Wordy is dynamic, of course no type checking is done.
 */
public class WordyCompiler {
  
  private String [] sources;
    
  /**
   * Constructs a WordyCompiler
   * @param sources - the array of file paths to sources for compilation
   */
  public WordyCompiler(String ... sources) {
    this.sources = sources;
  }
  
  public Map<String, FileStructure> compile() throws IOException{
    HashMap<String, FileStructure> structures = new HashMap<>();

    for(String currentFile : sources) {
      System.out.println("---COMPILING SOURCE: "+currentFile+" ---");

      Token [] fileTokens = Tokenizer.tokenize(currentFile);
      
      System.out.println("------TOKENS------");
      for (int i = 0; i < fileTokens.length; i++) {
        System.out.println(fileTokens[i]);
      }
      System.out.println("------TOKENS_END------");

      Formatter formatter = new Formatter(Arrays.asList(fileTokens), new File(currentFile).getName().split("\\.")[0]);
      FileStructure structure = formatter.formatSource();
      
      structures.put(currentFile, structure);
      
      System.out.println("---DONE FOR SOURCE: "+currentFile+" ---");
    }
    
    /*
     * We verify the structure of all files after all files have been formed.
     * This is so that all files and their structures are defined
     */
    
    ArrayList<FunctionKey> sysFuncs = new ArrayList<>();
    sysFuncs.addAll(Arrays.asList(new FunctionKey("println", 1), new FunctionKey("print", 1)));
    
    Collection<FileStructure> files = structures.values();
    for(FileStructure structure: files) {
      StructureVerifier verifier = new StructureVerifier(structure, sysFuncs, structures);
      verifier.verify();
    }
    
    return structures;
  }
    
  private void announceError(Throwable throwable) {
    System.err.println(throwable.getMessage());
  }
}
