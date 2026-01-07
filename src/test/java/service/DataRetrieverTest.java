package service;

import exceptions.UnknownPlayerGoalsException;
import models.ContinentEnum;
import models.Player;
import models.PlayerPositionEnum;
import models.Team;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataRetrieverTest{

    private DataRetriever dataRetriever;

    @BeforeEach
    void setUp() {
        dataRetriever = new DataRetriever();
    }

    // ============================
    // QUESTION 1 : findTeamById & getPlayersGoals
    // ============================
    @Test
    @Order(1)
    void testFindTeamByIdAndGetPlayersGoals() {
        Team team = dataRetriever.findTeamById(1);
        assertNotNull(team, "L'équipe avec id=1 doit exister");
        assertNotNull(team.getPlayers(), "Les joueurs doivent être chargés");

        for (Player player : team.getPlayers()) {
            assertNotNull(player.getGoalNb(), "Chaque joueur doit avoir goalNb renseigné pour ce test");
        }

        try {
            Integer totalGoals = team.getPlayersGoals();
            assertEquals(7, totalGoals, "Total des buts de Real Madrid CF attendu = 7");
        } catch (UnknownPlayerGoalsException e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    // ============================
    // QUESTION 2 : Pagination joueurs
    // ============================
    @Test
    @Order(2)
    void testPaginationPlayers() {
        List<Player> page1 = dataRetriever.findPlayers(1, 2);
        assertTrue(page1.size() <= 2, "Page 1 doit contenir au maximum 2 joueurs");

        List<Player> page2 = dataRetriever.findPlayers(2, 2);
        assertTrue(page2.size() <= 2, "Page 2 doit contenir au maximum 2 joueurs");
    }

    // ============================
    // QUESTION 3 : Création joueur
    // ============================
    @Test
    @Order(3)
    void testCreatePlayer() {
        Player player = new Player();
        player.setName("JUnit Player " + System.currentTimeMillis());
        player.setAge(22);
        player.setPosition(PlayerPositionEnum.MIDF);
        player.setGoalNb(4);

        List<Player> players = new ArrayList<>();
        players.add(player);

        List<Player> created = dataRetriever.createPlayers(players);
        assertNotNull(created.get(0).getId(), "L'ID du joueur créé ne doit pas être null");
        assertEquals(4, created.get(0).getGoalNb(), "Le nombre de buts doit être correct");
    }

    // ============================
    // QUESTION 4 : Sauvegarde et modification équipe
    // ============================
    @Test
    @Order(4)
    void testSaveAndUpdateTeam() {
        Team team = new Team();
        team.setName("JUnit Team");
        team.setContinent(ContinentEnum.EUROPA);

        Team saved = dataRetriever.saveTeam(team);
        assertNotNull(saved.getId(), "L'équipe doit avoir un ID après sauvegarde");
        assertEquals("JUnit Team", saved.getName());

        saved.setName("JUnit Team Modifiée");
        Team updated = dataRetriever.saveTeam(saved);
        assertEquals("JUnit Team Modifiée", updated.getName(), "Le nom de l'équipe doit être modifié");
    }

    // ============================
    // QUESTION 5 : Recherche équipes par joueur
    // ============================
    @Test
    @Order(5)
    void testFindTeamsByPlayerName() {
        List<Team> teams = dataRetriever.findTeamsByPlayerName("Courtois");
        assertFalse(teams.isEmpty(), "Au moins une équipe doit contenir le joueur 'Courtois'");
    }

    // ============================
    // QUESTION 6 : Recherche par critères
    // ============================
    @Test
    @Order(6)
    void testFindPlayersByCriteria() {
        List<Player> players = dataRetriever.findPlayersByCriteria(
                null,
                PlayerPositionEnum.DEF,
                null,
                null,
                1,
                10
        );

        for (Player p : players) {
            assertEquals(PlayerPositionEnum.DEF, p.getPosition(), "Tous les joueurs doivent être défenseurs");
        }
    }

    // ============================
    // BONUS : getPlayersGoals avec exception
    // ============================
    @Test
    @Order(7)
    void testGetPlayersGoalsWithUnknownException() {
        Team barcelona = dataRetriever.findTeamById(2);
        assertNotNull(barcelona);
        assertThrows(UnknownPlayerGoalsException.class, barcelona::getPlayersGoals,
                "Une exception doit être levée si un joueur a goalNb inconnu");
    }
}
