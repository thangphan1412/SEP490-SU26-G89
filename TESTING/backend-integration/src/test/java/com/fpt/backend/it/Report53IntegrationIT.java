package com.fpt.backend.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.backend.BackendApplication;
import com.fpt.backend.entity.Role;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.UserStatus;
import com.fpt.backend.mail.EmailService;
import com.fpt.backend.repository.role.RoleRepository;
import com.fpt.backend.repository.user.UserRepository;
import com.fpt.backend.repository.userRole.UserRoleRepository;
import com.fpt.backend.service.impl.user.RedisOtpService;
import com.fpt.backend.util.OTPGenerator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration coverage derived from Report 5.3 and executed against real SQL Server and Redis
 * containers. External email delivery stays mocked because it is outside the tested system.
 */
@SpringBootTest(classes = BackendApplication.class, properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=OFF",
        "logging.level.org.hibernate.orm.jdbc.bind=OFF"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@Transactional
class Report53IntegrationIT {

    private static final String PASSWORD = "StrongPass1!";
    private static final String OTP = "654321";

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisOtpService redisOtpService;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private OTPGenerator otpGenerator;

    private String redisEmailToClean;

    @AfterEach
    void clearRedisOtp() {
        if (redisEmailToClean != null) {
            redisOtpService.deleteOTP(redisEmailToClean);
        }
    }

    @Test
    void e2e01_loginWithValidCredentials_returnsJwtAndRole() throws Exception {
        String email = "report53-admin@example.com";
        seedUser(email, "Administrator");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, PASSWORD)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("Administrator"));
    }

    @Test
    void e2e01_loginWithWrongPassword_isRejected() throws Exception {
        String email = "report53-wrong-password@example.com";
        seedUser(email, "Administrator");

        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, "WrongPass1!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect email or password."));
    }

    @Test
    void alt01_employeeCannotOpenManagementDashboard_butAdministratorCan() throws Exception {
        String employeeEmail = "report53-employee@example.com";
        String administratorEmail = "report53-dashboard-admin@example.com";
        seedUser(employeeEmail, "Employee");
        seedUser(administratorEmail, "Administrator");

        mvc.perform(get("/api/v1/dashboard/overview")
                        .header("Authorization", "Bearer " + login(employeeEmail)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Access Denied"));

        mvc.perform(get("/api/v1/dashboard/overview")
                        .header("Authorization", "Bearer " + login(administratorEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAgreements").isNumber());
    }

    @Test
    void alt02_duplicateEmailStopsUserCreation() throws Exception {
        String email = "report53-duplicate@example.com";
        seedUser(email, "Employee");

        mvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Duplicate",
                                  "lastName": "User",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));

        assertThat(userRepository.findAll().stream()
                .filter(user -> email.equals(user.getEmail())))
                .hasSize(1);
    }

    @Test
    void alt08_usedOtpCannotResetPasswordTwice() throws Exception {
        String email = "report53-otp@example.com";
        String newPassword = "ChangedPass1!";
        seedUser(email, "HeadOfDepartment");
        redisEmailToClean = email;
        when(otpGenerator.generateOTP()).thenReturn(OTP);

        mvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(email)))
                .andExpect(status().isCreated());

        assertThat(redisOtpService.getOTP(email)).isEqualTo(OTP);

        String resetBody = """
                {
                  "email": "%s",
                  "otp": "%s",
                  "newPassword": "%s",
                  "newPasswordConfirm": "%s"
                }
                """.formatted(email, OTP, newPassword, newPassword);

        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody))
                .andExpect(status().isCreated());

        assertThat(redisOtpService.getOTP(email)).isNull();

        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("OTP is empty"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, newPassword)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    private String login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, PASSWORD)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("data").path("token").asText();
    }

    private String loginBody(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }

    private Users seedUser(String email, String roleName) {
        String uniquePart = email.substring(0, email.indexOf('@'))
                .replaceAll("[^A-Za-z0-9]", "_");
        Role role = new Role();
        role.setRoleCode((roleName + "_" + uniquePart).toUpperCase());
        role.setRoleName(roleName);
        role.setRoleDescription("Report 5.3 integration-test role");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role = roleRepository.saveAndFlush(role);

        Users user = Users.builder()
                .email(email)
                .password(passwordEncoder.encode(PASSWORD))
                .firstName("Report53")
                .lastName(roleName)
                .status(UserStatus.ACTIVE)
                .build();
        user = userRepository.saveAndFlush(user);

        userRoleRepository.saveAndFlush(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        // Exercise the same repository hydration path as production instead of reusing the
        // first-level-cache instance whose inverse userRoles collection has not been populated.
        entityManager.clear();
        return user;
    }
}
