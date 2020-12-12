package program.classes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class PrioritizedQueue {
    private final ArrayList<Queue<Process>> m_queue;

    public PrioritizedQueue(final int maxPriority) {
        m_queue = new ArrayList<>();
        for (int i = 0; i < maxPriority; ++i) {
            m_queue.add(new LinkedList<>());
        }
    }

    public boolean push(Process process) {
        int priority = process.getPriority();
        if (priority < m_queue.size()) {
            //higher priority - lower index in list
            m_queue.get(m_queue.size() - priority - 1).add(process);
            return true;
        }
        return false;
    }

    public Process pop() {
        for (Queue<Process> q : m_queue) {
            if (!q.isEmpty()) {
                return q.poll();
            }
        }
        return null;
    }

    public Process peek() {
        for (Queue<Process> q : m_queue) {
            if (!q.isEmpty()) {
                return q.peek();
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return getList().isEmpty();
    }

    public void clear() {
        for (Queue<Process> q : m_queue) {
            q.clear();
        }
    }

    public void push() {
        push(new Process());
    }

    public void push(int count) {
        for (int i = 0; i < count; i++) {
            push();
        }
    }

    public String getInfo() {
        return "";
    }

    public ArrayList<Process> getList() {
        ArrayList<Process> result = new ArrayList<>();
        for (Queue<Process> q : m_queue) {
            for (Process p : q) {
                result.add(p);
            }
        }
        return result;
    }
}
