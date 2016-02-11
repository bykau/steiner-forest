import java.util.Set;


public class RecoveryInfo {
	public static final int GROWTH_MODE = 0;
	public static final int MERGE_MODE = 1;
	public static final int TERMINAL_MODE = 2;
	
	private Node _previousNode;
	private Set<String> _p1;
	private Set<String> _p2;
	private int _recoveryMode;
	
	public RecoveryInfo(int recoveryMode,
			Node previousNode,
			Set<String> p1,
			Set<String> p2){
		_previousNode = previousNode;
		_p1 = p1;
		_p2 = p2;
		_recoveryMode = recoveryMode;
	}
	
	public Node getPreviousNode(){
		return _previousNode;
	}
	
	public Set<String> getP1(){
		return _p1;
	}
	
	public Set<String> getP2(){
		return _p2;
	}
	
	public int getMode(){
		return _recoveryMode;
	}

}
