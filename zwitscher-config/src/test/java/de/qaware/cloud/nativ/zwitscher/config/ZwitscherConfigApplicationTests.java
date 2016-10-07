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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.cloud.config.enabled:true"}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"native", "test"})
public class ZwitscherConfigApplicationTests {

    public static final int DEFINED_PORT = 8888;

    public static final String HOST = "http://localhost:";

    public static final String CONFIG_ENDPOINT = "/env/zwitscher-board-test.yml";

    @LocalServerPort
    private int configServerPort;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ConfigClientProperties configClientProperties;

    @Test
    public void discoveryClientIsEurekaDiscoveryClient() {
        assertTrue("discoveryClient is wrong type: " + this.discoveryClient, this.discoveryClient instanceof EurekaDiscoveryClient);
    }

    @Test
    public void servletContainerIsRunningOnDefinedPort() {
        assertTrue("servletContainer running on wrong port: ", this.configServerPort == DEFINED_PORT);
    }

    @Test
    public void configServerLocationMatchesUri() {
        assertEquals("service running under wrong Uri", HOST + DEFINED_PORT, configClientProperties.getUri());
    }

    @Test
    public void healthEndpointIsReachable() {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(HOST + DEFINED_PORT + "/admin/health", String.class);
        assertNotNull("responseEntity is null", responseEntity);
        Assert.assertEquals("wrong status code", HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("UP"));
    }

    @Test
    public void zwitscherBoardConfigIsLoadedAndContainsValue() {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(HOST + DEFINED_PORT + CONFIG_ENDPOINT, String.class);
        assertNotNull("responseEntity is null", responseEntity);
        Assert.assertEquals("wrong status code", HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue("config does not contain value", responseEntity.getBody().contains("FooTitle"));
    }

    @Test
    public void zwitscherBoardConfigIsLoadedAndDoesNotContainValue() {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity(HOST + DEFINED_PORT + CONFIG_ENDPOINT, String.class);
        assertNotNull("responseEntity is null", responseEntity);
        Assert.assertEquals("wrong status code", HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(!responseEntity.getBody().contains("foo"));
    }

}
