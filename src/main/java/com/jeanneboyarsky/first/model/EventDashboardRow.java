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
		String assigned = getAssignedVolunteers();
		if (!"VolunteersAssigned".equals(assigned)) {
			throw new IllegalStateException(
					"The volunteer assignments column has changed. It is now showing as " + assigned
							+ ". Please update EventDashboardRow");
		}
		String unassigned = getUnassignedVolunteers();
		if (!"UnassignedApplicants".equals(unassigned)) {
			throw new IllegalStateException(
					"The unassigned applicants column has changed. It is now showing as " + unassigned
							+ ". Please update EventDashboardRow");
		}
	}

}
