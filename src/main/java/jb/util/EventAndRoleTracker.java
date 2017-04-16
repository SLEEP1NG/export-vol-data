package jb.util;

import static jb.util.Constants.*;

import java.io.*;
import java.nio.file.*;
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
	private List<String> completedEvents;

	public EventAndRoleTracker(WebDriver driver) throws IOException {
		this.driver = driver;
		Path path = Paths.get(STATUS_TRACKER_FILE);
		List<String> lines = Files.readAllLines(path);
		setCompletedEvents(lines);
	}

	private void setCompletedEvents(List<String> lines) {
		String prefix = "Completed logging for event: ";
		completedEvents = lines.stream()
				// get events
				.filter(l -> l.startsWith(prefix))
				// get just the event name
				.map(l -> l.replace(prefix, ""))
				// and turn back into list
				.collect(Collectors.toList());
	}

	public boolean isEventCompleted(String eventName) {
		return completedEvents.contains(eventName);
	}

	public List<NameUrlPair> getEvents() {
		driver.get(HOME_PAGE);
		WebElement eventsTable = driver.findElement(By.id("EventsTable5"));
		List<WebElement> links = eventsTable.findElements(By.xpath("//a[contains(@href, 'EventDetails.aspx')]"));
		return links.stream().map(NameUrlPair::new).collect(Collectors.toList());
	}

	// TODO add retry logic
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
