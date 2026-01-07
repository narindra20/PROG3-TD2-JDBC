package exceptions;

public class UnknownPlayerGoalsException extends RuntimeException {
    public UnknownPlayerGoalsException(String playerName) {
        super("Le nombre de buts du joueur '" + playerName + "' est inconnu, impossible de calculer le total de l'équipe.");
    }
}