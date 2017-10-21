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
// printlns ok because a command line program 
@SuppressWarnings("squid:S106")
public class EventAndRoleTracker {

	private WebDriver driver;
	private List<String> completedEvents;
	private List<String> completedEventRolePairs;

	public EventAndRoleTracker(WebDriver driver) throws IOException {
		this.driver = driver;
		Path path = Paths.get(STATUS_TRACKER_FILE);
		List<String> lines = Files.readAllLines(path);
		setCompletedEvents(lines);
		setCompletedEventRolePairs(lines);
	}

	private void setCompletedEvents(List<String> lines) {
		String prefix = "Completed logging for event: ";
		completedEvents = lines.stream()
				// get events
				.filter(l -> l.startsWith(prefix))
				// get just the event name
				.map(l -> l.replace(prefix, "").trim())
				// and turn back into list
				.collect(Collectors.toList());
	}

	private void setCompletedEventRolePairs(List<String> lines) {
		String prefix = "Completed logging for event/role: ";
		completedEventRolePairs = lines.stream()
				// get events
				.filter(l -> l.startsWith(prefix))
				// get just the event name
				.map(l -> l.replace(prefix, "").trim())
				// and turn back into list
				.collect(Collectors.toList());
	}

	public boolean isEventCompleted(NameUrlPair event) {
		String eventName = event.getName();
		boolean result = completedEvents.contains(eventName);
		if (result) {
			System.out.println("Skipping event " + eventName + " because already logged");
		}
		return result;
	}

	public boolean isEventRoleCompleted(NameUrlPair event, String roleName) {
		String eventName = event.getName();
		boolean result = completedEventRolePairs.contains(eventName + "/" + roleName);
		if (result) {
			System.out.println("Skipping event/role " + eventName + "/" + roleName + " because already logged");
		}
		return result;
	}

	public List<NameUrlPair> getRemainingEvents() {
		// Match URls of format: https://my.usfirst.org/VMS/Events/EventDetails.aspx?ID=27887
		List<WebElement> links = driver.findElements(By.cssSelector("a[href*='Events']"));
		System.out.println("# events: " + links.size());
		return links.stream().map(NameUrlPair::new)
				// remove completed events
				.filter(e -> !isEventCompleted(e))
				// sort
				.sorted()
				// convert to list
				.collect(Collectors.toList());
	}

	public List<NameUrlPair> getRemainingRolesForEventByUrl(NameUrlPair event) {
		driver.get(event.getUrl());
		
		// there are separate tables for key and non-key roles
		List<WebElement> roleTables = driver.findElements(By.className("EventRoleTable"));
		return roleTables.stream().flatMap(row -> getRemainingRolesForTableInEvent(event, row))
				// sort
				.sorted((a, b) -> a.getName().compareTo(b.getName()))
				// turn back into list
				.collect(Collectors.toList());
	}
	
	private Stream<NameUrlPair> getRemainingRolesForTableInEvent(NameUrlPair event, WebElement row) {
		// ex: https://my.usfirst.org/VMS/Roles/RoleDetails.aspx?ID=17335&RoleID=273
		List<WebElement> roles = row.findElements(By.cssSelector("a[href*=RoleID]"));
		return roles.stream().map(NameUrlPair::new)
				// remove completed
				.filter(r -> !isEventRoleCompleted(event, r.getName()));
	}
	
	public NameUrlPair getAnyRoleForEventByUrl(NameUrlPair event) {
		driver.get(event.getUrl());

		// ex:
		// https://my.usfirst.org/VMS/Roles/RoleDetails.aspx?ID=17335&RoleID=273
		List<WebElement> roles = driver.findElements(By.cssSelector("a[href*=RoleID]"));
		return roles.stream().map(NameUrlPair::new).findAny().orElseThrow(RuntimeException::new);
	}

}
