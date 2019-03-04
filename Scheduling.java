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
			currentProcess.setABCIO();
			
			processList.add(currentProcess);
		}
			
		// TODO: Sort process array by arrival time.  
		
		Collections.sort(processList);
		
		FCFS(processList, randomScanner);
//		uniprogramming(processList, randomScanner);
		
//		printInfo(processList);
		inputScanner.close();
	}
	
	public static void FCFS(List<Process> processList, Scanner randomScanner) {
		Queue<Process> readyQueue = new LinkedList<>();
		LinkedList<Process> blockedQueue = new LinkedList<>();
		
		System.out.println("Before cycle\t" + Process.cycle++ + ": " + processList);
		
		int totalTime = 0;
		for (Process process : processList)
			totalTime += process.cpuTime;
		
		// Check for ready processes
		for (Process process : processList) {
			if (process.state.equals("unstarted") && process.arrivalTime <= Process.cycle) {
				process.state = "ready";
				readyQueue.add(process);
			}		
		}
		
		while (Process.cycle < totalTime) {
			Process blockedTop = blockedQueue.poll();
			Process readyTop = readyQueue.poll();
			
			// Instead of blockedQueue need a for loop here that goes over and decrements accordingly
			
			if (blockedTop != null) {
				int rand = randomScanner.nextInt();
				int blockTime = (rand % blockedTop.ioTime) + 1;
				blockedTop.blockTime = blockTime;
				System.out.println("Find I/O burst when blocking a process " + rand);
			}
			
			if (readyTop != null) {
				readyTop.state = "running";
				int rand = randomScanner.nextInt();
				int runTime = (rand % readyTop.cpuBurstTime) + 1;
				readyTop.cpuRunTime = runTime;
				System.out.println("Find burst when choosing ready process to run " + rand);
			}
			
			
			// Continue iterating until the ready and blocked times reach 0.
			while ((readyTop != null && readyTop.cpuTime > 0 && readyTop.cpuRunTime > 0) || 
					(blockedTop != null && blockedTop.blockTime > 0)) {
				
				System.out.println("Before cycle\t" + Process.cycle + ": " + processList);
				Process.cycle++;
				
				for (Process process : processList) {
					
				}
				
				if (readyTop != null && readyTop.cpuTime > 0 && readyTop.cpuRunTime > 0) {
					readyTop.cpuRunTime--;
					readyTop.cpuTime--;
					if (readyTop.cpuRunTime == 0) {
						if (blockedTop != null && blockedTop.blockTime > 0) {
							System.out.println("Adding blocked top with blockTime " + --blockedTop.blockTime);
							blockedQueue.addFirst(blockedTop);
						}
						System.out.println(blockedQueue);
						break;
					}
				}
				// Don't decrement just blockedTop since there can be multiple blocked processes.
				// Loop through a list of blocked processes and decrement them together.
				if (blockedTop != null && blockedTop.blockTime > 0) {
					blockedTop.blockTime--;
					if (blockedTop.blockTime == 0) {
						blockedTop.state = "ready";
						readyQueue.add(blockedTop);
					}
				}
				
			} // end while loop
			
			// Both ready and blocked process have reached 0 at this point.
			
			// CPU time for ready process has reached 0, can set as terminated
			if (readyTop != null && readyTop.cpuTime <= 0) {
				readyTop.state = "terminated";
			}
			
			// For the blocked check, check if there's something in the queue, then set to running/ready
			// accordingly
			
			// Otherwise put it into the blocked queue.
			else if (readyTop != null && readyTop.cpuRunTime <= 0) {
				System.out.println("adding blocked top " + readyTop);
				readyTop.state = "blocked";
				blockedQueue.addFirst(readyTop);
			}
			
			// Blocked time for blocked process has reached 0, can re-add to the readyQueue.
			
			if (Process.cycle >= 15)
				System.exit(0);
		}
			
		
		
		System.out.println(readyQueue);
		
	}
	
	public static void uniprogramming(List<Process> processList, Scanner randomScanner) {
		// Uniprogramming
		for (Process process : processList) {
			
			while (Process.cycle < process.arrivalTime) {
				Process.cycle++;
			}
			
			// UNSTARTED:
			
			if (process.state.equals("unstarted")) {
				System.out.print("Before cycle\t" + Process.cycle + ":");
				System.out.println(" " + processList);
				
				// Check for ready cycles during every cycle.
				for (Process subProcess : processList) {
					if (subProcess.state.equals("ready"))
						subProcess.waitingTime++;
					
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
					if (subProcess.state.equals("ready"))
						subProcess.waitingTime++;
					
					if (subProcess.state.equals("unstarted") && subProcess.arrivalTime >= Process.cycle)
						subProcess.state = "ready";
				}
				
				// Print all states and CPU burst time
				// Iterate until either cpuTime reaches 0 or runTime expires.
				while (process.cpuTime != 0 && process.cpuRunTime != 0) {
					System.out.print("Before cycle\t" + Process.cycle + ": ");
					System.out.println(processList);
					process.cpuTime--;
					process.cpuRunTime--;
					runTime--;
					Process.cycle++;	
				}
				
				if (process.cpuTime <= 0) {
					System.out.println("TERMINATED!");
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
						if (subProcess.state.equals("ready"))
							subProcess.waitingTime++;
						
						if (subProcess.state.equals("unstarted") && subProcess.arrivalTime >= Process.cycle)
							subProcess.state = "ready";
					}
					
					
					int blockTime = (nextRand % process.ioTime) + 1;
					process.blockTime = blockTime;
					
					while (process.blockTime > 0) {
						System.out.print("Before cycle\t" + Process.cycle + ":");
						System.out.println(" " + processList);
						process.blockTime--;
						Process.cycle++;
						process.totalIoTime++;
					}
					
					process.state = "running";

				} // end while blocked
				
			} // end while running
			process.finishingTime = Process.cycle;
		} // end for process loop
		
		randomScanner.close();
	}
	
	public static void printInfo(List<Process> processList) {
		System.out.print("\n");
		for (int i = 0; i < processList.size(); i++) {
			Process process = processList.get(i);
			process.setTurnaroundTime(process.finishingTime - process.arrivalTime);
			Process.finalFinishingTime = process.finishingTime;
			
			System.out.println("Process " + i + ":");
			System.out.println("\t (A,B,C,IO) = " + process.abcIO);
			System.out.println("\t Finishing time: " + --process.finishingTime);
			System.out.println("\t Turnaround time: " + --process.turnaroundTime);
			System.out.println("\t I/O time: " + process.totalIoTime);
			System.out.println("\t Waiting time: " + process.waitingTime);
			
		}
		
		System.out.println("\nSummary Data: ");
		System.out.println("\t Finishing time: " + --Process.finalFinishingTime);
		System.out.println("\t CPU Utilization: ");
		System.out.println("\t IO Utilization: ");
		System.out.println("\t Throughput: ");
		System.out.println("\t Average turnaround time: ");
		System.out.println("\t Average waiting time: ");
	}
		
}
