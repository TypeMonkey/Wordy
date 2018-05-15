import wordy.standard.Reflection; 
import java.io.File as Socky;

function main(a){
  println(">>>REFLECTION<<<");
  let ref = Reflection(Dry);
  ref = ref.getVariableNames();
  for(let v = 0; v < ref.length; v = v+1){
  	println("VAR: "+ref.get(v).getName()+" | "+ref.get(v).getValue());
  	ref.get(v).setValue(10);
  }
  
  for(let v = 0; v < ref.length; v = v+1){
  	println("VAR: N "+ref.get(v).getName()+" | "+ref.get(v).getValue());
  }
  
  println("REF? "+Dry.a);
  
  println("----SOCKET DEMONSTRATION----");
  let sock = Socky("hello.txt");
  sock.createNewFile();
  
}

function help(){
	return 10;
}


