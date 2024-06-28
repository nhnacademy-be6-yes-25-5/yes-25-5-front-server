package com.nhnacademy.frontserver1.common.jwt;

import com.nhnacademy.frontserver1.common.exception.JwtException;
import com.nhnacademy.frontserver1.common.exception.payload.ErrorStatus;
import com.nhnacademy.frontserver1.infrastructure.adaptor.AuthAdaptor;
import com.nhnacademy.frontserver1.presentation.dto.request.auth.CreateAccessTokenRequest;
import com.nhnacademy.frontserver1.presentation.dto.response.auth.CreateAccessTokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final AuthAdaptor authAdaptor;

    public JwtProvider(@Value("${jwt.secret}") String secretKey, AuthAdaptor authAdaptor) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.authAdaptor = authAdaptor;
    }

    public boolean isValidToken(String token) {
        try {
            Jws<Claims> claimJets = Jwts
                    .parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

            Claims claims = claimJets.getBody();

            if (claims.getExpiration().before(new Date())) {
                throw new JwtException(
                        ErrorStatus.toErrorStatus("토큰의 유효시간이 지났습니다.", 401, LocalDateTime.now())
                );
            }

            return true;
        } catch (SignatureException e) {
            throw new JwtException(
                    ErrorStatus.toErrorStatus("시크릿키 변경이 감지되었습니다.", 401, LocalDateTime.now())
            );
        }
    }

    public CreateAccessTokenResponse updateRefreshAccessToken(String refreshToken) {
        CreateAccessTokenRequest request = new CreateAccessTokenRequest(refreshToken);

        try {
            return authAdaptor.refreshToken(request);
        } catch (Exception e) {
            throw new JwtException(
                    ErrorStatus.toErrorStatus("토큰 갱신에 실패했습니다.", 401, LocalDateTime.now())
            );
        }
    }

    public Long getUserNameFromToken(String token) {
        Integer customerId = (Integer) Jwts
                .parserBuilder().setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("customerId", Integer.class);
        return customerId.longValue();
    }

    public String getRolesFromToken(String token) {
        return (String) Jwts
                .parserBuilder().setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles", String.class);
    }

    public String getLoginStateFromToken(String token) {
        return (String) Jwts
                .parserBuilder().setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("loginState", String.class);
    }

}