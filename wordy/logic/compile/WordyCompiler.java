package wordy.logic.compile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import wordy.logic.common.FunctionKey;
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
  public static final List<String> JAVA_CLASSES;
  
  static {
    List<String> classes = new ArrayList<>(getStandardJavaClasses());
    classes.add("wordy.standard.Array");
    JAVA_CLASSES = Collections.unmodifiableList(classes);
  }
  
  public static final String RUNTIME_JAR = "rt.jar";
  public static final String CLASS_FILE = ".class";
  
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
      
      structures.put(structure.getFileName(), structure);
      
      System.out.println("---DONE FOR SOURCE: "+currentFile+" ---");
    }
    
    /*
     * We verify the structure of all files after all files have been formed.
     * This is so that all files and their structures are defined
     */
    
    ArrayList<FunctionKey> sysFuncs = new ArrayList<>();
    sysFuncs.addAll(Arrays.asList(new FunctionKey("println", 1), new FunctionKey("print", 1)));
    
    Collection<FileStructure> files = structures.values();
    System.out.println("STRUCUTRE: "+files);
    for(FileStructure structure: files) {
      StructureVerifier verifier = new StructureVerifier(structure, sysFuncs, structures);
      verifier.verify();
    }
    
    return structures;
  }
    
  private static List<String> getStandardJavaClasses(){
    ArrayList<String> classNames = new ArrayList<>();
    String paths = ManagementFactory.getRuntimeMXBean().getBootClassPath();

    String [] indiv = paths.split(File.pathSeparator);
    File [] files = new File[indiv.length];

    for(int i = 0; i < indiv.length; i++) {
      files[i] = new File(indiv[i]);
    }

    File rtFile = null;
    for(File file: files) {
      if (file.getName().equals("rt.jar")) {
        rtFile = file;
        break;
      }
    }

    try {
      ZipInputStream inputStream = new ZipInputStream(new FileInputStream(rtFile));
      
      for(ZipEntry entry = inputStream.getNextEntry() ; entry != null; entry = inputStream.getNextEntry()) {
        if (entry.getName().startsWith("java/lang/")) {
          String className = entry.getName().replace('/', '.').replace(".class", "");
          classNames.add(className);
        }
      }
      
      inputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return classNames;
  }
  
  private void announceError(Throwable throwable) {
    System.err.println(throwable.getMessage());
  }
}
