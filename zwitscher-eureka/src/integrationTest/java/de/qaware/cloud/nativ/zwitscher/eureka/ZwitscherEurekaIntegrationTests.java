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

package de.qaware.cloud.nativ.zwitscher.eureka;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@SpringBootTest
public class ZwitscherEurekaIntegrationTests {

    private String dockerHost;

    private String dockerUrl;

    @Before
    public void initialize() throws URISyntaxException {
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
            .file("src/integrationTest/resources/docker-compose.yml")
            .waitingForService("zwitschereureka", HealthChecks.toHaveAllPortsOpen())
            .saveLogsTo("build/dockerLogs/dockerComposeRuleTest")
            .build();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void allServicesAreAvailable() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(dockerUrl + ":8761", String.class);
        assertNotNull("responseEntity is null", responseEntity);
        assertEquals("wrong status code", HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void serviceIsNotAvailable() {
        RestTemplate restTemplate = new RestTemplate();
        exception.expect(ResourceAccessException.class);
        exception.expectMessage("Connection refused");
        restTemplate.getForEntity(dockerUrl + ":9000", Object.class);
    }

}
