package com.sivateja.studycollabration.Security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Date;
import javax.crypto.SecretKey;

@Service
public class JwtConfig
{
    @Value("${jwtKey}")
    private String key;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        // This runs AFTER 'key' is injected by Spring
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes());
    }

    public String generateToken(UserDetails userDetails)
    {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ 1000L *3600*24*30))
                .signWith(secretKey)
                .compact();
    }

    public Claims getClaims(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserName(String token)
    {
        Claims claims=getClaims(token);
        return claims.getSubject();
    }

    public boolean valid(String token,UserDetails user)
    {
        String tokenUserName=getClaims(token).getSubject();
        Date expirationDate=getClaims(token).getExpiration();
        return tokenUserName.equals(user.getUsername()) && expirationDate.after(new Date());
    }


}
