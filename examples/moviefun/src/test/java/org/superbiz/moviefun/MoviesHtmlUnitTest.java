/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.moviefun;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.io.FileUtils;
import org.apache.tomee.embedded.EmbeddedTomEEContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class MoviesHtmlUnitTest {

    private static EJBContainer container;
    private static File webApp;
    private static int port;

    @BeforeClass
    public static void start() throws IOException {

        // get a random unused port to use for http requests
        ServerSocket server = new ServerSocket(0);
        port = server.getLocalPort();
        server.close();

        webApp = createWebApp();
        Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "moviefun");
        p.setProperty(EJBContainer.PROVIDER, "tomee-embedded"); // need web feature
        p.setProperty(EJBContainer.MODULES, webApp.getAbsolutePath());
        p.setProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, String.valueOf(port));
        container = EJBContainer.createEJBContainer(p);
    }

    @AfterClass
    public static void stop() {
        if (container != null) {
            container.close();
        }
        if (webApp != null) {
            try {
                FileUtils.forceDelete(webApp);
            } catch (IOException e) {
                FileUtils.deleteQuietly(webApp);
            }
        }
    }

    private static File createWebApp() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/tomee-" + Math.random());
        if (!file.mkdirs() && !file.exists()) {
            throw new RuntimeException("can't create " + file.getAbsolutePath());
        }

        FileUtils.copyDirectory(new File("target/classes"), new File(file, "WEB-INF/classes"));
        FileUtils.copyDirectory(new File("target/test-libs"), new File(file, "WEB-INF/lib"));
        FileUtils.copyDirectory(new File("src/main/webapp"), file);

        return file;
    }

    @Test
    public void testShouldMakeSureWebappIsWorking() throws Exception {
        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage("http://localhost:" + port + "/moviefun/setup.jsp");

        assertMoviesPresent(page);

        page = webClient.getPage("http://localhost:" + port + "/moviefun/moviefun");

        assertMoviesPresent(page);
        webClient.closeAllWindows();
    }

    private void assertMoviesPresent(HtmlPage page) {
        String pageAsText = page.asText();
        assertTrue(pageAsText.contains("Wedding Crashers"));
        assertTrue(pageAsText.contains("Starsky & Hutch"));
        assertTrue(pageAsText.contains("Shanghai Knights"));
        assertTrue(pageAsText.contains("I-Spy"));
        assertTrue(pageAsText.contains("The Royal Tenenbaums"));
    }
}
