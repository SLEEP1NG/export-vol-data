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
		String assigned = getAssignedVolunteers();
		if (!"TotalVolunteerAssignments".equals(assigned)) {
			throw new IllegalStateException(
					"The total volunteer assignments column has changed. It is now showing as " + assigned
							+ ". Please update VolunteerDashboardRow");
		}
		String unassigned = getUnassignedVolunteers();
		if (!"UnassignedApplicants".equals(unassigned)) {
			throw new IllegalStateException(
					"The unassigned applicants column has changed. It is now showing as " + unassigned
							+ ". Please update VolunteerDashboardRow");
		}
	}

}
