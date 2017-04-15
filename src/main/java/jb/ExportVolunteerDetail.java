package jb;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.csv.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

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
	private TreeMap<String, String> roleNameToUrl;
	private HashSet<String> processedVolunteerNames;

	// -------------------------------------------------------

	public static void main(String[] args) throws Exception {
		if (args.length != 3 && args.length != 4) {
			System.out.println(
					"Pass three or four parameters: email, password and url of event. Optional is a role name to resume from");
			System.out.println("ex: jb.ExportVolunteerDetail email password https://my.usfirst.org/VMS/Default.aspx");
			System.out.println(
					"ex: jb.ExportVolunteerDetail email password https://my.usfirst.org/VMS/Default.aspx Control System Advisor");
			System.exit(1);
		}

		String roleResumePoint = (args.length == 3) ? "" : args[3];

		Path path = Paths.get("volunteerDetail.csv");
		if (! path.toFile().exists()) {
			Files.createFile(path);
		}
		List<String> volunteersProcessedInPriorRun = Files.lines(path).map(r -> r.split(",")[0])
				.collect(Collectors.toList());

		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
				CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
				ExportVolunteerDetail detail = new ExportVolunteerDetail(printer, volunteersProcessedInPriorRun)) {

			detail.login(args[0], args[1]);
			detail.setRoles(args[2]);
			detail.setVolunteerInfoForAllRoles(roleResumePoint);
			detail.setVolunteerInfoForUnassigned();
		}
	}

	// -------------------------------------------------------

	private ExportVolunteerDetail(CSVPrinter printer, Collection<String> volunteersProcessedInPriorRun) {
		this.printer = printer;
		
		Path gecko = Paths.get("geckodriver-0.15.0/geckodriver");
		System.setProperty("webdriver.gecko.driver", gecko.toAbsolutePath().toString());
		driver = new FirefoxDriver();
		
		processedVolunteerNames = new HashSet<>(volunteersProcessedInPriorRun);

		// this doesn't work - get prompts on every page in Firefox
		// (ok because code proceeds despite alerts)
		// https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/27
		((JavascriptExecutor) driver).executeScript("window.alert = function(msg) { }");

	}

	private void login(String userName, String password) {
		driver.get("https://my.usfirst.org/VMS/Login.aspx");
		driver.findElement(By.id("EmailAddressTextBox")).sendKeys(userName);
		driver.findElement(By.id("PasswordTextBox")).sendKeys(password);
		driver.findElement(By.id("LoginButton")).click();
	}

	private void setRoles(String eventUrl) {
		driver.get(eventUrl);

		roleNameToUrl = new TreeMap<>();

		// ex:
		// https://my.usfirst.org/VMS/Roles/RoleDetails.aspx?ID=17335&RoleID=273
		List<WebElement> roles = driver.findElements(By.cssSelector("a[href*=RoleID]"));
		for (WebElement webElement : roles) {
			roleNameToUrl.put(webElement.getText(), webElement.getAttribute("href"));
		}
	}

	/*
	 * Can get unassigned applicants from any role so just pick one at random
	 */
	private void setVolunteerInfoForUnassigned() {
		String roleUrl = roleNameToUrl.values().iterator().next();
		System.out.println(roleUrl + " for unassigned volunteers");
		driver.get(roleUrl);

		driver.findElement(By.id("UnassignedTab")).click();
		setVolunteerInfoForSingleRole("UnassignedTable", false);
	}

	private void setVolunteerInfoForAllRoles(String roleResumePoint) {

		roleNameToUrl.forEach((roleName, url) -> {

			if (roleName.compareTo(roleResumePoint) >= 0) {

				System.out.println("--- Role: " + roleName + "---");

				driver.get(url);
				setVolunteerInfoForSingleRole("ScheduleTable", true);
			}
		});
	}

	private void setVolunteerInfoForSingleRole(String tableId, boolean includeRoleAssignment) {
		List<String> volunteerUrls = null;
		try {
			volunteerUrls = getNewVolunteerUrls(tableId);
		} catch (TimeoutException e) {
			// a better way of handling this would be to add a guard clause
			System.out.println("Skipping because no volunteers in this role");
			return;
		}
		for (String volunteerUrl : volunteerUrls) {
			driver.get(volunteerUrl);
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

			VolunteerDetail detail = new VolunteerDetail(name, commentText, personalDetails);
			printOneRecord(detail);

			processedVolunteerNames.add(name);

		}
	}

	/*
	 * Convert checked exception to runtime exception so can use with lambdas
	 */
	private void printOneRecord(VolunteerDetail detail) {
		try {
			printer.printRecord(detail.getAsArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> getNewVolunteerUrls(String tableId) {
		WebElement table = getElementByIdAfterTimeout(tableId);
		List<WebElement> volunteers = table.findElements(By.cssSelector("a[href*=People]"));

		return volunteers.stream().filter(e -> !processedVolunteerNames.contains(e.getText()))
				.filter(new DistinctByKey<>(WebElement::getText)::filter)
				.map(e -> e.getAttribute("href")).collect(Collectors.toList());
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
