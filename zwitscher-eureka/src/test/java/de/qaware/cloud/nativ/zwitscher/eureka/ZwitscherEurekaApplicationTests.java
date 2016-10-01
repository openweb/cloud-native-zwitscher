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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ZwitscherEurekaApplicationTests {

    public static final int DEFINED_EUREKA_SERVER_PORT = 8761;

    public static final String HEALTH_ENDPOINT = "/admin/health";

    public static final String KEYWORD = "UP";

    public static final String HOST = "http://localhost:";

    @LocalServerPort
    private int eurekServerPort;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void discoveryClientIsEureka() {
        assertTrue("discoveryClient is wrong type: " + this.discoveryClient, this.discoveryClient instanceof EurekaDiscoveryClient);
    }

    @Test
    public void definedStaticPortIsReachable() {
        assertTrue("servletContainer running on wrong port: ", this.eurekServerPort == DEFINED_EUREKA_SERVER_PORT);
    }

    @Test
    public void healtEndpointIsReachable() {
        String healthResponse = this.testRestTemplate.getForObject(HOST + DEFINED_EUREKA_SERVER_PORT + HEALTH_ENDPOINT, String.class);
        assertTrue("health Endpoint is not running", healthResponse.contains(KEYWORD));
    }

}
