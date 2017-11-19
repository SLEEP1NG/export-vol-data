package com.jeanneboyarsky.first.model;

import java.util.*;
import java.util.stream.*;

/**
 * The fields we want to include in the CSV file. (Some of these are in the
 * FIRST export and are included for cross checking)
 * 
 * @author Jeanne
 *
 */
public class VolunteerDetail {

	private String name;
	private String age;
	private String phone;
	private String email;
	private String address;
	private String yearsOfService;
	private String gender;
	private String shirt;
	private String firstExperienceDescription;
	private String rolePreferenceComment;

	enum PersonalDetailFieldNameEnum {
		AGE, YEARS_OF_SERVICE, PHONE, EMAIL, ADDRESS, GENDER, SHIRT, OTHER_LABEL, NO_LABEL;
		public static PersonalDetailFieldNameEnum getEnumForLabelPrefix(String prefix) {
			PersonalDetailFieldNameEnum result;
			if (prefix.startsWith("Age:")) {
				result = AGE;
			} else if (prefix.startsWith("Phone:")) {
				result = PHONE;
			} else if (prefix.startsWith("Email:")) {
				result = EMAIL;
			} else if (prefix.startsWith("Address:")) {
				result = PersonalDetailFieldNameEnum.ADDRESS;
			} else if (prefix.startsWith("Service:")) {
				result = YEARS_OF_SERVICE;
			} else if (prefix.startsWith("Gender:")) {
				result = GENDER;
			} else if (prefix.startsWith("Shirt:")) {
				result = SHIRT;
			} else if (isIgnoredLabel(prefix)) {
				result = OTHER_LABEL;
			} else {
				result = NO_LABEL;
			}
			return result;
		}

		private static boolean isIgnoredLabel(String prefix) {
			return Stream.of("Phone Number:", "Contact Name:").anyMatch(prefix::startsWith);
		}
	}

	public VolunteerDetail(String name, String rolePreferenceComment, List<String> personalDetails) {
		this.name = name;
		this.rolePreferenceComment = rolePreferenceComment;

		setPersonalInfo(personalDetails);
	}

	public VolunteerDetail(String name, String age, String phone, String email, String address, String yearsOfService,
			String gender, String shirt,
			String firstExperienceDescription, String rolePreferenceComment) {
		super();
		this.name = name;
		this.age = age;
		this.phone = phone;
		this.email = email;
		this.address = address;
		this.yearsOfService = yearsOfService;
		this.gender = gender;
		this.shirt = shirt;
		this.firstExperienceDescription = firstExperienceDescription;
		this.rolePreferenceComment = rolePreferenceComment;
	}

	/**
	 * Yech. This method is a hack because the FIRST experience field isn't exported
	 * and doesn't have a label
	 * 
	 * @param personalDetails
	 */
	private void setPersonalInfo(List<String> personalDetails) {
		// FIRST experience doesn't have a label so make sure only get once
		// after demographic info
		boolean alreadySetFirstExperience = false;

		PersonalDetailFieldNameEnum previousFieldName = null;
		PersonalDetailFieldNameEnum nextFieldName = null;
		for (String detailField : personalDetails) {
			// hack to determine when FIRST experience coming up
			if (detailField.startsWith("Contact Name")) {
				alreadySetFirstExperience = false;
			}
			if (nextFieldName != null) {
				// if value part of key/value pair
				setField(nextFieldName, detailField);
				nextFieldName = null;
			} else {
				// in key part or FIRST experience field
				// address is multiple lines
				nextFieldName = PersonalDetailFieldNameEnum.getEnumForLabelPrefix(detailField);
				if (nextFieldName == PersonalDetailFieldNameEnum.NO_LABEL
						&& previousFieldName == PersonalDetailFieldNameEnum.ADDRESS) {
					nextFieldName = PersonalDetailFieldNameEnum.ADDRESS;
				}
				if (nextFieldName == PersonalDetailFieldNameEnum.NO_LABEL && !alreadySetFirstExperience) {
					firstExperienceDescription = detailField;
					alreadySetFirstExperience = true;
				}
				previousFieldName = nextFieldName;
			}
		}
	}

	private void setField(PersonalDetailFieldNameEnum nextPersonalField, String value) {
		switch (nextPersonalField) {
		case AGE:
			age = value;
			break;
		case PHONE:
			phone = value;
			break;
		case EMAIL:
			email = value;
			break;
		case ADDRESS:
			appendToAddress(value);
			break;
		case YEARS_OF_SERVICE:
			yearsOfService = value;
			break;
		case GENDER:
			gender = value;
			break;
		case SHIRT:
			shirt = value;
			break;
		case NO_LABEL:
		case OTHER_LABEL:
			// intentionally set nothing
			break;
		default:
			throw new IllegalStateException(nextPersonalField + " is not a valid field name");
		}
	}

	private void appendToAddress(String value) {
		if (address == null) {
			address = value;
		} else {
			address += " ";
			address += value;
		}
	}

	public String[] getAsArray(String eventName, String roleName) {
		List<String> result = new ArrayList<>();
		result.add(eventName);
		result.add(roleName);
		result.add(nullSafe(name));
		result.add(nullSafe(age));
		result.add(nullSafe(phone));
		result.add(nullSafe(email));
		result.add(nullSafe(address));
		result.add(nullSafe(yearsOfService));
		result.add(nullSafe(gender));
		result.add(nullSafe(shirt));
		result.add(nullSafe(firstExperienceDescription));
		result.add(nullSafe(rolePreferenceComment));
		return result.toArray(new String[0]);
	}

	public static VolunteerDetail createFromCsvArray(String... fields) {
		String name = getField(fields, 0);
		String age = getField(fields, 1);
		String phone = getField(fields, 2);
		String email = getField(fields, 3);
		String address = getField(fields, 4);
		String yearsOfService = getField(fields, 5);
		String gender = getField(fields, 6);
		String shirt = getField(fields, 7);
		String firstExperienceDescription = getField(fields, 8);
		String rolePreferenceComment = getField(fields, 9);
		return new VolunteerDetail(name, age, phone, email, address, yearsOfService, gender, shirt,
				firstExperienceDescription, rolePreferenceComment);
	}

	private static String getField(String[] fields, int index) {
		if (fields.length <= index) {
			return "";
		}
		String value = fields[index];
		return value.replaceFirst("^\"", "").replaceFirst("\"$", "");
	}

	private String nullSafe(String text) {
		return (text == null) ? "" : text;
	}

	public String getName() {
		return name;
	}

}
