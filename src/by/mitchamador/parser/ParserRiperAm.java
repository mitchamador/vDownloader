package by.mitchamador.parser;

import by.mitchamador.Common;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vicok on 31.05.2016.
 */
public class ParserRiperAm extends Parser implements ParserInterface {

    public ParserRiperAm() {
        setParser(this);
        name = "riperam";
    }

    @Override
    public boolean match(String url) {
        if (url.startsWith("http://www.riper.am") || url.startsWith("http://riperam.org")) {
            return true;
        }
        return false;
    }

    @Override
    public  Map<String, String> login(Common common) throws Exception {
        if (loggedIn || login == null || password == null) return cookies;

        String loginUrl = "http://riperam.org/ucp.php?mode=login";

        Map<String, String> data = new HashMap<String, String>();

        Document doc = Jsoup.connect(loginUrl).timeout(Common.TIMEOUT).get();

        Element loginform = doc.getElementById("login");
        Elements inputElements = loginform.getElementsByTag("input");

        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("username"))
                value = login;
            else if (key.equals("password"))
                value = password;
            if (value != null && !value.isEmpty()) {
                data.put(key, URLEncoder.encode(value, "UTF-8"));
            }
        }

        Connection.Response res = Jsoup
                .connect(loginUrl)
                .data(data)
                .method(Connection.Method.POST)
                .execute();

        cookies = res.cookies();

        loggedIn = true;

        return cookies;
    }

    @Override
    public String getUserAgent() {
        return null;
    }

    @Override
    public ArrayList<String[]> parse(String url, Document doc) throws Exception {
        if (url.endsWith(".xml")) {
            return parseRss(doc);
        } else if (url.endsWith(".html")) {
            return parseTopic(doc);
        } else {
            return parseTopicList(doc);
        }
    }

    @Override
    public ArrayList<String[]> parseRss(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        for (Element e : doc.getElementsByTag("item")) {
            try {
                result.add(new String[] {
                                ((TextNode) e.getElementsByTag("title").get(0).childNode(0)).text(),
                                ((TextNode) e.getElementsByTag("guid").get(0).childNode(0)).text(),
                                "",
                        }
                );
            } catch (Exception e1) {
                throw new Exception("error parsing riper.am rss");
            }
        }

        return result;
    }

    @Override
    public ArrayList<String[]> parseTopic(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        try {
            result.add(new String[] {
                            doc.getElementsByAttribute("riper_name").get(0).attr("riper_name"),
                            "",
                            doc.getElementsByAttribute("riper_name").get(0).absUrl("href"),
                    }
            );
        } catch (Exception e) {
            throw new Exception("error parsing riper.am topic");
        }

        return result;
    }

    @Override
    public ArrayList<String[]> parseTopicList(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        for (Element e : doc.getElementsByAttributeValue("class", "topictitle")) {
            try {
                result.add(new String[] {
                                ((TextNode) e.childNode(0)).text(),
                                e.absUrl("href"),
                                e.parent().getElementsByAttributeValue("title", new String("Скачать торрент".getBytes("windows-1251"), "windows-1251")).get(0).absUrl("href"),
                        }
                );
            } catch (Exception e1) {
                throw new Exception("error parsing riper.am topic list");
            }
        }

        return result;
    }
}
