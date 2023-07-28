package shyrshovfinal.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String comment;
    private LocalDate created_at;
    @OneToOne

    @JoinColumn(name = "user_id")
    private User user;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "paid_id")
    private Paid paid;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Comment(long id, String comment, LocalDate created_at) {
        this.id = id;
        this.comment = comment;
        this.created_at = created_at;
    }

    public Comment(long id, String comment, LocalDate created_at, User user, Paid paid) {
        this.id = id;
        this.comment = comment;
        this.created_at = created_at;
        this.user = user;
        this.paid = paid;
    }

    public Paid getPaid() {
        return paid;
    }

    public void setPaid(Paid paid) {
        this.paid = paid;
    }

    public Comment() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDate created_at) {
        this.created_at = created_at;
    }
}
