package by.mitchamador;

import by.mitchamador.parser.Parser;
import by.mitchamador.parser.ParserInterface;
import by.mitchamador.parser.ParserEnum;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tmatesoft.sqljet.core.SqlJetException;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class VDownloader {

    private Common common;

    public VDownloader(String[] args) throws Exception {
        common = new Common();
        common.parseArgs(args);
    }

    public void run() throws Exception {

        try {
            common.log(Common.LOGLEVEL_VERBOSE, "=== started ===");

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

                    ArrayList<UrlItem> urlItemList = common.urlList.get(contentUrl);

                    if (urlItemList.isEmpty()) {
                        urlItemList.add(new UrlItem(".*", ""));
                    } else {
                        for (UrlItem urlItem : urlItemList) {
                            if (urlItem.pattern == null || urlItem.pattern.isEmpty()) continue;

                            if (urlItem.pattern.contains("%DATE1%")) {
                                urlItem.pattern = urlItem.pattern.replace("%DATE1%", new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
                            }
                            if (urlItem.pattern.contains("%DOM2SERIE%")) {
                                Calendar start = Calendar.getInstance();
                                start.clear();
                                start.set(2004, Calendar.MAY, 11);
                                long number = 1 + (new Date().getTime() - start.getTimeInMillis()) / (24 * 60 * 60 * 1000);
                                urlItem.pattern = urlItem.pattern.replace("%DOM2SERIE%", number + "");
                            }
                        }
                    }

                    Parser parser = null;
                    for (ParserEnum parserEnum : ParserEnum.values()) {
                        if (parserEnum.getParser().getParser().match(contentUrl)) {
                            parser = parserEnum.getParser();
                            break;
                        }
                    }

                    if (parser == null) continue;

                    // make sure cookies is turn on
                    CookieHandler.setDefault(new CookieManager());

                    parser.setCookies();
                    common.cookies = parser.getParser().login(common);

                    if (parser.loggedIn && (common.cookies == null || common.cookies.isEmpty())) {
                        common.log(Common.LOGLEVEL_DEFAULT, "empty cookies, " + parser.name + " login failed");
                    }

                    Document doc = getDocument(contentUrl, parser.getParser());

                    ArrayList<String[]> list = parser.getParser().parse(contentUrl, doc);

                    if (list == null) continue;

                    for (String[] s : list) {

                        for (UrlItem urlItem : urlItemList) {

                            if (urlItem.pattern == null || urlItem.pattern.isEmpty() || s[0].replace("\n", "").matches(urlItem.pattern)) {
                                urlItem.name = s[0].replace("\n", "");
                                try {
                                    if (s[2] != null && !s[2].isEmpty()) {
                                        urlItem.url = (s[1] == null || s[1].isEmpty()) ? contentUrl : s[1];
                                        urlItem.torrent = s[2];
                                        Aria2.sendToAria2(common, urlItem);
                                    } else {
                                        for (String[] s2 : parser.getParser().parseTopic(getDocument(s[1], parser.getParser()))) {
                                            urlItem.url = s2[1];
                                            urlItem.torrent = s2[2];
                                            Aria2.sendToAria2(common, urlItem);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    common.log(Common.LOGLEVEL_DEFAULT, "error parsing url: " + contentUrl);
                }
            }
        } finally {
            try {
                UrlItemDB.close();
            } catch (SqlJetException e) {
                e.printStackTrace();
            }
            common.log(Common.LOGLEVEL_VERBOSE, "=== ended ===");
        }
    }

    private Document getDocument(String contentUrl, ParserInterface parser) throws Exception {

        Connection con = Jsoup
                .connect(contentUrl)
                .timeout(Common.TIMEOUT);

        if (common.cookies != null) {
            con = con.cookies(common.cookies);
        }

        if (parser.getUserAgent() != null) {
            con = con.userAgent(parser.getUserAgent());
        }

        return con.get();
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
