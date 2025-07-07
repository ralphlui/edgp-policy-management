package sg.edu.nus.iss.edgp.policy.management.authentication;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import sg.edu.nus.iss.edgp.policy.management.dto.AuditDTO;
import sg.edu.nus.iss.edgp.policy.management.service.impl.AuditService;
import sg.edu.nus.iss.edgp.policy.management.service.impl.JwtService;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.JwtException;

import org.springframework.security.core.context.SecurityContextHolder;

public class JwtFilterTest {

	@InjectMocks
	private JwtFilter jwtFilter;

	@Mock
	private JwtService jwtService;

	@Mock
	private AuditService auditService;

	@Mock
	private FilterChain filterChain;

	@Mock
	private UserDetails userDetails;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		jwtFilter = new JwtFilter(jwtService, auditService);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	void testDoFilterInternal_ValidToken() throws JwtException, IllegalArgumentException, Exception {
		String token = "valid.jwt.token";
		request.addHeader("Authorization", "Bearer " + token);
		request.setRequestURI("/test");
		request.setMethod("GET");

		AuditDTO auditDTO = new AuditDTO();
		when(auditService.createAuditDTO("", "/test", "GET")).thenReturn(auditDTO);
		when(jwtService.getUserDetail(anyString(), eq(token))).thenReturn(userDetails);
		when(jwtService.validateToken(eq(token), eq(userDetails))).thenReturn(true);
		when(userDetails.getAuthorities()).thenReturn(null);

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain, times(1)).doFilter(request, response);
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertTrue(
				SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
	}

	@Test
	void testDoFilterInternal_MissingAuthorizationHeader() throws ServletException, IOException {
		request.setRequestURI("/test");
		request.setMethod("GET");

		AuditDTO auditDTO = new AuditDTO();
		when(auditService.createAuditDTO("", "/test", "GET")).thenReturn(auditDTO);

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(auditService, times(1)).logAudit(eq(auditDTO), eq(HttpServletResponse.SC_UNAUTHORIZED), anyString(),
				isNull());
		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
	}

	@Test
	void testDoFilterInternal_InvalidToken() throws JwtException, IllegalArgumentException, Exception {
		String token = "invalid.token";
		request.addHeader("Authorization", "Bearer " + token);
		request.setRequestURI("/test");
		request.setMethod("POST");

		AuditDTO auditDTO = new AuditDTO();
		when(auditService.createAuditDTO("", "/test", "POST")).thenReturn(auditDTO);
		when(jwtService.getUserDetail(anyString(), eq(token))).thenReturn(userDetails);
		when(jwtService.validateToken(eq(token), eq(userDetails))).thenReturn(false);

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(auditService).logAudit(eq(auditDTO), eq(HttpServletResponse.SC_UNAUTHORIZED),
				eq("Invalid or expired JWT token"), anyString());
		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
	}
}
