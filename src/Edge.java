


public class Edge {
	private Node _node1;
	private Node _node2;
	private Double _weight;
	
	public Edge(Node node1, Node node2, Double weight){
		_node1 = node1;
		_node2 = node2;
		_weight = weight;
	}
	
	/**
	 * Get another node of the edge
	 * @param node - except this node
	 * @return another node
	 */
	public Node getNode(Node node){
		if(_node1.equals(node))
			return _node2;
		else
			return _node1;
	}
	
	public Node getNode1(){
		return _node1;
	}
	
	public Node getNode2(){
		return _node2;
	}

	public Double getWeight() {
		return _weight;
	}
	
	@Override
	public String toString() {
		return "("+ _node1 +"," + _node2 +")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Edge){
			Node node1 = ((Edge) obj).getNode1();
			Node node2 = ((Edge) obj).getNode2();
			if((_node1.equals(node1) && _node2.equals(node2)) || (_node1.equals(node2) && _node2.equals(node1))){
				return true;
			}else
				return false;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return _node1.hashCode()^_node2.hashCode()^_weight.hashCode();
	}

}
