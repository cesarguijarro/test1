package com.devfactory.ui.backend.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.devfactory.aline.base.scm.services.ScmService;
import com.devfactory.aline.base.testing.MockS3;
import com.devfactory.aline.services.aws.AwsS3Service;
import com.devfactory.base.config.AlineConfiguration;
import com.devfactory.base.service.AlineResourceLoader;
import com.devfactory.ffc.auth.domain.AuthenticationToken;
import com.devfactory.ffc.auth.security.TokenIssuer;
import com.devfactory.ffc.config.FirewallDaoTestConfigurer;
import com.devfactory.ffc.config.MetricsUtDaoTestConfigurer;
import com.devfactory.ui.backend.config.ComponentsConfiguration;
import com.devfactory.ui.backend.config.FirewallJpaConfig;
import com.devfactory.ui.backend.config.MetricsJpaConfig;
import com.devfactory.ui.backend.config.UiBackendProperty;
import com.devfactory.ui.backend.config.WebSecurityConfiguration;
import com.devfactory.ui.backend.exception.DefaultExceptionMapper;
import com.devfactory.ui.backend.resources.BaseResourceTest.BaseMockConfiguration;
import com.devfactory.ui.backend.security.AuthorizationService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/*
 * This test class was ignored as it is used as a base class for some test, 
 * it was previously an abstract class which was they way to avoid running this class as a test.
 * I removed the abstract as it is not an actual abstract and added the ignore to not consider it as a test 
 * (it just contain the common stuff for its child classes)
 */
@Ignore
@RunWith(Parameterized.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ContextConfiguration(classes = BaseMockConfiguration.class)
@ActiveProfiles("unittest")
@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
public class BaseResourceTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    /* JUnit requires this to be public, NOT private */
    @Parameter
    public String apiPrefix;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    protected TokenIssuer tokenIssuer;

    MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Parameters
    public static String[] apiPrefixes() {
        return new String[] {"/api/v3", "/api/v4", "/v2", "/v3"};
    }

    @Before
    public void setUpMockMvc() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Before
    public void setupTokens() throws GeneralSecurityException, IOException {
        AuthenticationToken refreshToken = new AuthenticationToken();
        refreshToken.setTokenKey("refreshTokenKey");
        when(tokenIssuer.createToken(any(), any())).thenReturn(refreshToken);
    }

    @Configuration
    @Import({FirewallDaoTestConfigurer.class, MetricsUtDaoTestConfigurer.class, ComponentsConfiguration.class,
            WebSecurityConfiguration.class, DefaultExceptionMapper.class, MetricsJpaConfig.class,
            FirewallJpaConfig.class})
    @EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class})
    @ComponentScan("com.devfactory.ui.backend.resources")
    public static class BaseMockConfiguration {

        @MockBean
        private TokenIssuer tokenIssuer;

        @MockBean
        AuthorizationService authorizationService;

        @MockBean
        private ScmService scmService;

        @MockBean
        private AlineResourceLoader alineResourceLoader;

        @Bean
        @Primary
        public AlineConfiguration alineConfiguration() {
            return AlineConfiguration
                    .createInstance(UiBackendProperty.class, false,
                            Collections.emptyList(), new AwsS3Service(MockS3.create()),
                            "classpath:test/ui-backend.properties");
        }
    }
}