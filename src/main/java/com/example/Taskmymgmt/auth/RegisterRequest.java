package com.example.Taskmymgmt.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String username;

    private String password;

    public String getrequestUsername() {
        return username;
    }

    public void setrequestUsername(String username) {
        this.username = username;
    }

    public String getrequestPassword() {
        return password;
    }

    public void setrequestPassword(String password) {
        this.password = password;
    }
}
