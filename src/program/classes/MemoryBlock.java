package program.classes;

import java.util.Comparator;

public class MemoryBlock {
    public static Comparator<MemoryBlock> byEnd = Comparator.comparingInt(o -> o.end);
    int start;
    int end;

    public MemoryBlock(int start, int end) {
        this.start = start;
        this.end = end;
    }
}
