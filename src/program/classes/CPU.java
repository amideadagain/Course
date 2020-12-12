package program.classes;

import program.Main;
import program.util.ITickNotifier;

import java.util.ArrayList;

public class CPU implements ITickNotifier {
    private final Core[] m_cores;

    private int m_inactivityTicks = 0;

    public CPU(final int coresNumber) {
        m_cores = new Core[coresNumber];
        for (int i = 0; i < coresNumber; i++) {
            m_cores[i] = new Core(this);
        }
    }

    @Override
    public void tick(int time) {
        int freeCores = 0;
        for (Core core : m_cores) {
            core.tick(time);
            if (!core.isBusy()) freeCores++;
        }
        if (freeCores == m_cores.length) {
            m_inactivityTicks++;
            Main.guiController.updateCPUInactivity();
        }
    }

    public boolean execProcess(Process process) {
        Core core = getAvailableCore();
        if (core != null) {
            //System.out.println(process.toString() + " running on " + core);

            core.execProcess(process);
            return true;
        }
        return false;
    }

    public boolean stopProcess(Process process, String reason) {
        for (Core core : m_cores) {
            if (core.isBusy()) {
                if (core.getCurrentProcess().equals(process)) {
                    core.stopProcess(reason);
                    return true;
                }
            }
        }

        return false;
    }

    public void shutdown() {
        for (Core core : m_cores) {
            if (core.isBusy()) {
                core.stopProcess("CPU shutdown.");
            }
        }
    }

    public Core getAvailableCore() {
        for (Core core : m_cores) {
            if (!core.isBusy()) return core;
        }
        return null;
    }

    public int getLowPrioCoreIndx() {
        int lowestPriorityIndex = 0;
        for (int i = 1; i < m_cores.length; i++) {
            if (m_cores[i].getCurrentProcess().getPriority() > m_cores[lowestPriorityIndex].getCurrentProcess().getPriority()) {
                lowestPriorityIndex = i;
            }
        }
        return lowestPriorityIndex;
    }

    public Core getCore(int index) {
        if (index > m_cores.length || index < 0) return null;
        return m_cores[index];
    }

    public boolean hasAvailableCore() {
        return getAvailableCore() != null;
    }

    public int getCoresCount() {
        return m_cores.length;
    }

    public ArrayList<Process> getCoresContent() {
        ArrayList<Process> result = new ArrayList<>();
        for (Core core : m_cores) {
            if (core.getCurrentProcess() != null)
                result.add(core.getCurrentProcess());
        }

        return result;
    }

    public int getM_inactivityTicks() {
        return m_inactivityTicks;
    }
}
