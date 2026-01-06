package service;

import database.DBConnection;
import models.ContinentEnum;
import models.Player;
import models.PlayerPositionEnum;
import models.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    // QUESTION 1: Récupérer une équipe avec tous ses joueurs
    public Team findTeamById(Integer id) {
        Team team = null;
        String teamSql = "SELECT * FROM team WHERE id = ?";
        String playerSql = "SELECT * FROM player WHERE id_team = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement teamStmt = conn.prepareStatement(teamSql)) {

            teamStmt.setInt(1, id);
            ResultSet teamRs = teamStmt.executeQuery();

            if (teamRs.next()) {
                team = new Team();
                team.setId(teamRs.getInt("id"));
                team.setName(teamRs.getString("name"));
                team.setContinent(ContinentEnum.valueOf(teamRs.getString("continent")));

                // Récupérer les joueurs de cette équipe
                try (PreparedStatement playerStmt = conn.prepareStatement(playerSql)) {
                    playerStmt.setInt(1, id);
                    ResultSet playerRs = playerStmt.executeQuery();

                    List<Player> players = new ArrayList<>();
                    while (playerRs.next()) {
                        Player player = new Player();
                        player.setId(playerRs.getInt("id"));
                        player.setName(playerRs.getString("name"));
                        player.setAge(playerRs.getInt("age"));
                        player.setPosition(PlayerPositionEnum.valueOf(playerRs.getString("position")));
                        player.setTeam(team);
                        players.add(player);
                    }
                    team.setPlayers(players);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return team;
    }

    //QUESTION 2: Récupérer tous les joueurs avec pagination
    public List<Player> findPlayers(int page, int size) {
        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM player ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, size);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getInt("id"));
                player.setName(rs.getString("name"));
                player.setAge(rs.getInt("age"));
                player.setPosition(PlayerPositionEnum.valueOf(rs.getString("position")));
                players.add(player);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return players;
    }

    //QUESTION 3: Créer plusieurs joueurs
    public List<Player> createPlayers(List<Player> newPlayers) {
        List<Player> createdPlayers = new ArrayList<>();
        String sql = "INSERT INTO player (name, age, position, id_team) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Vérifier si un joueur existe déjà
            for (Player player : newPlayers) {
                if (playerExists(player.getName(), conn)) {
                    conn.rollback();
                    throw new RuntimeException("Le joueur " + player.getName() + " existe déjà");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (Player player : newPlayers) {
                    stmt.setString(1, player.getName());
                    stmt.setInt(2, player.getAge());
                    stmt.setString(3, player.getPosition().name()); // stocker l'ENUM comme String

                    if (player.getTeam() != null) {
                        stmt.setInt(4, player.getTeam().getId());
                    } else {
                        stmt.setNull(4, Types.INTEGER);
                    }

                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            player.setId(rs.getInt(1));
                            createdPlayers.add(player);
                        }
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création des joueurs: " + e.getMessage());
        }

        return createdPlayers;
    }

    // Méthode privée pour vérifier si un joueur existe déjà
    private boolean playerExists(String name, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM player WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    //QUESTION 4: Sauvegarder ou mettre à jour une équipe
    public Team saveTeam(Team teamToSave) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            if (teamToSave.getName() == null || teamToSave.getName().isBlank())
                throw new IllegalArgumentException("Le nom de l'équipe est requis");
            if (teamToSave.getContinent() == null)
                throw new IllegalArgumentException("Le continent de l'équipe est requis");

            if (teamToSave.getId() == null) {
                // INSERT
                String sql = "INSERT INTO team (name, continent) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, teamToSave.getName());
                    stmt.setString(2, teamToSave.getContinent().name());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            teamToSave.setId(rs.getInt(1));
                        }
                    }
                }

            } else {
                // UPDATE
                String sql = "UPDATE team SET name = ?, continent = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, teamToSave.getName());
                    stmt.setString(2, teamToSave.getContinent().name());
                    stmt.setInt(3, teamToSave.getId());
                    stmt.executeUpdate();
                }

                // Mise à jour des joueurs associés
                updateTeamPlayers(teamToSave, conn);
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde de l'équipe: " + e.getMessage());
        }

        return teamToSave;
    }

    // Mettre à jour les joueurs d'une équipe et leurs références objet
    private void updateTeamPlayers(Team team, Connection conn) throws SQLException {
        // Dé-associer tous les joueurs de cette équipe
        String sql = "UPDATE player SET id_team = NULL WHERE id_team = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, team.getId());
            stmt.executeUpdate();
        }

        // Associer les joueurs de la liste
        if (team.getPlayers() != null) {
            sql = "UPDATE player SET id_team = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Player player : team.getPlayers()) {
                    if (player.getId() == null)
                        throw new IllegalArgumentException("Tous les joueurs doivent être sauvegardés avant d'être associés à une équipe");

                    stmt.setInt(1, team.getId());
                    stmt.setInt(2, player.getId());
                    stmt.addBatch();

                    // Mise à jour côté objet
                    player.setTeam(team);
                }
                stmt.executeBatch();
            }
        }
    }

    //QUESTION 5: Rechercher des équipes par nom de joueur
    public List<Team> findTeamsByPlayerName(String playerName) {
        List<Team> teams = new ArrayList<>();
        String sql = """
                SELECT DISTINCT t.id, t.name, t.continent
                FROM team t
                JOIN player p ON t.id = p.id_team
                WHERE LOWER(p.name) LIKE LOWER(?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + playerName + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Team team = new Team();
                team.setId(rs.getInt("id"));
                team.setName(rs.getString("name"));
                team.setContinent(ContinentEnum.valueOf(rs.getString("continent")));
                teams.add(team);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche des équipes par joueur: " + e.getMessage());
        }

        return teams;
    }

    //QUESTION 6: Rechercher des joueurs selon plusieurs critères
    public List<Player> findPlayersByCriteria(String playerName,
                                              PlayerPositionEnum position,
                                              String teamName,
                                              ContinentEnum continent,
                                              int page,
                                              int size) {
        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.name, p.age, p.position, " +
                        "t.id AS team_id, t.name AS team_name, t.continent AS team_continent " +
                        "FROM player p " +
                        "LEFT JOIN team t ON p.id_team = t.id " +
                        "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (playerName != null && !playerName.isBlank()) {
            sql.append("AND LOWER(p.name) LIKE LOWER(?) ");
            params.add("%" + playerName + "%");
        }

        if (position != null) {
            sql.append("AND p.position = ? ");
            params.add(position.name());
        }

        if (teamName != null && !teamName.isBlank()) {
            sql.append("AND LOWER(t.name) LIKE LOWER(?) ");
            params.add("%" + teamName + "%");
        }

        if (continent != null) {
            sql.append("AND t.continent = ? ");
            params.add(continent.name());
        }

        sql.append("ORDER BY p.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getInt("id"));
                player.setName(rs.getString("name"));
                player.setAge(rs.getInt("age"));
                player.setPosition(PlayerPositionEnum.valueOf(rs.getString("position")));

                if (rs.getInt("team_id") != 0) {
                    Team team = new Team();
                    team.setId(rs.getInt("team_id"));
                    team.setName(rs.getString("team_name"));
                    team.setContinent(ContinentEnum.valueOf(rs.getString("team_continent")));
                    player.setTeam(team);
                }

                players.add(player);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur recherche joueurs par critères : " + e.getMessage());
        }

        return players;
    }
}
