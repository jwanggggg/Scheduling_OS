import java.util.*;
import java.io.*;

public class Scheduling {
	
	public static void main(String[] args) throws IOException {
		File input = new File("src/InputLab2/input-5");
		File randomNumbers = new File("src/InputLab2/random-numbers.txt");
		Scanner inputScanner = new Scanner(input);
		Scanner randomScanner = new Scanner(randomNumbers);
		
		List<Process> processList = new ArrayList<>(); 
		
		// For FCFS, wait for the I/O time given.
		String line = inputScanner.nextLine();
		String[] splitLine = line.split("\\s+");
		// Use to iterate through
		int index = 0;
		
		int numProcesses = Integer.parseInt(splitLine[index++]);
		
		int[] allProcesses = new int[numProcesses * 4];
		
		while (index < allProcesses.length + 1) {
			allProcesses[index - 1] = Integer.parseInt(splitLine[index]);
			index++;
		}
		
		int i = 0;
		
		while (i < allProcesses.length) {
			Process currentProcess = new Process();
			int arrivalTime = allProcesses[i++];
			int cpuBurstTime = allProcesses[i++];
			int cpuTime = allProcesses[i++];
			int ioTime = allProcesses[i++];
			
			currentProcess.arrivalTime = arrivalTime;
			currentProcess.cpuBurstTime = cpuBurstTime;
			currentProcess.cpuTime = cpuTime;
			currentProcess.ioTime = ioTime;
			currentProcess.state = "unstarted";
			
			processList.add(currentProcess);
		}
		
		inputScanner.close();
		
		// TODO: Sort process array by arrival time. 
		// Then while (Process.cycle < processList.get(0).arrivalTime) Process.cycle++; 
		
		Collections.sort(processList);
		
		// Uniprogramming
		for (Process process : processList) {
			
			while (Process.cycle < process.arrivalTime)
				Process.cycle++;
			
			// UNSTARTED:
			
			if (process.state.equals("unstarted")) {
				System.out.print("Before cycle\t" + Process.cycle + ":");
				System.out.println(" " + processList);
				
				// Set everything that's available to ready
				
				// Check for ready cycles during every cycle.
				for (Process subProcess : processList) {
					if (subProcess.state.equals("unstarted") && subProcess.arrivalTime >= Process.cycle)
						subProcess.state = "ready";
				}
				
				// Increment cycle
				Process.cycle++;
			}
			
			if (process.state.equals("ready") && process.arrivalTime <= Process.cycle)
				process.state = "running";
			
			// RUNNING:
			
			while (process.state.equals("running")) {
				System.out.println(process.cpuTime);
				int rand = randomScanner.nextInt();
				int runTime = (rand % process.cpuBurstTime) + 1;
				
				process.cpuRunTime = runTime;
				
				System.out.println("Find burst when choosing ready process to run " + rand);
				
				// Check for ready cycles during every cycle.
				for (Process subProcess : processList) {
					if (subProcess.state.equals("unstarted") && subProcess.arrivalTime >= Process.cycle)
						subProcess.state = "ready";
				}
				
				// Print all states and CPU burst time
				boolean firstTimeRunning = true;
				// Iterate until either cpuTime reaches 0 or runTime expires.
				while (process.cpuTime > 0 && process.cpuRunTime > 0) {
					System.out.print("Before cycle\t" + Process.cycle + ": ");
//					if (Process.cycle >= 500)
//						System.exit(0);
					System.out.println(processList);
					if (firstTimeRunning) {
						process.cpuTime -= runTime;
						firstTimeRunning = false;
					}
					
					process.cpuRunTime--;
					runTime--;
					Process.cycle++;
				}
				
				if (process.cpuTime <= 0) {
					System.out.println("TERMINATED!");
//					while (process.cpuRunTime > 0) {
//						System.out.print("Before cycle\t" + Process.cycle++ + ": ");
//						System.out.println(processList);
//						process.cpuRunTime--;
//					}
					// Time has expired, set runtime to zero and state to terminated
					process.state = "terminated";
					process.cpuRunTime = 0;
					break;
				}
				
				else if (process.cpuRunTime <= 0) {
					process.state = "blocked";
				}
				
				// BLOCKED:
				
				while (process.state.equals("blocked")) {
					int nextRand = randomScanner.nextInt();
					System.out.println("Find I/O burst when blocking a process " + nextRand);
					
					// Check for ready cycles during every cycle.
					for (Process subProcess : processList) {
						if (subProcess.state.equals("unstarted") && subProcess.arrivalTime >= Process.cycle)
							subProcess.state = "ready";
					}
					
					
					int blockTime = (nextRand % process.ioTime) + 1;
					process.blockTime = blockTime;
					
					while (process.blockTime > 0) {
						System.out.print("Before cycle\t" + Process.cycle++ + ":");
						System.out.println(" " + processList);
						process.blockTime--;
					}
					
					process.state = "running";

				} // end while blocked
				
			} // end while running
			
		} // end for process loop
		
		randomScanner.close();
	}
	
	public static void uniprogramming() {
		
	}
		
}
