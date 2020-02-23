package by.mitchamador.parser;

import by.mitchamador.Common;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static by.mitchamador.Common.LogLevel.LOGLEVEL_DEFAULT;
import static by.mitchamador.Common.LogLevel.LOGLEVEL_VERBOSE;

/**
 * Created by vicok on 31.05.2016.
 */
public abstract class Parser {

    private final String name;

    protected boolean loggedIn = false;
    private Map<String, String> cookies = null;

    protected String login = null;
    protected String password = null;
    private String cookiesArg;

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCookiesArg() {
        return cookiesArg;
    }

    public void setCookiesArg(String cookiesArg) {
        this.cookiesArg = cookiesArg;
    }

    public String getName() {
        return name;
    }

    private Common common;

    public Parser(String name) {
        this.name = name;
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

    public byte[] getFile(String url) throws IOException {

        Connection con = Jsoup.connect(url).timeout(Common.TIMEOUT).ignoreContentType(true);

        if (cookies != null) {
            con = con.cookies(cookies);
        }

        if (getUserAgent() != null) {
            con = con.userAgent(getUserAgent());
        }

        return con.execute().bodyAsBytes();

    }

    public Document getDocument(String contentUrl, boolean needLogIn) throws Exception {

        if (needLogIn) {
            setCookies();
            login(common);

            if (loggedIn && (cookies == null || cookies.isEmpty())) {
                common.log(LOGLEVEL_VERBOSE, "empty cookies, " + name + " login failed");
            }
        }

        Connection con = Jsoup
                .connect(contentUrl)
                .timeout(Common.TIMEOUT);

        if (cookies != null) {
            con = con.cookies(cookies);
        }

        if (getUserAgent() != null) {
            con = con.userAgent(getUserAgent());
        }

        Document d = null;

        try {
            d = con.get();
        } catch (IOException e) {
            if (e instanceof HttpStatusException) {
                if (((HttpStatusException) e).getStatusCode() == 404) {
                    common.log(LOGLEVEL_DEFAULT, contentUrl + " : not found");
                }
            }
        }

        return d == null ? new Document("http://localhost") : d;
    }

    public abstract boolean match(String url);

    public abstract void login(Common common) throws Exception;

    public abstract String getUserAgent();

    public abstract ArrayList<String[]> parse(String url) throws Exception;

    public abstract ArrayList<String[]> parseRss(Document doc) throws Exception;

    public abstract ArrayList<String[]> parseTopic(Document doc) throws Exception;

    public abstract ArrayList<String[]> parseTopicList(Document doc) throws Exception;

}
