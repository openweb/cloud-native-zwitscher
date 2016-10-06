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
package de.qaware.cloud.nativ.zwitscher.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClient;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.cloud.config.enabled:true","eureka.client.enabled:false"}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("native")
@DirtiesContext
public class ZwitscherConfigApplicationTests {

    public static final int DEFINED_CONFIG_SERVER_PORT = 8888;

    public static final String HEALTH_ENDPOINT = "/admin/health";

    public static final String KEYWORD = "UP";

    public static final String URI = "http://localhost:8888";

    public static final String BOARD_TITLE = "FooTitle";

    public static final String CONFIG_ENDPOINT = "/env/zwitscher-board-test.yml";

    public static final String MISSING_CONFIG_VALUE = "foo";

    @LocalServerPort
    private int configServerPort;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ConfigClientProperties configClientProperties;

    @Test
    public void discoveryClientIsNoopDiscoveryClient() {
        assertTrue("discoveryClient is wrong type: " + this.discoveryClient, this.discoveryClient instanceof NoopDiscoveryClient);
    }

    @Test
    public void servletContainerIsRunningOnDefinedPort() {
        assertTrue("servletContainer running on wrong port: ", this.configServerPort == DEFINED_CONFIG_SERVER_PORT);
    }

    @Test
    public void configServerLocationMatchesUri() {
        assertEquals("service running under wrong Uri", URI, configClientProperties.getUri());
    }

    @Test
    public void healthEndpointIsReachable() {
        String healthResponse = this.testRestTemplate.getForObject(URI + HEALTH_ENDPOINT, String.class);
        assertTrue("health Endpoint is not reachable", healthResponse.contains(KEYWORD));
    }

    @Test
    public void zwitscherBoardConfigIsLoadedAndContainsValue() {
        String forObject = this.testRestTemplate.getForObject(URI + CONFIG_ENDPOINT, String.class);
        assertTrue("config does not contain value", forObject.contains(BOARD_TITLE));
    }

    @Test
    public void zwitscherBoardConfigIsLoadedAndDoesNotContainValue() {
        String forObject = this.testRestTemplate.getForObject(URI + CONFIG_ENDPOINT, String.class);
        assertTrue("config contains value", !forObject.contains(MISSING_CONFIG_VALUE));
    }


}
