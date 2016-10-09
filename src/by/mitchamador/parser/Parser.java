package by.mitchamador.parser;

import by.mitchamador.Common;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vicok on 31.05.2016.
 */
public class Parser {

    public boolean loggedIn = false;
    public Map<String, String> cookies = null;
    public String name = null;

    public String login = null;
    public String password = null;
    public String cookiesArg;

    private ParserInterface parser;

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    private Common common;

    public Parser() {
    }

    public void setCookies() {
        if (cookiesArg == null || cookiesArg.trim().isEmpty()) return;

        cookies = new HashMap<String, String>();

        try {
            for (String pair : cookiesArg.split(";")) {
                String[] s = pair.split("=");
                cookies.put(s[0], s[1]);
            }

            loggedIn = true;
            cookiesArg = null;
        } catch (Exception e) {
        }

        return;
    }

    public void setParser(ParserInterface parser) {
        this.parser = parser;
    }

    public boolean match(String url) {
        if (parser != null) {
            return parser.match(url);
        }
        return false;
    }

    public ArrayList<String[]> parse(String url) throws Exception {
        try {

            // make sure cookies is turn on
            CookieHandler.setDefault(new CookieManager());

            setCookies();
            parser.login(common);

            if (loggedIn && (cookies == null || cookies.isEmpty())) {
                common.log(Common.LOGLEVEL_DEFAULT, "empty cookies, " + name + " login failed");
            }

            return parser.parse(url, getDocument(url));
        } catch (Exception e) {
            throw e;
        }
    }

    public byte[] getFile(String url) throws IOException {

        Connection con = Jsoup.connect(url).timeout(Common.TIMEOUT).ignoreContentType(true);

        if (cookies != null) {
            con = con.cookies(cookies);
        }

        if (parser.getUserAgent() != null) {
            con = con.userAgent(parser.getUserAgent());
        }

        return con.execute().bodyAsBytes();

    }

    private Document getDocument(String contentUrl) throws Exception {

        Connection con = Jsoup
                .connect(contentUrl)
                .timeout(Common.TIMEOUT);

        if (cookies != null) {
            con = con.cookies(cookies);
        }

        if (parser.getUserAgent() != null) {
            con = con.userAgent(parser.getUserAgent());
        }

        return con.get();
    }

}
