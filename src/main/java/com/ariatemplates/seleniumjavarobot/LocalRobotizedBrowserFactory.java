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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;

import org.apache.commons.exec.OS;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

public abstract class LocalRobotizedBrowserFactory implements IRobotizedBrowserFactory {

    private IRobot robot;
    protected final DesiredCapabilities capabilities;

    private static void numLockStateWorkaround() {
        try {
            // Shift key is not kept pressed while using keyPress method
            // cf https://forums.oracle.com/thread/2232592
            // The workaround is to use the following line:
            Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
            SeleniumJavaRobot.log("Num lock state was successfully changed.");
        } catch (UnsupportedOperationException e) {
            SeleniumJavaRobot.log("Did not change num lock state: " + e);
        }
    }

    public LocalRobotizedBrowserFactory(Capabilities capabilities) {
        try {
            this.robot = new LocalRobot(new Robot());
            this.capabilities = new DesiredCapabilities(capabilities);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        numLockStateWorkaround();
    }

    protected RemoteWebDriver createWebDriver() {
        throw new UnsupportedOperationException();
    }

    protected IBrowser createBrowser() {
        RemoteWebDriver driver = createWebDriver();
        driver.manage().window().maximize();
        return new RemoteWebDriverBrowser(driver);
    }

    public RobotizedBrowser createRobotizedBrowser() {
        return new RobotizedBrowser(robot, createBrowser());
    }

    public static class LocalBrowser<T extends RemoteWebDriver> extends LocalRobotizedBrowserFactory {
        private final Constructor<T> webdriverClass;

        public LocalBrowser(Class<T> webdriverClass, Capabilities capabilities) {
            super(capabilities);
            try {
                this.webdriverClass = webdriverClass.getConstructor(Capabilities.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public RemoteWebDriver createWebDriver() {
            try {
                return webdriverClass.newInstance(capabilities);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class LocalFirefox extends LocalBrowser<FirefoxDriver> {

        public LocalFirefox(Capabilities capabilities) {
            super(FirefoxDriver.class, capabilities);

            String firefoxProfileProperty = System.getProperty("webdriver.firefox.profile");
            if (firefoxProfileProperty == null && !this.capabilities.is(FirefoxDriver.PROFILE)) {
                ProfilesIni allProfiles = new ProfilesIni();
                // Use the default profile to make extensions available,
                // and especially to ease debugging with Firebug

                this.capabilities.setCapability(FirefoxDriver.PROFILE, allProfiles.getProfile("default"));
            }
        }
    }

    public static class LocalSafari extends LocalRobotizedBrowserFactory {

        public LocalSafari(Capabilities capabilities) {
            super(capabilities);
        }

        @Override
        public RemoteWebDriver createWebDriver() {
            SafariDriver safari = new SafariDriver(capabilities);
            if (OS.isFamilyMac()) {
                try {
                    // put the browser in the foreground:
                    String cmdline = "open -a safari";
                    SeleniumJavaRobot.log("Executing: " + cmdline);
                    Runtime.getRuntime().exec(cmdline);
                } catch (Exception e) {
                }
            }
            return safari;
        }
    }

    public static class LocalDebuggableChrome extends LocalRobotizedBrowserFactory {
        public LocalDebuggableChrome(Capabilities capabilities) {
            super(capabilities);
        }

        @Override
        protected IBrowser createBrowser() {
            return new DebuggableChrome(capabilities);
        }
    }

    public static LocalRobotizedBrowserFactory createRobotizedWebDriverFactory(Capabilities capabilities) {
        String browser = capabilities.getBrowserName();
        if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
            return new LocalFirefox(capabilities);
        } else if (BrowserType.SAFARI.equalsIgnoreCase(browser)) {
            return new LocalSafari(capabilities);
        } else if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
            if (capabilities.is(DebuggableChrome.CAPABILITY)) {
                return new LocalDebuggableChrome(capabilities);
            } else {
                return new LocalBrowser<ChromeDriver>(ChromeDriver.class, capabilities);
            }
        } else if (BrowserType.IE.equalsIgnoreCase(browser)) {
            return new LocalBrowser<InternetExplorerDriver>(InternetExplorerDriver.class, capabilities);
        } else {
            throw new RuntimeException("Unknown browser value: " + browser);
        }

    }
}
