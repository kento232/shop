package jp.ken.shop.common.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.ken.shop.application.service.UserSearchService;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserSearchService userSearchService;

    public LoginSuccessHandler(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // UserSearchService が withUsername(memberMail) なので、ここはメールになる
        String email = authentication.getName();

        Integer memberId = userSearchService.findMemberIdByEmail(email);

        HttpSession session = request.getSession(true);
        session.setAttribute("memberId", memberId);

        // ログイン成功後の遷移先（SecurityConfig の defaultSuccessUrl と揃える）
        response.sendRedirect(request.getContextPath() + "/top");
    }
}
