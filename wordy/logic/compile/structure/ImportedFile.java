package wordy.logic.compile.structure;

import wordy.logic.compile.Token;

public class ImportedFile {

  private Token importKey;
  private String imported;
  private boolean isASystemImport;
  
  public ImportedFile(Token importKey, String imported, boolean systemImport) {
    this.importKey = importKey;
    this.imported = imported;
    this.isASystemImport = systemImport;
  }

  public Token getImportKey() {
    return importKey;
  }

  public String getImported() {
    return imported;
  }

  public boolean isASystemImport() {
    return isASystemImport;
  } 
}
