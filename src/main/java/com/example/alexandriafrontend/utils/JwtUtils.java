package com.example.alexandriafrontend.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtUtils {

    private static final String SECRET_KEY = "aLEXandria2005"; // misma que el backend

    public static JsonObject decodificarToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        Gson gson = new Gson();
        return gson.toJsonTree(claims).getAsJsonObject();
    }
}
