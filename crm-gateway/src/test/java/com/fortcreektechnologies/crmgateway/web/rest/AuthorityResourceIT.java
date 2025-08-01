package com.fortcreektechnologies.crmgateway.web.rest;

import static com.fortcreektechnologies.crmgateway.domain.AuthorityAsserts.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortcreektechnologies.crmgateway.IntegrationTest;
import com.fortcreektechnologies.crmgateway.domain.Authority;
import com.fortcreektechnologies.crmgateway.repository.AuthorityRepository;
import com.fortcreektechnologies.crmgateway.repository.EntityManager;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link AuthorityResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser(authorities = { "ROLE_ADMIN" })
class AuthorityResourceIT {

    private static final String ENTITY_API_URL = "/api/authorities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{name}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Authority authority;

    private Authority insertedAuthority;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authority createEntity() {
        return new Authority().name(UUID.randomUUID().toString());
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Authority createUpdatedEntity() {
        return new Authority().name(UUID.randomUUID().toString());
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Authority.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        authority = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAuthority != null) {
            authorityRepository.delete(insertedAuthority).block();
            insertedAuthority = null;
        }
        deleteEntities(em);
    }

    @Test
    void createAuthority() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Authority
        var returnedAuthority = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(authority))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Authority.class)
            .returnResult()
            .getResponseBody();

        // Validate the Authority in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertAuthorityUpdatableFieldsEquals(returnedAuthority, getPersistedAuthority(returnedAuthority));

        insertedAuthority = returnedAuthority;
    }

    @Test
    void createAuthorityWithExistingId() throws Exception {
        // Create the Authority with an existing ID
        insertedAuthority = authorityRepository.save(authority).block();

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(authority))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Authority in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllAuthoritiesAsStream() {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        authorityRepository.save(authority).block();

        List<Authority> authorityList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Authority.class)
            .getResponseBody()
            .filter(authority::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(authorityList).isNotNull();
        assertThat(authorityList).hasSize(1);
        Authority testAuthority = authorityList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertAuthorityAllPropertiesEquals(authority, testAuthority);
        assertAuthorityUpdatableFieldsEquals(authority, testAuthority);
    }

    @Test
    void getAllAuthorities() {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.save(authority).block();

        // Get all the authorityList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=name,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].name")
            .value(hasItem(authority.getName()));
    }

    @Test
    void getAuthority() {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.save(authority).block();

        // Get the authority
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, authority.getName())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.name")
            .value(is(authority.getName()));
    }

    @Test
    void getNonExistingAuthority() {
        // Get the authority
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void deleteAuthority() {
        // Initialize the database
        authority.setName(UUID.randomUUID().toString());
        insertedAuthority = authorityRepository.save(authority).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the authority
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, authority.getName())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return authorityRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Authority getPersistedAuthority(Authority authority) {
        return authorityRepository.findById(authority.getName()).block();
    }

    protected void assertPersistedAuthorityToMatchAllProperties(Authority expectedAuthority) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAuthorityAllPropertiesEquals(expectedAuthority, getPersistedAuthority(expectedAuthority));
        assertAuthorityUpdatableFieldsEquals(expectedAuthority, getPersistedAuthority(expectedAuthority));
    }

    protected void assertPersistedAuthorityToMatchUpdatableProperties(Authority expectedAuthority) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAuthorityAllUpdatablePropertiesEquals(expectedAuthority, getPersistedAuthority(expectedAuthority));
        assertAuthorityUpdatableFieldsEquals(expectedAuthority, getPersistedAuthority(expectedAuthority));
    }
}
