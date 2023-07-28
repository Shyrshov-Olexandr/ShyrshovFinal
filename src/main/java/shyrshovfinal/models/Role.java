package shyrshovfinal.models;

import javax.persistence.*;
import java.util.Collection;
@Entity
public class Role {
    @Id
    private Long id;
    @Transient
    @ManyToMany
    private Collection<User> users;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }

    public ERole getName() {
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }
}
