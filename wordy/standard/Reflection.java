package wordy.standard;

import java.util.Collection;
import java.util.Iterator;

import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;

public class Reflection {
	private Instance instance;
	
	public Reflection(Instance instance) {
		this.instance = instance;
		System.out.println("---INVOKED: "+instance.getName());
	}
	
	public Array getVariableNames() {
	  System.out.println("--GATHER: "+instance.varMap().size());
	  System.out.println("--AGAIN: "+instance.getDefinition().getVariables().size());
		Iterator<VariableMember> vars = instance.varMap().values().iterator();
		Array array = new Array(instance.varMap().size());
		for(int i = 0; vars.hasNext(); i++) {
			array.set(i, new VariableRepresentation(vars.next()));
		}
		
		return array;
	}
	
	public static Instance wrapValue(Object object) {
	  return JavaInstance.wrapInstance(object);
	}
	
	public static class VariableRepresentation{
	  
	  private VariableMember member;
	  
	  public VariableRepresentation(VariableMember member) {
	    this.member = member;
	  }
	  
	  public Instance setValue(Object object) {
	    Instance prev = member.getValue();
	    member.setValue(JavaInstance.wrapInstance(object));
	    return prev;
	  }
	  
	  public String getName() {
	    return member.getName();
	  }
	  
	  public Instance getValue() {
	    return member.getValue();
	  }
	  
	}
}
