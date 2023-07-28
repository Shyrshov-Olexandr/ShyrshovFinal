package shyrshovfinal.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

@Entity
@Table(name = "auth_user")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String email;

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL,mappedBy = "user",fetch = FetchType.LAZY)
    private Profile profile;
    @JsonIgnore
    private String password;
    private boolean is_active;
    private boolean is_superuser;
    private boolean is_blocked;
    private Date last_login;
    private Date created;
    private Date updated;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new HashSet<>();
    @JsonIgnore
    public Collection<Role> getRoles() {
        return roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }

    public User(String email) {
        this.email = email;
    }

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(long id, String email, Profile profile, String password, boolean is_active, boolean is_superuser,boolean is_blocked, Date last_login, Date created, Date updated, Collection<Role> roles) {
        this.id = id;
        this.email = email;
        this.profile = profile;
        this.password = password;
        this.is_active = is_active;
        this.is_superuser = is_superuser;
        this.is_blocked = is_blocked;
        this.last_login = last_login;
        this.created = created;
        this.updated = updated;
        this.roles = roles;
    }

    public User(long id, String email, Profile profile, String password, boolean is_active, boolean is_superuser ,boolean is_blocked, Date last_login, Date created, Date updated) {
        this.id = id;
        this.email = email;
        this.profile = profile;
        this.password = password;
        this.is_active = is_active;
        this.is_superuser = is_superuser;
        this.is_blocked = is_blocked;
        this.last_login = last_login;
        this.created = created;
        this.updated = updated;
    }

    public User(long id, String email, Profile profile, String password) {
        this.id = id;
        this.email = email;
        this.profile = profile;
        this.password = password;
    }

    public User(String email, String password, boolean is_active, boolean is_superuser) {
        this.email = email;
        this.password = password;
        this.is_active = is_active;
        this.is_superuser = is_superuser;
    }

    public boolean isIs_blocked() {
        return is_blocked;
    }

    public void setIs_blocked(boolean is_blocked) {
        this.is_blocked = is_blocked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public boolean isIs_superuser() {
        return is_superuser;
    }

    public void setIs_superuser(boolean is_superuser) {
        this.is_superuser = is_superuser;
    }

    public Date getLast_login() {
        return last_login;
    }

    public void setLast_login(Date last_login) {
        this.last_login = last_login;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}