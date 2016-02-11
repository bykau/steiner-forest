import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GSF<V> {
	public static final Double DEFAULT_WEIGHT = new Double(1);

	public Double GSFonDPH1(Map<Node,Set<Edge>> graph, Set<Set<AtomVector<Node>>> s){
		//run GST on the union of all groups
		DPH1 dph1 = new DPH1();
		Set<AtomVector<Node>> union = new HashSet<AtomVector<Node>>();
		for(Set<AtomVector<Node>> element :s){
			union.addAll(element);
		}
		Map<Tree,Double> trees = dph1.DPH1SolutionSpace(graph,union);
		//extract necessary trees
		Map<Set<AtomVector<Node>>,Double> GST = new HashMap<Set<AtomVector<Node>>,Double>();
		Set<Set<Set<AtomVector<Node>>>> subtrees = Util.powerSet( s );
		for(Set<Set<AtomVector<Node>>> subtree: subtrees){
			Double min = Double.MAX_VALUE;
			Set<AtomVector<Node>> subV = new HashSet<AtomVector<Node>>();
			for(Set<AtomVector<Node>> element :subtree){
				for(AtomVector<Node> subelement: element)
					subV.add(subelement);
			}
			Set<String> keySet = new HashSet<String>();
			for(AtomVector<Node> elem: subV){
				keySet.add(elem.getKey());
			}
			for(Tree t: trees.keySet()){
				if(t.getP().equals(keySet) && trees.get(t) < min){
					min = trees.get(t);
				}					
			}
			if(subV.size() == 0)
				min = new Double(0);
			GST.put(subV, min);
		}	
		GSF<AtomVector<Node>> gsf = new GSF<AtomVector<Node>>();
		return gsf.executeGSF(GST, s);
	}
	
	/**
	 * return not the best tree size but the memory used
	 * @param graph
	 * @param s
	 * @return
	 */
	public Integer memoryCostGSFonDPBF(Map<Node,Set<Edge>> graph, Set<Set<AtomVector<Node>>> s){
		DPBF dpbf = new DPBF();
		long t1 = System.currentTimeMillis();
		Object[] GST = dpbf.DPBF4GSF(graph, s);
		//System.out.println(System.currentTimeMillis()-t1);
		GSF<AtomVector<Node>> gsf = new GSF<AtomVector<Node>>();
		gsf.executeGSF((Map<Set<AtomVector<Node>>,Double>) GST[0], s);
		return (Integer) GST[1];		
	}
	
	public Double executeGSFonDPBF(Map<Node,Set<Edge>> graph, Set<Set<String>> equalGroups){
		DPBF dpbf = new DPBF();	
		Set<Set<AtomVector<Node>>> s = new HashSet<Set<AtomVector<Node>>>();
		int atomVectorId = 0;
		for(Set<String> equalGroup: equalGroups){
			Set<AtomVector<Node>> tmp = new HashSet<AtomVector<Node>>();
			for(String node: equalGroup){
				AtomVector<Node> av = new AtomVector<Node>();
				av.setKey(String.valueOf(atomVectorId));
				av.addElement(new Node(node));
				tmp.add(av);
				atomVectorId++;
			}
			s.add(tmp);
		}
		//System.out.println(s);
		Object[] GST = dpbf.DPBF4GSF(graph, s);
		GSF<AtomVector<Node>> gsf = new GSF<AtomVector<Node>>();
		return gsf.executeGSF((Map<Set<AtomVector<Node>>,Double>) GST[0], s);		
	}
	
	/**
	 * returns both weight and the found Steiner Tree
	 * @param graph
	 * @param equalGroups
	 * @return
	 */
	public Object[] executeGSFonDPBFwithST(Map<Node,Set<Edge>> graph,
			Set<Set<String>> equalGroups){
		DPBF dpbf = new DPBF();	
		Set<Set<AtomVector<Node>>> s = new HashSet<Set<AtomVector<Node>>>();
		int atomVectorId = 0;
		for(Set<String> equalGroup: equalGroups){
			Set<AtomVector<Node>> tmp = new HashSet<AtomVector<Node>>();
			for(String node: equalGroup){
				AtomVector<Node> av = new AtomVector<Node>();
				av.setKey(String.valueOf(atomVectorId));
				av.addElement(new Node(node));
				tmp.add(av);
				atomVectorId++;
			}
			s.add(tmp);
		}
		//System.out.println(s);
		Object[] GST = dpbf.DPBF4GSFwithST(graph, s);
		GSF<AtomVector<Node>> gsf = new GSF<AtomVector<Node>>();
		return gsf.executeGSFwithST((Map<Set<AtomVector<Node>>,Object[]>) GST[0], s);		
	}

	public Double executeGSF(Map<Set<V>,Double> GST, Set<Set<V>> s){
		Map<Set<Set<V>>,Double> GSF = new HashMap<Set<Set<V>>,Double>();
		
		//init values
		for(Set<V> group: GST.keySet()){
			Set<Set<V>> hashSet = new HashSet<Set<V>>();
			hashSet.add(group);
			GSF.put(hashSet,GST.get(group));
		}	
		//generate combinations of S
		List<Set<Set<Set<V>>>> permutations = new ArrayList<Set<Set<Set<V>>>>();
		Set<Set<Set<V>>> allPermutations = Util.powerSet( s );
		for(int i = 0; i <= s.size(); i++){
			permutations.add(new HashSet<Set<Set<V>>>());
		}
		for(Set<Set<V>> element: allPermutations){
			Set<Set<Set<V>>> hashSet = permutations.get(element.size());
			hashSet.add( element );
		}
		//generate intermidiate solutions
		for(int i = 2; i < s.size(); i++){
			for(Set<Set<V>> H: permutations.get(i) ){
				Set<Set<Set<V>>> E = Util.powerSet(H);
				E.remove(new HashSet<Set<Set<V>>>());
				Double min = Double.MAX_VALUE;
				for(Set<Set<V>> elementE: E){ 
					Set<V> plainUnion = Util.plainUnion(elementE);
					Set<Set<V>> difference = Util.difference(H,elementE);
					//add empty set
					if(difference.size() == 0){
						difference.add(new HashSet<V>());
					}
					Double candidateScore = GST.get(plainUnion)+ GSF.get(difference);
					if(min > candidateScore)
						min = candidateScore;
				}				
				GSF.put(H, min);
			}			
		}
		//compute the answer
		Double min = Double.MAX_VALUE;
		allPermutations.remove(new HashSet<Set<V>>());
		for(Set<Set<V>> H: allPermutations){
			Set<V> plainUnion = Util.plainUnion(H);
			Set<Set<V>> difference = Util.difference(s,H);
			//add empty set
			if(difference.size() == 0){
				difference.add(new HashSet<V>());
			}
			Double candidateScore = GST.get( plainUnion )+ GSF.get( difference );
			if(min > candidateScore)
				min = candidateScore;
		}
		return min;
	}
	
	@SuppressWarnings("unchecked")
	public Object[] executeGSFwithST(Map<Set<V>,Object[]> GST, Set<Set<V>> s){
		Map<Set<Set<V>>,Object[]> GSF = new HashMap<Set<Set<V>>,Object[]>();
		
		//init values
		for(Set<V> group: GST.keySet()){
			Set<Set<V>> hashSet = new HashSet<Set<V>>();
			hashSet.add(group);
			GSF.put(hashSet,GST.get(group));
		}	
		//generate combinations of S
		List<Set<Set<Set<V>>>> permutations = new ArrayList<Set<Set<Set<V>>>>();
		Set<Set<Set<V>>> allPermutations = Util.powerSet( s );
		for(int i = 0; i <= s.size(); i++){
			permutations.add(new HashSet<Set<Set<V>>>());
		}
		for(Set<Set<V>> element: allPermutations){
			Set<Set<Set<V>>> hashSet = permutations.get(element.size());
			hashSet.add( element );
		}
		//generate intermidiate solutions
		for(int i = 2; i < s.size(); i++){
			for(Set<Set<V>> H: permutations.get(i) ){
				Set<Set<Set<V>>> E = Util.powerSet(H);
				E.remove(new HashSet<Set<Set<V>>>());
				Double min = Double.MAX_VALUE;
				Map<Node,Set<Edge>> recGraph = new HashMap<Node,Set<Edge>>();
				for(Set<Set<V>> elementE: E){ 
					Set<V> plainUnion = Util.plainUnion(elementE);
					Set<Set<V>> difference = Util.difference(H,elementE);
					//add empty set
					if(difference.size() == 0){
						difference.add(new HashSet<V>());
					}
					Double candidateScore = ((Double) GST.get(plainUnion)[0])+
					((Double) GSF.get(difference)[0]);
					if(min > candidateScore){
						min = candidateScore;
						recGraph.putAll(((Map<Node,Set<Edge>>) GST.get(plainUnion)[1]));
						recGraph.putAll(((Map<Node,Set<Edge>>) GSF.get(difference)[1]));
					}
				}				
				GSF.put(H, new Object[]{min,recGraph});
			}			
		}
		//compute the answer
		Double min = Double.MAX_VALUE;
		Map<Node,Set<Edge>> recGraph = new HashMap<Node,Set<Edge>>();
		allPermutations.remove(new HashSet<Set<V>>());
		for(Set<Set<V>> H: allPermutations){
			Set<V> plainUnion = Util.plainUnion(H);
			Set<Set<V>> difference = Util.difference(s,H);
			//add empty set
			if(difference.size() == 0){
				difference.add(new HashSet<V>());
			}
			Object[] o1 = GST.get(plainUnion);
			Double d1 = (Double) o1[0];
			Map<Node,Set<Edge>> g1 = (Map<Node,Set<Edge>>) o1[1];
			Object[] o2 = GSF.get(difference);
			Double d2 = (Double) o2[0];
			Map<Node,Set<Edge>> g2 = (Map<Node,Set<Edge>>) o2[1];
			Double candidateScore = d1 + d2;		
			if(min > candidateScore){
				min = candidateScore;
				recGraph.putAll(g1);
				recGraph.putAll(g2);
			}
		}
		return new Object[]{min,recGraph};
	}
}
