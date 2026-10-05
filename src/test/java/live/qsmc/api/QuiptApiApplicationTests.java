package live.qsmc.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileNotFoundException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class QuiptApiApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeAll
    static void startUp() throws FileNotFoundException {
        QuiptApiApplication.main(new String[]{"--skip-update"});
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testLandingPageIsServed() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Quick Solutions")))
                .andExpect(content().string(containsString("30-Day Uptime")))
                .andExpect(content().string(containsString("uptime-ring-wrap")));
    }

    @Test
    void testDocsPageIsServed() throws Exception {
        mockMvc.perform(get("/docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Quipt API Reference")))
                .andExpect(content().string(containsString("Account API")))
                .andExpect(content().string(containsString("/account/register")));
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
