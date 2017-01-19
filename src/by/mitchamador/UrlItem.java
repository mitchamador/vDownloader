package by.mitchamador;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Viktar on 22.11.2015.
 */
public class UrlItem implements Serializable {

    public long getHash() {
        return hash(torrent);
    }

    public String getDateString() {
        return SimpleDateFormat.getDateTimeInstance().format(new Date(this.date));
    }

    public long rowID;
    public long date;
    public String name;
    public String pattern;
    public String dir;
    public String url;
    public String torrent;
    public long hash;

    public static UrlItem read(ISqlJetCursor cursor) throws SqlJetException {
        return new UrlItem(cursor.getRowId(), cursor.getInteger("date"), cursor.getString("name"), "", "", cursor.getString("url"), "", cursor.getInteger("hash"));
    }

    public UrlItem(String pattern, String dir) {
        this(-1, new Date().getTime(), "", pattern, dir, "", "", 0);
    }

    public UrlItem(long rowID, long date, String name, String pattern, String dir, String url, String torrent, long hash) {
        this.rowID = rowID;
        this.date = date;
        this.name = name;
        this.pattern = pattern;
        this.dir = dir;
        this.url = url;
        this.torrent = torrent;
        this.hash = hash;
    }

    private static final long[] byteTable;
    private static final long HSTART = 0xBB40E64DA205B064L;
    private static final long HMULT = 7664345821815920749L;

    static {
        byteTable = new long[256];
        long h = 0x544B2FBACAAF1684L;
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 31; j++) {
                h = (h >>> 7) ^ h;
                h = (h << 11) ^ h;
                h = (h >>> 10) ^ h;
            }
            byteTable[i] = h;
        }
    }

    private long hash(String str) {
        if (str == null) return 0;

        byte[] data = str.getBytes();
        long h = HSTART;
        final long hmult = HMULT;
        final long[] ht = byteTable;
        for (int len = data.length, i = 0; i < len; i++) {
            h = (h * hmult) ^ ht[data[i] & 0xff];
        }
        return h;
    }

    public static String filterTags(String pattern) {
        if (pattern.contains("%DATE1%")) {
            pattern = pattern.replace("%DATE1%", new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
        }
        if (pattern.contains("%DOM2SERIE%")) {
            Calendar start = Calendar.getInstance();
            start.clear();
            start.set(2004, Calendar.MAY, 11);
            long number = 1 + (new Date().getTime() - start.getTimeInMillis()) / (24 * 60 * 60 * 1000);
            pattern = pattern.replace("%DOM2SERIE%", number + "");
        }
        if (pattern.contains("%SERIESPATTERN%")) {
            pattern = pattern.replace("%SERIESPATTERN%", "\\[(S[0-9]{2}|([0-9]{2}(x|�))?(01((-[0-9]{2})? из [0-9]{2})?)?)]");
        }
        return pattern;
    }
}

