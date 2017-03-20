package ee.tartu.jpg.stuudium.data;

import java.util.Iterator;
import java.util.TreeSet;

import ee.tartu.jpg.stuudium.data.upper.StuudiumData;

public class DataSet<E extends StuudiumData> extends TreeSet<E> {

    private static final long serialVersionUID = -6388755479063983206L;

    @Override
    public boolean add(E obj) {
        if (this.contains(obj)) {
            int hash = obj.hashCode();
            Iterator<? extends E> e = iterator();
            while (e.hasNext()) {
                E prev = e.next();
                if (prev.hashCode() == hash) {
                    if (prev.isOlderThan(obj)) {
                        remove(obj);
                        return super.add(obj);
                    }
                    return false;
                }
            }
        }
        return super.add(obj);

    }

}
