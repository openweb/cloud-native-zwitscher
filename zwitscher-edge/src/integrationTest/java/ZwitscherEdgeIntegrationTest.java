/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 QAware GmbH, Munich, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import de.qaware.cloud.nativ.zwitscher.edge.ZwitscherEdgeApplication;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZwitscherEdgeApplication.class)
@ActiveProfiles("integration-test")
public class ZwitscherEdgeIntegrationTest {

    private String dockerHost;

    private String dockerUrl;


    @Before
    public void initialize() throws URISyntaxException, IOException {
        initializeDockerHost();
        initializeDockerUrl();
    }

    private void initializeDockerHost() {
        dockerHost = System.getenv("DOCKER_HOST");

        if (dockerHost == null) {
            dockerHost = "https://192.168.99.100:2376";
            return;
        }

        if (dockerHost.contains("tcp")) {
            dockerHost = dockerHost.replace("tcp", "https");
            return;
        }
    }

    private void initializeDockerUrl() throws URISyntaxException {
        URI uri = new URI(dockerHost);
        dockerUrl = "http://" + uri.getHost();
    }

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/integrationTest/resources/compose.yml")
            .waitingForService("zwitscheredge", HealthChecks.toHaveAllPortsOpen(), Duration.standardMinutes(5))
            .saveLogsTo("build/dockerLogs/dockerComposeRuleTest")
            .build();


    @Test
    public void edgeServerIsUsingZwitscherService() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(dockerUrl + ":8765", String.class);
        assertTrue(responseEntity.getBody().contains("Random Quote"));
        assertTrue(!responseEntity.getBody().contains("Everything fails all the time."));
        assertTrue(true);
    }

}
