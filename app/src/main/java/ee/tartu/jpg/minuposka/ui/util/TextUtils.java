package ee.tartu.jpg.minuposka.ui.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.service.trigger.HomeworkNotificationScheduler;
import ee.tartu.jpg.minuposka.service.trigger.LessonNotificationScheduler;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.utils.DateUtils;

/**
 * Provides several text manipulation utils.
 */
public class TextUtils {

    private static final String TAG = "TextUtils";

    private static final Pattern httpUrlPattern = Pattern.compile("(https?|ftp):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])");
    private static final Pattern wwwUrlPattern = Pattern.compile("www.[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])");
    private static final Pattern emailPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    private static String lastTranslationLanguage = "";
    private static Map<String, String> translatedStrings = new HashMap<>();

    public static String colorize(Context context, String str, int colRes) {
        return String.format("<font color=\"#%s\">%s</font>", Integer.toHexString(ContextCompat.getColor(context, colRes)).substring(2), str);
    }

    public static Spanned html(String str) {
        if (!str.startsWith("<html>"))
            str = "<html>" + str;
        if (!str.endsWith("</html>"))
            str = str + "</html>";
        return Html.fromHtml(str);
    }

    public static String justify(String str) {
        return "<p align=\"justify\">" + str + "</p>";
    }

    public static String addAfter(String str, String add, String separator) {
        return (str == null || str.isEmpty()) ? add : (str + separator + add);
    }

    public static String addBefore(String str, String add, String separator) {
        return (str == null || str.isEmpty()) ? add : (add + separator + str);
    }

    public static String capitalize(String str) {
        if (str.length() <= 1)
            return str.toUpperCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static CharSequence linkify(CharSequence str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);
        Linkify.addLinks(ssb, emailPattern, "mailto:");
        Linkify.addLinks(ssb, httpUrlPattern, null);
        Linkify.addLinks(ssb, wwwUrlPattern, "http://");
        return ssb;
    }

    public static String boldify(String str) {
        return (str == null || str.isEmpty()) ? "" : ("<strong>" + str + "</strong>");
    }

    /**
     * Inject CSS method: read style.css from assets folder
     * Append stylesheet to document head
     */
    public static void injectCSS(Context context, String filename, WebView webView) {
        try {
            InputStream inputStream = context.getAssets().open(filename);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            webView.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject custom CSS", e);
        }
    }

    public static String translateFromEstonian(Context context, String str) {
        if (str == null || str.isEmpty())
            return "";

        String lang = DataUtils.getLanguage(context);
        if (lang.equals("et"))
            return str;
        if (lang.equals(lastTranslationLanguage)) {
            String translated = translatedStrings.get(str);
            if (translated != null) return translated;
        } else {
            translatedStrings.clear();
            lastTranslationLanguage = lang;
        }

        String[] trans = toTranslationFormat(str);
        String translated = translate(context, trans);
        if (Character.isUpperCase(str.codePointAt(0)))
            translated = translated.substring(0, 1).toUpperCase() + translated.substring(1);
        Log.v(TAG, "New translated str: " + translated);
        translatedStrings.put(str, translated);
        return translated;
    }

    private static String translate(Context context, String[] trans) {
        int iwc = getWordCount(trans);
        int wordCount = iwc;
        while (wordCount > 0) {
            for (int start = 0; start <= iwc - wordCount; start++) {
                String translated = getTranslated(context, toLookupFormat(trans, start, wordCount));
                if (translated != null) {
                    String result = "";
                    if (start != 0) {
                        result = translate(context, copyOfRange(trans, 0, start)) + " ";
                    }
                    String[] translatedSection = copyOfRange(trans, start, start + wordCount);
                    for (int i = 0; i < translatedSection.length; i++) {
                        if (!translatedSection[i].startsWith("["))
                            break;
                        result += translatedSection[i];
                    }
                    result += translated;
                    String endingSymbols = "";
                    for (int i = translatedSection.length - 1; i >= 0; i--) {
                        if (!translatedSection[i].startsWith("["))
                            break;
                        endingSymbols = translatedSection[i] + endingSymbols;
                    }
                    if (!endingSymbols.isEmpty()) {
                        result += " " + endingSymbols;
                    }
                    if (start + wordCount != iwc) {
                        result += " " + translate(context, copyOfRange(trans, start + wordCount, iwc));
                    }
                    return decode(result);
                }
            }
            wordCount--;
        }
        return decode(toReadableFormat(trans));
    }

    private static String decode(String str) {
        str = str.replace("[,]", ",").replace("[.]", ".").replace("[!]", "!").replace("[?]", "?").replace("[:]", ":").replace("[;]", ";").replace("[(]", "(").replace("[)]", ")");
        str = str.replace(" .", ".").replace(" !", "!").replace(" ?", "?").replace(" ,", ",").replace(" :", ":").replace(" ;", ";").replace("( ", "(").replace(" )", ")");
        return str;
    }

    private static String encode(String str) {
        str = str.replace(",", " [,] ").replace(".", " [.] ").replace("!", " [!] ").replace("?", " [?] ").replace(":", " [:] ").replace(";", " [;] ").replace("(", " [(] ").replace(")", " [)] ");
        return str;
    }

