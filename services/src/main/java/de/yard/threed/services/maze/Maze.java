package de.yard.threed.services.maze;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Data
@Table
public class Maze {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maze_id_generator")
    @SequenceGenerator(name="maze_id_generator", sequenceName = "maze_seq", allocationSize = 1)
    private Long id;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="grid", nullable = false)
    private String grid;

    @Column(name="secret")
    private String secret;

    @Column(name="description", nullable = false)
    private String description;

    @Column(name="type")
    private String type;

    @Column(name="created_at",nullable = false)
    private ZonedDateTime createdAt;

    @Column(name="created_by",nullable = false)
    private String createdBy;

    @Column(name="modified_at",nullable = false)
    private ZonedDateTime modifiedAt;

    @Column(name="modified_by",nullable = false)
    private String modifiedBy;

}
