package com.jeanneboyarsky.first.model;
import static com.jeanneboyarsky.first.util.AlertWorkarounds.*;

import org.openqa.selenium.*;

public class NameUrlPair implements Comparable<NameUrlPair> {

	private String name;
	private String url;

	public NameUrlPair(WebElement webElement) {
		name = getText(webElement).replace("(Hidden)", "").trim();
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

	@Override
	public int compareTo(NameUrlPair n) {
		return name.compareTo(n.name);
	}

}
