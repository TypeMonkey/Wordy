import wordy.logic.Main;
import java.util.Random;
import java.util.Scanner;

class Ob{
	
	let k;
	
	Ob(a){
	  k = a;
	}
	
}

function main(a){
   let b = Array(3);
   b.set(0, 10);
   b.set(1, 20);
   b.set(2, 30);
   
   readList(ArrayList(b));
   println("------");
   let c = b.clone();
   c.set(0, 90);
   
   readList(ArrayList(c));
}

function readList(list){
  for(let a = 0; a < list.size(); a = a+1){
    println(list.get(a));
  }
}

function help(){
	return 10;
}


