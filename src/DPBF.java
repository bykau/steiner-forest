import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class DPBF {
	Map<Node,Set<Edge>> _graph;
	Set<AtomVector<Node>> _v;
	
	Map<Tree,RecoveryInfo> _rec;
	
	Integer _QMaxSize = 0;
	
//	public static void main(String[] args){			
//		DPBF dpbf = new DPBF();
//		dpbf.loadDataFromFiles("c:\\graph_1.txt", "c:\\query_1.txt");
//	}
	
	public Double executeDPBF(){		
		Queue<Tree> Q = new PriorityQueue<Tree>(10,new Comparator<Tree>(){
		public int compare(Tree t1, Tree t2){
			if(t1.getWeight() == t2.getWeight())
				return 0;
			else 
				if(t1.getWeight() < t2.getWeight())
					return -1;
				else 
					return 1;
		}
		});
		Map<Tree,Double> trees = new HashMap<Tree,Double>();
	
		//Initialize trees (Eq.(9))
		Set<Node> nodes = _graph.keySet();
		for(Node node: nodes){
			Tree tmpTree = new Tree(node,new HashSet<String>(),new Double(0));
			Set<String> keySet = new HashSet<String>();
			for(AtomVector<Node> elem: _v){
				keySet.add(elem.getKey());
			}
			for(String keyword: keySet){
				for(AtomVector<Node> elem: _v){
					if(elem.getKey().equals( keyword ) && elem.getElements().contains( node )){
						tmpTree.addKeyword( keyword );
					}
				}
			}
			if( tmpTree.getP().size() != 0 ){
				Q.add( tmpTree );
				trees.put(tmpTree, tmpTree.getWeight());
			}
		}
	
		Set<String> keySet = new HashSet<String>();
		//TODO think about keyword sets
		for(AtomVector<Node> elem: _v){
			keySet.add(elem.getKey());
		}
		while(!Q.isEmpty()){
			Tree tree = Q.poll();		
			if(tree.getP().equals( keySet )){
				return tree.getWeight();
			}
			
			//tree grow Eq.(11)
			Tree growTree = null;
			for(Edge edge: _graph.get( tree.getRoot() ) ){
				Node u = edge.getNode(tree.getRoot());
				//find u's keywords
				Set<String> uP = new HashSet<String>();
				for(String keyword: keySet){
					for(AtomVector<Node> elem: _v){
						if(elem.getKey().equals( keyword ) && elem.getElements().contains( u )){
							uP.add( keyword );
						}
					}
				}
				uP.addAll(tree.getP());
				growTree = new Tree(u, uP, edge.getWeight() + tree.getWeight());
				if(growTree.getWeight() < (trees.get(growTree) == null ? Double.MAX_VALUE : trees.get(growTree))){
					Q.add( growTree );
					trees.put(growTree, growTree.getWeight());
				}
			}
			
			
			//tree merge Eq.(12)
			for(Set<String> set: Util.powerSet(Util.difference(keySet, tree.getP())) ){
				Set<String> union = new HashSet<String>();
				union.addAll(set);
				union.addAll(tree.getP());
				Tree p2 = new Tree(tree.getRoot(), set, new Double(0));
				Double p2Cost = trees.get(p2);
				if(p2Cost != null){
					Tree mergedTree = new Tree(tree.getRoot(),union,tree.getWeight()+p2Cost);
					if(mergedTree.getWeight() < (trees.get(mergedTree) == null ? Double.MAX_VALUE : trees.get(mergedTree))){
						Q.add( mergedTree );
						trees.put(mergedTree, mergedTree.getWeight());
					}
				}			
			}
		}

		return new Double(0);
	}
	
	public Object[] executeDPBFwithST(){		
		_rec = new HashMap<Tree,RecoveryInfo>();
		Queue<Tree> Q = new PriorityQueue<Tree>(10,new Comparator<Tree>(){
		public int compare(Tree t1, Tree t2){
			if(t1.getWeight() == t2.getWeight())
				return 0;
			else 
				if(t1.getWeight() < t2.getWeight())
					return -1;
				else 
					return 1;
		}
		});
		Map<Tree,Double> trees = new HashMap<Tree,Double>();
	
		//Initialize trees (Eq.(9))
		Set<Node> nodes = _graph.keySet();
		for(Node node: nodes){
			Tree tmpTree = new Tree(node,new HashSet<String>(),new Double(0));
			Set<String> keySet = new HashSet<String>();
			for(AtomVector<Node> elem: _v){
				keySet.add(elem.getKey());
			}
			for(String keyword: keySet){
				for(AtomVector<Node> elem: _v){
					if(elem.getKey().equals( keyword ) && elem.getElements().contains( node )){
						tmpTree.addKeyword( keyword );
					}
				}
			}
			if( tmpTree.getP().size() != 0 ){
				Q.add( tmpTree );
				trees.put(tmpTree, tmpTree.getWeight());
				_rec.put(tmpTree, new RecoveryInfo(RecoveryInfo.TERMINAL_MODE,
						null,
						null,
						null));
			}
		}
	
		Set<String> keySet = new HashSet<String>();
		//TODO think about keyword sets
		for(AtomVector<Node> elem: _v){
			keySet.add(elem.getKey());
		}
		while(!Q.isEmpty()){
			Tree tree = Q.poll();		
			if(tree.getP().equals( keySet )){
				Map<Node,Set<Edge>> reconstructedTree = reconstructTree(tree,_v);
				return new Object[]{tree.getWeight(),reconstructedTree};
			}
			
			//tree grow Eq.(11)
			Tree growTree = null;
			for(Edge edge: _graph.get( tree.getRoot() ) ){
				Node u = edge.getNode(tree.getRoot());
				//find u's keywords
				Set<String> uP = new HashSet<String>();
				for(String keyword: keySet){
					for(AtomVector<Node> elem: _v){
						if(elem.getKey().equals( keyword ) && elem.getElements().contains( u )){
							uP.add( keyword );
						}
					}
				}
				uP.addAll(tree.getP());
				growTree = new Tree(u, uP, edge.getWeight() + tree.getWeight());
				if(growTree.getWeight() < (trees.get(growTree) == null ? Double.MAX_VALUE : trees.get(growTree))){
					Q.add( growTree );
					trees.put(growTree, growTree.getWeight());
					_rec.put(growTree, new RecoveryInfo(RecoveryInfo.GROWTH_MODE,tree.getRoot(),null,null));
				}
			}
			
			
			//tree merge Eq.(12)
			for(Set<String> set: Util.powerSet(Util.difference(keySet, tree.getP())) ){
				Set<String> union = new HashSet<String>();
				union.addAll(set);
				union.addAll(tree.getP());
				Tree p2 = new Tree(tree.getRoot(), set, new Double(0));
				Double p2Cost = trees.get(p2);
				if(p2Cost != null){
					Tree mergedTree = new Tree(tree.getRoot(),union,tree.getWeight()+p2Cost);
					if(mergedTree.getWeight() < (trees.get(mergedTree) == null ? Double.MAX_VALUE : trees.get(mergedTree))){
						Q.add( mergedTree );
						trees.put(mergedTree, mergedTree.getWeight());
						_rec.put(mergedTree, new RecoveryInfo(RecoveryInfo.MERGE_MODE,null,set,tree.getP()));
					}
				}			
			}
		}

		return new Object[0];
	}
	
	private Map<Node, Set<Edge>> reconstructTree(Tree tree,Set<AtomVector<Node>> v) {
		Map<Node, Set<Edge>> graph = new HashMap<Node,Set<Edge>>();
		Stack<Tree> treeStack = new Stack<Tree>();
		treeStack.push(tree);
		while(!treeStack.empty()){
			Tree tmpTree = treeStack.pop();
			if(_rec.get(tmpTree).getMode() == RecoveryInfo.GROWTH_MODE){
				Node previousNode = _rec.get(tmpTree).getPreviousNode();
				Double weight = GSF.DEFAULT_WEIGHT;
	        	Edge edge = new Edge(previousNode,tmpTree.getRoot(),weight);
	        	if(graph.keySet().contains(previousNode)){
	        		graph.get(previousNode).add(edge);
	        	}else{
	        		 Set<Edge> edgeSet = new HashSet<Edge>();
	        		 edgeSet.add(edge);
	        		 graph.put(previousNode, edgeSet);
	        	 }
	        	 if(graph.keySet().contains(tmpTree.getRoot())){
	        		 graph.get(tmpTree.getRoot()).add(edge);
	        	 }else{
					Set<Edge> edgeSet = new HashSet<Edge>();
					edgeSet.add(edge);
					graph.put(tmpTree.getRoot(), edgeSet);
	        	 }
	        	 Tree t = new Tree(previousNode,tmpTree.getP(),GSF.DEFAULT_WEIGHT);	        	 
	        	 if(_rec.get(t) == null){
	        		//find all keywords of tmpTree.getRoot() node
	        		 Set<String> nodeP = new HashSet<String>();
	        		 for(AtomVector<Node> av: v){
	        			 if(av.getElements().contains(tmpTree.getRoot()))
	        				 nodeP.add(av.getKey());
	        		 }
	        		 t = new Tree(previousNode,
	        				 Util.difference(tmpTree.getP(),nodeP),
	        				 GSF.DEFAULT_WEIGHT);
	        	 }
	        	if(!(_rec.get(t).getMode()==RecoveryInfo.GROWTH_MODE
	        				 &&
	        		_rec.get(t).getPreviousNode().equals(tmpTree.getRoot())))
	        		treeStack.push(t);
	        	 
	        	 
			}
			if(_rec.get(tmpTree).getMode() == RecoveryInfo.MERGE_MODE){
				Set<String> p1 = _rec.get(tmpTree).getP1();
				Set<String> p2 = _rec.get(tmpTree).getP2();
				Tree t1 = new Tree(tmpTree.getRoot(),p1,GSF.DEFAULT_WEIGHT);
				Tree t2 = new Tree(tmpTree.getRoot(),p2,GSF.DEFAULT_WEIGHT);
				treeStack.push(t1);
				treeStack.push(t2);
			}
		}
		return graph;
	}

	public Object[] DPBF4GSF(Map<Node,Set<Edge>> graph, Set<Set<AtomVector<Node>>> s){
		Map<Set<AtomVector<Node>>,Double> result = new HashMap<Set<AtomVector<Node>>,Double>();
		result.put(new HashSet<AtomVector<Node>>(), new Double(0));
		Set<AtomVector<Node>> v = new HashSet<AtomVector<Node>>();
		for(Set<AtomVector<Node>> element :s){
			v.addAll(element);
		}
		Set<Set<Set<AtomVector<Node>>>> subtrees = Util.powerSet( s );
		
		
		Queue<Tree> Q = new PriorityQueue<Tree>(10,new Comparator<Tree>(){
		public int compare(Tree t1, Tree t2){
			if(t1.getWeight() == t2.getWeight())
				return 0;
			else 
				if(t1.getWeight() < t2.getWeight())
					return -1;
				else 
					return 1;
		}
		});
		Map<Tree,Double> trees = new HashMap<Tree,Double>();
	
		//Initialize trees (Eq.(9))
		Set<Node> nodes = graph.keySet();
		for(Node node: nodes){
			Tree tmpTree = new Tree(node,new HashSet<String>(),new Double(0));
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
			if( tmpTree.getP().size() != 0 ){
				Q.add( tmpTree );
				if(Q.size() > _QMaxSize)
					_QMaxSize = Q.size();
				trees.put(tmpTree, tmpTree.getWeight());
			}
		}
	
		Set<String> keySet = new HashSet<String>();
		//TODO think about keyword sets
		for(AtomVector<Node> elem: v){
			keySet.add(elem.getKey());
		}
		while(!Q.isEmpty()){
			Tree tree = Q.poll();
			Set<String> intermediateSet = new HashSet<String>();
			Set<Set<AtomVector<Node>>> tmpSubTree = null;
			for(Set<Set<AtomVector<Node>>> subsubtree: subtrees){
				intermediateSet.clear();
				for(AtomVector<Node> elem: Util.plainUnion(subsubtree)){
					intermediateSet.add(elem.getKey());
				}
				if(tree.getP().containsAll( intermediateSet )){
					result.put(Util.plainUnion( subsubtree ),tree.getWeight());
					tmpSubTree = subsubtree;
				}
			}		
			if( tmpSubTree != null){
				subtrees.remove( tmpSubTree );
				if(subtrees.isEmpty()){
					return new Object[]{result,_QMaxSize+result.size()+trees.size()};
				}
			}
			
			//tree grow Eq.(11)
			Tree growTree = null;
			for(Edge edge: graph.get( tree.getRoot() ) ){
				Node u = edge.getNode(tree.getRoot());
				//find u's keywords
				Set<String> uP = new HashSet<String>();
				for(String keyword: keySet){
					for(AtomVector<Node> elem: v){
						if(elem.getKey().equals( keyword ) && elem.getElements().contains( u )){
							uP.add( keyword );
						}
					}
				}
				uP.addAll(tree.getP());
				growTree = new Tree(u, uP, edge.getWeight() + tree.getWeight());
				if(growTree.getWeight() < (trees.get(growTree) == null ? Double.MAX_VALUE : trees.get(growTree))){
					Q.add( growTree );
					if(Q.size() > _QMaxSize)
						_QMaxSize = Q.size();
					trees.put(growTree, growTree.getWeight());
				}
			}
			
			
			//tree merge Eq.(12)
			for(Set<String> set: Util.powerSet(Util.difference(keySet, tree.getP())) ){
				Set<String> union = new HashSet<String>();
				union.addAll(set);
				union.addAll(tree.getP());
				Tree p2 = new Tree(tree.getRoot(), set, new Double(0));
				Double p2Cost = trees.get(p2);
				if(p2Cost != null){
					Tree mergedTree = new Tree(tree.getRoot(),union,tree.getWeight()+p2Cost);
					if(mergedTree.getWeight() < (trees.get(mergedTree) == null ? Double.MAX_VALUE : trees.get(mergedTree))){
						Q.add( mergedTree );
						if(Q.size() > _QMaxSize)
							_QMaxSize = Q.size();
						trees.put(mergedTree, mergedTree.getWeight());
					}
				}			
			}
		}

		return null;
	}
	
	public Object[] DPBF4GSFwithST(Map<Node,Set<Edge>> graph, Set<Set<AtomVector<Node>>> s){
		Map<Set<AtomVector<Node>>,Object[]> result = new HashMap<Set<AtomVector<Node>>,Object[]>();
		_rec = new HashMap<Tree,RecoveryInfo>();
		result.put(new HashSet<AtomVector<Node>>(), new Object[]{new Double(0),null});
		Set<AtomVector<Node>> v = new HashSet<AtomVector<Node>>();
		for(Set<AtomVector<Node>> element :s){
			v.addAll(element);
		}
		Set<Set<Set<AtomVector<Node>>>> subtrees = Util.powerSet( s );
		
		
		Queue<Tree> Q = new PriorityQueue<Tree>(10,new Comparator<Tree>(){
		public int compare(Tree t1, Tree t2){
			if(t1.getWeight() == t2.getWeight())
				return 0;
			else 
				if(t1.getWeight() < t2.getWeight())
					return -1;
				else 
					return 1;
		}
		});
		Map<Tree,Double> trees = new HashMap<Tree,Double>();
	
		//Initialize trees (Eq.(9))
		Set<Node> nodes = graph.keySet();
		for(Node node: nodes){
			Tree tmpTree = new Tree(node,new HashSet<String>(),new Double(0));
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
			if( tmpTree.getP().size() != 0 ){
				Q.add( tmpTree );
				if(Q.size() > _QMaxSize)
					_QMaxSize = Q.size();
				trees.put(tmpTree, tmpTree.getWeight());
				_rec.put(tmpTree, new RecoveryInfo(RecoveryInfo.TERMINAL_MODE,
						null,
						null,
						null));
			}
		}
	
		Set<String> keySet = new HashSet<String>();
		//TODO think about keyword sets
		for(AtomVector<Node> elem: v){
			keySet.add(elem.getKey());
		}
		while(!Q.isEmpty()){
			Tree tree = Q.poll();
			Set<String> intermediateSet = new HashSet<String>();
			Set<Set<AtomVector<Node>>> tmpSubTree = null;
			for(Set<Set<AtomVector<Node>>> subsubtree: subtrees){
				intermediateSet.clear();
				for(AtomVector<Node> elem: Util.plainUnion(subsubtree)){
					intermediateSet.add(elem.getKey());
				}
				if(tree.getP().containsAll( intermediateSet )){
					Map<Node,Set<Edge>> reconstructedTree = reconstructTree(tree,v);
					result.put(Util.plainUnion( subsubtree ),new Object[]{tree.getWeight(),reconstructedTree});
					tmpSubTree = subsubtree;
				}
			}		
			if( tmpSubTree != null){
				subtrees.remove( tmpSubTree );
				if(subtrees.isEmpty()){
					return new Object[]{result,_QMaxSize+result.size()+trees.size()};
				}
			}
			
			//tree grow Eq.(11)
			Tree growTree = null;
			for(Edge edge: graph.get( tree.getRoot() ) ){
				Node u = edge.getNode(tree.getRoot());
				//find u's keywords
				Set<String> uP = new HashSet<String>();
				for(String keyword: keySet){
					for(AtomVector<Node> elem: v){
						if(elem.getKey().equals( keyword ) && elem.getElements().contains( u )){
							uP.add( keyword );
						}
					}
				}
				uP.addAll(tree.getP());
				growTree = new Tree(u, uP, edge.getWeight() + tree.getWeight());
				if(growTree.getWeight() < (trees.get(growTree) == null ? Double.MAX_VALUE : trees.get(growTree))){
					Q.add( growTree );
					if(Q.size() > _QMaxSize)
						_QMaxSize = Q.size();
					trees.put(growTree, growTree.getWeight());
					_rec.put(growTree, new RecoveryInfo(RecoveryInfo.GROWTH_MODE,tree.getRoot(),null,null));
				}
			}
			
			
			//tree merge Eq.(12)
			for(Set<String> set: Util.powerSet(Util.difference(keySet, tree.getP())) ){
				Set<String> union = new HashSet<String>();
				union.addAll(set);
				union.addAll(tree.getP());
				Tree p2 = new Tree(tree.getRoot(), set, new Double(0));
				Double p2Cost = trees.get(p2);
				if(p2Cost != null){
					Tree mergedTree = new Tree(tree.getRoot(),union,tree.getWeight()+p2Cost);
					if(mergedTree.getWeight() < (trees.get(mergedTree) == null ? Double.MAX_VALUE : trees.get(mergedTree))){
						Q.add( mergedTree );
						if(Q.size() > _QMaxSize)
							_QMaxSize = Q.size();
						trees.put(mergedTree, mergedTree.getWeight());
						_rec.put(mergedTree, new RecoveryInfo(RecoveryInfo.MERGE_MODE,null,set,tree.getP()));
					}
				}			
			}
		}

		return null;
	}
}
