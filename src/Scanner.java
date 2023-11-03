import Tokens.Token;
import Tokens.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Tokens.TokenType.*;

public class Scanner {

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int startPosition = 0; // Start position of lexime
    private int currentPosition = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            this.startPosition = this.currentPosition;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return this.currentPosition >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PARENTHESIS); break;
            case ')': addToken(RIGHT_PARENTHESIS); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if(match('/')) {
                    //it's a comment that goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if(isDigit(c)) {
                    number();
                } else if(isAlpha(c)) {
                    identifier();
                } else {
                    Lbu.error(this.line, "Unexpected Character");
                }
                break;
        }
    }

    private char advance() {
        return source.charAt(currentPosition++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(startPosition, currentPosition);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(currentPosition) != expected) return false;

        currentPosition++;
        return true;
    }

    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(currentPosition);
    }

    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            Lbu.error(line ,"Unterminated string");
            return;
        }

        advance();

        //trim surrounding quotes
        String value = source.substring(startPosition + 1, currentPosition - 1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while(isDigit(peek())) advance();

        if(peek() == '.' && isDigit(peekNext())) {
            //consume the '.'
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(startPosition, currentPosition)));
    }

    private char peekNext() {
        if(currentPosition + 1 >= source.length()) return '\0';
        return source.charAt(currentPosition+1);
    }

    private void identifier() {
        while (isAplhaNumeric(peek())) advance();

        String text = source.substring(startPosition, currentPosition);
        TokenType tokenType = keywords.get(text);
        if(tokenType == null) tokenType = IDENTIFIER;

        addToken(tokenType);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAplhaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }


}
