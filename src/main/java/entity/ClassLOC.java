package entity;

public class ClassLOC {
    private final String className;
    private final int loc;

    public ClassLOC(String fileName, int loc) {
        this.className = fileName;
        this.loc = loc;
    }

    public String getFileName() {
        return className;
    }

    public int getLoc() {
        return loc;
    }
}