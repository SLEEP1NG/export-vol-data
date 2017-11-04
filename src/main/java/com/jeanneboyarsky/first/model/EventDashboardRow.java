package com.jeanneboyarsky.first.model;

import java.util.*;

import org.openqa.selenium.*;

/**
 * Represents one row representing a role on the event dashboard.
 * 
 * @author jeanne
 *
 */
public class EventDashboardRow extends AbstractDashboardRow {

	public EventDashboardRow(List<WebElement> columns) {
		super(columns, 5, 7);
	}

	// ------------------------------------------------------

	/**
	 * Checks the column headers have changed. If they have, need to update the
	 * logic in this class.
	 */
	@Override
	public void validateHeaderColumns() {
		if (!"Volunteers Assigned".equals(getAssignedVolunteers())) {
			throw new IllegalStateException(
					"The volunteer assignments column has changed. "
							+ "Please update EventDashboardRow");
		}
		if (!"Unassigned Applicants".equals(getUnassignedVolunteers())) {
			throw new IllegalStateException(
					"The unassigned applicants column has changed. "
							+ "Please update EventDashboardRow");
		}

	}

}
