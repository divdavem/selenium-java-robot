/*
 * Copyright 2014 Amadeus s.a.s.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ariatemplates.seleniumjavarobot;

import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteRobotizedBrowserFactory implements IRobotizedBrowserFactory {
    private final DesiredCapabilities capabilities;
    private final URL server;

    public RemoteRobotizedBrowserFactory(URL server, Capabilities capabilities) {
        this.capabilities = new DesiredCapabilities(capabilities);
        this.server = server;
    }

    public RobotizedBrowser createRobotizedBrowser() {
        RemoteWebDriver webdriver = new RemoteWebDriver(server, capabilities);
        webdriver.manage().window().maximize();
        return new RobotizedBrowser(new RemoteSeleniumRobot(webdriver), new RemoteWebDriverBrowser(webdriver));
    }

}
