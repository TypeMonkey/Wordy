package wordy.logic.runtime;

import java.util.HashMap;
import java.util.Map;

import wordy.logic.compile.structure.FileStructure;

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
   */
  public Object execute(String file, int argc,  Constant ... constants) {
    if (runtimeInitialized == false) {
      throw new RuntimeException("Runtime hasn't been initialized!");
    }
    else if (constants.length != argc) {
      throw new RuntimeException("The amount of arguments given doesn't match the "
                                + "amount of arguments the main function accepts");
    }
    else {
      return null;
    }
  }
  
  public FileInstance findFile(String name) {
    return files.get(name);
  }
}
