package service;

import database.DBConnection;
import models.ContinentEnum;
import models.Player;
import models.PlayerPositionEnum;
import models.Team;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataRetrieverTest {

    private DataRetriever dataRetriever;

    @BeforeEach
    void setup() throws SQLException {
        dataRetriever = new DataRetriever();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Supprimer les tables si elles existent
            stmt.execute("DROP TABLE IF EXISTS player");
            stmt.execute("DROP TABLE IF EXISTS team");

            // Créer table team
            stmt.execute("""
                    CREATE TABLE team (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        continent VARCHAR(50) NOT NULL
                    )
                    """);

            // Créer table player
            stmt.execute("""
                    CREATE TABLE player (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        age INT NOT NULL CHECK (age > 0),
                        position VARCHAR(50) NOT NULL,
                        id_team INT REFERENCES team(id) ON DELETE SET NULL
                    )
                    """);

            // Insérer les équipes
            stmt.execute("""
                    INSERT INTO team (name, continent) VALUES
                    ('Real Madrid CF', 'EUROPA'),
                    ('FC Barcelona', 'EUROPA'),
                    ('Atlético de Madrid', 'EUROPA'),
                    ('Al Ahly SC', 'AFRICA'),
                    ('Inter Miami CF', 'AMERICA')
                    """);

            // Insérer les joueurs
            stmt.execute("""
                    INSERT INTO player (name, age, position, id_team) VALUES
                    ('Thibaut Courtois', 32, 'GK', 1),
                    ('Dani Carvajal', 33, 'DEF', 1),
                    ('Jude Bellingham', 21, 'MIDF', 1),
                    ('Robert Lewandowski', 36, 'STR', 2),
                    ('Antoine Griezmann', 33, 'STR', 3)
                    """);
        }
    }

    @AfterEach
    void teardown() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS player");
            stmt.execute("DROP TABLE IF EXISTS team");
        }
    }

    @Test
    void testFindTeamById() {
        // Test Real Madrid
        Team team = dataRetriever.findTeamById(1);
        assertNotNull(team);
        assertEquals("Real Madrid CF", team.getName());
        assertEquals(ContinentEnum.EUROPA, team.getContinent());
        assertEquals(3, team.getPlayers().size());
    }

    @Test
    void testFindPlayersPagination() {
        // Page 1, 2 joueurs
        List<Player> page1 = dataRetriever.findPlayers(1, 2);
        assertEquals(2, page1.size());
        assertEquals("Thibaut Courtois", page1.get(0).getName());

        // Page 2, 2 joueurs
        List<Player> page2 = dataRetriever.findPlayers(2, 2);
        assertEquals(2, page2.size());
        assertEquals("Jude Bellingham", page2.get(0).getName());
    }

    @Test
    void testFindTeamsByPlayerName() {
        // Recherche Lewandowski
        List<Team> teams = dataRetriever.findTeamsByPlayerName("lewa");
        assertEquals(1, teams.size());
        assertEquals("FC Barcelona", teams.get(0).getName());
    }

    @Test
    void testFindPlayersByCriteria() {
        // Recherche joueurs EUROPA et position STR
        List<Player> players = dataRetriever.findPlayersByCriteria(
                null, PlayerPositionEnum.STR, null, ContinentEnum.EUROPA, 1, 10
        );
        assertEquals(2, players.size()); // Lewandowski et Griezmann
    }

    @Test
    void testSaveTeamInsert() {
        Team newTeam = new Team();
        newTeam.setName("Manchester United");
        newTeam.setContinent(ContinentEnum.EUROPA);

        Team savedTeam = dataRetriever.saveTeam(newTeam);
        assertNotNull(savedTeam.getId());
        assertEquals("Manchester United", savedTeam.getName());
    }

    @Test
    void testUpdateTeamWithPlayers() {
        // Récupérer Real Madrid
        Team team = dataRetriever.findTeamById(1);

        // Ajouter un joueur fictif
        Player p = new Player();
        p.setName("Karim Benzema");
        p.setAge(34);
        p.setPosition(PlayerPositionEnum.STR);
        dataRetriever.createPlayers(List.of(p));

        team.getPlayers().add(p);

        // Update équipe
        Team updated = dataRetriever.saveTeam(team);

        // Vérifier que le joueur est associé
        boolean found = updated.getPlayers().stream()
                .anyMatch(pl -> pl.getName().equals("Karim Benzema"));
        assertTrue(found);
    }
}
