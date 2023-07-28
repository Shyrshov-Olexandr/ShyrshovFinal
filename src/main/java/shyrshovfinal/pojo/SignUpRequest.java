package shyrshovfinal.pojo;

import javax.validation.constraints.Email;

public class SignUpRequest {
    @Email
    private String email;
    private String name;
    private String username;



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }



    public SignUpRequest() {
    }


    public SignUpRequest(String email, String name, String username) {
        this.email = email;
        this.name = name;
        this.username = username;
    }
}
