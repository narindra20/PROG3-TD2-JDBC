package service;

import exceptions.UnknownPlayerGoalsException;
import models.ContinentEnum;
import models.Player;
import models.PlayerPositionEnum;
import models.Team;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataRetrieverTest {

    private DataRetriever dataRetriever;

    @BeforeEach
    void setUp() {
        dataRetriever = new DataRetriever();
    }

    // ============================
    // QUESTION 1: findTeamById et getPlayersGoals
    // ============================
    @Test
    @Order(1)
    void realMadridGoals_shouldReturnTotal7() {
        Team team = dataRetriever.findTeamById(1);
        assertNotNull(team, "Real Madrid CF doit exister");

        try {
            Integer totalGoals = team.getPlayersGoals();
            assertEquals(7, totalGoals, "Total des buts attendu = 7 (0+2+5)");
        } catch (UnknownPlayerGoalsException e) {
            fail("Exception inattendue : " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void fcBarcelonaGoals_shouldThrowException() {
        Team team = dataRetriever.findTeamById(2);
        assertNotNull(team, "FC Barcelona doit exister");

        assertThrows(UnknownPlayerGoalsException.class, team::getPlayersGoals,
                "Exception attendue car Lewandowski a goalNb = NULL");
    }

    @Test
    @Order(3)
    void atleticoMadridGoals_shouldThrowException() {
        Team team = dataRetriever.findTeamById(3);
        assertNotNull(team, "Atlético de Madrid doit exister");

        assertThrows(UnknownPlayerGoalsException.class, team::getPlayersGoals,
                "Exception attendue car Griezmann a goalNb = NULL");
    }

    // ============================
    // QUESTION 2: Pagination joueurs
    // ============================
    @Test
    @Order(4)
    void findPlayersPagination_shouldReturnCorrectNumber() {
        List<Player> page1 = dataRetriever.findPlayers(1, 2);
        assertNotNull(page1, "Page 1 ne doit pas être null");
        assertTrue(page1.size() <= 2, "Page 1 doit contenir au maximum 2 joueurs");
    }

    // ============================
    // QUESTION 3: Création joueur
    // ============================
    @Test
    @Order(5)
    void createPlayer_shouldSucceed() throws Exception {
        Player player = new Player();
        player.setName("Joueur Test Unique " + System.currentTimeMillis());
        player.setAge(22);
        player.setPosition(PlayerPositionEnum.MIDF);
        player.setGoalNb(4);

        List<Player> createdPlayers = dataRetriever.createPlayers(List.of(player));
        assertNotNull(createdPlayers.get(0).getId(), "ID du joueur créé ne doit pas être null");
        assertEquals(player.getName(), createdPlayers.get(0).getName(), "Nom du joueur doit être correct");
    }

    // ============================
    // QUESTION 4: Sauvegarde équipe
    // ============================
    @Test
    @Order(6)
    void saveNewTeam_shouldSucceed() {
        Team team = new Team();
        team.setName("Équipe Test TD");
        team.setContinent(ContinentEnum.ASIA);

        Team saved = dataRetriever.saveTeam(team);
        assertNotNull(saved.getId(), "ID de l'équipe créée ne doit pas être null");

        // Vérification persistance
        Team retrieved = dataRetriever.findTeamById(saved.getId());
        assertEquals(saved.getName(), retrieved.getName(), "Nom persistant doit correspondre");
    }

    @Test
    @Order(7)
    void updateTeam_shouldSucceed() {
        Team team = dataRetriever.findTeamById(5); // Inter Miami CF
        assertNotNull(team, "Inter Miami CF doit exister");

        String oldName = team.getName();
        team.setName(oldName + " [MODIFIÉ]");

        Team updated = dataRetriever.saveTeam(team);
        assertNotEquals(oldName, updated.getName(), "Nom de l'équipe doit être modifié");

        Team reloaded = dataRetriever.findTeamById(5);
        assertEquals(updated.getName(), reloaded.getName(), "Modification doit être persistée en base");
    }

    // ============================
    // QUESTION 5: Recherche équipes par joueur
    // ============================
    @Test
    @Order(8)
    void findTeamsByPlayerName_shouldReturnTeams() {
        List<Team> teams = dataRetriever.findTeamsByPlayerName("Courtois");
        assertFalse(teams.isEmpty(), "Doit retourner au moins une équipe");
    }

    // ============================
    // QUESTION 6: Recherche par critères
    // ============================
    @Test
    @Order(9)
    void findPlayersByCriteria_shouldReturnCorrectPlayers() {
        List<Player> players = dataRetriever.findPlayersByCriteria(
                null, PlayerPositionEnum.DEF, null, null, 1, 10
        );
        assertNotNull(players, "Liste des joueurs ne doit pas être null");
        for (Player p : players) {
            assertEquals(PlayerPositionEnum.DEF, p.getPosition(), "Le joueur doit être DEF");
        }
    }
}
