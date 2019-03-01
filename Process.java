
public class Process implements Comparable<Process> {
	// Static variables
	static int cycle = 0;
	static int finalFinishingTime = 0;
	static double cpuUtilization = 0;
	static double ioUtilization = 0;
	static double throughput = 0;
	static double averageTurnaroundTime = 0;
	static double averageWaitingTime = 0;
	
	// Instance variables
	int arrivalTime;
	int cpuTime;
	int cpuBurstTime;
	int ioTime;
	
	int cpuRunTime;
	int blockTime;
	
	String state;
	
	// Process-specific statistics
	int finishingTime;
	int turnaroundTime;
	int totalIoTime;
	int waitingTime;
	
	// Default constructor
	public Process() {
		this.state = "unstarted";
		this.cpuRunTime = 0;
		this.blockTime = 0;
		
		this.finishingTime = 0;
		this.turnaroundTime = 0;
		this.totalIoTime = 0;
		this.waitingTime = 0;
	}
	
	// Arg constructor
	public Process(int arrivalTime, int cpuTime, int cpuBurstTime, int ioTime) {
		this.arrivalTime = arrivalTime;
		this.cpuTime = cpuTime;
		this.cpuBurstTime = cpuBurstTime;
		this.ioTime = ioTime;
		this.state = "unstarted";
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.state + " ");
		
		switch (this.state) {
			case "running":
				sb.append(this.cpuRunTime);
				break;
			case "blocked":
				sb.append(this.blockTime);
				break;
			case "ready":
			case "unstarted":
			case "terminated":
				sb.append(0);
				break;
		}
		
		return sb.toString();
	}
	
	// Sort using arrivalTime
	
	@Override
	public int compareTo(Process otherProcess) {
		return this.arrivalTime > otherProcess.arrivalTime ? 1 : this.arrivalTime < otherProcess.arrivalTime ? -1 : 0;		
	}
	
}
