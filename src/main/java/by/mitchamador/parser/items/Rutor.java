package by.mitchamador.parser.items;

import by.mitchamador.Common;
import by.mitchamador.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

/**
 * Created by vicok on 31.05.2016.
 */
public class Rutor extends Parser {

    public Rutor() {
        super("rutor");
    }

    @Override
    public boolean match(String url) {
        if (url.startsWith("http://rutor.is") || url.startsWith("http://rutor.info") || url.startsWith("http://live-rutor.org")) {
            return true;
        }
        return false;
    }

    @Override
    public void login(Common common) {
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
        } else if (url.contains("/torrent/")) {
            return parseTopic(getDocument(url, false));
        } else {
            return parseTopicList(getDocument(url, false));
        }
    }

    @Override
    public ArrayList<String[]> parseRss(Document doc) {
        return null;
    }

    @Override
    public ArrayList<String[]> parseTopic(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        try {
            result.add(new String[]{
                            String.valueOf(doc.getElementsByTag("h1").get(0).childNode(0)),
                            "",
                            String.valueOf(doc.getElementsByAttributeValue("id", "download").get(0).getElementsByAttributeValueStarting("href", "magnet:").attr("href")),
                    }
            );
        } catch (Exception e) {
            throw new Exception("error parsing rutor topic");
        }

        return result;
    }

    @Override
    public ArrayList<String[]> parseTopicList(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        for (Element e : doc.getElementsByAttributeValueStarting("href", "magnet:")) {
            try {
                result.add(new String[]{
                                String.valueOf(e.parent().childNode(3).childNode(0)),
                                e.parent().childNode(3).absUrl("href"),
                                e.attr("href")
                        }
                );
            } catch (Exception e1) {
                throw new Exception("error parsing rutor topic list");
            }
        }

        return result;
    }
}
