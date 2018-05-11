package wordy.logic.runtime;

import java.util.Map;

import wordy.logic.compile.structure.FileStructure;

/**
 * Front-end for executing a Wordy program
 * @author Jose Guaro
 *
 */
public class WordyRuntime {

  private Map<String, FileStructure> sources;
  private boolean runtimeInitialized;
  
  public WordyRuntime(Map<String, FileStructure> sources) {
    this.sources = sources;
  }
  
  public void initialize() {
    if (!runtimeInitialized) {
      
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
}
