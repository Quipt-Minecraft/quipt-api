package live.qsmc.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuiptApiApplicationTests {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
    }

    @Test
    void testLandingPageIsServed() {
        String body = restTemplate.getForObject("http://localhost:" + port + "/", String.class);

        assertThat(body).contains("Quick Solutions")
                .contains("30-Day Uptime")
                .contains("uptime-ring-wrap");
    }

    @Test
    void testDocsPageIsServed() {
        String body = restTemplate.getForObject("http://localhost:" + port + "/docs", String.class);

        assertThat(body).contains("Quipt API Reference")
                .contains("Account API")
                .contains("/account/register");
    }

//    @Test
//    void testStatusEndpointReturnsUptime() throws Exception {
//        mockMvc.perform(get("/status"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is("SUCCESS")))
//                .andExpect(jsonPath("$.data.status", is("UP")))
//                .andExpect(jsonPath("$.data.uptime_ms", notNullValue()))
//                .andExpect(jsonPath("$.data.start_time_ms", notNullValue()))
//                .andExpect(jsonPath("$.data.formatted_uptime", notNullValue()));
//
//        mockMvc.perform(get("/data/status"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is("SUCCESS")))
//                .andExpect(jsonPath("$.data.status", is("UP")));
//    }

}
