package jb.util;
import static jb.util.Constants.*;
import java.util.*;
import java.util.stream.*;

import org.openqa.selenium.*;

import jb.model.*;

/**
 * While I only am VC for one event, Norm is for many. This class keeps track of
 * which events and roles the program has processed so it can automatically
 * resume from the right point.
 * 
 * @author Jeanne
 *
 */
public class EventAndRoleTracker {

	

	private WebDriver driver;

	public EventAndRoleTracker(WebDriver driver) {
		this.driver = driver;
	}

	// TODO add retry logic
	public List<NameUrlPair> getRemainingEvents() {
		driver.get(HOME_PAGE);
		WebElement eventsTable = driver.findElement(By.id("EventsTable5"));
		List<WebElement> links = eventsTable.findElements(By.xpath("//a[contains(@href, 'EventDetails.aspx')]"));
		return links.stream().map(NameUrlPair::new).collect(Collectors.toList());
	}

	public SortedMap<String, String> getRemainingRolesForEventByUrl(NameUrlPair event) {
		driver.get(event.getUrl());

		SortedMap<String, String> roleNameToUrl = new TreeMap<>();

		// ex:
		// https://my.usfirst.org/VMS/Roles/RoleDetails.aspx?ID=17335&RoleID=273
		List<WebElement> roles = driver.findElements(By.cssSelector("a[href*=RoleID]"));
		for (WebElement webElement : roles) {
			roleNameToUrl.put(webElement.getText(), webElement.getAttribute("href"));
		}
		return roleNameToUrl;
	}

}
