package ee.tartu.jpg.timetable.utils;

public interface Filter<T> {
	public boolean accept(T t);
}
