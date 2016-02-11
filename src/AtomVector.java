

import java.util.HashSet;
import java.util.Set;

public class AtomVector<N> {
	private String _key;
	private Set<N> _elements = new HashSet<N>();
	
	public void setKey(String key){
		_key = key;
	}
	
	public void setElements(Set<N> elements){
		_elements = elements;
	}
	
	public void addElement(N element){
		_elements.add(element);
	}
	
	public String getKey(){
		return _key;
	}
	
	public Set<N> getElements(){
		return _elements;
	}
	
	@Override
	public String toString() {
		return _key+":"+_elements;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AtomVector<?>){
			return _key.equals(((AtomVector<?>) obj).getKey());
		}
		return super.equals(obj);
	}
}
