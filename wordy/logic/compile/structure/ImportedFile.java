package wordy.logic.compile.structure;

import java.util.Objects;

import wordy.logic.compile.Token;

public class ImportedFile {

  private Token importKey;
  private String imported;
  
  private Token alias;
  
  public ImportedFile(Token importKey, String imported) {
    this.importKey = importKey;
    this.imported = imported;
  }
  
  public boolean equals(Object object) {
    if (object instanceof ImportedFile) {
      ImportedFile file = (ImportedFile) object;
      
      return file.getTypeNameImported().equals(this.getTypeNameImported()) &&
             (file.getAlias() != null && this.alias != null 
               && file.getAlias().equals(this.alias));
    }
    return false;
  }
  
  public int hashCode() {
    return Objects.hash(getTypeNameImported());
  }
  
  public void setAlias(Token alias) {
    this.alias = alias;
  }
  
  public Token getAlias() {
    return alias;
  }

  public Token getImportKey() {
    return importKey;
  }

  public String getImported() {
    return imported;
  }
  
  public String getTypeNameImported() {
    String [] typeNameArr = imported.split("\\.");
    return typeNameArr[typeNameArr.length-1];
  }
}
