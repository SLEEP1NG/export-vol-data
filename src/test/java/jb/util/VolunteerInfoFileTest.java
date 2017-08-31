package jb.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

import jb.model.*;

public class VolunteerInfoFileTest {

	private VolunteerInfoFile target;

	@BeforeEach
	public void setUpData() {
		target = new VolunteerInfoFile();
		List<String> lines = new ArrayList<>();
		lines.add("event,role,volunteer,a,b,c");
		target.setLinesFromPreviousRun(lines);
	}

	private void addDetail() {
		VolunteerDetail detail = new VolunteerDetail("newName", "comment", new ArrayList<>());
		target.addNewLine("event", "role", detail);
	}

	@Test
	public void isLogged_fromFile() {
		assertTrue(target.isLogged("event", "role", "volunteer"));
	}

	@Test
	public void isLogged_fromObject() {
		addDetail();
		assertTrue(target.isLogged("event", "role", "newName"));
	}

	@Test
	public void isNotLogged() {
		assertFalse(target.isLogged("different", "role", "volunteer"));
		assertFalse(target.isLogged("event", "different", "volunteer"));
		assertFalse(target.isLogged("event", "role", "different"));
	}

	@Test
	public void getVolunteerInfo_fromFile() {
		Optional<VolunteerDetail> actual = target.getVolunteerInfo("volunteer");
		assertTrue(actual.isPresent());
		assertEquals("volunteer", actual.get().getName());
	}

	@Test
	public void getVolunteerInfo_fromObject() {
		addDetail();
		Optional<VolunteerDetail> actual = target.getVolunteerInfo("newName");
		assertTrue(actual.isPresent());
		assertEquals("newName", actual.get().getName());
	}

	@Test
	public void getVolunteerInfo_forNotPresent() {
		Optional<VolunteerDetail> actual = target.getVolunteerInfo("different");
		assertFalse(actual.isPresent());
	}

}
