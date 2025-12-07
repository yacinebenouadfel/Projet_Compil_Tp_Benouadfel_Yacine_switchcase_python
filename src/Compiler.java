import java.io.*;
import java.util.*;

/**
 * Classe Compiler - Programme Principal
 *
 * C'est le point d'entrée du compilateur.
 * Il offre un menu interactif pour :
 * 1. Compiler un fichier
 * 2. Entrer du code directement
 */
public class Compiler {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        afficherBanniere();

        while (true) {
            afficherMenu();
            String choix = scanner.nextLine().trim();

            switch (choix) {
                case "1":
                    compilerFichier(scanner);
                    break;
                case "2":
                    compilerCodeDirect(scanner);
                    break;
                case "3":
                    System.out.println("\n👋 Au revoir ! Merci d'avoir utilisé le compilateur !");
                    System.out.println("📧 N'oubliez pas de commit sur GitHub !\n");
                    return;
                default:
                    System.out.println("❌ Choix invalide ! Choisissez 1, 2 ou 3.");
            }
        }
    }

    /**
     * Affiche la bannière du programme
     */
    private static void afficherBanniere() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                              ║");
        System.out.println("║       MINI-COMPILATEUR PYTHON AVEC SWITCH/CASE              ║");
        System.out.println("║                                                              ║");
        System.out.println("║       Université A/Mira de Béjaïa                           ║");
        System.out.println("║       Département d'Informatique - L3                        ║");
        System.out.println("║       Module: Compilation                                    ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Affiche le menu principal
     */
    private static void afficherMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    MENU PRINCIPAL");
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("  [1] 📁 Compiler un fichier");
        System.out.println("  [2] ⌨️  Entrer du code directement");
        System.out.println("  [3] 🚪 Quitter");
        System.out.println();
        System.out.print("Votre choix: ");
    }

    /**
     * Option 1: Compiler un fichier
     */
    private static void compilerFichier(Scanner scanner) {
        System.out.println("\n📁 COMPILATION D'UN FICHIER");
        System.out.println("─".repeat(60));
        System.out.print("Nom du fichier (ex: test.py): ");
        String nomFichier = scanner.nextLine().trim();

        try {
            String code = lireFichier(nomFichier);
            System.out.println("✓ Fichier lu avec succès (" + code.length() + " caractères)");
            compiler(code, nomFichier);
        } catch (IOException e) {
            System.out.println("❌ Erreur de lecture du fichier: " + e.getMessage());
            System.out.println("💡 Vérifiez que le fichier existe et le chemin est correct.");
        }
    }

    /**
     * Option 2: Entrer du code directement
     */
    private static void compilerCodeDirect(Scanner scanner) {
        System.out.println("\n⌨️  SAISIE DE CODE DIRECTE");
        System.out.println("─".repeat(60));
        System.out.println("Entrez votre code Python (terminez avec une ligne contenant seulement 'FIN'):");
        System.out.println();
        System.out.println("Exemple:");
        System.out.println("  x = 10");
        System.out.println("  switch (x) {");
        System.out.println("      case 1:");
        System.out.println("          print(\"un\")");
        System.out.println("          break");
        System.out.println("      case 10:");
        System.out.println("          print(\"dix\")");
        System.out.println("          break");
        System.out.println("      default:");
        System.out.println("          print(\"autre\")");
        System.out.println("  }");
        System.out.println("  FIN");
        System.out.println();
        System.out.println("Votre code:");

        StringBuilder code = new StringBuilder();

        while (true) {
            String ligne = scanner.nextLine();

            // Arrêter si l'utilisateur tape "FIN"
            if (ligne.trim().equalsIgnoreCase("FIN")) {
                break;
            }

            code.append(ligne).append("\n");
        }

        if (code.length() == 0) {
            System.out.println("❌ Aucun code saisi !");
            return;
        }

        compiler(code.toString(), "saisie directe");
    }

    /**
     * Méthode principale de compilation
     *
     * @param code Le code source à compiler
     * @param source Nom de la source (pour affichage)
     * @return true si compilation réussie, false sinon
     */
    private static boolean compiler(String code, String source) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  COMPILATION: " + source);
        System.out.println("=".repeat(60));

        // ====================================
        // ÉTAPE 1: ANALYSE LEXICALE
        // ====================================
        System.out.println("\n[ÉTAPE 1/2] 🔍 Analyse Lexicale (Tokenisation)...");
        System.out.println("─".repeat(60));

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        // Vérifier les erreurs lexicales
        List<String> erreursLexicales = lexer.getErrors();

        if (!erreursLexicales.isEmpty()) {
            System.out.println("❌ Erreurs lexicales détectées:");
            for (String erreur : erreursLexicales) {
                System.out.println("  • " + erreur);
            }
        } else {
            System.out.println("✓ Analyse lexicale réussie !");
        }

        // Afficher les tokens (sauf NEWLINE et EOF pour plus de clarté)
        System.out.println("\n📋 Tokens reconnus:");
        int count = 0;
        for (Token token : tokens) {
            if (token.getType() != Token.TokenType.EOF &&
                    token.getType() != Token.TokenType.NEWLINE) {
                System.out.printf("  %3d. %-20s : '%s'\n",
                        ++count,
                        token.getType(),
                        token.getValue());
            }
        }
        System.out.println("  Total: " + count + " tokens");

        // ====================================
        // ÉTAPE 2: ANALYSE SYNTAXIQUE
        // ====================================
        System.out.println("\n[ÉTAPE 2/2] 🔍 Analyse Syntaxique (Parsing)...");
        System.out.println("─".repeat(60));

        Parser parser = new Parser(tokens);
        boolean syntaxeCorrecte = parser.parse();

        // ====================================
        // RÉSUMÉ FINAL
        // ====================================
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    RÉSULTAT FINAL");
        System.out.println("=".repeat(60));

        boolean compilationReussie = erreursLexicales.isEmpty() && syntaxeCorrecte;

        if (compilationReussie) {
            System.out.println("\n  ✅✅✅ COMPILATION RÉUSSIE ✅✅✅");
            System.out.println("\n  Le code est syntaxiquement correct !");
            System.out.println("  Aucune erreur détectée.");
        } else {
            System.out.println("\n  ❌❌❌ COMPILATION ÉCHOUÉE ❌❌❌");
            System.out.println("\n  Erreurs détectées:");
            System.out.println("    • Erreurs lexicales: " + erreursLexicales.size());
            System.out.println("    • Erreurs syntaxiques: " + parser.getErrors().size());
            System.out.println("  Total: " + (erreursLexicales.size() + parser.getErrors().size()) + " erreur(s)");
        }

        System.out.println("\n" + "=".repeat(60));

        return compilationReussie;
    }

    /**
     * Lit un fichier et retourne son contenu
     */
    private static String lireFichier(String nomFichier) throws IOException {
        StringBuilder contenu = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(nomFichier))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                contenu.append(ligne).append("\n");
            }
        }

        return contenu.toString();
    }
}