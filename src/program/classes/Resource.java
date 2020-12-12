package program.classes;

import program.Configuration;
import program.Controller;
import program.Main;
import program.util.ITickNotifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Resource implements ITickNotifier {
    private final String m_name;
    private final Queue<Process> m_queue;
    private Status m_status;

    private int m_processTime;
    private Process m_currentProcess;
    private int m_timer;

    private final Random m_random = new Random();

    public Resource(String name) {
        this.m_name = name;
        m_queue = new LinkedList<>();
        m_status = Status.READY;
    }

    @Override
    public void tick(int time) {
        if (m_queue.isEmpty()) return;
        if (m_status == Status.READY) {
            if (execProcess(m_queue.poll())) {
                setStatus(Status.BUSY);
            }
        } else if (m_status == Status.BUSY) {
            if (m_timer < m_processTime) {
                if (Configuration.runtimeErrorsEnabled() && m_random.nextInt(Configuration.getProcessTerminationChance()) == 0)
                    throwException();

                m_timer++;
            } else {
                sendProcessToCPU();
                setStatus(Status.READY);
            }
        }

        Main.guiController.updateTable(Controller.Tables.RESOURCES);
    }

    public void scheduleProcess(Process process) {
        process.setState(Process.State.WAITING);
        m_queue.add(process);
    }

    private boolean execProcess(Process process) {
        if (m_status == Status.BUSY || process == null) return false;

        m_timer = 0;
        m_currentProcess = process;
        m_currentProcess.setResource(m_name);
        process.setState(Process.State.WAITING);
        m_processTime = Math.floorDiv(process.getTimeRequired(), 100) * m_random.nextInt(20) + 5;

        Main.guiController.updateTable(Controller.Tables.RESOURCES);

        return true;
    }

    public String getName() {
        return m_name;
    }

    public String getInfo() {
        return "";
    }

    public Status getStatus() {
        return m_status;
    }

    public void setStatus(Status value) {
        this.m_status = value;
    }

    public void sendProcessToCPU() {
        m_currentProcess.setResource("");
        Main.getTaskScheduler().scheduleTask(m_currentProcess);
        m_status = Status.READY;
    }

    public void throwException() {
        m_currentProcess.setState(Process.State.TERMINATED);
        m_currentProcess.setInterruptionReason("Runtime Error (" + m_name + ")");
        setStatus(Status.READY);
    }

    public ArrayList<Process> getTaskList() {
        return new ArrayList(m_queue);
    }

    public Process getCurrentProcess() {
        return m_currentProcess;
    }

    public void shutdown() {
        m_queue.clear();
        m_currentProcess = null;
    }

    public enum Status {
        READY,
        BUSY
    }
}


