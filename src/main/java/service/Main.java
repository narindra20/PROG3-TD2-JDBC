package service;

import exceptions.UnknownPlayerGoalsException;
import models.ContinentEnum;
import models.Player;
import models.PlayerPositionEnum;
import models.Team;

import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    private void run() {
        System.out.println("=== PROG3 - TD JDBC ===\n");

        DataRetriever dataRetriever = new DataRetriever();

        testQuestion1(dataRetriever);
        testQuestion2(dataRetriever);
        testQuestion3(dataRetriever);
        testQuestion4(dataRetriever);
        testQuestion5(dataRetriever);
        testQuestion6(dataRetriever);

        System.out.println("\n=== TESTS TERMINÉS ===");
    }

    // ============================
    // QUESTION 1
    // ============================
    private void testQuestion1(DataRetriever dataRetriever) {
        System.out.println("\n--- QUESTION 1 : Équipe et joueurs ---");

        Team team = dataRetriever.findTeamById(1);

        if (team == null) {
            System.out.println("Équipe non trouvée");
            return;
        }

        System.out.println("Équipe : " + team.getName());

        if (team.getPlayers() == null) {
            System.out.println("Aucun joueur");
            return;
        }

        for (Player player : team.getPlayers()) {
            if (player.getGoalNb() == null) {
                System.out.println("- " + player.getName() + " : buts inconnus");
            } else {
                System.out.println("- " + player.getName() + " : " + player.getGoalNb() + " buts");
            }
        }

        try {
            Integer total = team.getPlayersGoals();
            System.out.println("Total buts équipe = " + total);
        } catch (UnknownPlayerGoalsException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // ============================
    // QUESTION 2
    // ============================
    private void testQuestion2(DataRetriever dataRetriever) {
        System.out.println("\n--- QUESTION 2 : Pagination ---");

        List<Player> players = dataRetriever.findPlayers(1, 2);

        System.out.println("Page 1 (2 joueurs) :");
        for (Player player : players) {
            System.out.println("- " + player.getName());
        }
    }

    // ============================
    // QUESTION 3
    // ============================
    private void testQuestion3(DataRetriever dataRetriever) {
        System.out.println("\n--- QUESTION 3 : Création joueur ---");

        Player player = new Player();
        player.setName("Joueur Test Simple");
        player.setAge(22);
        player.setPosition(PlayerPositionEnum.MIDF);
        player.setGoalNb(4);

        List<Player> players = new ArrayList<>();
        players.add(player);

        try {
            List<Player> createdPlayers = dataRetriever.createPlayers(players);
            System.out.println("Joueur créé avec ID : " + createdPlayers.get(0).getId());
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // ============================
    // QUESTION 4
    // ============================
    private void testQuestion4(DataRetriever dataRetriever) {
        System.out.println("\n--- QUESTION 4 : Sauvegarde équipe ---");

        Team team = new Team();
        team.setName("Équipe Simple");
        team.setContinent(ContinentEnum.EUROPA);

        Team savedTeam = dataRetriever.saveTeam(team);

        System.out.println("Équipe créée : " + savedTeam.getName());
        System.out.println("ID : " + savedTeam.getId());

        savedTeam.setName("Équipe Simple Modifiée");
        dataRetriever.saveTeam(savedTeam);

        System.out.println("Nom modifié avec succès");
    }

    // ============================
    // QUESTION 5
    // ============================
    private void testQuestion5(DataRetriever dataRetriever) {
        System.out.println("\n--- QUESTION 5 : Recherche équipes par joueur ---");

        List<Team> teams = dataRetriever.findTeamsByPlayerName("Courtois");

        for (Team team : teams) {
            System.out.println("- " + team.getName() + " (" + team.getContinent() + ")");
        }
    }

    // ============================
    // QUESTION 6
    // ============================
    private void testQuestion6(DataRetriever dataRetriever) {
        System.out.println("\n--- QUESTION 6 : Recherche par critères ---");

        List<Player> players = dataRetriever.findPlayersByCriteria(
                null,
                PlayerPositionEnum.DEF,
                null,
                null,
                1,
                10
        );

        for (Player player : players) {
            System.out.println("- " + player.getName() + " | " + player.getPosition());
        }
    }
}
