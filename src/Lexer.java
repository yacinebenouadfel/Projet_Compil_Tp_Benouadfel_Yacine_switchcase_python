import java.util.*;

/**
 * Classe Lexer - Analyseur Lexical
 *
 * Son job : Transformer du texte en liste de tokens
 *
 * Exemple :
 * Input  : "x = 10"
 * Output : [Token(IDENTIFIER,"x"), Token(ASSIGN,"="), Token(INTEGER,"10")]
 */
public class Lexer {

    private String input;      // Le code source à analyser
    private int position;      // Position actuelle dans le code
    private int line;          // Ligne actuelle
    private int column;        // Colonne actuelle
    private List<String> errors;  // Liste des erreurs trouvées

    // Table des mots-clés : "switch" → TokenType.SWITCH
    private static final Map<String, Token.TokenType> KEYWORDS = new HashMap<>();

    static {
        // Mots-clés pour switch/case
        KEYWORDS.put("switch", Token.TokenType.SWITCH);
        KEYWORDS.put("case", Token.TokenType.CASE);
        KEYWORDS.put("default", Token.TokenType.DEFAULT);
        KEYWORDS.put("break", Token.TokenType.BREAK);

        // Autres structures de contrôle
        KEYWORDS.put("if", Token.TokenType.IF);
        KEYWORDS.put("elif", Token.TokenType.ELIF);
        KEYWORDS.put("else", Token.TokenType.ELSE);
        KEYWORDS.put("while", Token.TokenType.WHILE);
        KEYWORDS.put("for", Token.TokenType.FOR);
        KEYWORDS.put("in", Token.TokenType.IN);
        KEYWORDS.put("range", Token.TokenType.RANGE);

        // Fonctions et classes
        KEYWORDS.put("def", Token.TokenType.DEF);
        KEYWORDS.put("class", Token.TokenType.CLASS);
        KEYWORDS.put("return", Token.TokenType.RETURN);
        KEYWORDS.put("continue", Token.TokenType.CONTINUE);
        KEYWORDS.put("pass", Token.TokenType.PASS);

        // Opérateurs logiques
        KEYWORDS.put("and", Token.TokenType.AND);
        KEYWORDS.put("or", Token.TokenType.OR);
        KEYWORDS.put("not", Token.TokenType.NOT);

        // Booléens
        KEYWORDS.put("True", Token.TokenType.BOOLEAN);
        KEYWORDS.put("False", Token.TokenType.BOOLEAN);


        KEYWORDS.put("BENOUADFEL", Token.TokenType.BENOUADFEL);
        KEYWORDS.put("Yacine", Token.TokenType.Yacine);
    }

