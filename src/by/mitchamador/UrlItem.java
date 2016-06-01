package by.mitchamador;

import java.io.Serializable;

/**
 * Created by Viktar on 22.11.2015.
 */
public class UrlItem implements Serializable {


    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDir() {
        return dir;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTorrent() {
        return torrent;
    }

    public void setTorrent(String torrent) {
        this.torrent = torrent;
    }

    public long getHash() {
        return torrent != null ? hash(torrent) : hash(url);
    }

    public String name;

    private String pattern;
    private String dir;
    private String url;
    private String torrent;

    public UrlItem(String pattern, String dir) {
        this.pattern = pattern;
        this.dir = dir;
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
}

