package jb.model;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.hamcrest.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;

public class EventDashboardRowTest {

	private static final String NAME = "test name";

	private List<WebElement> elements;

	@BeforeEach
	void createList() {
		elements = new ArrayList<>();
	}

	private void addElement(String text) {
		WebElement mockElement = mock(WebElement.class);
		when(mockElement.getText()).thenReturn(text);
		when(mockElement.getAttribute("textContent")).thenReturn(text);
		elements.add(mockElement);
	}
	
	private void addNameElement() {
		WebElement mockElement = mock(WebElement.class);
		WebElement mockNestedElement = mock(WebElement.class);
		List<WebElement> nestedMocks = Arrays.asList(mockNestedElement);
		when(mockElement.findElements(By.tagName("a"))).thenReturn(nestedMocks);
		when(mockNestedElement.getAttribute("textContent")).thenReturn(NAME);
		elements.add(mockElement);
	}

	private void addElements(String assignedColumn, String unassignedColumn) {
		addNameElement();
		addElement("start date");
		addElement("# days");
		addElement("program");
		addElement("event target");
		addElement(assignedColumn);
		addElement("tentative");
		addElement(unassignedColumn);
		addElement("# teams");
	}
	
	private void addHeaderElements(String assignedColumn, String unassignedColumn) {
		addElement("name/url");
		addElement("start date");
		addElement("# days");
		addElement("program");
		addElement("event target");
		addElement(assignedColumn);
		addElement("tentative");
		addElement(unassignedColumn);
		addElement("# teams");
	}

	// -----------------------------------------------
	@Test
	void withAssignedVolunteer() {
		addElements("1", "0");

		EventDashboardRow actual = new EventDashboardRow(elements);
		assertEquals(NAME, actual.getNameUrlElement().getName(), "name");
		assertEquals(1, actual.getNumberAssignedVolunteers(), "# assigned");
		assertEquals(0, actual.getNumberUnassignedVolunteers(), "# unassigned");
		assertTrue(actual.isAssignedVolunteersInRow(), "volunteers in record");
	}

	@Test
	void withUnassignedVolunteer() {
		addElements("0", "1");

		EventDashboardRow actual = new EventDashboardRow(elements);
		assertEquals(NAME, actual.getNameUrlElement().getName(), "name");
		assertEquals(0, actual.getNumberAssignedVolunteers(), "# assigned");
		assertEquals(1, actual.getNumberUnassignedVolunteers(), "# unassigned");
		assertFalse(actual.isAssignedVolunteersInRow(), "volunteers in record");
	}
	
	@Test
	void withNewUnassignedVolunteers() {
		addElements("0", "4 (2 new)");

		EventDashboardRow actual = new EventDashboardRow(elements);
		assertEquals(NAME, actual.getNameUrlElement().getName(), "name");
		assertEquals(0, actual.getNumberAssignedVolunteers(), "# assigned");
		assertEquals(4, actual.getNumberUnassignedVolunteers(), "# unassigned");
		assertFalse(actual.isAssignedVolunteersInRow(), "volunteers in record");
	}

	@Test
	void withNoVolunteers() {
		addElements("0", "0");

		EventDashboardRow actual = new EventDashboardRow(elements);
		assertEquals(NAME, actual.getNameUrlElement().getName(), "name");
		assertEquals(0, actual.getNumberAssignedVolunteers(), "# assigned");
		assertEquals(0, actual.getNumberUnassignedVolunteers(), "# unassigned");
		assertFalse(actual.isAssignedVolunteersInRow(), "volunteers in record");
	}

	// -----------------------------------------------
	@Test
	void headerColumnsSame() {
		addHeaderElements("Volunteers\n  Assigned  ", "Unassigned\n  Applicants  ");
		EventDashboardRow actual = new EventDashboardRow(elements);
		actual.validateHeaderColumns();
	}

	@Test
	void changeInAssignmentTotalColumn() {
		addHeaderElements("Other Volunteer Assignments", "Unassigned\n  Applicants");
		EventDashboardRow row = new EventDashboardRow(elements);
		IllegalStateException actual = assertThrows(IllegalStateException.class,
				() -> row.validateHeaderColumns());
		assertThat(actual.getMessage(), Matchers.startsWith("The volunteer assignments column has changed."));
	}

	@Test
	void changeInUnassignedTotalColumn() {
		addHeaderElements("Volunteers\n  Assigned", "Other\n  Applicants");
		EventDashboardRow row = new EventDashboardRow(elements);
		IllegalStateException actual = assertThrows(IllegalStateException.class,
				() -> row.validateHeaderColumns());
		assertThat(actual.getMessage(), Matchers.startsWith("The unassigned applicants column has changed."));
	}

}
