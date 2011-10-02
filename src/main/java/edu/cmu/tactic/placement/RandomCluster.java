package edu.cmu.tactic.placement;

public class RandomCluster extends Cluster {
	public RandomCluster(String name) {
		super(name);		
	}
	
	public void place() {
		Host[] hostArray = new Host[hosts.size()];
		hostArray = hosts.values().toArray(hostArray);
			
		for (VirtualMachine vm:vms.values()) {
			int pick = (int)Math.round(Math.random()*(hostArray.length-1));
			mapping.put(hostArray[pick], vm);
		}
	}
	
}
