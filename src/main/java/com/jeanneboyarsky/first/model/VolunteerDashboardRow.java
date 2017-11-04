package com.jeanneboyarsky.first.model;

import java.util.*;

import org.openqa.selenium.*;

/**
 * Represents one row representing a role on the event dashboard.
 * 
 * @author jeanne
 *
 */
public class VolunteerDashboardRow extends AbstractDashboardRow {

	public VolunteerDashboardRow(List<WebElement> columns) {
		super(columns, 2, 4);
	}

	// ------------------------------------------------------

	/**
	 * Checks the column headers have changed. If they have, need to update the
	 * logic in this method or the column index in the constructor.
	 */
	@Override
	public void validateHeaderColumns() {
		if (!"Total Volunteer Assignments".equals(getAssignedVolunteers())) {
			throw new IllegalStateException(
					"The total volunteer assignments column has changed. "
							+ "Please update VolunteerDashboardRow");
		}
		if (!"Unassigned Applicants".equals(getUnassignedVolunteers())) {
			throw new IllegalStateException(
					"The unassigned applicants column has changed. "
							+ "Please update VolunteerDashboardRow");
		}

	}

}
