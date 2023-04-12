package de.yard.threed.services.maze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.ZonedDateTime;

@Entity
@Data
@Table
//@JsonDeserialize(using = MazeDeserializer.class)
// Exclude some fields from deserialization
@JsonIgnoreProperties(value = {"id","createdAt","createdBy","modifiedAt","modifiedBy"}, allowGetters = true)
public class Maze {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maze_id_generator")
    @SequenceGenerator(name = "maze_id_generator", sequenceName = "maze_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "grid", nullable = false)
    private String grid;

    @JsonIgnore
    @Column(name = "secret")
    private String secret;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "type")
    private String type;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "modified_at", nullable = false)
    private ZonedDateTime modifiedAt;

    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;

    @Transient
    private boolean locked;

    public Maze() {
        createdAt = ZonedDateTime.now();
        createdBy = "";
        modifiedAt = ZonedDateTime.now();
        modifiedBy = "";
    }

    public boolean getLocked() {
        return getSecret() != null;
    }
}
