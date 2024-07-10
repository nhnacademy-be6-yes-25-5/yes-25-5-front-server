package com.nhnacademy.frontserver1.common.interceptor;

import com.nhnacademy.frontserver1.common.exception.TokenCookieMissingException;
import com.nhnacademy.frontserver1.common.provider.CookieTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import com.nhnacademy.frontserver1.common.context.TokenContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * JWT 인증을 위한 Feign 요청 인터셉터입니다.
 * 이 인터셉터는 쿠키에서 JWT 토큰을 추출하여 Feign 요청의 Authorization 헤더에 추가합니다.
 * Authorization 쿠키(토큰이 담겨있는 쿠키)가 없는 경우 NoTokenCookieException을 던져 로그인 페이지로 리디렉션합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class FeignJwtTokenInterceptor implements RequestInterceptor {

    private final CookieTokenProvider cookieTokenProvider;

    /**
     * Feign 요청에 JWT 토큰을 추가합니다.
     * '/auth/login' 경로에 대한 요청은 인증이 필요하지 않으므로 토큰을 추가하지 않습니다.
     * @param template Feign 요청 템플릿
     */
    @Override
    public void apply(RequestTemplate template) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String path = request.getServletPath();

        if (path.equals("/") || path.startsWith("/auth/login") || path.startsWith("/orders/none") || path.startsWith("/category") || path.startsWith("/search")
          || path.startsWith("/sign-up") || path.startsWith("/books") || path.matches("/coupons") || path.startsWith("/check-email")
                || path.startsWith("/auth/dormant") || path.startsWith("/detail") || path.startsWith("/users/sign-up") || path.equals("/callback")
        ||  path.startsWith("/users/find/password")  || path.equals("/users/find-email") || path.equals("/reset-password/{email}") || path.equals("/users/info")) {

            return;
        }

        String accessToken = TokenContext.getAccessToken();
        String refreshToken = TokenContext.getRefreshToken();

        boolean isTokensEmpty = accessToken == null || refreshToken == null;

        if (isTokensEmpty && (path.matches(".*/orders/.*/delivery.*") || path.startsWith("/users/cart-books")
                || path.startsWith("/detail") || path.startsWith("/books") || path.matches("/coupons") || path.startsWith("/reviews/books"))) {
            return;
        }

        if (isTokensEmpty && (path.startsWith("/users/cart-books") || request.getMethod().equalsIgnoreCase("POST"))) {
            return;
        }

        if (!isTokensEmpty) {
            if (!(accessToken.isBlank() || refreshToken.isBlank())) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                log.debug("Adding Authorization header: Bearer {}", accessToken);
                template.header("Refresh-Token", refreshToken);
                log.debug("Adding RefreshToken header: {}", refreshToken);
            }
        } else {
            log.warn("Authorization token is missing in the cookies.");
            throw new TokenCookieMissingException();
        }
    }

}
