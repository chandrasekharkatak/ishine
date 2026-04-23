package com.apmosys.employeeportal.utility;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.exception.InvalidTokenException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

@Service
public class PoPortalAPIAuthenticationJWTUtility {

	@Value("${token_expiration_timeout}")
	private Long tokenExpirationTimeout;

	public static final String TOKEN_PREFIX = "Ishine ";
	public static final String JWT_ISSUER = "IShine-ApMoSys Technology Pvt. Ltd.";
	public static final String JWT_TOKEN_ENCODER = "A1P2M3O4S5YS6#T1E2C3H4N5O6L7O8G9I10ES#P1V2T#L1T2D";

	public String generateAccessToken() {
		String jwtToken = JWT.create().withIssuer(JWT_ISSUER).withIssuedAt(new Date()).withSubject("Ishine")
				.withExpiresAt(new Date(System.currentTimeMillis() + tokenExpirationTimeout))
				.sign(HMAC512(JWT_TOKEN_ENCODER.getBytes()));

		return TOKEN_PREFIX + jwtToken;
	}

	public void extractAndValidateToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("PoPortal ")) {
			throw new InvalidTokenException("Missing or invalid Authorization header");
		}
		String token = authHeader.substring(9);
		validateToken(token);
	}

	public DecodedJWT validateToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC512(JWT_TOKEN_ENCODER);
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(JWT_ISSUER).acceptLeeway(1800).build();
			return verifier.verify(token);
		} catch (TokenExpiredException e) {
			throw new InvalidTokenException("Token has expired", e);
		} catch (JWTVerificationException e) {
			throw new InvalidTokenException("Invalid token: " + e.getMessage(), e);
		}
	}
	

	public String extractTraceId(HttpServletRequest request) {
		return request != null ? request.getHeader("X-Trace-Id") : null;
	}
}
