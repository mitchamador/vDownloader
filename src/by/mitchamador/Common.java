package by.mitchamador;

import by.mitchamador.parser.Parser;
import by.mitchamador.parser.ParserBase;
import by.mitchamador.parser.ParserEnum;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by vicok on 31.05.2016.
 */
public class Common {

    public static final int LOGLEVEL_NONE = 0;
    public static final int LOGLEVEL_DEFAULT = 1;
    public static final int LOGLEVEL_VERBOSE = 2;
    public static final int LOGLEVEL_DEBUG = 3;

    public int logLevel = LOGLEVEL_DEFAULT;

    public boolean dbList;

    public boolean test;

    public HashMap<String, ArrayList<UrlItem>> urlList;

    public String aria2Url = "http://192.168.1.2:6800/jsonrpc";

    public boolean forceDownload;

    public final static int TIMEOUT = 30 * 1000;

    public final static int BUFFER_SIZE = 16384;

    public Map<String, String> cookies;

    public Common() {
    }

    public void parseArgs(String[] args) {
        if (args != null) {
            urlList = new HashMap<String, ArrayList<UrlItem>>();
            int c = 0;
            while (c < args.length) {
                String arg = args[c].toLowerCase();
                if ("--url".equals(arg)) {
                    c++;
                    if (c < args.length) {
                        if (!urlList.containsKey(args[c])) {
                            urlList.put(args[c], new ArrayList<UrlItem>());
                        }

                        ArrayList<UrlItem> patternList = urlList.get(args[c]);

                        String pattern = "";
                        if (c + 2 < args.length && "--pattern".equals(args[c + 1])) {
                            c += 2;
                            pattern = args[c];
                        }

                        String dir = "";
                        if (c + 2 < args.length && "--dir".equals(args[c + 1])) {
                            c += 2;
                            dir = args[c];
                        }

                        patternList.add(new UrlItem(pattern, dir));
                    }
                } else if ("--quiet".equals(arg)) {
                    logLevel = LOGLEVEL_NONE;
                } else if ("--verbose".equals(arg)) {
                    logLevel = LOGLEVEL_VERBOSE;
                } else if ("--debug".equals(arg)) {
                    logLevel = LOGLEVEL_DEBUG;
                } else if (arg.startsWith("--") && arg.endsWith("login")) {
                    Parser tParser = findParser(arg.substring(2, arg.length() - 5));
                    c++;
                    if (c < args.length) {
                        if (tParser != null) {
                            ((ParserBase) tParser).login = args[c];
                        }
                    }
                } else if (arg.startsWith("--") && arg.endsWith("password")) {
                    Parser tParser = findParser(arg.substring(2, arg.length() - 8));
                    c++;
                    if (c < args.length) {
                        if (tParser != null) {
                            ((ParserBase) tParser).password = args[c];
                        }
                    }
                } else if ("--test".equals(arg)) {
                    logLevel = LOGLEVEL_VERBOSE;
                    test = true;
                } else if ("--aria2rpc".equals(arg)) {
                    c++;
                    if (c < args.length) {
                        aria2Url = args[c];
                    }
                } else if ("--list".equals(arg)) {
                    dbList = true;
                } else if ("--forcedownload".equals(arg)) {
                    forceDownload = true;
                }
                c++;
            }

            if (urlList.isEmpty()) {
                urlList.put("http://rutor.org/torrent/471190/beguwij-v-labirinte-ispytanie-ognjom_maze-runner-the-scorch-trials-2015-hdrip-ot-scarabey-licenzija", new ArrayList<UrlItem>(Arrays.asList(new UrlItem[] {new UrlItem("", "")})));

                logLevel = LOGLEVEL_VERBOSE;
                test = true;
            }
        }
    }

    private Parser findParser(String name) {
        for (ParserEnum parserEnum : ParserEnum.values()) {
            Parser tParser = parserEnum.getParser();
            if (name.equals(((ParserBase) tParser).name)) {
                return tParser;
            }
        }
        return null;
    }

    public void log(int logLevel, String message) {
        if (this.logLevel >= logLevel) {
            System.out.println(SimpleDateFormat.getDateTimeInstance().format(new Date()) + " " + message);
        }
    }

    public void saveFile(String filePath, byte[] buffer) throws IOException {
        ByteArrayInputStream bi = new ByteArrayInputStream(buffer);
        FileOutputStream fo = new FileOutputStream(filePath);

        byte[] buf = new byte[262144];
        int x;
        while ((x = bi.read(buf)) != -1)
            fo.write(buf, 0, x);
        fo.flush();
        fo.close();
    }


}
