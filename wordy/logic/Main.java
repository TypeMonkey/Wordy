package wordy.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.google.common.reflect.ClassPath;

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
  
  public static void main(String[] args) throws Exception {
    String sourceFile = "src\\Sources\\Source1.w";    

    //WordyCompiler compiler = new WordyCompiler(sourceFile);
    //Map<String, FileStructure> fileMap = compiler.compile();
    
    final ClassLoader loader = ClassLoader.getSystemClassLoader();
    System.out.println(loader.loadClass("java.lang.Object"));
    System.out.println(ClassPath.from(loader).getTopLevelClasses("java.lang.").size());
  }
}
