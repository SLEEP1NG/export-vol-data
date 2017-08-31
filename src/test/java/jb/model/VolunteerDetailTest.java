package jb.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

public class VolunteerDetailTest {

	private Object[] actual;

	@Test
	public void name() {
		VolunteerDetail detail = new VolunteerDetail("name", null, new ArrayList<String>());
		actual = detail.getAsArray("event", "role");
		assertArrayForCsv("event", "role", "name", "", "", "", "", "");
	}

	@Test
	public void nameAndRolePreferenceComment() {
		VolunteerDetail detail = new VolunteerDetail("name", "role", new ArrayList<String>());
		actual = detail.getAsArray("event", "role");
		assertArrayForCsv("event", "role", "name", "", "", "", "", "role");
	}

	@Test
	public void nameAndPersonalInfo() {
		List<String> personalInfo = Arrays.asList("Phone:", "123-456-7890", "Email: ", "a@b.com", "Address: ",
				"123 Main St", "", "City, State, Zip", "Age: ", "34", "Service: ", "10", "Gender:", "Male", "Shirt:",
				"M", "a", "", "Contact Name:", "Parent", "Phone Number:", "999-999-9999", "My FIRST experience",
				"cert pass");

		VolunteerDetail detail = new VolunteerDetail("name", "role", personalInfo);
		actual = detail.getAsArray("event", "role");
		assertArrayForCsv("event", "role", "name", "34", "10", "Male", "My FIRST experience", "role");
	}

	// ---------------------------------------------------

	@Test
	public void createFromArrayByNameOnly() {
		VolunteerDetail detailFromArray = VolunteerDetail.createFromCsvArray("name");
		actual = detailFromArray.getAsArray("event", "role");
		assertArrayForCsv("event", "role", "name", "", "", "", "", "");
	}

	@Test
	public void createFromArrayWithRolePreference() {
		VolunteerDetail detailFromArray = VolunteerDetail.createFromCsvArray("name", "", "", "", "", "role");
		actual = detailFromArray.getAsArray("event", "role");
		assertArrayForCsv("event", "role", "name", "", "", "", "", "role");
	}

	@Test
	public void createFromArrayWithPersonalInfoAndQuotes() {
		VolunteerDetail detailFromArray = VolunteerDetail.createFromCsvArray("name", "34", "10", "Male",
				"\"My FIRST Experience\"", "role");
		actual = detailFromArray.getAsArray("event", "role");
		assertArrayForCsv("event", "role", "name", "34", "10", "Male", "My FIRST Experience", "role");
	}

	// ---------------------------------------------------

	private void assertArrayForCsv(String... expected) {
		assertArrayEquals(expected, actual);
	}

}
