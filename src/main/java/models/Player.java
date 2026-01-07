package models;

public class Player {

    private Integer id;
    private String name;
    private int age;
    private PlayerPositionEnum position;
    private Team team;
    private Integer goalNb;


    // Constructeur
    public Player() {
        this.id = id;
        this.name = name;
        this.age = age;
        this.position = position;
        this.team = team;
    }


    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public PlayerPositionEnum getPosition() {
        return position;
    }

    public void setPosition(PlayerPositionEnum position) {
        this.position = position;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }


    public String getTeamName() {
        throw new RuntimeException("Not supported yet.");
    }

    public Integer getGoalNb() {
        return goalNb;
    }

    public void setGoalNb(Integer goalNb) {
        this.goalNb = goalNb;
    }

    @Override
    public String toString() {
        return "Player{id=" + id + ", name='" + name + "', age=" + age +
                ", position=" + position + ", goalNb=" + goalNb + "}";
    }
}

