package jb;

import static jb.util.Constants.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.csv.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import jb.model.*;
import jb.util.*;

/**
 * Screen scrapes info not in the export (FIRST experience field). Assumes no
 * two volunteers have the same first/last name combination. Skips any
 * volunteers already processed and in the CSV file to support resuming. Assumes
 * at least 1 unassigned applicant.
 * 
 * Note: get alerts from VMS system. Just ignore them in the browser, it seems
 * to proceed anyway
 * 
 * @author Jeanne
 *
 */
public class ExportVolunteerDetail implements AutoCloseable {

	private WebDriver driver;
	private CSVPrinter printer;
	private SortedMap<String, String> roleNameToUrl;
	private EventAndRoleTracker tracker;
	private NameUrlPair currentEvent;
	private VolunteerInfoFile volunteerFileCache;

	// -------------------------------------------------------

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Pass two parameters: email, password and url of event.");
			System.out.println("ex: jb.ExportVolunteerDetail email password");
			System.exit(1);
		}

		Path path = Paths.get(CSV_DATA);
		if (!path.toFile().exists()) {
			Files.createFile(path);
		}
		VolunteerInfoFile volunteerFileCache = new VolunteerInfoFile();
		volunteerFileCache.loadFile(path);

		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
				CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
				ExportVolunteerDetail detail = new ExportVolunteerDetail(printer, volunteerFileCache)) {

			detail.login(args[0], args[1]);
			detail.execute(detail);
		}
	}

	// -------------------------------------------------------

	private ExportVolunteerDetail(CSVPrinter printer, VolunteerInfoFile volunteerFileCache) {
		this.printer = printer;
		this.volunteerFileCache = volunteerFileCache;

		// the FIRST site doesn't work with the htmlunit or phantomjs drivers
		Path gecko = Paths.get("geckodriver-0.15.0/geckodriver");
		System.setProperty("webdriver.gecko.driver", gecko.toAbsolutePath().toString());
		driver = new FirefoxDriver();

		// this doesn't work - get prompts on every page in Firefox
		// (ok because code proceeds despite alerts)
		// https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/27
		((JavascriptExecutor) driver).executeScript("window.alert = function(msg) { }");

		tracker = new EventAndRoleTracker(driver);

	}

	private void login(String userName, String password) {
		driver.get(HOME_PAGE);
		driver.findElement(By.id("EmailAddressTextBox")).sendKeys(userName);
		driver.findElement(By.id("PasswordTextBox")).sendKeys(password);
		driver.findElement(By.id("LoginButton")).click();
	}

	private void execute(ExportVolunteerDetail detail) {
		List<NameUrlPair> events = tracker.getRemainingEvents();
		for (NameUrlPair event : events) {
			currentEvent = event;
			detail.setRoles();
			// TODO remove empty string param
			detail.setVolunteerInfoForAllRoles("");
			detail.setVolunteerInfoForUnassigned();
		}

	}

	private void setRoles() {
		roleNameToUrl = tracker.getRemainingRolesForEventByUrl(currentEvent);
	}

	/*
	 * Can get unassigned applicants from any role so just pick one at random
	 */
	private void setVolunteerInfoForUnassigned() {
		String roleUrl = roleNameToUrl.values().iterator().next();
		System.out.println(roleUrl + " for unassigned volunteers");
		driver.get(roleUrl);

		driver.findElement(By.id("UnassignedTab")).click();
		setVolunteerInfoForSingleRole("Unassigned", "UnassignedTable", false);
	}

	// TODO remove resume point logic
	private void setVolunteerInfoForAllRoles(String roleResumePoint) {

		roleNameToUrl.forEach((roleName, url) -> {

			if (roleName.compareTo(roleResumePoint) >= 0) {

				System.out.println("--- Role: " + roleName + "---");

				driver.get(url);
				setVolunteerInfoForSingleRole(roleName, "ScheduleTable", true);
			}
		});
	}

	private void setVolunteerInfoForSingleRole(String roleName, String tableId, boolean includeRoleAssignment) {
		List<NameUrlPair> volunteerUrls = null;
		try {
			volunteerUrls = getNewVolunteerUrls(tableId);
		} catch (TimeoutException e) {
			// a better way of handling this would be to add a guard clause
			System.out.println("Skipping because no volunteers in this role");
			return;
		}
		for (NameUrlPair volunteerPair : volunteerUrls) {
			// skip this record if got in previous run
			String volunteerName = volunteerPair.getName();
			if (volunteerFileCache.isLogged(currentEvent.getName(), roleName, volunteerName)) {
				System.out.println("Skipping becuase already logged: " + currentEvent.getName() + ", " + roleName + ", " + volunteerName);
			} else {	
				Optional<VolunteerDetail> optional = volunteerFileCache.getVolunteerInfo(volunteerName);
				VolunteerDetail detail;
				// used cached volunteer info if have already looked it up
				if (optional.isPresent()) {
					detail = optional.get();
				} else {
					detail = getVolunteerDetail(includeRoleAssignment, volunteerPair);
				}

				printOneRecord(roleName, detail);
			}
		}
	}

	private VolunteerDetail getVolunteerDetail(boolean includeRoleAssignment, NameUrlPair volunteerPair) {
		VolunteerDetail detail;
		driver.get(volunteerPair.getUrl());
		String commentText = "";
		if (includeRoleAssignment) {
			driver.findElement(By.id("MainContent_RolePreferencesLinkButton")).click();
			WebElement comments = getElementByIdAfterTimeout("MainContent_VolunteerComments");
			commentText = comments.getText();
		}

		WebElement secondarySection = driver.findElement(By.className("secondarySection"));
		String name = secondarySection.findElement(By.tagName("h2")).getText();
		List<WebElement> personalComments = driver.findElements(By.className("personalLabel"));

		System.out.println("Processing " + name);

		List<String> personalDetails = personalComments.stream().map(WebElement::getText).collect(Collectors.toList());

		detail = new VolunteerDetail(name, commentText, personalDetails);
		return detail;
	}

	/*
	 * Convert checked exception to runtime exception so can use with lambdas
	 */
	private void printOneRecord(String roleName, VolunteerDetail detail) {
		try {
			printer.printRecord(detail.getAsArray(currentEvent.getName(), roleName));
			volunteerFileCache.addNewLine(currentEvent.getName(), roleName, detail);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<NameUrlPair> getNewVolunteerUrls(String tableId) {
		WebElement table = getElementByIdAfterTimeout(tableId);
		List<WebElement> volunteers = table.findElements(By.cssSelector("a[href*=People]"));

		return volunteers.stream().filter(new DistinctByKey<>(WebElement::getText)::filter).map(NameUrlPair::new)
				.collect(Collectors.toList());
	}

	private WebElement getElementByIdAfterTimeout(String id) {
		WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));

		return driver.findElement(By.id(id));
	}

	@Override
	public void close() {
		driver.close();
	}

}
