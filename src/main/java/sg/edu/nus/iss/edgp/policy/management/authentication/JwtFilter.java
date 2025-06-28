package sg.edu.nus.iss.edgp.policy.management.authentication;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.policy.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.policy.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.JwtService;
import sg.edu.nus.iss.edgp.policy.management.utility.JwtTokenErrorResponse;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final AuditService auditLogService;
	private String authorizationHeader;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		authorizationHeader = request.getHeader("Authorization");
		AuditDTO auditDTO = auditLogService.createAuditDTO("", request.getRequestURI(), request.getMethod());

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			handleErrorResponse(response, "Authorization header is missing or invalid.",
					HttpServletResponse.SC_UNAUTHORIZED, auditDTO);
			return;
		}

		String jwtToken = authorizationHeader.substring(7);
		try {
			UserDetails userDetails = jwtService.getUserDetail(authorizationHeader, jwtToken);
			if (jwtService.validateToken(jwtToken, userDetails)) {
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				handleErrorResponse(response, "Invalid or expired JWT token", HttpServletResponse.SC_UNAUTHORIZED,
						auditDTO);
				return;
			}
		} catch (ExpiredJwtException e) {
			handleErrorResponse(response, "JWT token is expired", HttpServletResponse.SC_UNAUTHORIZED, auditDTO);
			return;
		} catch (MalformedJwtException | SecurityException e) {
			handleErrorResponse(response, "Invalid JWT token", HttpServletResponse.SC_UNAUTHORIZED, auditDTO);
			return;
		} catch (Exception e) {
			handleErrorResponse(response, e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED, auditDTO);
			return;
		}

		filterChain.doFilter(request, response);

	}

	private void handleErrorResponse(HttpServletResponse response, String message, int status, AuditDTO auditDTO)
			throws IOException {
		auditLogService.logAudit(auditDTO, status, message, authorizationHeader);
		JwtTokenErrorResponse.sendErrorResponse(response, message, status);
	}

}
