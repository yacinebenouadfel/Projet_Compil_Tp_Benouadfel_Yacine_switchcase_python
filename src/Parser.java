import java.util.*;

/**
 * Parser - Analyseur Syntaxique par Descente Récursive
 * Structure PRINCIPALE : Switch/Case en Python
 *

 */
public class Parser {

    private static List<Token> tokens;
    private static int index;
    private static Token tc;
    private static boolean r;
    private static List<String> errors;

    public Parser(List<Token> tokenList) {
        tokens = tokenList;
        index = 0;
        r = true;
        errors = new ArrayList<>();
        if (tokens.size() > 0) {
            tc = tokens.get(index);
        }
    }

    public boolean parse() {
        Program();

        if (tc.getType() == Token.TokenType.EOF && r) {
            System.out.println("✓ Analyse syntaxique réussie !");
            return true;
        } else {
            if (!errors.isEmpty()) {
                System.out.println("✗ Erreurs détectées :");
                for (String error : errors) {
                    System.out.println("  " + error);
                }
            }
            return false;
        }
    }

    /**
     * RÈGLE : Program
     * Program ::= Statement*
     */
    private static void Program() {
        skipNewlines();

        while (tc.getType() != Token.TokenType.EOF && r) {
            Statement();
            skipNewlines();
        }
    }

    /**
     * RÈGLE : Statement
     * Statement ::= SwitchStatement | Assignment | Expression
     *
     * IMPORTANT : Les structures if, while, for sont IGNORÉES (non analysées)
     */
    private static void Statement() {
        skipNewlines();

        if (tc.getType() == Token.TokenType.SWITCH) {
            // STRUCTURE PRINCIPALE : Switch/Case
            SwitchStatement();
        } else if (tc.getType() == Token.TokenType.IDENTIFIER) {
            // Déclarations et affectations
            AssignmentOrExpression();
        } else if (tc.getType() == Token.TokenType.BREAK) {
            advance();
            skipNewlines();
        } else if (tc.getType() == Token.TokenType.CONTINUE) {
            advance();
            skipNewlines();
        } else if (tc.getType() == Token.TokenType.PASS) {
            advance();
            skipNewlines();
        } else if (tc.getType() == Token.TokenType.NEWLINE) {
            advance();
        } else if (tc.getType() == Token.TokenType.IF ||
                tc.getType() == Token.TokenType.WHILE ||
                tc.getType() == Token.TokenType.FOR ||
                tc.getType() == Token.TokenType.DEF ||
                tc.getType() == Token.TokenType.CLASS) {
            // IGNORÉ : Ces structures ne sont pas analysées (conformément au projet)
            System.out.println("⚠️  Instruction '" + tc.getValue() + "' ignorée lors de l'analyse syntaxique (seul switch/case est analysé)");
            skipUntilNextStatement();
        } else if (tc.getType() != Token.TokenType.EOF &&
                tc.getType() != Token.TokenType.RBRACE &&
                tc.getType() != Token.TokenType.CASE &&
                tc.getType() != Token.TokenType.DEFAULT) {
            error("Instruction non reconnue : " + tc.getValue());
            advance();
        }
    }

    /**
     * Ignore une structure non analysée jusqu'à la prochaine instruction
     */
    private static void skipUntilNextStatement() {
        advance();

        // Sauter jusqu'au prochain switch ou identifiant au même niveau
        int braceLevel = 0;
        int parenLevel = 0;

        while (tc.getType() != Token.TokenType.EOF && r) {
            if (tc.getType() == Token.TokenType.LBRACE) {
                braceLevel++;
            } else if (tc.getType() == Token.TokenType.RBRACE) {
                braceLevel--;
                if (braceLevel < 0) break;
            } else if (tc.getType() == Token.TokenType.LPAREN) {
                parenLevel++;
            } else if (tc.getType() == Token.TokenType.RPAREN) {
                parenLevel--;
            }

            // Si on revient au niveau 0 et qu'on trouve switch ou un identifiant
            if (braceLevel == 0 && parenLevel == 0) {
                if (tc.getType() == Token.TokenType.SWITCH ||
                        (tc.getType() == Token.TokenType.IDENTIFIER &&
                                tokens.get(index - 1).getType() == Token.TokenType.NEWLINE)) {
                    break;
                }
            }

            advance();
        }
    }

