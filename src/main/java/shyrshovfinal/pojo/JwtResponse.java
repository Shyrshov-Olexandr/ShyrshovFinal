package shyrshovfinal.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import shyrshovfinal.models.Profile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@JsonIgnoreProperties(value = {"role"}, allowGetters = true)
@Schema(description = "JWT response")
public class JwtResponse {
    @Schema(description = "User ID", example = "0")
    private Long id;

    @Schema(description = "User email", example = "john@example.com")
    private String email;

    @Schema(description = "Whether the user account is active", example = "true")
    private boolean is_active;

    @Schema(description = "Whether the user is a superuser", example = "false")
    private boolean is_superuser;

    private boolean is_blocked;

    @Schema(description = "Date of last login", example = "2022-02-28T14:23:10.123Z")
    private Date last_login;

    @Schema(description = "Date the user account was created", example = "2022-01-01T00:00:00.000Z")
    private Date created;

    @Schema(description = "Date the user account was last updated", example = "2022-02-01T12:34:56.789Z")
    private Date updated;

    @Schema(description = "User profile")
    private Profile profile;

    @Schema(description = "Access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    private String access_token;

    @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    private String refresh_token;


    public JwtResponse(Long id, String email, boolean is_active, boolean is_superuser, Date last_login, Date created, Date updated, Profile profile, String access_token, String refresh_token) {
        this.id = id;
        this.email = email;
        this.is_active = is_active;
        this.is_superuser = is_superuser;
        this.last_login = last_login;
        this.created = created;
        this.updated = updated;
        this.profile = profile;
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

    public JwtResponse(String access_token, String refresh_token) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

    public JwtResponse(Long id, String access_token, String refresh_token) {
        this.id = id;
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

    public JwtResponse() {
    }

    public boolean isIs_blocked() {
        return is_blocked;
    }

    public void setIs_blocked(boolean is_blocked) {
        this.is_blocked = is_blocked;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
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


}
