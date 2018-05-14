import wordy.logic.Main;

function main(a){
  a = Dry.Ob(30);
  Dry.re(a).k = 20;
  println("result: "+a.k);
  
  let b = Array(10);
  println("array length: "+b.length);
  
  a = "hello";
  b.set(0, a);
  println("GOT: "+b.get(0));
  
  let f = Dry.re(b);
  f.set(0, "bye");
  println("GOT again: "+b.get(0));
  
  Dry.change(b);
  println("GOT again1: "+b.get(0));
  
  println("TIME: "+System.nanoTime());
  
  f = Long("123");
  a = Integer("12");
  println("parsed: "+(f+a));
}

function help(){
	return Main.changeMe;
}


