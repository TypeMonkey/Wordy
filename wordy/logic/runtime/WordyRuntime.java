package wordy.logic.runtime;

import java.util.HashMap;
import java.util.Map;

import wordy.logic.compile.structure.FileStructure;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.GenVisitor;

/**
 * Front-end for executing a Wordy program
 * @author Jose Guaro
 *
 */
public class WordyRuntime {
  
  private Map<String, FileInstance> files;
  private boolean runtimeInitialized;
  
  public WordyRuntime() {
    files = new HashMap<>();
  }
  
  public void initialize(Map<String, FileStructure> sources) {
    if (!runtimeInitialized) {
      for(FileStructure structure : sources.values()) {
        RuntimeFile file = new RuntimeFile(structure.getFileName());
        FileInstance instance = file.initialize(structure, this);
        
        files.put(file.getName(), instance);
      }
      runtimeInitialized = true;
    }
  }
  
  /**
   * Invokes the main function of this Wordy program
   * @param file - the file name whose main function to invoke
   * @param argc - the amount of arguments this function accepts
   * @param constants - the arguments to pass to the main function
   * 
   * @return the return value of the main function, or null if no return
   */
  public Object execute(String file, int argc,  Instance ... constants) {
    if (runtimeInitialized == false) {
      throw new RuntimeException("Runtime hasn't been initialized!");
    }
    else if (constants.length != argc) {
      throw new RuntimeException("The amount of arguments given doesn't match the "
                                + "amount of arguments the main function accepts");
    }
    else {
      FileInstance fileInstance = files.get(file);
      if (fileInstance == null) {
        throw new RuntimeException("Cannot find the file '"+file+"' !");
      }
      
      Callable main = fileInstance.getDefinition().findFunction("main", argc);
      if (main == null) {
        throw new RuntimeException("The file '"+file+"' doesn't contain a main function "+
                                   "that takes in "+argc+" arguments");
      }
      
      RuntimeFile orgFile = (RuntimeFile) fileInstance.getDefinition();
            
      Map [] varMaps = {orgFile.getVariables()};
      Map [] funcMaps = {orgFile.getFunctions()};
      
      System.out.println(">INITIALIZE: "+varMaps[0].size());
      
      RuntimeTable table = new RuntimeTable(varMaps, funcMaps, orgFile.getJavaClassMap() );
      GenVisitor visitor = new GenVisitor(table, this);
      Instance ret = main.call(visitor, table, constants);
      
      if (ret != null) {
        if (ret instanceof JavaInstance) {
          JavaInstance instance = (JavaInstance) ret;
          return instance.getInstance();
        }
      }
      return ret;
    }
  }
  
  public FileInstance findFile(String name) {
    return files.get(name);
  }
}
