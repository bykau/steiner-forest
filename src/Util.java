import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {
	public static <T> boolean intersect(Set<T> s1, Set<T> s2){
		for(T t: s1){
			if(s2.contains(t))
				return true;
		}
		return false;
	}
	
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
	    Set<Set<T>> sets = new HashSet<Set<T>>();
	    if (originalSet.isEmpty()) {
	        sets.add(new HashSet<T>());
	        return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (Set<T> set : powerSet(rest)) {
	        Set<T> newSet = new HashSet<T>();
	        newSet.add(head);
	        newSet.addAll(set);
	        sets.add(newSet);
	        sets.add(set);
	    }           
	    return sets;
	}
	
	public static <V> Set<Set<V>> union(Set<Set<V>> input){
		Set<Set<V>> result = new HashSet<Set<V>>();
		Set<V> firstItem = new HashSet<V>();
		for(Set<V> element: input){
			firstItem.addAll( element );
		}
		result.add(firstItem);
		return result;
	}
	
	public static <V> Set<V> plainUnion(Set<Set<V>> input){
		Set<V> result = new HashSet<V>();
		for(Set<V> element: input){
			result.addAll( element );
		}
		return result;
	}

	public static <T> Set<T> difference(Set<T> h, Set<T> e) {
		Set<T> result = new HashSet<T>();
		for(T element: h){
			if(!e.contains(element))
				result.add(element);
		}
		return result;
	}		
	
	public static void graph2dot(Map<Node,Set<Edge>> graph, String dotFile){
		FileWriter outFile;
		try {
			outFile = new FileWriter(dotFile);		
			PrintWriter out = new PrintWriter(outFile);
			out.println("digraph hierarchy {");
			
			Set<Edge> edges = new HashSet<Edge>();
			for(Node node: graph.keySet()){
				edges.addAll(graph.get(node));
			}
			
			for(Edge edge: edges){
				out.println("\"" + edge.getNode1() + "\" -> \"" + edge.getNode2() + "\"");
			}
			
			out.println("}");
			out.close();
		} catch (IOException e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}
	
	public static void graph2file(Map<Node,Set<Edge>> graph, String dotFile){
		FileWriter outFile;
		try {
			outFile = new FileWriter(dotFile);		
			PrintWriter out = new PrintWriter(outFile);			
			
			Set<Edge> edges = new HashSet<Edge>();
			for(Node node: graph.keySet()){
				edges.addAll(graph.get(node));
			}
			for(Edge edge: edges){
				out.println(edge.getNode1() +":"+ edge.getNode2()+":1");
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}
	
	public static Map<Node,Set<Edge>> mergeGraphsChain(Map<Node,Set<Edge>> graph1,
			Map<Node,Set<Edge>> graph2){
		Map<Node,Set<Edge>> mergedGraph = new HashMap<Node,Set<Edge>>();
		mergedGraph.putAll(graph1);
		mergedGraph.putAll(graph2);
		
		//create a tie
		Node node1 = graph1.keySet().iterator().next();
		Node node2 = graph2.keySet().iterator().next();
		Edge tie = new Edge(node1,node2,new Double(1));
		mergedGraph.get(node1).add(tie);
		mergedGraph.get(node2).add(tie);
		return mergedGraph;
	}
	
	public static Map<Node,Set<Edge>> mergeGraphsStar(Node starRoot,
			Map<Node,Set<Edge>> graph1,
			Map<Node,Set<Edge>> graph2){		
		Map<Node,Set<Edge>> mergedGraph = new HashMap<Node,Set<Edge>>();
		mergedGraph.putAll(graph1);
		mergedGraph.putAll(graph2);
		
		//create a tie
		Node node1 = graph1.keySet().iterator().next();
		Node node2 = graph2.keySet().iterator().next();
		
		if(!graph1.keySet().contains(starRoot)){
			Edge tie = new Edge(starRoot,node1,new Double(1));
			mergedGraph.get(starRoot).add(tie);
			mergedGraph.get(node1).add(tie);
		}
		if(!graph2.keySet().contains(starRoot)){
			Edge tie = new Edge(starRoot,node2,new Double(1));
			mergedGraph.get(starRoot).add(tie);
			mergedGraph.get(node2).add(tie);
		}
		return mergedGraph;
	}

	public static boolean includes(Set<Set<String>> tuple,
			Set<Set<String>> existingTuple) {				
		for(Set<String> g2: tuple){
			boolean isFound = false;
			for(Set<String> g1: existingTuple){
			if(g1.containsAll(g2))
				isFound = true;
			}
			if(!isFound)
				return false;
		}	
		return true;
	}
}
