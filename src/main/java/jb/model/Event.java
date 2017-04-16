package jb.model;

import org.openqa.selenium.*;

public class Event {

	private String name;
	private String url;

	public Event(WebElement webElement) {
		name = webElement.getText();
		url = webElement.getAttribute("href");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
