package service;

import database.DBConnection;
import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DataRetriever {

    // =====================================================
    // QUESTION 1 : Récupérer une équipe avec ses joueurs
    // =====================================================
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
                team.setContinent(
                        ContinentEnum.valueOf(teamRs.getString("continent"))
                );

                // Charger les joueurs
                List<Player> players = new ArrayList<>();
                try (PreparedStatement playerStmt = conn.prepareStatement(playerSql)) {
                    playerStmt.setInt(1, id);
                    ResultSet playerRs = playerStmt.executeQuery();

                    while (playerRs.next()) {
                        Player p = new Player();
                        p.setId(playerRs.getInt("id"));
                        p.setName(playerRs.getString("name"));
                        p.setAge(playerRs.getInt("age"));
                        p.setPosition(
                                PlayerPositionEnum.valueOf(playerRs.getString("position"))
                        );
                        p.setGoalNb(playerRs.getObject("goal_nb", Integer.class));
                        p.setTeam(team);
                        players.add(p);
                    }
                }

                team.setPlayers(players);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return team;
    }

    // =====================================================
    // QUESTION 2 : Pagination des joueurs
    // =====================================================
    public List<Player> findPlayers(int page, int size) {
        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;

        String sql = """
                SELECT * FROM player
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, size);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Player p = new Player();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setAge(rs.getInt("age"));
                p.setPosition(PlayerPositionEnum.valueOf(rs.getString("position")));
                p.setGoalNb(rs.getObject("goal_nb", Integer.class));
                players.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return players;
    }

    // =====================================================
    // QUESTION 3 : Créer plusieurs joueurs (TRANSACTION)
    // =====================================================
    public List<Player> createPlayers(List<Player> newPlayers) {
        List<Player> created = new ArrayList<>();

        String sql = """
                INSERT INTO player (name, age, position, id_team, goal_nb)
                VALUES (?, ?, ?::player_position, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Vérification des doublons
            for (Player p : newPlayers) {
                if (playerExists(p.getName(), conn)) {
                    conn.rollback();
                    throw new RuntimeException("Le joueur existe déjà : " + p.getName());
                }
            }

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                for (Player p : newPlayers) {
                    stmt.setString(1, p.getName());
                    stmt.setInt(2, p.getAge());
                    stmt.setString(3, p.getPosition().name());

                    if (p.getTeam() != null) {
                        stmt.setInt(4, p.getTeam().getId());
                    } else {
                        stmt.setNull(4, Types.INTEGER);
                    }

                    if (p.getGoalNb() != null) {
                        stmt.setInt(5, p.getGoalNb());
                    } else {
                        stmt.setNull(5, Types.INTEGER);
                    }

                    stmt.executeUpdate();

                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        p.setId(rs.getInt(1));
                        created.add(p);
                    }
                }
            }

            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur création joueurs : " + e.getMessage());
        }

        return created;
    }

    private boolean playerExists(String name, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM player WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // =====================================================
    // QUESTION 4 : Sauvegarder une équipe (INSERT / UPDATE)
    // =====================================================
    public Team saveTeam(Team team) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            if (team.getId() == null) {
                // INSERT
                String sql = """
                        INSERT INTO team (name, continent)
                        VALUES (?, ?::continent_type)
                        """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, team.getName());
                    stmt.setString(2, team.getContinent().name());
                    stmt.executeUpdate();

                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        team.setId(rs.getInt(1));
                    }
                }
            } else {
                // UPDATE
                String sql = """
                        UPDATE team
                        SET name = ?, continent = ?::continent_type
                        WHERE id = ?
                        """;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, team.getName());
                    stmt.setString(2, team.getContinent().name());
                    stmt.setInt(3, team.getId());
                    stmt.executeUpdate();
                }
            }

            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur saveTeam : " + e.getMessage());
        }

        return team;
    }

    // =====================================================
    // QUESTION 5 : Rechercher équipes par nom de joueur
    // =====================================================
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
                Team t = new Team();
                t.setId(rs.getInt("id"));
                t.setName(rs.getString("name"));
                t.setContinent(
                        ContinentEnum.valueOf(rs.getString("continent"))
                );
                teams.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teams;
    }

    // =====================================================
    // QUESTION 6 : Recherche multicritères + pagination
    // =====================================================
    public List<Player> findPlayersByCriteria(
            String playerName,
            PlayerPositionEnum position,
            String teamName,
            ContinentEnum continent,
            int page,
            int size
    ) {
        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("""
                SELECT p.*, t.name AS team_name, t.continent
                FROM player p
                LEFT JOIN team t ON p.id_team = t.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (playerName != null && !playerName.isBlank()) {
            sql.append(" AND LOWER(p.name) LIKE LOWER(?)");
            params.add("%" + playerName + "%");
        }
        if (position != null) {
            sql.append(" AND p.position = ?::player_position");
            params.add(position.name());
        }
        if (teamName != null && !teamName.isBlank()) {
            sql.append(" AND LOWER(t.name) LIKE LOWER(?)");
            params.add("%" + teamName + "%");
        }
        if (continent != null) {
            sql.append(" AND t.continent = ?::continent_type");
            params.add(continent.name());
        }

        sql.append(" ORDER BY p.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Player p = new Player();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setAge(rs.getInt("age"));
                p.setPosition(PlayerPositionEnum.valueOf(rs.getString("position")));
                p.setGoalNb(rs.getObject("goal_nb", Integer.class));
                players.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return players;
    }
}
