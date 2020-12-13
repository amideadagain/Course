package program.classes;

import program.Configuration;
import program.Controller;
import program.Main;
import program.util.ITickNotifier;

import java.util.ArrayList;
import java.util.Random;

public class TaskScheduler implements ITickNotifier {
    private final CPU cpu;
    private final MemoryScheduler memoryScheduler;

    private final PrioritizedQueue processQueue;
    private final ArrayList<Process> completedList;
    private final ArrayList<Process> rejectsList;

    private final Random random = new Random();

    private int lastId = 1;

    private int tasksFinished = 0;
    private int tasksRejected = 0;

    public TaskScheduler(CPU cpu, int systemMemory) {
        this.cpu = cpu;
        memoryScheduler = new MemoryScheduler(systemMemory);

        processQueue = new PrioritizedQueue(Configuration.PROCESS_MAX_PRIORITY);
        completedList = new ArrayList<>();
        rejectsList = new ArrayList<>();

        memoryScheduler.aloc(Configuration.OS_MEMORY_USAGE);
        Main.guiController.updateMemoryUsage();
    }

    /**
     * распределяем процессы с очереди в процессор, генерируем новые процессы
     */
    @Override
    public void tick(int time) {
        Process nextProcess = processQueue.peek();
        if (nextProcess != null) {
            if (cpu.hasAvailableCore()) {
                processQueue.pop();
                cpu.execProcess(nextProcess);
            } else {
                Core lowestPriorityCore = cpu.getCore(cpu.getLowPrioCoreIndx());
                if (nextProcess.getPriority() < lowestPriorityCore.getCurrentProcess().getPriority()) {
                    processQueue.pop();
                    lowestPriorityCore.switchExecProcess(nextProcess);
                }
            }
        }
        if (Configuration.randomProcessGenerationEnabled()) {
            if (random.nextInt(10) == 0) {
                scheduleRandom();
            }
        }

        //printTasks();
    }

    public void scheduleTask(String name) {
        Process task = new Process(name);
        scheduleTask(task);
    }

    public boolean scheduleTask(Process task) {
        //re-add the task from resource queue to cpu queue
        if (task.getState() == Process.State.WAITING) {
            int additionalTime = task.getTimeRequired() - task.getBurstTime();
            additionalTime = Math.floorDiv(additionalTime, 100) * random.nextInt(16) + 5;
            task.increaseRequiredTime(additionalTime);
            task.setInterruptionReason("");

            processQueue.push(task);
            task.setState(Process.State.READY);
            task.setResource("");
            Main.guiController.updateCPUQueue();

            return true;
        } else if (task.getState() == Process.State.READY) {
            if (task.getBurstTime() > 0) {
                processQueue.push(task);
                Main.guiController.updateCPUQueue();

                return true;
            }
        }

        MemoryBlock memory = memoryScheduler.aloc(task.getMemoryUsage());
        if (memory == null) {
            rejectProcess(task);
            return false;
        }

        task.setLocationInMemory(memory);
        processQueue.push(task);
        task.setState(Process.State.READY);
        Main.guiController.updateMemoryUsage();

        return true;
    }

    public void scheduleRandom() {
        Process task = new Process();
        scheduleTask(task);
    }

    public void freeMemoryBlock(MemoryBlock block) {
        memoryScheduler.del(block);
        Main.guiController.updateMemoryUsage();
    }

    public void addProcessToCompleted(Process process) {
        completedList.add(process);
        tasksFinished++;
        Main.guiController.updateTable(Controller.Tables.FINISHED);
        Main.guiController.updateTasksFinished();
    }

    public void rejectProcess(Process process) {
        rejectsList.add(process);
        tasksRejected++;
        Main.guiController.updateTable(Controller.Tables.REJECTED);
        Main.guiController.updateTasksRejected();
    }

    public void finishWork() {
        for (Resource r : Main.getSystemResources()) {
            r.shutdown();
        }
        processQueue.clear();
        cpu.shutdown();
    }

    public ArrayList<Process> getCPUTaskList() {
        ArrayList<Process> result = new ArrayList<>();
        result.addAll(cpu.getCoresContent());
        result.addAll(processQueue.getList());

        return result;
    }

    public ArrayList<Process> getResourceTaskList(int resourceIndex) {
        return Main.getSystemResources().get(resourceIndex).getTaskList();
    }

    public ArrayList<Process> getRejectsList() {
        return rejectsList;
    }

    public ArrayList<Process> getCompletedList() {
        return completedList;
    }

    public ArrayList<Process> getResourcesContent() {
        ArrayList<Process> result = new ArrayList<>();
        result.addAll(cpu.getCoresContent());

        for (Resource r : Main.getSystemResources()) {
            result.add(r.getCurrentProcess());
        }

        return result;
    }

    public void printTasks() {
        System.out.println("==========================================================================");
        System.out.println("CPU: " + getCPUTaskList().toString());
        for (Resource r : Main.getSystemResources()) {
            System.out.println(" " + r.getName() + ": " + r.getTaskList().toString());
        }
        System.out.println("Rej: " + getRejectsList().toString());
        System.out.println("Fin: " + getCompletedList().toString());
    }

    public int getLastId() {
        return lastId;
    }

    public void incrementLastId() {
        lastId++;
        Main.guiController.updateTasksTotal();
    }

    /*--Statistics--*/
    public int getTasksFinished() {
        return tasksFinished;
    }

    public int getTasksRejected() {
        return tasksRejected;
    }

    public int getQueueLength() {
        return processQueue.getList().size();
    }

    public int getCPUInactivity() {
        return cpu.getM_inactivityTicks();
    }

    public int getMemoryUsage() {
        return memoryScheduler.getMemoryUsage();
    }
}