    /**
     * RÈGLE PRINCIPALE : SwitchStatement
     * SwitchStatement ::= SWITCH LPAREN Expression RPAREN LBRACE CaseClause* [DefaultClause] RBRACE
     */
    private static void SwitchStatement() {
        if (tc.getType() == Token.TokenType.SWITCH) {
            advance();
        } else {
            error("'switch' attendu");
            return;
        }

        if (tc.getType() == Token.TokenType.LPAREN) {
            advance();
        } else {
            error("'(' attendu après 'switch'");
            return;
        }

        Expression();

        if (tc.getType() == Token.TokenType.RPAREN) {
            advance();
        } else {
            error("')' attendu après l'expression");
            return;
        }

        if (tc.getType() == Token.TokenType.LBRACE) {
            advance();
        } else {
            error("'{' attendu pour ouvrir le bloc switch");
            return;
        }

        skipNewlines();

        if (tc.getType() != Token.TokenType.CASE && tc.getType() != Token.TokenType.DEFAULT) {
            error("Au moins un 'case' ou 'default' attendu dans le switch");
        }

        while (tc.getType() == Token.TokenType.CASE && r) {
            CaseClause();
        }

        if (tc.getType() == Token.TokenType.DEFAULT && r) {
            DefaultClause();
        }

        if (tc.getType() == Token.TokenType.RBRACE) {
            advance();
        } else {
            error("'}' attendu pour fermer le bloc switch");
        }
    }

    /**
     * RÈGLE : CaseClause
     * CaseClause ::= CASE Expression COLON Statement* [BREAK]
     */
    private static void CaseClause() {
        if (tc.getType() == Token.TokenType.CASE) {
            advance();
        } else {
            error("'case' attendu");
            return;
        }

        Expression();

        if (tc.getType() == Token.TokenType.COLON) {
            advance();
        } else {
            error("':' attendu après la valeur du case");
            return;
        }

        skipNewlines();

        // Traiter les instructions du case
        while (tc.getType() != Token.TokenType.CASE &&
                tc.getType() != Token.TokenType.DEFAULT &&
                tc.getType() != Token.TokenType.RBRACE &&
                tc.getType() != Token.TokenType.EOF && r) {

            if (tc.getType() == Token.TokenType.BREAK) {
                advance();
                skipNewlines();
                return;
            }

            Statement();
            skipNewlines();
        }
    }

    /**
     * RÈGLE : DefaultClause
     * DefaultClause ::= DEFAULT COLON Statement*
     */
    private static void DefaultClause() {
        if (tc.getType() == Token.TokenType.DEFAULT) {
            advance();
        } else {
            error("'default' attendu");
            return;
        }

        if (tc.getType() == Token.TokenType.COLON) {
            advance();
        } else {
            error("':' attendu après 'default'");
            return;
        }

        skipNewlines();

        while (tc.getType() != Token.TokenType.RBRACE &&
                tc.getType() != Token.TokenType.EOF && r) {

            if (tc.getType() == Token.TokenType.BREAK) {
                advance();
                skipNewlines();
                return;
            }

            Statement();
            skipNewlines();
        }
    }

    /**
     * RÈGLE : AssignmentOrExpression
     * AssignmentOrExpression ::= IDENTIFIER (ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | INCREMENT | DECREMENT | AccessSuffix*) Expression
     */
    private static void AssignmentOrExpression() {
        if (tc.getType() == Token.TokenType.IDENTIFIER) {
            advance();

            if (tc.getType() == Token.TokenType.ASSIGN ||
                    tc.getType() == Token.TokenType.PLUS_ASSIGN ||
                    tc.getType() == Token.TokenType.MINUS_ASSIGN) {
                advance();
                Expression();
            } else if (tc.getType() == Token.TokenType.INCREMENT ||
                    tc.getType() == Token.TokenType.DECREMENT) {
                advance();
            } else {
                // Accès (attributs, méthodes, tableaux)
                while ((tc.getType() == Token.TokenType.DOT ||
                        tc.getType() == Token.TokenType.LBRACKET ||
                        tc.getType() == Token.TokenType.LPAREN) && r) {

                    if (tc.getType() == Token.TokenType.DOT) {
                        advance();
                        if (tc.getType() == Token.TokenType.IDENTIFIER) {
                            advance();
                        } else {
                            error("Identifiant attendu après '.'");
                        }
                    } else if (tc.getType() == Token.TokenType.LBRACKET) {
                        advance();
                        Expression();
                        if (tc.getType() == Token.TokenType.RBRACKET) {
                            advance();
                        } else {
                            error("']' attendu");
                        }
                    } else if (tc.getType() == Token.TokenType.LPAREN) {
                        advance();
                        ArgumentList();
                        if (tc.getType() == Token.TokenType.RPAREN) {
                            advance();
                        } else {
                            error("')' attendu");
                        }
                    }
                }
            }
        } else {
            error("Identifiant attendu");
        }
    }

    /**
     * EXPRESSIONS - Hiérarchie de précédence
     */

    private static void Expression() {
        LogicalOr();
    }

    private static void LogicalOr() {
        LogicalAnd();

        while (tc.getType() == Token.TokenType.OR && r) {
            advance();
            LogicalAnd();
        }
    }