    // Constructeur
    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.errors = new ArrayList<>();
    }

    /**
     * Méthode principale : transforme tout le code en tokens
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        // Tant qu'il reste du code à lire
        while (position < input.length()) {
            Token token = nextToken();

            if (token != null) {
                // Si c'est une erreur, on l'enregistre
                if (token.getType() == Token.TokenType.ERROR) {
                    errors.add(String.format(
                            "Erreur lexicale ligne %d, colonne %d: Caractère invalide '%s'",
                            token.getLine(), token.getColumn(), token.getValue()
                    ));
                }

                // On ignore les commentaires
                if (token.getType() != Token.TokenType.COMMENT) {
                    tokens.add(token);
                }
            }
        }

        // Ajouter un token EOF (End Of File) à la fin
        tokens.add(new Token(Token.TokenType.EOF, "", line, column));
        return tokens;
    }

    /**
     * Lit le prochain token
     */
    private Token nextToken() {
        // Ignorer les espaces
        skipWhitespace();

        // Si on est à la fin, retourner null
        if (position >= input.length()) {
            return null;
        }

        char current = input.charAt(position);
        int startLine = line;
        int startColumn = column;

        // Commentaires (# ...)
        if (current == '#') {
            return scanComment();
        }

        // Identifiants et mots-clés (x, switch, if, ...)
        if (Character.isLetter(current) || current == '_') {
            return scanIdentifier();
        }

        // Nombres (10, 3.14, ...)
        if (Character.isDigit(current)) {
            return scanNumber();
        }

        // Chaînes de caractères ("hello", 'world')
        if (current == '"' || current == '\'') {
            return scanString(current);
        }

        // Opérateurs et symboles
        return scanOperator(startLine, startColumn);
    }

    /**
     * Scanne un identifiant ou un mot-clé
     * Exemple : "switch", "x", "age"
     */
    private Token scanIdentifier() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        // Tant que c'est une lettre, un chiffre ou _
        while (position < input.length() &&
                (Character.isLetterOrDigit(input.charAt(position)) ||
                        input.charAt(position) == '_')) {
            sb.append(input.charAt(position));
            position++;
            column++;
        }

        String value = sb.toString();

        // Est-ce un mot-clé ?
        Token.TokenType type = KEYWORDS.getOrDefault(value, Token.TokenType.IDENTIFIER);

        return new Token(type, value, startLine, startColumn);
    }

    /**
     * Scanne un nombre
     * Exemple : "10", "3.14"
     */
    private Token scanNumber() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;

        while (position < input.length()) {
            char c = input.charAt(position);

            if (Character.isDigit(c)) {
                sb.append(c);
                position++;
                column++;
            } else if (c == '.' && !isFloat) {
                isFloat = true;
                sb.append(c);
                position++;
                column++;
            } else {
                break;
            }
        }

        Token.TokenType type = isFloat ? Token.TokenType.FLOAT : Token.TokenType.INTEGER;
        return new Token(type, sb.toString(), startLine, startColumn);
    }

    /**
     * Scanne une chaîne de caractères
     * Exemple : "hello", 'world'
     */
    private Token scanString(char quote) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        position++; column++;  // Sauter le guillemet d'ouverture

        while (position < input.length() && input.charAt(position) != quote) {
            // Gérer les échappements (\n, \", etc.)
            if (input.charAt(position) == '\\' && position + 1 < input.length()) {
                position++; column++;
                sb.append(input.charAt(position));
            } else {
                sb.append(input.charAt(position));
            }
            position++;
            column++;
        }

        if (position < input.length()) {
            position++; column++;  // Sauter le guillemet de fermeture
        } else {
            errors.add(String.format(
                    "Erreur lexicale ligne %d, colonne %d: Chaîne non terminée",
                    startLine, startColumn
            ));
        }

        return new Token(Token.TokenType.STRING, sb.toString(), startLine, startColumn);
    }

    /**
     * Scanne un opérateur ou un symbole
     */
    private Token scanOperator(int startLine, int startColumn) {
        char current = input.charAt(position);

        switch (current) {
            case '+':
                position++; column++;
                if (peek() == '=') {
                    position++; column++;
                    return new Token(Token.TokenType.PLUS_ASSIGN, "+=", startLine, startColumn);
                } else if (peek() == '+') {
                    position++; column++;
                    return new Token(Token.TokenType.INCREMENT, "++", startLine, startColumn);
                }
                return new Token(Token.TokenType.PLUS, "+", startLine, startColumn);

            case '-':
                position++; column++;
                if (peek() == '=') {
                    position++; column++;
                    return new Token(Token.TokenType.MINUS_ASSIGN, "-=", startLine, startColumn);
                } else if (peek() == '-') {
                    position++; column++;
                    return new Token(Token.TokenType.DECREMENT, "--", startLine, startColumn);
                }
                return new Token(Token.TokenType.MINUS, "-", startLine, startColumn);

            case '*':
                position++; column++;
                return new Token(Token.TokenType.MULTIPLY, "*", startLine, startColumn);

            case '/':
                position++; column++;
                return new Token(Token.TokenType.DIVIDE, "/", startLine, startColumn);

            case '%':
                position++; column++;
                return new Token(Token.TokenType.MODULO, "%", startLine, startColumn);

            case '=':
                position++; column++;
                if (peek() == '=') {
                    position++; column++;
                    return new Token(Token.TokenType.EQUAL, "==", startLine, startColumn);
                }
                return new Token(Token.TokenType.ASSIGN, "=", startLine, startColumn);

            case '!':
                position++; column++;
                if (peek() == '=') {
                    position++; column++;
                    return new Token(Token.TokenType.NOT_EQUAL, "!=", startLine, startColumn);
                }
                return new Token(Token.TokenType.ERROR, "!", startLine, startColumn);

            case '<':
                position++; column++;
                if (peek() == '=') {
                    position++; column++;
                    return new Token(Token.TokenType.LESS_EQUAL, "<=", startLine, startColumn);
                }
                return new Token(Token.TokenType.LESS, "<", startLine, startColumn);

            case '>':
                position++; column++;
                if (peek() == '=') {
                    position++; column++;
                    return new Token(Token.TokenType.GREATER_EQUAL, ">=", startLine, startColumn);
                }
                return new Token(Token.TokenType.GREATER, ">", startLine, startColumn);

            case '(':
                position++; column++;
                return new Token(Token.TokenType.LPAREN, "(", startLine, startColumn);

            case ')':
                position++; column++;
                return new Token(Token.TokenType.RPAREN, ")", startLine, startColumn);

            case '{':
                position++; column++;
                return new Token(Token.TokenType.LBRACE, "{", startLine, startColumn);

            case '}':
                position++; column++;
                return new Token(Token.TokenType.RBRACE, "}", startLine, startColumn);

            case '[':
                position++; column++;
                return new Token(Token.TokenType.LBRACKET, "[", startLine, startColumn);

            case ']':
                position++; column++;
                return new Token(Token.TokenType.RBRACKET, "]", startLine, startColumn);

            case ',':
                position++; column++;
                return new Token(Token.TokenType.COMMA, ",", startLine, startColumn);

            case ':':
                position++; column++;
                return new Token(Token.TokenType.COLON, ":", startLine, startColumn);

            case ';':
                position++; column++;
                return new Token(Token.TokenType.SEMICOLON, ";", startLine, startColumn);

            case '.':
                position++; column++;
                return new Token(Token.TokenType.DOT, ".", startLine, startColumn);

            case '\n':
                position++; line++; column = 1;
                return new Token(Token.TokenType.NEWLINE, "\\n", startLine, startColumn);

            default:
                position++; column++;
                return new Token(Token.TokenType.ERROR, String.valueOf(current), startLine, startColumn);
        }
    }

    /**
     * Scanne un commentaire
     */
    private Token scanComment() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (position < input.length() && input.charAt(position) != '\n') {
            sb.append(input.charAt(position));
            position++;
            column++;
        }

        return new Token(Token.TokenType.COMMENT, sb.toString(), startLine, startColumn);
    }

    /**
     * Ignore les espaces, tabulations, etc.
     */
    private void skipWhitespace() {
        while (position < input.length()) {
            char c = input.charAt(position);
            if (c == ' ' || c == '\t' || c == '\r') {
                position++;
                column++;
            } else {
                break;
            }
        }
    }

    /**
     * Regarde le prochain caractère sans avancer
     */
    private char peek() {
        if (position + 1 < input.length()) {
            return input.charAt(position + 1);
        }
        return '\0';
    }

    /**
     * Retourne la liste des erreurs
     */
    public List<String> getErrors() {
        return errors;
    }
}