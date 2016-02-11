import java.util.Set;

public class Tree {
	private Node _root;
	private Set<String> _p;
	private Double _weight;
	
	
	public Tree(Node root, Set<String> p, Double score){
		_root = root;
		_p = p;
		_weight = score;
	}
	
	public Node getRoot(){
		return _root;
	}
	
	public Double getWeight(){
		return _weight;
	}
	
	public Set<String> getP(){
		return _p;
	}
	
	public void addKeyword(String keyword) {
		_p.add( keyword );	
	}
	
	@Override
	public int hashCode() {
		return _root.hashCode()^_p.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Tree){
			Tree tree = (Tree) obj;
			if(tree.getRoot().equals( _root ) && 
					tree.getP().equals( _p ))
				return true;
			else 
				return false;
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return "\nT("+_root.toString()+","+_p.toString()+","+_weight+")";
	}
}
