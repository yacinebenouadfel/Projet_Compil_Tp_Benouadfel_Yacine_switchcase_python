public class Token {

    // Énumération de tous les types de tokens possibles
    public enum TokenType {
        // Mots-clés pour switch/case
        SWITCH,    // switch
        CASE,      // case
        DEFAULT,   // default
        BREAK,     // break

        // Autres mots-clés Python
        IF,        // if
        ELIF,      // elif
        ELSE,      // else
        WHILE,     // while
        FOR,       // for
        IN,        // in
        RANGE,     // range
        DEF,       // def (pour les fonctions)
        CLASS,     // class
        RETURN,    // return
        CONTINUE,  // continue
        PASS,      // pass

        // Opérateurs arithmétiques
        PLUS,           // +
        MINUS,          // -
        MULTIPLY,       // *
        DIVIDE,         // /
        MODULO,         // %

        // Opérateurs d'affectation
        ASSIGN,         // =
        PLUS_ASSIGN,    // +=
        MINUS_ASSIGN,   // -=
        INCREMENT,      // ++
        DECREMENT,      // --

        // Opérateurs de comparaison
        EQUAL,          // ==
        NOT_EQUAL,      // !=
        LESS,           // <
        LESS_EQUAL,     // <=
        GREATER,        // >
        GREATER_EQUAL,  // >=

        // Opérateurs logiques
        AND,       // and
        OR,        // or
        NOT,       // not

        // Délimiteurs
        LPAREN,    // (
        RPAREN,    // )
        LBRACE,    // {
        RBRACE,    // }
        LBRACKET,  // [
        RBRACKET,  // ]
        COMMA,     // ,
        COLON,     // :
        SEMICOLON, // ;
        DOT,       // .

        // Littéraux (valeurs)
        IDENTIFIER,  // x, nom, age (noms de variables)
        INTEGER,     // 10, 42, 100
        FLOAT,       // 3.14, 0.5
        STRING,      // "hello", 'world'
        BOOLEAN,     // True, False

        // Spéciaux
        NEWLINE,   // Retour à la ligne
        EOF,       // Fin de fichier
        COMMENT,   // # commentaire

        // Mots-clés personnalisés
        BENOUADFEL,       // mon nom
        Yacine,  // mon prénom

        // Erreur
        ERROR      // Caractère invalide
    }

    // Attributs d'un token
    private TokenType type;    // Le type (ex: IDENTIFIER)
    private String value;      // La valeur (ex: "x")
    private int line;          // Numéro de ligne
    private int column;        // Numéro de colonne

    // Constructeur
    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    // Getters (pour accéder aux attributs)
    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    // Pour afficher un token joliment
    @Override
    public String toString() {
        return String.format("Token{type=%s, value='%s', line=%d, col=%d}",
                type, value, line, column);
    }
}