    private static String[] copyOfRange(String[] strs, int start, int end) {
        int startOffset = 0;
        if (start != 0)
            for (int i = 0; i <= start; i++)
                if (strs[i + startOffset].startsWith("[") && strs[i + startOffset].endsWith("]")) {
                    i--;
                    startOffset++;
                }
        int insideOffset = 0;
        for (int i = start, n = startOffset + start; i < end + 1; i++, n++) {
            if (strs[i + startOffset + insideOffset].startsWith("[") && strs[i + startOffset + insideOffset].endsWith("]")) {
                i--;
                insideOffset++;
            }
            if (n == strs.length - 1)
                break;
        }
        return Arrays.copyOfRange(strs, startOffset + start, startOffset + insideOffset + end);
    }

    private static String toReadableFormat(String[] strs) {
        String readable = "";
        for (String str : strs) {
            if (!readable.isEmpty()) {
                if (!readable.endsWith("("))
                    readable += " ";
            }
            readable += str;
        }
        return readable;
    }

    private static String[] toTranslationFormat(String str) {
        str = encode(str);
        while (str.contains("  ")) {
            str = str.replace("  ", " ");
        }
        return str.split(" ");
    }

    private static int getWordCount(String[] strs) {
        int count = 0;
        for (String str : strs) {
            if (str.startsWith("[") && str.endsWith("]"))
                continue;
            count++;
        }
        return count;
    }

    private static String toLookupFormat(String[] str, int start, int words) {
        String res = "";
        int offset = 0;
        for (int i = 0; i < start; i++)
            if (str[i + offset].startsWith("[") && str[i + offset].endsWith("]")) {
                i--;
                offset++;
            }

        for (int i = start; i < start + words; i++) {
            if (str[i + offset].startsWith("[") && str[i + offset].endsWith("]")) {
                i--;
                offset++;
                continue;
            }
            if (!res.isEmpty())
                res += "_";
            res += str[i + offset];
        }
        return res.toLowerCase().replace('õ', '6').replace('ä', '2').replace('ö', '8').replace('ü', 'y').replace('-', '_').replace('/', '_');
    }

    private static String getTranslated(Context context, String str) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier("data_" + str, "string", packageName);
        if (resId == 0)
            return null;
        try {
            return context.getString(resId);
        } catch (Resources.NotFoundException nfe) {
            Log.e(TAG, "Couldn't find resource: data_" + str, nfe);
            return null;
        }
    }

    public static String getDayStringResource(Context context, Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        int nickName = 0;
        if (DateUtils.isToday(cal)) {
            nickName = R.string.today;
        } else if (DateUtils.isWithinDaysFuture(cal, 1)) {
            nickName = R.string.tomorrow;
        } else if (DateUtils.isWithinDaysFuture(cal, 2)) {
            nickName = R.string.day_after_tomorrow;
        } else if (DateUtils.isWithinDaysPast(cal, 1)) {
            nickName = R.string.yesterday;
        } else if (DateUtils.isWithinDaysPast(cal, 2)) {
            nickName = R.string.day_before_yesterday;
        }

        int dayName = 0;
        int n = cal.get(Calendar.DAY_OF_WEEK);
        switch (n) {
            case Calendar.MONDAY:
                dayName = R.string.monday;
                break;
            case Calendar.TUESDAY:
                dayName = R.string.tuesday;
                break;
            case Calendar.WEDNESDAY:
                dayName = R.string.wednesday;
                break;
            case Calendar.THURSDAY:
                dayName = R.string.thursday;
                break;
            case Calendar.FRIDAY:
                dayName = R.string.friday;
                break;
            case Calendar.SATURDAY:
                dayName = R.string.saturday;
                break;
            case Calendar.SUNDAY:
                dayName = R.string.sunday;
                break;
        }
        if (nickName != 0) {
            return context.getString(nickName) + " - " + context.getString(dayName);
        }
        return context.getString(dayName);
    }

    public static int getExtraLabelColor(Event.ExtraLabel l) {
        switch (l.getLabelName()) {
            case "is_absent_excused":
            case "is_excused":
                return R.color.positive_grade;
            case "is_absent_unexcused":
            case "something_else":
                return R.color.negative_grade;
            case "is_late":
            case "is_no_homework":
            default:
                return R.color.warning_grade;
        }
    }

    public static void updateLanguage(Activity a) {
        if (a == null)
            return;
        Context context = a.getApplicationContext();
        Locale l;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            l = context.getResources().getConfiguration().getLocales().get(0);
        else l = context.getResources().getConfiguration().locale;

        String languageSelection = DataUtils.getLanguage(context);
        if (l != null && l.getLanguage().equalsIgnoreCase(languageSelection))
            return;

        Configuration cfg = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            cfg.setLocale(new Locale(languageSelection));
        else cfg.locale = new Locale(languageSelection);
        context.getResources().updateConfiguration(cfg, null);

        // Restart activity and notifications
        a.finish();
        Intent intent = a.getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        LessonNotificationScheduler.updateBroadcastReceiver(context);
        HomeworkNotificationScheduler.updateBroadcastReceiver(context);
    }

    public static void updateLanguageInContext(Context context) {
        String languageSelection = DataUtils.getLanguage(context);
        Configuration cfg = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            cfg.setLocale(new Locale(languageSelection));
        else cfg.locale = new Locale(languageSelection);
        context.getResources().updateConfiguration(cfg, null);
    }
}
