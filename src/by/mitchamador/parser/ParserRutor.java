package by.mitchamador.parser;

import by.mitchamador.Common;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by vicok on 31.05.2016.
 */
public class ParserRutor extends Parser implements ParserInterface {

    public ParserRutor() {
        setParser(this);
        name = "rutor";
    }

    @Override
    public boolean match(String url) {
        if (url.startsWith("http://rutor.is") || url.startsWith("http://rutor.info")) {
            return true;
        }
        return false;
    }

    @Override
    public Map<String, String> login(Common common) {
        return cookies;
    }

    @Override
    public String getUserAgent() {
        return "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko";
    }

    @Override
    public ArrayList<String[]> parse(String url, Document doc) throws Exception {
        if (url.endsWith(".xml")) {
            return parseRss(doc);
        } else if (url.contains("/torrent/")) {
            return parseTopic(doc);
        } else {
            return parseTopicList(doc);
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
