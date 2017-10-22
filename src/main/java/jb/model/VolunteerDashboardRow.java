package jb.model;

import java.util.*;

import org.openqa.selenium.*;

/**
 * Represents one row representing a role on the event dashboard.
 * 
 * @author jeanne
 *
 */
public class VolunteerDashboardRow {

	private NameUrlPair nameUrlElement;
	private String assignedVolunteers;
	private String unassignedVolunteers;

	public VolunteerDashboardRow(List<WebElement> columns) {
		// use link if present; otherwise use cell directly
		WebElement first = columns.get(0);
		List<WebElement> links = first.findElements(By.tagName("a"));
		if (! links.isEmpty()) {
			first = links.get(0);
		}
		nameUrlElement = new NameUrlPair(first);
		assignedVolunteers = columns.get(2).getText();
		unassignedVolunteers = columns.get(4).getText();
	}

	// ------------------------------------------------------

	public NameUrlPair getNameUrlElement() {
		return nameUrlElement;
	}

	public int getNumberAssignedVolunteers() {
		return Integer.parseInt(assignedVolunteers);
	}

	public int getNumberUnassignedVolunteers() {
		String withoutNewCount = unassignedVolunteers.replaceFirst(" .*$", "");
		return Integer.parseInt(withoutNewCount);
	}

	public boolean isAssignedVolunteersInRow() {
		return getNumberAssignedVolunteers() > 0;
	}

	// ------------------------------------------------------

	/**
	 * Checks the column headers have changed. If they have, need to update the
	 * logic in this class.
	 */
	public void validateHeaderColumns() {
		if (!"Total Volunteer Assignments".equals(assignedVolunteers)) {
			throw new IllegalStateException(
					"The total volunteer assignments column has changed. "
							+ "Please update the constructor of VolunteerDashboardRow");
		}
		if (!"Unassigned\nApplicants".equals(unassignedVolunteers)) {
			throw new IllegalStateException(
					"The unassigned applicants column has changed. "
							+ "Please update the constructor of VolunteerDashboardRow");
		}

	}

}
