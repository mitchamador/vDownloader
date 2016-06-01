package by.mitchamador;

import by.mitchamador.parser.Parser;
import by.mitchamador.parser.ParserEnum;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mapdb.DBMaker;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class VDownloader {

    private Common common;

    public VDownloader(String[] args) throws Exception {

        common = new Common();
        common.parseArgs(args);

    }

    public void run() throws Exception {

        common.log(Common.LOGLEVEL_VERBOSE, "=== started ===");

        try {
            common.db = DBMaker
                    .fileDB(new File(common.dbFile))
                    .transactionDisable()
                    .closeOnJvmShutdown()
                    .make();
            common.urlItemsMap = common.db.hashMap(Common.URL_ITEMS_DB);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (common.dbList) {
            for (long key : common.urlItemsMap.keySet()) {
                System.out.println(common.urlItemsMap.get(key).name + " " + Long.toHexString(key));
            }
            return;
        }

        if (common.urlList == null) return;

        for (String contentUrl : common.urlList.keySet()) {
            if (contentUrl == null) continue;

            try {

                Parser parser = null;
                for (ParserEnum parserEnum : ParserEnum.values()) {
                    Parser tParser = parserEnum.getParser();
                    if (tParser.match(contentUrl)) {
                        parser = tParser;
                        break;
                    }
                }

                if (parser == null) continue;

                // make sure cookies is turn on
                CookieHandler.setDefault(new CookieManager());

                common.cookies = parser.login(common);

                ArrayList<UrlItem> urlItemList = common.urlList.get(contentUrl);

                if (urlItemList.isEmpty()) {
                    urlItemList.add(new UrlItem(".*", ""));
                } else {
                    for (UrlItem urlItem : urlItemList) {
                        if (urlItem.getPattern() == null || urlItem.getPattern().isEmpty()) continue;

                        if (urlItem.getPattern().contains("%DATE%")) {
                            urlItem.setPattern(urlItem.getPattern().replace("%DATE%", new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime())));
                        }
                    }
                }

                Document doc = getDocument(contentUrl, parser);

                ArrayList<String[]> list = parser.parse(contentUrl, doc);

                if (list == null) continue;

                for (String[] s : list) {

                    for (UrlItem urlItem : urlItemList) {

                        if (urlItem.getPattern() == null || urlItem.getPattern().isEmpty() || s[0].matches(urlItem.getPattern())) {
                            urlItem.name = s[0];
                            try {
                                if (s[2] != null && !s[2].isEmpty()) {
                                    urlItem.setUrl(s[2]);
                                    Aria2.sendToAria2(common, urlItem);
                                } else {
                                    for (String[] s2 : parser.parseTopic(getDocument(s[1], parser))) {
                                        urlItem.setUrl(s2[2]);
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

        common.log(Common.LOGLEVEL_VERBOSE, "=== ended ===");
    }

    private Document getDocument(String contentUrl, Parser parser) throws Exception {

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
