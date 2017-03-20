package ee.tartu.jpg.stuudium.data.upper;

public abstract class StuudiumData extends SubStuudiumData implements Comparable<StuudiumData> {

    private long initTime;

    protected StuudiumData() {
        this.initTime = System.currentTimeMillis();
    }

    public boolean isOlderThan(StuudiumData sd) {
        return initTime < sd.initTime;
    }
}
