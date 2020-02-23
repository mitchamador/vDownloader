package by.mitchamador.parser.items;

import by.mitchamador.Common;
import by.mitchamador.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

/**
 * Created by vicok on 31.05.2016.
 */
public class NnmClub extends Parser {

    public NnmClub() {
        super("nnmclub");
    }

    @Override
    public boolean match(String url) {
        if (url.startsWith("http://nnmclub.to")) {
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
        return null;
    }

    @Override
    public ArrayList<String[]> parse(String url) throws Exception {
        if (url.endsWith(".xml")) {
            return parseRss(getDocument(url, false));
        } else if (url.contains("viewtopic.php?t=")) {
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
                    doc.getElementsByClass("maintitle").get(0).childNode(0).toString(),
                    "",
                    doc.getElementsByAttributeValueStarting("href", "magnet:").get(0).attr("href")
            });
        } catch (Exception e1) {
            throw new Exception("error parsing nnmclub topic");
        }

        return result;
    }

    @Override
    public ArrayList<String[]> parseTopicList(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        for (Element e : doc.getElementsByClass("tDL")) {
            try {
                if (e.parent().getElementsByClass("topictitle").size() > 1) {
                    result.add(new String[]{
                            e.parent().getElementsByClass("topictitle").get(1).text(),
                            e.parent().getElementsByClass("topictitle").get(1).absUrl("href"),
                            "",
                    });
                }
            } catch (Exception e1) {
                throw new Exception("error parsing nnmclub topic list");
            }
        }

        return result;
    }
}
