package by.mitchamador.parser;

import java.util.Map;

/**
 * Created by vicok on 31.05.2016.
 */
public class ParserBase {

    public boolean loggedIn = false;
    public Map<String, String> cookies = null;
    public String name = null;

    public String login = null;
    public String password = null;

    public ParserBase() {
    }

}
