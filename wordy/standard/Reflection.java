package wordy.standard;

import java.util.Iterator;

import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.components.Instance;

/**
 * Acts as a front-end to reflection related functionalities of a Wordy
 * environment
 * @author Jose Guaro
 *
 */
public class Reflection {
	private Instance instance;
	
	public Reflection(Instance instance) {
		this.instance = instance;
	}
	
	public Array getVariableNames() {
		Iterator<VariableMember> vars = instance.varMap().values().iterator();
		Array array = new Array(instance.varMap().size());
		for(int i = 0; vars.hasNext(); i++) {
			array.set(i, vars.next());
		}
		
		return array;
	}
}
