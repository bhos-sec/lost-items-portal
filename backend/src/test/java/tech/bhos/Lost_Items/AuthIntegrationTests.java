package tech.bhos.Lost_Items;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tech.bhos.Lost_Items.model.LostItem;
import tech.bhos.Lost_Items.repository.LostItemRepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AuthIntegrationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LostItemRepo lostItemRepo;

    @Test
    @DisplayName("Unauthenticated users cannot create lost items")
    void unauthenticatedCannotCreateLostItem() throws Exception {
        String payload = """
                {
                  "itemName":"Wallet",
                  "itemDesc":"Black leather wallet",
                  "itemLocation":"Library",
                  "founderNumber":"+12025550123"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/lost-items",
                new HttpEntity<>(payload, headers),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
    }

    @Test
    @DisplayName("Authenticated item creation links creator identity")
    void authenticatedCreationStoresOwnerIdentity() throws Exception {
        String registerPayload = """
                {"email":"owner@example.com","password":"strongpass123"}
                """;
        ResponseEntity<String> registerResponse = postJson("/api/auth/register", registerPayload, null);
        assertThat(registerResponse.getStatusCode()).isEqualTo(OK);
        String registerBody = registerResponse.getBody();

        JsonNode registerJson = objectMapper.readTree(registerBody);
        Long userId = registerJson.get("userId").asLong();
        String accessToken = registerJson.get("accessToken").asText();

        String itemPayload = """
                {
                  "itemName":"Laptop",
                  "itemDesc":"Silver laptop",
                  "itemLocation":"Engineering building",
                  "founderNumber":"+12025550124"
                }
                """;
        ResponseEntity<String> createResponse = postJson("/api/lost-items", itemPayload, accessToken);
        assertThat(createResponse.getStatusCode()).isEqualTo(OK);

        LostItem created = lostItemRepo.findAll().stream().findFirst().orElseThrow();
        assertThat(created.getCreatedByUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Non-owner cannot update a lost item")
    void nonOwnerCannotUpdateItem() throws Exception {
        String ownerBody = postJson(
                "/api/auth/register",
                "{\"email\":\"owner2@example.com\",\"password\":\"strongpass123\"}",
                null
        ).getBody();
        String ownerToken = objectMapper.readTree(ownerBody).get("accessToken").asText();

        String createPayload = """
                {
                  "itemName":"Badge",
                  "itemDesc":"Blue student badge",
                  "itemLocation":"Cafeteria",
                  "founderNumber":"+12025550125"
                }
                """;
        ResponseEntity<String> createResponse = postJson("/api/lost-items", createPayload, ownerToken);
        assertThat(createResponse.getStatusCode()).isEqualTo(OK);
        String createBody = createResponse.getBody();
        Integer itemId = objectMapper.readTree(createBody).get("itemId").asInt();

        String otherBody = postJson(
                "/api/auth/register",
                "{\"email\":\"other@example.com\",\"password\":\"strongpass123\"}",
                null
        ).getBody();
        String otherToken = objectMapper.readTree(otherBody).get("accessToken").asText();

        String updatePayload = """
                {
                  "itemName":"Badge Updated",
                  "itemDesc":"Blue student badge",
                  "itemLocation":"Cafeteria",
                  "founderNumber":"+12025550125"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(otherToken);
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "/api/lost-items/" + itemId,
                HttpMethod.PUT,
                new HttpEntity<>(updatePayload, headers),
                String.class
        );
        assertThat(updateResponse.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    private ResponseEntity<String> postJson(String path, String jsonPayload, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (accessToken != null && !accessToken.isBlank()) {
            headers.setBearerAuth(accessToken);
        }
        return restTemplate.postForEntity(path, new HttpEntity<>(jsonPayload, headers), String.class);
    }
}
