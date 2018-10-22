class LinkedList{
	
	let head;  //head Node 
	let tail;  //tail Node
	let size;
	
	LinkedList(){
	  size = 0;
	}
	
	function add(value){
	  if(head == null){
	    head = Node(value);
	    println("EMPTY: placing "+value);
	  }
	  else if(tail == null){
	    tail = Node(value);
		
		head.next = tail;
		tail.prev = head;
		
		println("SIZE IS 2: "+head.value+" | "+head.next.value);
		println("*CHECK? "+tail.prev.value+" | "+tail.value);
	  }
	  else{
	    let tempTail = tail;
		
		tail = Node(value);
		tempTail.next = tail;
		tail.prev = tempTail;
		
		println("NEW NODE: PREV: "+tail.prev.value+" | NEXT: "+tail.value);
	  }
	  
	  size = size + 1;
	}
	
	function printAll(){
	  let cur = head;
	  
	  while(cur != null){
	    println("ELEMENT: "+cur.value);
		cur = cur.next;
	  }
	}
	
	function getSize(){
	  return size;
	}

}

class Node{
	
	let next;  //next Node
	let prev;  //prev Node
	let value;  //the value held by this Node
	
	Node(val){
	  value = val;
	}
	
}

function main(args){
   let a = 0;
   let b = 9;
   let llist = LinkedList();
   
   while(a <= b){
	llist.add(a);
	a = a + 1;
   }
   
   llist.printAll();
}







