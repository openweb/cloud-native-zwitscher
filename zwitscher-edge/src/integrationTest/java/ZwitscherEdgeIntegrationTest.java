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
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import de.qaware.cloud.nativ.zwitscher.edge.ZwitscherEdgeApplication;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
@ActiveProfiles({"native", "integration-test"})
public class ZwitscherEdgeIntegrationTest {

    private static final String SERVICE_NAME = "zwitscheredge";

    private static final int SERVICE_PORT = 8765;

    private static final String DOCKER_PORT_FORMAT = "http://$HOST:$EXTERNAL_PORT";

    private static final String DOCKER_COMPOSE_FILE_LOCATION = "src/integrationTest/resources/docker-compose.yml";

    private static final String LOG_OUTPUT_DIRECTORY = "build/dockerLogs/dockerComposeRuleTest";

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file(DOCKER_COMPOSE_FILE_LOCATION)
            .waitingForService(SERVICE_NAME, HealthChecks.toHaveAllPortsOpen(), Duration.standardMinutes(5))
            .saveLogsTo(LOG_OUTPUT_DIRECTORY)
            .build();


    @Test
    public void edgeServerIsUsingZwitscherService() {
        DockerPort dockerPort = docker.containers().container(SERVICE_NAME).port(SERVICE_PORT);
        String url = dockerPort.inFormat(DOCKER_PORT_FORMAT);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
        assertTrue(responseEntity.getBody().contains("Random Quote"));
        assertTrue(!responseEntity.getBody().contains("Everything fails all the time."));
    }

}
