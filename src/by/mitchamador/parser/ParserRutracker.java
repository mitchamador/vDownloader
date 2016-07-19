package by.mitchamador.parser;

import by.mitchamador.Common;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vicok on 31.05.2016.
 */
public class ParserRutracker extends Parser implements ParserInterface {

    public ParserRutracker() {
        setParser(this);
        name = "rutracker";
    }

    @Override
    public boolean match(String url) {
        if (url.startsWith("http://rutracker.org")) {
            return true;
        }
        return false;
    }

    @Override
    public Map<String, String> login(Common common) throws Exception {
        if (loggedIn || login == null || password == null) return cookies;

        String loginUrl = "http://login.rutracker.org/forum/login.php";

        Map<String, String> data = new HashMap<String, String>();

        Document doc = Jsoup.connect(loginUrl).timeout(Common.TIMEOUT).get();

        Element loginform = doc.getElementById("login-form");
        Elements inputElements = loginform.getElementsByTag("input");

        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("login_username"))
                value = login;
            else if (key.equals("login_password"))
                value = password;
            if (value != null && !value.isEmpty()) {
                data.put(key, URLEncoder.encode(value, "Windows-1251"));
            }
        }

        Connection.Response res = Jsoup
                .connect(loginUrl)
                .data(data)
                .method(Connection.Method.POST)
                .execute();

        cookies = res.cookies();

        loggedIn = cookies.size() > 0;

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
        } else if (url.contains("viewtopic.php?t=")) {
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

        // получение magnet ссылки для rutracker
        // без входа - doc.getElementsByClass("attach_link").get(0).getElementsByAttributeValueStarting("href", "magnet:").attr("href"))
        // с выполненным входом - doc.getElementsByClass("magnet-link-16").get(0).getElementsByAttributeValueStarting("href", "magnet:").attr("href"))
        try {
            result.add(new String[] {
                            String.valueOf(doc.getElementsByAttributeValue("id", "topic-title").text()),
                            "",
                            String.valueOf(doc.getElementsByClass(loggedIn ? "magnet-link-16" : "attach_link").get(0).getElementsByAttributeValueStarting("href", "magnet:").attr("href")),
                    }
            );
        } catch (Exception e) {
            throw new Exception("error parsing rutracker.org topic");
        }

        return result;
    }

    @Override
    public ArrayList<String[]> parseTopicList(Document doc) throws Exception {
        ArrayList<String[]> result = new ArrayList<String[]>();

        for (Element e : doc.getElementsByAttributeValueStarting("class", "topictitle")) {
            try {
                result.add(new String[] {
                                e.text(),
                                e.absUrl("href"),
                                "",
                        }
                );
            } catch (Exception e1) {
                throw new Exception("error parsing rutracker.org topic list");
            }
        }

        return result;
    }
}
