package hywt.mandel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class TightFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        // convert millis to date
        Date date = new Date(record.getMillis());
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        return String.format("[%s][%s] %s\n", formattedDate, record.getLevel(), record.getMessage());
    }
}
