package jb.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import jb.model.*;

/**
 * Cache data in file so don't duplicate event/role/name combinations and
 * provide optimization. (if already have volunteer's info, don't need to parse
 * it again when they volunteer in another role or at another event.)
 * 
 * @author Jeanne
 *
 */
public class VolunteerInfoFile {

	private List<String> linesInFile;
	private Set<String> eventRoleVolunteerCache;
	private Map<String, VolunteerDetail> volunteerToDetailsCache;

	public VolunteerInfoFile() {
		linesInFile = new ArrayList<>();
		eventRoleVolunteerCache = new HashSet<>();
		volunteerToDetailsCache = new HashMap<>();
	}
	
	public void loadFile(Path path) throws IOException {
		setLinesFromPreviousRun(Files.readAllLines(path));
	}

	void setLinesFromPreviousRun(List<String> lines) {
		lines.forEach(this::addLineFromPreviousRun);
	}

	void addLineFromPreviousRun(String line) {
		linesInFile.add(line);
		
		// add to event cache
		String[] columns = line.split(",");
		eventRoleVolunteerCache.add(columns[0] + "," + columns[1] + "," + columns[2]);
		
		// add to volunteer cache
		String[] columnsWithoutEventRole = Arrays.copyOfRange(columns, 2, columns.length);
		VolunteerDetail detail = VolunteerDetail.createFromCsvArray(columnsWithoutEventRole);
		volunteerToDetailsCache.put(detail.getName(), detail);
	}

	public void addNewLine(String eventName, String roleName, VolunteerDetail detail) {
		eventRoleVolunteerCache.add(eventName + "," + roleName + "," + detail.getName());
		volunteerToDetailsCache.put(detail.getName(), detail);
	}

	public boolean isLogged(String eventName, String roleName, String volunteerName) {
		return eventRoleVolunteerCache.contains(eventName + "," + roleName + "," + volunteerName);
	}

	public Optional<VolunteerDetail> getVolunteerInfo(String volunteerName) {
		VolunteerDetail detail = volunteerToDetailsCache.get(volunteerName);
		return Optional.ofNullable(detail);
	}

}
