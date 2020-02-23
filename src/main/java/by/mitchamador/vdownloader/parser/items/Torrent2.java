package by.mitchamador.vdownloader.parser.items;

import by.mitchamador.vdownloader.Common;
import by.mitchamador.vdownloader.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

/**
 * Created by vicok on 31.05.2016.
 */
public class Torrent2 extends Parser {

    public Torrent2() {
        super("torrent2");
    }

    @Override
    public boolean match(String url) {
        if (url.startsWith("http://torrent-2.net/")) {
            return true;
        }
        return false;
    }

    @Override
    public void login(Common common) throws Exception {
        return;
    }

    @Override
    public String getUserAgent() {
        return "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko";
    }

    @Override
    public ArrayList<String[]> parse(String url) throws Exception {
        if (url.endsWith(".xml")) {
            return parseRss(getDocument(url, false));
        } else if (url.endsWith(".html")) {
            return parseTopic(getDocument(url, false));
        } else {
            return parseTopicList(getDocument(url, false));
        }
    }

    @Override
    public ArrayList<String[]> parseRss(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        return result;
    }

    @Override
    public ArrayList<String[]> parseTopic(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        try {
            for (Element e : doc.getElementsByAttributeValueStarting("href", "magnet")) {
                result.add(new String[] {
                                e.parent().parent().getElementsByClass("info_d1").get(0).childNode(0).toString(),
                                "",
                                e.attr("href"),
                        }
                );

            }
        } catch (Exception e) {
            throw new Exception("error parsing torrent2 topic");
        }

        return result;
    }

    @Override
    public ArrayList<String[]> parseTopicList(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        return result;
    }
}
