import wordy.standard.Reflection; 
import wordy.logic.Main;
import java.io.File;

function main(a){
  
  let file = File("hello.txt");
  file.createNewFile();
  println(file);
}

function help(){
	return 10;
}


