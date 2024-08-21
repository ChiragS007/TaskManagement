package com.example.Taskmymgmt.auth;

import com.example.Taskmymgmt.Entity.UserEntity;
import com.example.Taskmymgmt.Config.JwtService;
import com.example.Taskmymgmt.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {

    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("/signup")  // this will hold all the details for the sign up request
    public ResponseEntity<AuthenticationResponse> signup(
            @RequestBody RegisterRequest request
    ) {
//        userService.saveNewUser(user);
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    )
    {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/create_admin")
    public ResponseEntity<String> createAdmin() {
        try {
            authenticationService.createAdmin();
            return ResponseEntity.ok("Admin user created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }




    //    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestBody UserEntity user){
//        try{
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
//            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
//            String jwt = jwtService.generateToken(userDetails);
//            return new ResponseEntity<>(jwt, HttpStatus.OK);
//        } catch (BadCredentialsException e) {
//            log.error(e.getMessage());
//            return new ResponseEntity<>("Incorrect username and password", HttpStatus.BAD_REQUEST);
//        }
//    }


}
