package com.baito.my_app.support;

import com.baito.my_app.common.config.SecurityConfig;
import com.baito.my_app.common.security.AuthTokenService;
import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.common.security.MemberUserDetailsService;
import com.baito.my_app.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Common base for the REST Docs slice tests.
 *
 * <p>Each concrete subclass adds its own {@code @WebMvcTest(SomeController.class)} and mocks the
 * inbound-port beans that controller needs. This base:
 * <ul>
 *   <li>imports the real {@link SecurityConfig} so {@code @PreAuthorize} / role checks and the JSON
 *       error responses are exercised exactly as in production,</li>
 *   <li>wires Spring REST Docs manually (Spring Boot 4 no longer ships {@code @AutoConfigureRestDocs}),
 *       so {@code document(...)} snippets land under {@code build/generated-snippets},</li>
 *   <li>exposes helper principals and request post-processors for the two roles.</li>
 * </ul>
 */
@ExtendWith(RestDocumentationExtension.class)
@Import(SecurityConfig.class)
public abstract class RestDocsSupport {

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // SecurityConfig#authenticationManager depends on this bean; login tests stub it, the rest ignore it.
    @MockitoBean
    protected MemberUserDetailsService memberUserDetailsService;

    // SecurityConfig's bearer-token filter depends on this bean; auth tests stub it, the rest ignore it.
    @MockitoBean
    protected AuthTokenService authTokenService;

    @BeforeEach
    void setUpMockMvc(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    protected static final LoginMember OWNER = new LoginMember(1L, "owner01", "", Role.OWNER);
    protected static final LoginMember PART_TIMER = new LoginMember(2L, "worker01", "", Role.PART_TIMER);

    /** Authenticates the request as an OWNER (ROLE_OWNER, memberId=1). */
    protected RequestPostProcessor owner() {
        return authentication(authenticationOf(OWNER));
    }

    /** Authenticates the request as a PART_TIMER (ROLE_PART_TIMER, memberId=2). */
    protected RequestPostProcessor partTimer() {
        return authentication(authenticationOf(PART_TIMER));
    }

    private static Authentication authenticationOf(LoginMember member) {
        return new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());
    }
}
