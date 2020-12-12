package program.classes;

import program.Configuration;
import program.Controller;
import program.Main;
import program.util.NameGenerator;

import java.util.Random;

public class Process {
    private final int m_id;
    private final String m_name;
    private final int m_priority;
    private final int m_memoryUsage;
    private final int m_startTime;

    private int m_requiredTime;
    private int m_burstTime;
    private State m_state;
    private String m_interruptionReason = "";

    private MemoryBlock m_memoryBlock;

    private String m_resource = "";

    private final Random m_random = new Random();

    public Process(String name) {
        this.m_name = name;
        this.m_id = Main.getTaskScheduler().getLastId();
        Main.getTaskScheduler().incrementLastId();
        this.m_startTime = Main.getSystemTime();

        m_requiredTime = m_random.nextInt(90) + 10;
        m_memoryUsage = m_random.nextInt(Configuration.PROCESS_MAX_MEMORY_USAGE - Configuration.PROCESS_MIN_MEMORY_USAGE) + Configuration.PROCESS_MIN_MEMORY_USAGE;
        m_priority = m_random.nextInt(Configuration.PROCESS_MAX_PRIORITY - 1) + 1;

        m_burstTime = 0;
        m_state = State.READY;
    }

    public Process() {
        this(NameGenerator.generate());
    }

    public String getInfo() {
        return "";
    }

    public boolean isFinished() {
        return this.m_state == State.FINISHED;
    }

    public void increaseBurstTime(int ticks) {
        m_burstTime += ticks;
    }

    public void increaseRequiredTime(int ticks) {
        this.m_requiredTime += ticks;
    }

    @Override
    public String toString() {
        return "Process " + m_id +
                '{' +
                "name='" + m_name + '\'' +
                '}';
    }

    /**
     * --GETTERS--
     */
    public int getId() {
        return m_id;
    }

    public String getName() {
        return m_name;
    }

    public int getPriority() {
        return m_priority;
    }

    public int getMemoryUsage() {
        return m_memoryUsage;
    }

    public int getTimeRequired() {
        return m_requiredTime;
    }

    public int getStartTime() {
        return m_startTime;
    }

    public int getBurstTime() {
        return m_burstTime;
    }

    public State getState() {
        return m_state;
    }

    /**
     * --SETTERS--
     */
    public void setState(State m_state) {
        if (m_state == State.FINISHED || m_state == State.TERMINATED) {
            Main.getTaskScheduler().freeMemoryBlock(m_memoryBlock);
            Main.getTaskScheduler().addProcessToCompleted(this);
        } else if (m_state == State.RUNNING || m_state == State.READY) {
            Main.guiController.updateTable(Controller.Tables.RUNNING);
        }

        this.m_state = m_state;
    }

    public MemoryBlock getLocationInMemory() {
        return m_memoryBlock;
    }

    public void setLocationInMemory(MemoryBlock memoryBlock) {
        this.m_memoryBlock = memoryBlock;
    }

    public String getInterruptionReason() {
        return m_interruptionReason;
    }

    public void setInterruptionReason(String value) {
        this.m_interruptionReason = value;
    }

    public String getResource() {
        return m_resource;
    }

    public void setResource(String resourceName) {
        m_resource = resourceName;
    }

    public enum State {
        READY,      //waiting for CPU
        WAITING,    //waiting for resource
        RUNNING,    //processing
        FINISHED,   //completed
        TERMINATED  //aborted
    }
}