    private static void LogicalAnd() {
        Equality();

        while (tc.getType() == Token.TokenType.AND && r) {
            advance();
            Equality();
        }
    }

    private static void Equality() {
        Comparison();

        while ((tc.getType() == Token.TokenType.EQUAL ||
                tc.getType() == Token.TokenType.NOT_EQUAL) && r) {
            advance();
            Comparison();
        }
    }

    private static void Comparison() {
        Term();

        while ((tc.getType() == Token.TokenType.LESS ||
                tc.getType() == Token.TokenType.LESS_EQUAL ||
                tc.getType() == Token.TokenType.GREATER ||
                tc.getType() == Token.TokenType.GREATER_EQUAL) && r) {
            advance();
            Term();
        }
    }

    private static void Term() {
        Factor();

        while ((tc.getType() == Token.TokenType.PLUS ||
                tc.getType() == Token.TokenType.MINUS) && r) {
            advance();
            Factor();
        }
    }

    private static void Factor() {
        Unary();

        while ((tc.getType() == Token.TokenType.MULTIPLY ||
                tc.getType() == Token.TokenType.DIVIDE ||
                tc.getType() == Token.TokenType.MODULO) && r) {
            advance();
            Unary();
        }
    }

    private static void Unary() {
        if (tc.getType() == Token.TokenType.NOT ||
                tc.getType() == Token.TokenType.MINUS ||
                tc.getType() == Token.TokenType.INCREMENT ||
                tc.getType() == Token.TokenType.DECREMENT) {
            advance();
            Unary();
        } else {
            Primary();
        }
    }

    private static void Primary() {
        if (tc.getType() == Token.TokenType.INTEGER ||
                tc.getType() == Token.TokenType.FLOAT ||
                tc.getType() == Token.TokenType.STRING ||
                tc.getType() == Token.TokenType.BOOLEAN ||
                tc.getType() == Token.TokenType.BENOUADFEL ||
                tc.getType() == Token.TokenType.Yacine) {
            advance();
            return;
        }

        if (tc.getType() == Token.TokenType.IDENTIFIER) {
            advance();

            while ((tc.getType() == Token.TokenType.DOT ||
                    tc.getType() == Token.TokenType.LBRACKET ||
                    tc.getType() == Token.TokenType.LPAREN) && r) {

                if (tc.getType() == Token.TokenType.DOT) {
                    advance();
                    if (tc.getType() == Token.TokenType.IDENTIFIER) {
                        advance();
                    } else {
                        error("Identifiant attendu après '.'");
                    }
                } else if (tc.getType() == Token.TokenType.LBRACKET) {
                    advance();
                    Expression();
                    if (tc.getType() == Token.TokenType.RBRACKET) {
                        advance();
                    } else {
                        error("']' attendu");
                    }
                } else if (tc.getType() == Token.TokenType.LPAREN) {
                    advance();
                    ArgumentList();
                    if (tc.getType() == Token.TokenType.RPAREN) {
                        advance();
                    } else {
                        error("')' attendu");
                    }
                }
            }
            return;
        }

        if (tc.getType() == Token.TokenType.LPAREN) {
            advance();
            Expression();

            if (tc.getType() == Token.TokenType.RPAREN) {
                advance();
            } else {
                error("')' attendu");
            }
            return;
        }

        if (tc.getType() == Token.TokenType.LBRACKET) {
            advance();

            if (tc.getType() != Token.TokenType.RBRACKET) {
                Expression();

                while (tc.getType() == Token.TokenType.COMMA && r) {
                    advance();
                    Expression();
                }
            }

            if (tc.getType() == Token.TokenType.RBRACKET) {
                advance();
            } else {
                error("']' attendu");
            }
            return;
        }

        error("Expression invalide : " + tc.getValue());
    }

    private static void ArgumentList() {
        if (tc.getType() != Token.TokenType.RPAREN) {
            Expression();

            while (tc.getType() == Token.TokenType.COMMA && r) {
                advance();
                Expression();
            }
        }
    }

    /**
     * UTILITAIRES
     */

    private static void advance() {
        if (index < tokens.size() - 1) {
            index++;
            tc = tokens.get(index);
        }
    }

    private static void skipNewlines() {
        while (tc.getType() == Token.TokenType.NEWLINE && index < tokens.size() - 1) {
            advance();
        }
    }

    private static void error(String message) {
        String errorMsg = String.format("Erreur ligne %d, colonne %d: %s (trouvé '%s')",
                tc.getLine(), tc.getColumn(), message, tc.getValue());
        errors.add(errorMsg);
        System.out.println("✗ " + errorMsg);
        r = false;
    }

    public List<String> getErrors() {
        return errors;
    }
}