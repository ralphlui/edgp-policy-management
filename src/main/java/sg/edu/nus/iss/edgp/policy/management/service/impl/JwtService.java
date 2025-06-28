package sg.edu.nus.iss.edgp.policy.management.service.impl;

import java.util.function.Function;

import org.json.simple.JSONObject;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.configuration.JwtConfig;
import sg.edu.nus.iss.edgp.policy.management.pojo.User;
import sg.edu.nus.iss.edgp.policy.management.utility.JSONReader;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtConfig jwtConfig;
	private final JSONReader jsonReader;
	public static final String USER_EMAIL = "userEmail";
	public static final String USER_NAME = "userName";
	public static final String ORG_ID = "orgId";
	public static final String SCOPE = "scope";

	public UserDetails getUserDetail(String authorizationHeader, String token)
			throws JwtException, IllegalArgumentException, Exception {
		String userID = extractSubject(token);

		JSONObject userJSONObjet = jsonReader.getActiveUserInfo(userID, authorizationHeader);
		Boolean success = jsonReader.getSuccessFromResponse(userJSONObjet);

		if (success) {
			User user = jsonReader.getUserObject(userJSONObjet);
			return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
					.password(user.getPassword()).roles(user.getRole().toString()).build();
		} else {
			String message = jsonReader.getMessageFromResponse(userJSONObjet);
			throw new Exception(message);
		}

	}

	public String extractSubject(String token) throws JwtException, IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimResolver)
			throws JwtException, IllegalArgumentException, Exception {
		final Claims cliams = extractAllClaims(token);
		return claimResolver.apply(cliams);
	}

	public Claims extractAllClaims(String token) throws JwtException, IllegalArgumentException, Exception {
		return Jwts.parser().verifyWith(jwtConfig.loadPublicKey()).build().parseSignedClaims(token).getPayload();
	}

	public Boolean validateToken(String token, UserDetails userDetails)
			throws JwtException, IllegalArgumentException, Exception {
		Claims claims = extractAllClaims(token);
		String userEmail = claims.get(USER_EMAIL, String.class);
		return (userEmail.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public boolean isTokenExpired(String token) throws JwtException, IllegalArgumentException, Exception {
		return extractExpiration(token).before(new Date());
	}

	public Date extractExpiration(String token) throws JwtException, IllegalArgumentException, Exception {
		return extractClaim(token, Claims::getExpiration);
	}

	public String extractUserNameFromToken(String token) {
		try {
			Claims claims = extractAllClaims(token);
			return claims.get(USER_NAME, String.class);
		} catch (ExpiredJwtException e) {
			return e.getClaims().get(USER_NAME, String.class);
		} catch (Exception e) {
			return "Invalid Username";
		}
	}

	public String extractUserIdFromToken(String token) {
		try {
			Claims claims = extractAllClaims(token);
			return claims.getSubject();
		} catch (ExpiredJwtException e) {
			return e.getClaims().getSubject();
		} catch (Exception e) {
			return "Invalid UserID";
		}
	}

	public String extractOrgIdFromToken(String token) {
		try {
			Claims claims = extractAllClaims(token);
			return claims.get(ORG_ID, String.class);
		} catch (Exception e) {
			return "Invalid OrgId";
		}
	}

	public String extractScopeFromToken(String token) {
		try {
			Claims claims = extractAllClaims(token);
			return claims.get(SCOPE, String.class);
		} catch (Exception e) {
			return "Invalid Scope";
		}
	}

}
