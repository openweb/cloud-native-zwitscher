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
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@SpringBootTest
public class ZwitscherEurekaIntegrationTests {

    private static final String SERVICE_NAME = "zwitschereureka";

    private static final int SERVICE_PORT = 8761;

    private static final String DOCKER_PORT_FORMAT = "http://$HOST:$EXTERNAL_PORT";

    private static final String DOCKER_COMPOSE_FILE_LOCATION = "src/integrationTest/resources/docker-compose.yml";

    private static final String LOG_OUTPUT_DIRECTORY = "build/dockerLogs/dockerComposeRuleTest";

    private static final String KEYWORD = "eureka";

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file(DOCKER_COMPOSE_FILE_LOCATION)
            .waitingForService(SERVICE_NAME, HealthChecks.toHaveAllPortsOpen())
            .saveLogsTo(LOG_OUTPUT_DIRECTORY)
            .build();

    @Test
    public void eurekaServerIsAvailable() throws Exception {
        DockerPort dockerPort = docker.containers().container(SERVICE_NAME).port(SERVICE_PORT);
        String url = dockerPort.inFormat(DOCKER_PORT_FORMAT);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        assertNotNull("responseEntity is null", responseEntity);
        assertEquals("wrong status code", HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue("eureka is not running", responseEntity.getBody().contains(KEYWORD));
    }

}
