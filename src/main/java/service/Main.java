package service;

import models.ContinentEnum;
import models.Player;
import models.PlayerPositionEnum;
import models.Team;
import exceptions.UnknownPlayerGoalsException;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    private void run() {
        System.out.println("=== PROG3 - TD JDBC - SUJET PLAYERS ===\n");

        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("=== TEST 1: findTeamById et getPlayersGoals ===\n");
        testTeamWithKnownGoals(dataRetriever, 1, "Real Madrid CF", 7);
        testTeamWithUnknownGoals(dataRetriever, 2, "FC Barcelona", "Robert Lewandowski");
        testTeamWithUnknownGoals(dataRetriever, 3, "Atlético de Madrid", "Antoine Griezmann");

        System.out.println("=== TEST 2: saveTeam ===\n");
        testCreateNewTeam(dataRetriever);
        testUpdateExistingTeam(dataRetriever, 5, "Inter Miami CF");

        System.out.println("=== TEST 3: Pagination joueurs ===\n");
        testPaginationPlayers(dataRetriever, 1, 2);

        System.out.println("=== TEST 4: Recherche équipes par joueur ===\n");
        testFindTeamsByPlayer(dataRetriever, "Courtois");

        System.out.println("=== TEST 5: Recherche joueurs par critères ===\n");
        testFindPlayersByCriteria(dataRetriever, PlayerPositionEnum.DEF, 1, 10);

        System.out.println("\n=== TD TERMINÉ ===");
    }

    // ------------------------
    // Tests équipe avec buts connus
    // ------------------------
    private void testTeamWithKnownGoals(DataRetriever dataRetriever, int teamId, String expectedName, int expectedGoals) {
        Team team = dataRetriever.findTeamById(teamId);
        System.out.println("Test équipe : " + expectedName + " (id=" + teamId + ")");
        if (team != null) {
            System.out.println("✓ Équipe trouvée: " + team.getName());
            try {
                Integer totalGoals = team.getPlayersGoals();
                System.out.println("Total buts = " + totalGoals + " (attendu " + expectedGoals + ")");
            } catch (UnknownPlayerGoalsException e) {
                System.out.println("✗ Exception inattendue : " + e.getMessage());
            }
        } else {
            System.out.println("✗ Équipe non trouvée");
        }
        System.out.println();
    }

    // ------------------------
    // Tests équipe avec buts inconnus (NULL)
    // ------------------------
    private void testTeamWithUnknownGoals(DataRetriever dataRetriever, int teamId, String expectedName, String playerWithNullGoals) {
        Team team = dataRetriever.findTeamById(teamId);
        System.out.println("Test équipe : " + expectedName + " (id=" + teamId + ")");
        if (team != null) {
            System.out.println("✓ Équipe trouvée: " + team.getName());
            try {
                team.getPlayersGoals();
                System.out.println("✗ ERREUR : Exception attendue car " + playerWithNullGoals + " a goal_nb = NULL");
            } catch (UnknownPlayerGoalsException e) {
                System.out.println("✓ SUCCÈS : " + e.getMessage());
            }
        } else {
            System.out.println("✗ Équipe non trouvée");
        }
        System.out.println();
    }

    // ------------------------
    // Création d'une nouvelle équipe
    // ------------------------
    private void testCreateNewTeam(DataRetriever dataRetriever) {
        try {
            Team team = new Team();
            team.setName("Équipe Test TD");
            team.setContinent(ContinentEnum.ASIA);

            Team saved = dataRetriever.saveTeam(team);
            System.out.println("✓ Équipe créée : " + saved.getName() + ", ID=" + saved.getId());

            Team retrieved = dataRetriever.findTeamById(saved.getId());
            if (retrieved != null) {
                System.out.println("✓ Équipe récupérée depuis la base");
                try {
                    System.out.println("Total buts équipe (vide) = " + retrieved.getPlayersGoals());
                } catch (UnknownPlayerGoalsException e) {
                    System.out.println("✗ Exception : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur lors de la création de l'équipe : " + e.getMessage());
        }
        System.out.println();
    }

    // ------------------------
    // Mise à jour équipe existante
    // ------------------------
    private void testUpdateExistingTeam(DataRetriever dataRetriever, int teamId, String teamName) {
        try {
            Team team = dataRetriever.findTeamById(teamId);
            if (team != null) {
                System.out.println("Avant modification : " + team.getName());
                String oldName = team.getName();
                team.setName(oldName + " [MODIFIÉ]");

                Team updated = dataRetriever.saveTeam(team);
                System.out.println("Après modification : " + updated.getName());
            } else {
                System.out.println("⚠ " + teamName + " non trouvée");
            }
        } catch (Exception e) {
            System.out.println("✗ Erreur lors de la mise à jour : " + e.getMessage());
        }
        System.out.println();
    }

    // ------------------------
    // Pagination joueurs
    // ------------------------
    private void testPaginationPlayers(DataRetriever dataRetriever, int pageNumber, int pageSize) {
        List<Player> players = dataRetriever.findPlayers(pageNumber, pageSize);
        System.out.println("Page " + pageNumber + " (" + pageSize + " joueurs max) :");
        for (Player player : players) {
            System.out.println("- " + player.getName());
        }
        System.out.println();
    }

    // ------------------------
    // Recherche équipes par joueur
    // ------------------------
    private void testFindTeamsByPlayer(DataRetriever dataRetriever, String playerName) {
        List<Team> teams = dataRetriever.findTeamsByPlayerName(playerName);
        System.out.println("Équipes contenant le joueur '" + playerName + "' :");
        for (Team team : teams) {
            System.out.println("- " + team.getName() + " (" + team.getContinent() + ")");
        }
        System.out.println();
    }

    // ------------------------
    // Recherche joueurs par critères
    // ------------------------
    private void testFindPlayersByCriteria(DataRetriever dataRetriever, PlayerPositionEnum position, int page, int pageSize) {
        List<Player> players = dataRetriever.findPlayersByCriteria(
                null, position, null, null, page, pageSize
        );
        System.out.println("Joueurs filtrés par position = " + position + " :");
        for (Player player : players) {
            System.out.println("- " + player.getName() + " | " + player.getPosition());
        }
        System.out.println();
    }
}
