package by.mitchamador.vdownloader.parser;

import by.mitchamador.vdownloader.parser.items.*;

/**
 * Created by vicok on 31.05.2016.
 */
public enum ParserEnum {

    PARSER_RIPERAM(new RiperAm()),
    PARSER_RUTOR(new Rutor()),
    PARSER_RUTRACKER(new Rutracker()),
    PARSER_NNMCLUB(new NnmClub()),
    PARSER_TORRENT2(new Torrent2());

    /**
     * Parser implementation
     */
    private Parser parser;

    public Parser getParser() {
        return parser;
    }

    ParserEnum(Parser parser) {
        this.parser = parser;
    }
}
