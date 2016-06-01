package by.mitchamador.parser;

/**
 * Created by vicok on 31.05.2016.
 */
public enum ParserEnum {

    PARSER_RIPERAM(new ParserRiperAm()),
    PARSER_RUTOR(new ParserRutor()),
    PARSER_RUTRACKER(new ParserRutracker()),
    PARSER_NNMCLUB(new ParserNnmClub());

    public Parser getParser() {
        return parser;
    }

    Parser parser;

    ParserEnum(Parser parser) {
        this.parser = parser;
    }
}
