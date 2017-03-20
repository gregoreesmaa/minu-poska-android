package ee.tartu.jpg.stuudium.data.upper;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public abstract class SubStuudiumData {

    @SuppressLint("SimpleDateFormat")
    protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @SuppressLint("SimpleDateFormat")
    protected static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @SuppressLint("SimpleDateFormat")
    public static final DateFormat dateOnlyFormat = new SimpleDateFormat("dd.MM.yyyy");

    @SuppressLint("SimpleDateFormat")
    protected static final DateFormat timeOnlyFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public abstract int hashCode();
}
