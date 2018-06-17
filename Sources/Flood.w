import wordy.logic.Main;
import java.util.Random;
import java.util.Scanner;

class Sample : Parent{
	
	let k;
	
	Sample(a){
	  k = a;
	}
	
	
	
}

class Parent{

	function call(){
		return "from grandpa";
	}

}

function main(a){
  a = Dry.Ob(10);
  println(a.k);
  println(a.call());
}




