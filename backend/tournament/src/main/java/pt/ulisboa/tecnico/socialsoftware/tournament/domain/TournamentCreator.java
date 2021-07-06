package pt.ulisboa.tecnico.socialsoftware.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.common.dtos.user.StudentDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TournamentCreator {

    @Column(name = "creator_id")
    private Integer id;

    @Column(name = "creator_username")
    private String username;

    @Column(name = "creator_name")
    private String name;

    public TournamentCreator() {
    }

    public TournamentCreator(int id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StudentDto getStudentDto() {
        StudentDto dto = new StudentDto();
        dto.setId(getId());
        dto.setName(getName());
        dto.setUsername(getUsername());
        return dto;
    }

    @Override
    public String toString() {
        return "TournamentCreator{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
