

public class Node {
	private String _id;
	
	public Node(String nodeId){
		_id = nodeId;
	}
	
	public String getId(){
		return _id;
	}
	
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
	
	@Override
	public String toString() {
		return _id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Node)
			return _id.equals(((Node) obj).getId());
		return super.equals(obj);
	}

	public Node cloneInst() {
		return new Node(_id);
	}

}
