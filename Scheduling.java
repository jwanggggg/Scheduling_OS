import java.util.*;
import java.io.*;

public class Scheduling {
	
	public static void main(String[] args) throws IOException {
		File input = new File("src/InputLab2/input-3");
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
			currentProcess.index = (i-4)/4;
			
			processList.add(currentProcess);
		}
			
		// TODO: Sort process array by arrival time.  
		
		Collections.sort(processList);
		
//		FCFS(processList, randomScanner); // First Come First Serve
		uniprogramming(processList, randomScanner); // Uniprogramming
//		SJF(processList, randomScanner); // Shortest Job First
//		roundRobin(processList, randomScanner); // Round Robin
		
		inputScanner.close();
	}
	
	public static void roundRobin(List<Process> processList, Scanner randomScanner) {
		Process.resetStatic();
		
		Process runningProcess = null;
		Queue<Process> readyQueue = new LinkedList<>();

		List<Process> blockedList = new ArrayList<>();
		List<Process> terminatedList = new ArrayList<>();
		
		System.out.println("Before cycle\t" + Process.cycle++ + ": " + processList);
		
		while (terminatedList.size() < processList.size()) {			
			// Check for ready processes
			for (Process process : processList) {
				if (process.state.equals("unstarted") && process.arrivalTime < Process.cycle) {
					process.state = "ready";
					readyQueue.add(process);
				}
			}
			
			if (runningProcess == null) {
				runningProcess = readyQueue.poll();
				if (runningProcess != null) {
					runningProcess.state = "running";
					
					// If using quantum remainder, set cpuRunTime to the remainder
					
					if (runningProcess.useQuantumRemainder) {
						runningProcess.cpuRunTime = runningProcess.quantumRemainder;
						
						int quantum = 0;
						if (runningProcess.cpuRunTime > 2) {
							quantum = 2;
							runningProcess.quantumRemainder = runningProcess.cpuRunTime - quantum;
							runningProcess.cpuRunTime = quantum;
							runningProcess.quantum = quantum;
						} 
						else {
							runningProcess.quantum = runningProcess.cpuRunTime;
							runningProcess.quantumRemainder = 0;
						}
					} else {
						int rand = randomScanner.nextInt();
						int runTime = (rand % runningProcess.cpuBurstTime) + 1;
						System.out.println("Find burst when choosing ready process to run " + rand);
						
						runningProcess.cpuRunTime = runTime;
						
						int quantum = 0;
						if (runningProcess.cpuRunTime > 2) {
							quantum = 2;
							runningProcess.quantumRemainder = runTime - quantum;
							runningProcess.cpuRunTime = quantum;
							runningProcess.quantum = quantum;
							
						} 
						else {
							runningProcess.quantum = runningProcess.cpuRunTime;
							runningProcess.quantumRemainder = 0;
						}
					}
					
				}
			}
			
			System.out.println("Before cycle\t" + Process.cycle + ": " + processList);
			
			if (runningProcess != null) {
				runningProcess.cpuTime--;
				runningProcess.cpuRunTime--;
				runningProcess.quantum--;
			}
			Process.cycle++;
			
			// Now check blocked processes and decrement @ each iteration
			
			List<Process> addReadyList = new ArrayList<>();
			Process[] blockedListArray = blockedList.toArray(new Process[0]);
			
			for (int i = 0; i < blockedListArray.length; i++) {
				Process blockedProcess = blockedListArray[i];
				blockedProcess.blockTime--;
				
				if (blockedProcess.blockTime == 0) {
					// Queue up the process and remove it from the blocked list
					blockedProcess.state = "ready";
					addReadyList.add(blockedProcess);
					blockedList.remove(blockedProcess);
				}
			}
			
			if (runningProcess != null) {
				// Terminated
				if (runningProcess.cpuTime == 0) {
					runningProcess.state = "terminated";
					terminatedList.add(runningProcess);
					runningProcess = null;
				}
				
				// Quantum has ticked down, re-queue the process 
				else if (runningProcess.cpuRunTime == 0 && runningProcess.quantum == 0 && runningProcess.quantumRemainder > 0) {
					runningProcess.quantum = runningProcess.quantumRemainder;
					runningProcess.state = "ready";
					runningProcess.useQuantumRemainder = true;
					addReadyList.add(runningProcess);
					runningProcess = null;
				}
				
				// Process has blocked
				else if (runningProcess.cpuRunTime == 0 && runningProcess.quantum == 0 && runningProcess.quantumRemainder <= 0) {
					int rand = randomScanner.nextInt();
					int blockTime = (rand % runningProcess.ioTime) + 1;
					runningProcess.blockTime = blockTime;
					runningProcess.state = "blocked";
					runningProcess.useQuantumRemainder = false;
					blockedList.add(runningProcess);
					System.out.println("Find I/O burst when blocking a process " + rand);
					runningProcess = null;
				}
				
			}
			
			// Add all processes that were blocked but are now to be added to ready queue, sorting
			// by index to break any ties that arise
			Comparator_ArrivalTime indexComparator = new Comparator_ArrivalTime();
			Collections.sort(addReadyList, indexComparator);
			for (Process readyProcess : addReadyList) {
				readyQueue.add(readyProcess);
			}
			
		} // end while
	}
	
	public static void SJF(List<Process> processList, Scanner randomScanner) {
		Process.resetStatic();
		// Use a priority queue that dequeues based on the shortest time remaining.
		
		Process runningProcess = null;
		Queue<Process> readyQueue = new PriorityQueue<Process>(new Comparator<Process>() {

			@Override
			public int compare(Process process1, Process process2) {
				if (process1.cpuTime > process2.cpuTime)
					return 1;
				else if (process1.cpuTime < process2.cpuTime)
					return -1;
				return 0;
			}
			
		});
		
		List<Process> blockedList = new ArrayList<>();
		List<Process> terminatedList = new ArrayList<>();
		
		System.out.println("Before cycle\t" + Process.cycle++ + ": " + processList);
		
		while (terminatedList.size() < processList.size()) {
			// Check for ready processes
			for (Process process : processList) {
				if (process.state.equals("unstarted") && process.arrivalTime < Process.cycle) {
					process.state = "ready";
					readyQueue.add(process);
				}
			}
			
			if (runningProcess == null) {
				runningProcess = readyQueue.poll();
				if (runningProcess != null) {
					runningProcess.state = "running";
					int rand = randomScanner.nextInt();
					int runTime = (rand % runningProcess.cpuBurstTime) + 1;
					runningProcess.cpuRunTime = runTime;
					System.out.println("Find burst when choosing ready process to run " + rand);
				}
			}
			
			System.out.println("Before cycle\t" + Process.cycle + ": " + processList);
			
			if (runningProcess != null) {
				runningProcess.cpuTime--;
				runningProcess.cpuRunTime--;
			}
			Process.cycle++;
			
			// Now check blocked processes and decrement @ each iteration
			
			List<Process> addReadyList = new ArrayList<>();
			Process[] blockedListArray = blockedList.toArray(new Process[0]);
			
			for (int i = 0; i < blockedListArray.length; i++) {
				Process blockedProcess = blockedListArray[i];
				blockedProcess.blockTime--;
				
				if (blockedProcess.blockTime == 0) {
					// Queue up the process and remove it from the blocked list
					blockedProcess.state = "ready";
					addReadyList.add(blockedProcess);
					blockedList.remove(blockedProcess);
				}
			}
			
			// Add all processes that were blocked but are now to be added to ready queue, sorting
			// by index to break any ties that arise
			Comparator_Index indexComparator = new Comparator_Index();
			Collections.sort(addReadyList, indexComparator);
			for (Process readyProcess : addReadyList) {
				readyQueue.add(readyProcess);
			}
			
			if (runningProcess != null) {
				// Terminated
				if (runningProcess.cpuTime == 0) {
					runningProcess.state = "terminated";
					terminatedList.add(runningProcess);
					runningProcess = null;
				}
				
				else if (runningProcess.cpuRunTime <= 0) {
					int rand = randomScanner.nextInt();
					int blockTime = (rand % runningProcess.ioTime) + 1;
					runningProcess.blockTime = blockTime;
					runningProcess.state = "blocked";
					blockedList.add(runningProcess);
					System.out.println("Find I/O burst when blocking a process " + rand);
					runningProcess = null;
				}
				
			}
 
		} // end while
	}
	
	public static void FCFS(List<Process> processList, Scanner randomScanner) {
		Process.resetStatic();
		
		Process runningProcess = null;
		Queue<Process> readyQueue = new LinkedList<>();
		List<Process> blockedList = new ArrayList<>();
		List<Process> terminatedList = new ArrayList<>();
		
		System.out.println("Before cycle\t" + Process.cycle++ + ": " + processList);
		
		while (terminatedList.size() < processList.size()) {
			// Check for ready processes
			for (Process process : processList) {
				if (process.state.equals("unstarted") && process.arrivalTime < Process.cycle) {
					process.state = "ready";
					readyQueue.add(process);
				}
			}
			
			if (runningProcess == null) {
				runningProcess = readyQueue.poll();
				if (runningProcess != null) {
					runningProcess.state = "running";
					int rand = randomScanner.nextInt();
					int runTime = (rand % runningProcess.cpuBurstTime) + 1;
					runningProcess.cpuRunTime = runTime;
					System.out.println("Find burst when choosing ready process to run " + rand);
				}
			}
			
			System.out.println("Before cycle\t" + Process.cycle + ": " + processList);
			
			if (runningProcess != null) {
				runningProcess.cpuTime--;
				runningProcess.cpuRunTime--;
			}
			Process.cycle++;
			
			// Now check blocked processes and decrement @ each iteration
			
			List<Process> addReadyList = new ArrayList<>();
			Process[] blockedListArray = blockedList.toArray(new Process[0]);
			
			for (int i = 0; i < blockedListArray.length; i++) {
				Process blockedProcess = blockedListArray[i];
				blockedProcess.blockTime--;
				
				if (blockedProcess.blockTime == 0) {
					// Queue up the process and remove it from the blocked list
					blockedProcess.state = "ready";
					addReadyList.add(blockedProcess);
					blockedList.remove(blockedProcess);
				}
			}
			
			// Add all processes that were blocked but are now to be added to ready queue, sorting
			// by index to break any ties that arise
			Comparator_Index indexComparator = new Comparator_Index();
			Collections.sort(addReadyList, indexComparator);
			for (Process readyProcess : addReadyList) {
				readyQueue.add(readyProcess);
			}
			
			if (runningProcess != null) {
				// Terminated
				if (runningProcess.cpuTime == 0) {
					runningProcess.state = "terminated";
					terminatedList.add(runningProcess);
					runningProcess = null;
				}
				
				else if (runningProcess.cpuRunTime <= 0) {
					int rand = randomScanner.nextInt();
					int blockTime = (rand % runningProcess.ioTime) + 1;
					runningProcess.blockTime = blockTime;
					runningProcess.state = "blocked";
					blockedList.add(runningProcess);
					System.out.println("Find I/O burst when blocking a process " + rand);
					runningProcess = null;
				}
				
			}
 
		} // end while
		
	}
	
	public static void uniprogramming(List<Process> processList, Scanner randomScanner) {
		Process.resetStatic();
		
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
				int rand = randomScanner.nextInt();
				int runTime = (rand % process.cpuBurstTime) + 1;
				
				process.cpuRunTime = runTime;
				
//				System.out.println("Find burst when choosing ready process to run " + rand);
				
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
//					System.out.println("Find I/O burst when blocking a process " + nextRand);
					
					// Check for ready cycles during every cycle.
					for (Process subProcess : processList) {
						if (subProcess.state.equals("ready")) {
							System.out.println(subProcess.index + "\t" + subProcess.waitingTime);
							subProcess.waitingTime++;
						}
							
						
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
		
		printInfo(processList);
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
