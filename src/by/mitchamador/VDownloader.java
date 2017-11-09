package by.mitchamador;

import by.mitchamador.parser.Parser;
import by.mitchamador.parser.ParserEnum;
import org.tmatesoft.sqljet.core.SqlJetException;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static by.mitchamador.Common.LogLevel.LOGLEVEL_DEBUG;
import static by.mitchamador.Common.LogLevel.LOGLEVEL_VERBOSE;

public class VDownloader {

    private Common common;

    public VDownloader(String[] args) throws Exception {
        common = new Common();
        common.parseArgs(args);
    }

    public void run() throws Exception {

        try {
            common.log(LOGLEVEL_DEBUG, "=== started ===");

            UrlItemDB.open();

            if (common.dbList) {
                ArrayList<UrlItem> items = UrlItemDB.getAllItemsList();
                for (UrlItem item : items) {
                    System.out.println(item.getDateString() + " " + item.name + " " + item.url);
                }
                return;
            }

            if (common.urlList == null) return;

            for (String contentUrl : common.urlList.keySet()) {
                if (contentUrl == null) continue;

                try {

                    Parser parser = null;
                    for (ParserEnum parserEnum : ParserEnum.values()) {
                        if (parserEnum.getParser().match(contentUrl)) {
                            parser = parserEnum.getParser();
                            break;
                        }
                    }

                    if (parser == null) continue;

                    if (parser.getCommon() == null) parser.setCommon(common);

                    ArrayList<UrlItem> urlItemList = common.urlList.get(contentUrl);

                    if (urlItemList.isEmpty()) {
                        urlItemList.add(new UrlItem(".*", ""));
                    } else {
                        int c = 0;
                        while (c < urlItemList.size()) {
                            UrlItem urlItem = urlItemList.get(c);

                            if (urlItem.pattern == null || urlItem.pattern.isEmpty()) continue;

                            try {
                                Pattern.compile(urlItem.pattern);
                            } catch (PatternSyntaxException e) {
                                common.log(LOGLEVEL_VERBOSE, "error parsing pattern: " + urlItem.pattern);
                                urlItemList.remove(c--);
                            }

                            c++;
                        }
                    }

                    if (urlItemList.isEmpty()) continue;

                    ArrayList<String[]> list = parser.parse(contentUrl);

                    if (list == null) continue;

                    for (String[] s : list) {

                        s[0] = s[0].replace("\n", "").trim();

                        for (UrlItem urlItem : urlItemList) {
                            if (urlItem.pattern == null || urlItem.pattern.isEmpty() || s[0].matches(urlItem.pattern)) {
                                urlItem.name = s[0];

                                try {
                                    if (s[2] != null && !s[2].isEmpty()) {
                                        urlItem.url = (s[1] == null || s[1].isEmpty()) ? contentUrl : s[1];
                                        urlItem.torrent = s[2];
                                        Aria2.sendToAria2(common, urlItem, parser);
                                    } else {
                                        ArrayList<String[]> sList = parser.parse(s[1]);
                                        if (sList == null) {
                                            common.log(LOGLEVEL_VERBOSE, "error parsing url (no matching item): " + s[1]);
                                        } else {
                                            for (String[] s2 : sList) {
                                                urlItem.url = s2[1];
                                                urlItem.torrent = s2[2];
                                                Aria2.sendToAria2(common, urlItem, parser);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    common.log(LOGLEVEL_VERBOSE, e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    common.log(LOGLEVEL_VERBOSE, "error parsing url: " + contentUrl);
                }
            }
        } finally {
            try {
                UrlItemDB.close();
            } catch (SqlJetException e) {
                e.printStackTrace();
            }
            common.log(LOGLEVEL_DEBUG, "=== ended ===");
        }
    }

    public static void main(String[] args) {
        try {
            VDownloader downloader = new VDownloader(args);
            downloader.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
