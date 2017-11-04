package com.jeanneboyarsky.first.util;

import static com.jeanneboyarsky.first.util.Constants.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/**
 * Some screens take time to load (or for the alert cancellation to go through).
 * This class provides the helper logic for dealing with that.
 * 
 * @author jeanne
 *
 */
public class AlertWorkarounds {
	
	private static final int MAX_SECONDS_TO_WAIT = 60;

	private WebDriver driver;

	public AlertWorkarounds(WebDriver driver) {
		this.driver = driver;
	}

	public void loadEventListDashboardPage() {
		driver.get(HOME_PAGE);
		WebDriverWait wait = new WebDriverWait(driver, MAX_SECONDS_TO_WAIT);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[text()='Manage My Events']")));
	}
	
	public void loadEventDashboardPage(String url) {
		driver.get(url);
		WebDriverWait wait = new WebDriverWait(driver, MAX_SECONDS_TO_WAIT);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[text()='Event Details']")));
	}
	
	/**
	 * getText() used to work and now causes an UnhandledAlertException. Using textDontent as a workaround. 
	 * @return text in element
	 */
	public static String getText(WebElement webElement) {
		return webElement.getAttribute("textContent");
	}

	/**
	 * Shouldn't need this logic since closing alerts by default.
	 * 
	 * @param driver
	 *            webdriver
	 */
	public void clickIfPresent() {
		try {
			Alert alert = driver.switchTo().alert();
			alert.accept();
		} // try
		catch (Exception e) {
			// e.printStackTrace();
			System.out.println("ignored");
			// ignore exception
		}

	}
}
