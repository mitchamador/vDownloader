package by.mitchamador.parser;

import by.mitchamador.Common;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by vicok on 31.05.2016.
 */
public interface ParserInterface {

    boolean match(String url);

    Map<String, String> login(Common common) throws Exception;

    String getUserAgent();

    ArrayList<String[]> parse(String url, Document doc) throws Exception;

    ArrayList<String[]> parseRss(Document doc) throws Exception;

    ArrayList<String[]> parseTopic(Document doc) throws Exception;

    ArrayList<String[]> parseTopicList(Document doc) throws Exception;

}
