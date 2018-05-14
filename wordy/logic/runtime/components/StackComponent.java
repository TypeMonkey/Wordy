package wordy.logic.runtime.components;

/**
 * Represents Components that can be pushed into the runtime stack, such as variables and instances 
 * (not frame stack, but the computation stack used when interpreting statements)
 * 
 * @author Jose Guaro
 *
 */
public abstract class StackComponent extends Component{

  public StackComponent(String name) {
    super(name);
  }
  
  public final boolean isCallable() {
    return false;
  }
  
  public abstract boolean isAnInstance();
}
