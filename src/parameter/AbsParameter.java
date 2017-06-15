package parameter;

public class AbsParameter {
	
	public String gameType;
	public String cardAbsType;
	public String actionAbsType;
	public boolean doAverage;
	
	public AbsParameter() {
		// set some default params
		gameType = "Kuhn";
		cardAbsType = "NullCardAbstraction";
		actionAbsType = "NullActionAbstraction";
		doAverage = true;
	}
	
	public AbsParameter(String gameType, String cardAbsType, String actionAbsType, boolean doAverage) {
		this.gameType = gameType;
		this.cardAbsType = cardAbsType;
		this.actionAbsType = actionAbsType;
		this.doAverage = doAverage;
	}
	
}
