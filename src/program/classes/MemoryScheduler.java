package program.classes;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class MemoryScheduler {
    private final int m_accessibleMemory;
    private final ArrayList<MemoryBlock> m_memoryBlocks;

    public MemoryScheduler(int accessibleMemory) {
        this.m_accessibleMemory = accessibleMemory;
        m_memoryBlocks = new ArrayList<>();
    }

    public MemoryBlock aloc(int size) {
        if (size > m_accessibleMemory) return null;

        int start = getAlocPosition(size);
        if (start == -1) return null;

        MemoryBlock result = new MemoryBlock(start, start + size - 1);
        if (indexBlock(result)) return result;

        return null;
    }

    public void del(MemoryBlock block) {
        if (block == null) return;
        m_memoryBlocks.remove(block);
    }

    /**
     * Returns the start of free block with size equal or greater than given size.
     * and less suitable place to put new block
     */
    private int getAlocPosition(int size) {
        if (size > m_accessibleMemory) return -1;

        if (m_memoryBlocks.isEmpty()) {
            //less suitable place to put first block (in the middle of all memory)
            return (m_accessibleMemory - size) / 2;
        }
        m_memoryBlocks.sort(MemoryBlock.byEnd);
        //pair<position, size>
        ArrayList<Pair<Integer, Integer>> windows = new ArrayList<>();
        windows.add(new Pair<>(0, m_memoryBlocks.get(0).start));
        int lastWindowStart = m_memoryBlocks.get(m_memoryBlocks.size() - 1).end + 1;
        windows.add(new Pair<>(lastWindowStart, m_accessibleMemory - lastWindowStart));

        //finding block with required size using the 'less suitable' strategy
        for (int i = 0; i < m_memoryBlocks.size() - 1; i++) {
            int windowStart = m_memoryBlocks.get(i).end;
            windows.add(new Pair<>(windowStart, m_memoryBlocks.get(i + 1).start - windowStart));
        }

        Optional<Pair<Integer, Integer>> p = windows.stream().max(Comparator.comparingInt(Pair::getValue));
        if (p.isPresent()) {
            if (p.get().getValue() - size >= 0) {
                //less suitable place to put block (in the middle of biggest memory window)
                //only 50% of memory will be used in average)
                return p.get().getKey() + ((p.get().getValue() - size) / 2);
            }
        }

        return -1;
    }

    private boolean indexBlock(MemoryBlock block) {
        if (block == null) return false;
        return m_memoryBlocks.add(block);
    }

    public int getMemoryUsage() {
        int busy = 0;
        for (MemoryBlock block : m_memoryBlocks) {
            busy += block.end - block.start;
        }
        return busy;
    }
}
