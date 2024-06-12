package entity;

public class ClassLOC {
    private String className;
    private int loc;

    public ClassLOC(String fileName, int loc) {
        this.className = fileName;
        setLoc(loc);
    }

    public String getFileName() {
        return className;
    }

    public int getLoc() {
        return loc;
    }

    private void setLoc(int loc){
        this.loc = loc;
    }
}