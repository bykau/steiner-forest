import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class DPH1 {	
	public Double executeDPH1(Map<Node,Set<Edge>> graph, Set<AtomVector<Node>> v){
		Map<Tree,Double> trees = DPH1SolutionSpace(graph,v);
		Double min = Double.MAX_VALUE;
		for(Tree t: trees.keySet()){
			Set<String> keySet = new HashSet<String>();
			for(AtomVector<Node> elem: v){
				keySet.add(elem.getKey());
			}
			if(t.getP().equals(keySet) && trees.get(t) < min){
				min = trees.get(t);
			}					
		}
		return min;
	}
	
	public Map<Tree,Double> DPH1SolutionSpace(Map<Node,Set<Edge>> graph, Set<AtomVector<Node>> v){
		//Storage for the solution space
		Map<Tree,Double> trees = new HashMap<Tree,Double>();		
		
		//Initialize trees (Eq.(5))
		Set<Node> nodes = graph.keySet();
		for(Node node: nodes){
			Tree tmpTree = new Tree(node,new HashSet<String>(),0.0);
			Set<String> keySet = new HashSet<String>();
			for(AtomVector<Node> elem: v){
				keySet.add(elem.getKey());
			}
			for(String keyword: keySet){
				for(AtomVector<Node> elem: v){
					if(elem.getKey().equals( keyword ) && elem.getElements().contains( node )){
						tmpTree.addKeyword( keyword );
					}
				}
			}
			if( tmpTree.getP().size() != 0 )
				trees.put( tmpTree, new Double(0));
		}
		
		int h = 0;
		Set<String> keySet = new HashSet<String>();
		for(AtomVector<Node> elem: v){
			keySet.add(elem.getKey());
		}
		Set<Set<String>> subsetsOfP = Util.powerSet( keySet );
		while(h < graph.size()){
			h++;
			for(Node node: nodes){
				Map<Tree,Double> tmpTrees = new HashMap<Tree,Double>();	
				//T(v,p,h-1)
				for(Tree t: trees.keySet()){
					if(t.getH() == ((double) (h-1)) && t.getRoot().equals( node ))
						tmpTrees.put(t, trees.get(t));
				}
				//T_g(v,p,h)
				for(Set<String> set: subsetsOfP ){
					Double min = Double.MAX_VALUE;
					for(Edge edge: graph.get( node ) ){
						Tree adjacentTree = new Tree(edge.getNode(node), set, (double) h-1);
						Double adjacentTreeCost = trees.get(adjacentTree);
						if(adjacentTreeCost != null){
							if(min > (edge.getWeight() + adjacentTreeCost)){
								min = edge.getWeight() + adjacentTreeCost;
							}						
						}
					}
					if(min != Double.MAX_VALUE)
						tmpTrees.put(new Tree(node,set,(double) h), min);
				} 
				//T_m(v,p,h)
				Map<Tree,Double> mergedTrees = new HashMap<Tree,Double>();
				for( Tree t1: tmpTrees.keySet() ){
					for( Tree t2: tmpTrees.keySet() ){
						if(!Util.intersect(t1.getP(),t2.getP())){
							Set<String> unionOfP = new HashSet<String>();
							unionOfP.addAll(t1.getP());
							unionOfP.addAll(t2.getP());
							put(new Tree(node,unionOfP, (double) h),tmpTrees.get(t1)+
					                  tmpTrees.get(t2), mergedTrees);
						}
					}
				}
				for(Tree t: mergedTrees.keySet()){
					put(t,mergedTrees.get(t),tmpTrees);					
				}
				//Eq.(6)
				for(Tree t: tmpTrees.keySet()){
					put(t,tmpTrees.get(t),trees);					
				}
			}			
		}
		
		return trees;
	}		
	
	/**
	 * Put a new tree into <code>dest</code>. If it is new - inserts, 
	 * if it exists already - updates only in case of a lesser value.
	 * @param tree
	 * @param score
	 * @param dest
	 */
	public static void put(Tree tree, Double score, Map<Tree,Double> dest){
		Double existingTreeScore = dest.get(tree);
		if( existingTreeScore == null){
			dest.put(tree, score);
		}else{
			if(existingTreeScore > score)
				dest.put(tree, score);
		}	
	}
	
}
