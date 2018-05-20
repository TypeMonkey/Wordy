import wordy.standard.Reflection; 
import wordy.logic.Main;
import java.io.File;

function main(a){
  let b = Reflection(Dry).getVariableNames();
  
  b = Dry.Ob(b);
  b = b.k;
  
  a = 0;
  while(a < b.length){
  	println("QUERY: "+b.get(a));
  	let var = b.get(a);
  	var.setValue(10);
  	println(" ---NEW: "+var.getValue());
  	a = a + 1;
  }
  
  println("a? "+Dry.a);
  
  
}

function help(){
	return 10;
}


