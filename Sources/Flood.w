import wordy.logic.Main;
import wordy.standard.Exception;
import wordy.standard.Array;
import java.util.Random;
import java.util.Scanner;

class Sample : Exception{

	Sample(a){
		super("error! "+a);
	}

}

function lol(){
  	throw Sample("from lol function");
}

function main(a){
  try{
  	lol();
  	println("continue??");
  }
  catch(Sample e){
  	println(e.message);
  }
  
  println("end of main");
  
  a = 1;
  let arr = Array(10);
    
  while(a < arr.length){
  	println(arr.get(a));
  	a = a+1;
  }
  
  eprintln("error print?");
}




