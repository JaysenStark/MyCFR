package acpc;

public class Action {
	public ActionType type;
	public int size;
	public Action(ActionType type, int size) {
		this.type = type;
		this.size = size;
	}
	
	public String toString() {
		String ret = null;
		
		switch (type) {
		case a_call :
			ret = "c";
			break;
		case a_fold :
			ret = "f";
			break;
		case a_raise :
			if ( size == 0 ) {
				return "r";
			}
			ret = "r" + size;
			break;
		default :
			System.out.println("ERROR: unexpected action!");
			System.exit(-1);
			break;
		}
		
		return ret;
	}
}
