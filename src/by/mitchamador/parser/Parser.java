package by.mitchamador.parser;

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

    public ParserInterface getParser() {
        return parser;
    }

    public void setParser(ParserInterface parser) {
        this.parser = parser;
    }
}
