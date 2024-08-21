package com.example.Taskmymgmt.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.Taskmymgmt.Entity.UserEntity;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/* Purpose : 1. To extract all claims from the jwt token
             2. To check if the token is expired or not
             3. To generate a token

*/
@Service
public class JwtService {

//    private static final String SECRET_KEY = "s6hP9Rb7C+dDkT2e4ZhL2wth9RjUjcj8RJyBz5Gt4kY=";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // we are extracting usable data from the claims of the jWT token
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }


    // This method allows us to extract a single claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);                           // We use claims to read some data from our jwt
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token)
    {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey()) // use to digitally sign the jwt key to create a signature of the jwt so we can verify that jwt centre is who it claims it to be and havent been changed along the way
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Key getSigningKey() {
        String secretKey = "s6hP9Rb7C+dDkT2e4ZhL2wth9RjUjcj8RJyBz5Gt4kY=";
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }



    public String generateToken(            // this is use to create a token without extra claims just using the user details
            UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraclaims, // this map would contain the claims or the extra claims that we want to add
            UserDetails userDetails
    ) {
        UserEntity userEntity = (UserEntity) userDetails; // Cast to UserEntity to access the role
        extraclaims.put("role", "ROLE_" + userEntity.getRoles().name()); // Add role to claims with ROLE_ prefix

        return Jwts
                .builder()
                .setClaims(extraclaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {    // to check if the this token belongs to this user details
        final String username = this.extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
