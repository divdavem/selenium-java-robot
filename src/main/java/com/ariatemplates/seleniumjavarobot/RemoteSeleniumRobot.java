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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteSeleniumRobot implements IRobot {
    private static final Map<Integer, CharSequence> keys = new HashMap<Integer, CharSequence>();
    static {
        keys.put(KeyEvent.VK_0, "0");
        keys.put(KeyEvent.VK_1, "1");
        keys.put(KeyEvent.VK_2, "2");
        keys.put(KeyEvent.VK_3, "3");
        keys.put(KeyEvent.VK_4, "4");
        keys.put(KeyEvent.VK_5, "5");
        keys.put(KeyEvent.VK_6, "6");
        keys.put(KeyEvent.VK_7, "7");
        keys.put(KeyEvent.VK_8, "8");
        keys.put(KeyEvent.VK_9, "9");
        keys.put(KeyEvent.VK_A, "a");
        keys.put(KeyEvent.VK_B, "b");
        keys.put(KeyEvent.VK_C, "c");
        keys.put(KeyEvent.VK_D, "d");
        keys.put(KeyEvent.VK_E, "e");
        keys.put(KeyEvent.VK_F, "f");
        keys.put(KeyEvent.VK_G, "g");
        keys.put(KeyEvent.VK_H, "h");
        keys.put(KeyEvent.VK_I, "i");
        keys.put(KeyEvent.VK_J, "j");
        keys.put(KeyEvent.VK_K, "k");
        keys.put(KeyEvent.VK_L, "l");
        keys.put(KeyEvent.VK_M, "m");
        keys.put(KeyEvent.VK_N, "n");
        keys.put(KeyEvent.VK_O, "o");
        keys.put(KeyEvent.VK_P, "p");
        keys.put(KeyEvent.VK_Q, "q");
        keys.put(KeyEvent.VK_R, "r");
        keys.put(KeyEvent.VK_S, "s");
        keys.put(KeyEvent.VK_T, "t");
        keys.put(KeyEvent.VK_U, "u");
        keys.put(KeyEvent.VK_V, "v");
        keys.put(KeyEvent.VK_W, "w");
        keys.put(KeyEvent.VK_X, "x");
        keys.put(KeyEvent.VK_Y, "y");
        keys.put(KeyEvent.VK_Z, "z");
        keys.put(KeyEvent.VK_CONTROL, Keys.CONTROL);
        keys.put(KeyEvent.VK_SHIFT, Keys.SHIFT);
        keys.put(KeyEvent.VK_ALT, Keys.ALT);
        keys.put(KeyEvent.VK_META, Keys.META);
        keys.put(KeyEvent.VK_ENTER, Keys.RETURN);
        keys.put(KeyEvent.VK_UP, Keys.UP);
        keys.put(KeyEvent.VK_DOWN, Keys.DOWN);
        keys.put(KeyEvent.VK_LEFT, Keys.LEFT);
        keys.put(KeyEvent.VK_RIGHT, Keys.RIGHT);
    }

    private RemoteWebDriver webdriver;
    private Point offsetInBrowserWindow = new Point(0, 0);

    public RemoteSeleniumRobot(RemoteWebDriver webdriver) {
        this.webdriver = webdriver;
    }

    public BufferedImage createScreenCapture(Rectangle screenRect) {
        BufferedImage browserImage;
        byte[] screenCapture = webdriver.getScreenshotAs(OutputType.BYTES);
        ByteArrayInputStream is = new ByteArrayInputStream(screenCapture);
        try {
            browserImage = ImageIO.read(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BufferedImage result = new BufferedImage(screenRect.width, screenRect.height, browserImage.getType());
        Graphics resultGraphics = result.createGraphics();
        Window window = webdriver.manage().window();
        Point browserWindowPosition = window.getPosition();
        Dimension browserSize = window.getSize();
        Graphics browserCoordinates = resultGraphics.create(-screenRect.x + browserWindowPosition.x, -screenRect.y + browserWindowPosition.y,
                browserSize.width, browserSize.height);
        browserCoordinates.drawImage(browserImage, offsetInBrowserWindow.x, offsetInBrowserWindow.y, null);
        browserCoordinates.dispose();
        resultGraphics.dispose();
        return result;
    }

    public void keyPress(int keycode) {
        CharSequence charToSend = keys.get(keycode);
        if (charToSend != null) {
            if (charToSend instanceof Keys) {
                webdriver.getKeyboard().pressKey(charToSend);
            } else {
                webdriver.getKeyboard().sendKeys(charToSend);
            }
        } else {
            throw new IllegalArgumentException("Unknown key: " + keycode);
        }
    }

    public void keyRelease(int keycode) {
        CharSequence charToSend = keys.get(keycode);
        if (charToSend != null) {
            if (charToSend instanceof Keys) {
                webdriver.getKeyboard().releaseKey(charToSend);
            }
        } else {
            throw new IllegalArgumentException("Unknown key: " + keycode);
        }
    }

    public void mouseMove(int x, int y) {
        Point browserWindowPosition = webdriver.manage().window().getPosition();
        Coordinates coord = ((Locatable) webdriver.findElementByTagName("body")).getCoordinates();
        webdriver.getMouse().mouseMove(coord, x - browserWindowPosition.x - offsetInBrowserWindow.x, y - browserWindowPosition.y - offsetInBrowserWindow.y);
    }

    public void mousePress(int buttons) {
        if (buttons != InputEvent.BUTTON1_MASK) {
            throw new UnsupportedOperationException();
        }
        webdriver.getMouse().mouseDown(null);
    }

    public void mouseRelease(int buttons) {
        if (buttons != InputEvent.BUTTON1_MASK) {
            throw new UnsupportedOperationException();
        }
        webdriver.getMouse().mouseUp(null);
    }

    public void mouseWheel(int wheelAmt) {
        throw new UnsupportedOperationException();
    }

}
