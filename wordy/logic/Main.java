package wordy.logic;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import wordy.logic.compile.WordyCompiler;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;

public class Main {  
  
  //short usage string for printing
  private static final String USAGE = "Usage: wordy [options] <sources> [args]";
  
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      //no arguments. Print usage
      printFullUsage(createOptions());
    }
    else {
      ParsedArguments arguments = parseCompilerArgs(args);
      if (arguments != null) {
        //no error occurred while parsing arguments
        String [] programArgs = arguments.arguments.toArray(new String[arguments.arguments.size()]);
        String [] sourceFiles = arguments.sources.toArray(new String[arguments.sources.size()]);  
        if (sourceFiles.length != 0) {
          printCompilerIntro(sourceFiles);
          
          System.out.println("---SOURCES: "+Arrays.toString(sourceFiles));
          
          WordyCompiler compiler = new WordyCompiler(sourceFiles);
          Map<String, FileStructure> fileMap = compiler.compile();
          
          
          System.out.println("      --...Form completed. Interpreting now...--      ");
          WordyRuntime runtime = new WordyRuntime();
          System.out.println("      --Initializing runtime environment--      ");
          runtime.initialize(fileMap);
          String mainSourceFile = arguments.mainFile;
          if (mainSourceFile == null) {
            //Have to correctly get the first source file's name, without the file extension
            mainSourceFile = Paths.get(sourceFiles[0]).getFileName().toString();
            mainSourceFile = mainSourceFile.substring(0, mainSourceFile.indexOf("."));
          }
          
          System.out.println("      --Invoking main function in "+mainSourceFile+" --      ");
          System.out.println();
          
          Instance [] progArgs = new Instance[programArgs.length];
          for(int i = 0 ; i < progArgs.length; i++) {
            progArgs[i] = JavaInstance.wrapInstance(programArgs[i]);
          }
          
          runtime.execute(mainSourceFile, 1, JavaInstance.wrapInstance("hello"));
        }
        else {
          //must have at least one source file
          System.out.println(USAGE);
        }
      }
    }  
  }
  
  private static void printCompilerIntro(String [] sources) {
    System.out.println("      WORDY: Vers. "+WordyCompiler.WORDY_VERSION+"      ");
    System.out.println("--Target source files: ");
    for(String x : sources) {
      System.out.println("* "+x);
    }
    System.out.println("........FORMING SOURCES........");
    System.out.println();
  }
  
  /**
   * Parses the arguments provided to the Wordy compiler
   * @param args - the Strign arguments
   * @return a ParsedArguments object holding all parsed information, or 
   *         null if an error occurred while parsing
   */
  private static ParsedArguments parseCompilerArgs(String [] args) {
    ParsedArguments arguments = new ParsedArguments();
    Options options = createOptions();
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse( options, args);

      for(Option proc : cmd.getOptions()) {
        if (proc.getLongOpt().equals("help")) {
          //print full sage and exit program
          printFullUsage(options);
          return null;
        }
        else if (proc.getLongOpt().equals("main")) {
          //set given main source
          arguments.setMainFile(proc.getValue());
        }
        else if (proc.getLongOpt().equals("class")) {
          String directory = proc.getValue();
          if (new File(directory).isDirectory() == false) {
            System.err.println("'"+directory+"' isn't a directory.");
            System.err.println(USAGE);
            //return; DEV NOTE: uncomment later
          }
          arguments.setClassFolder(directory);
        }
      }
      
      String [] parsedArgs = cmd.getArgs();
      
      int index = 0;
      while (index < parsedArgs.length) {
        if (parsedArgs[index].endsWith(".w")) {
          arguments.addSource(parsedArgs[index]);
        }
        else {
          break;
        }
        index++;
      }
      
      while (index < parsedArgs.length) {
        if (parsedArgs[index].endsWith(".w")) {
          System.err.println("Expected program arguments, not Wordy source files.");
          System.out.println();
          System.out.println(USAGE);
          return null;
        }
        arguments.addArgument(parsedArgs[index]);
        index++;
      }
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      System.out.println();
      printFullUsage(options);
    }
    
    return arguments;
  }
  
  /**
   * Creates the Options object used for parsing command line options
   * @return the Options object created
   */
  private static Options createOptions() {
    Options options = new Options();
    Option help = new Option("h", "prints the usage of this program, along with this message to stdout");
    help.setLongOpt("help");
    help.setArgs(0);
    
    Option mainFunc = new Option("m", "Sets which .w file contains the main function to execute"+System.lineSeparator()+
                                      "By default, the left most .w file's main function will be executed, if it has one"+System.lineSeparator()+
                                      "If not, an error will be thrown");
    mainFunc.setLongOpt("main");
    mainFunc.setArgs(1);
    
    Option compiledClasses = new Option("c", "Folder to search for .class files for type resolution");
    compiledClasses.setArgs(1);
    compiledClasses.setLongOpt("class");
    
    options.addOption(help);
    options.addOption(mainFunc);
    options.addOption(compiledClasses);
    
    return options;
  }
  
  /**
   * Prints the full usage message - includes short usage, but also options and their descriptions
   * @param options - the Options object to use that holds all Option
   */
  private static void printFullUsage(Options options) {
    System.out.println(USAGE);
    for(Option ops : options.getOptions()) {
      System.out.println("   -"+ops.getLongOpt()+" , -"+ops.getOpt()+"     "+ops.getDescription());
    }
  }
  
  public static class ParsedArguments{
    private String mainFile;
    private String classFolder; 
    private List<String> sources;
    private List<String> arguments;
    
    public ParsedArguments() {
      sources = new ArrayList<>();
      arguments = new ArrayList<>();
    }
    
    public void setMainFile(String mainFile) {
      this.mainFile = mainFile;
    }
    
    public void setClassFolder(String classFolder) {
      this.classFolder = classFolder;
    }
    
    public void addArgument(String arg) {
      arguments.add(arg);
    }
    
    public void addSource(String source) {
      if (sources.contains(source) == false) {
        sources.add(source);
      }
    }
  }
}
