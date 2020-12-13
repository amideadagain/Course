package program.classes;

import program.Configuration;
import program.Controller;
import program.Main;
import program.util.ITickNotifier;

import java.util.Random;

public class Core implements ITickNotifier {
    private final CPU m_cpu;
    private final Random m_random = new Random();
    private boolean m_busy = false;
    private Process m_currentProcess;

    public Core(CPU parent) {
        this.m_cpu = parent;
    }

    /**
     * если есть установленый процесс - выполнять его
     * в некоторой вероятностью отправить на выполнение в ресурс
     * и/или симулировать исключение
     */
    @Override
    public void tick(int time) {
        if (m_currentProcess == null) return;
        if (m_busy) {
            m_currentProcess.increaseBurstTime(1);

            //randomly trying to send the process to the random resource
            int percent = Math.round(m_currentProcess.getTimeRequired() * 0.01f);
            if (m_currentProcess.getBurstTime() > percent * 10 + 5) {
                if (m_random.nextInt(m_currentProcess.getBurstTime()) < percent * 4) {
                    Resource r = Main.getSystemResources().get(m_random.nextInt(Configuration.getResourcesCount()));

                    r.scheduleProcess(m_currentProcess);
                    this.m_currentProcess = null;
                    m_busy = false;
                    return;
                }
            }

            if (Configuration.runtimeErrorsEnabled() && m_random.nextInt(Configuration.getProcessTerminationChance()) == 0) {
                throwException();
                return;
            }

            if (m_currentProcess.getTimeRequired() <= m_currentProcess.getBurstTime()) {
                stopProcess("Completed.");
            }

            Main.guiController.updateTable(Controller.Tables.RESOURCES);
        }
    }

    /**
     * назначить процесс для выполнения
     * @param process
     */
    public void execProcess(Process process) {
        m_currentProcess = process;
        m_currentProcess.setResource("CPU");
        m_currentProcess.setState(Process.State.RUNNING);
        m_busy = true;
        Main.guiController.updateTable(Controller.Tables.RESOURCES);
    }

    /**
     * завершить процесс
     */
    private void stopProcess() {
        if (m_currentProcess.getBurstTime() < m_currentProcess.getTimeRequired()) {
            m_currentProcess.setState(Process.State.TERMINATED);
        } else
            m_currentProcess.setState(Process.State.FINISHED);

        this.m_currentProcess = null;
        m_busy = false;
    }

    /**
     * завершить процесс
     */
    public void stopProcess(String reason) {
        m_currentProcess.setInterruptionReason(reason);
        m_currentProcess.setResource("");
        stopProcess();
    }

    /**
     * приостановить выполнение текущего процесса и начать другой
     */
    public void switchExecProcess(Process newProcess) {
        m_currentProcess.setState(Process.State.READY);
        Main.getTaskScheduler().scheduleTask(m_currentProcess);
        execProcess(newProcess);
    }

    public Process getCurrentProcess() {
        return m_currentProcess;
    }

    public void throwException() {
        stopProcess("Runtime Error (CPU)");
    }

    public boolean isBusy() {
        return m_busy;
    }
}
