package com.a3m.studyassistant.backend.features.user;

import com.a3m.studyassistant.backend.features.topic.Topic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Topic> topics;

    public User() {
        this.topics = new ArrayList<>();
    }

    public User(String firstName, String lastName, String email, LocalDate dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dob = dob;
        this.topics = new ArrayList<>();
    }

    public User(UUID id, String firstName, String lastName, String email, LocalDate dob) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dob = dob;
        this.topics = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void addTopic(Topic topic) {
        if(this.topics == null) {
           this.topics = new ArrayList<>();
        }
        this.topics.add(topic);
        topic.setUser(this);
    }

    public void removeTopic(Topic topic) {
        if(this.topics != null) {
            this.topics.remove(topic);
            topic.setUser(null);
        }
    }

}
