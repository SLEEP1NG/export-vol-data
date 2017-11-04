package com.jeanneboyarsky.first.model;
import static com.jeanneboyarsky.first.util.AlertWorkarounds.*;

import java.util.*;

import org.openqa.selenium.*;

/**
 * Common logic for rows in table on Event and Volunteer pages.
 * 
 * @author jeanne
 *
 */
public abstract class AbstractDashboardRow {

	private NameUrlPair nameUrlElement;
	private String assignedVolunteers;
	private String unassignedVolunteers;

	// ------------------------------------------------------

	/**
	 * Checks the column headers have changed. If they have, need to update the
	 * logic.
	 */
	public abstract void validateHeaderColumns();
	// ------------------------------------------------------

	AbstractDashboardRow(List<WebElement> columns, int assignedIndex, int unassignedIndex) {
		// use link if present; otherwise use cell directly
		WebElement first = columns.get(0);
		List<WebElement> links = first.findElements(By.tagName("a"));
		if (!links.isEmpty()) {
			first = links.get(0);
		}
		nameUrlElement = new NameUrlPair(first);
		assignedVolunteers = getText(columns.get(assignedIndex));
		unassignedVolunteers = getText(columns.get(unassignedIndex));
	}

	// ------------------------------------------------------

	public NameUrlPair getNameUrlElement() {
		return nameUrlElement;
	}

	public int getNumberAssignedVolunteers() {
		return Integer.parseInt(assignedVolunteers);
	}
	
	public String getAssignedVolunteers() {
		return normalizeWhitespace(assignedVolunteers);
	}
	
	private String normalizeWhitespace(String value) {
		return value.replaceAll("\\s+", " ").trim();
	}

	public int getNumberUnassignedVolunteers() {
		String withoutNewCount = unassignedVolunteers.replaceFirst(" .*$", "");
		return Integer.parseInt(withoutNewCount);
	}
	
	public String getUnassignedVolunteers() {
		return normalizeWhitespace(unassignedVolunteers);
	}

	public boolean isAssignedVolunteersInRow() {
		return getNumberAssignedVolunteers() > 0;
	}

}
