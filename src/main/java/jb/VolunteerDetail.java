package jb;

import java.util.*;

/**
 * The fields we want to include in the CSV file. (Some of these are in the
 * FIRST export and are included for cross checking)
 * 
 * @author nyjeanne
 *
 */
public class VolunteerDetail {

	private String name;
	private String age;
	private String yearsOfService;
	private String gender;
	private String firstExperienceDescription;
	private String rolePreferenceComment;

	static enum PersonalDetailFieldNameEnum {
		AGE, YEARS_OF_SERVICE, GENDER, OTHER_LABEL, NO_LABEL;
		public static PersonalDetailFieldNameEnum getEnumForLabelPrefix(String prefix) {
			PersonalDetailFieldNameEnum result;
			if (prefix.startsWith("Age:")) {
				result = AGE;
			} else if (prefix.startsWith("Service:")) {
				result = YEARS_OF_SERVICE;
			} else if (prefix.startsWith("Gender:")) {
				result = GENDER;
			} else if (prefix.startsWith("Phone Number:") || prefix.startsWith("Email:")
					|| prefix.startsWith("Contact Name:") || prefix.startsWith("Phone:") || prefix.startsWith("Shirt:")
					|| prefix.startsWith("Address:")) {
				result = OTHER_LABEL;
			} else {
				result = NO_LABEL;
			}
			return result;
		}
	}

	public VolunteerDetail(String name, String rolePreferenceComment, List<String> personalDetails) {
		this.name = name;
		this.rolePreferenceComment = rolePreferenceComment;

		setPersonalInfo(personalDetails);
	}

	/**
	 * Yech. This method is a hack because the FIRST experience field isn't
	 * exported and doesn't have a label
	 * 
	 * @param personalDetails
	 */
	private void setPersonalInfo(List<String> personalDetails) {
		// FIRST experience doesn't have a label so make sure only get once
		// after demographic info
		boolean alreadySetFirstExperience = false;

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
				nextFieldName = PersonalDetailFieldNameEnum.getEnumForLabelPrefix(detailField);
				if (nextFieldName == PersonalDetailFieldNameEnum.NO_LABEL && !alreadySetFirstExperience) {
					firstExperienceDescription = detailField;
					alreadySetFirstExperience = true;
				}
			}
		}
	}

	private void setField(PersonalDetailFieldNameEnum nextPersonalField, String value) {
		switch (nextPersonalField) {
		case AGE:
			age = value;
			break;
		case YEARS_OF_SERVICE:
			yearsOfService = value;
			break;
		case GENDER:
			gender = value;
			break;
		case NO_LABEL:
			// intentionally set nothing
			break;
		case OTHER_LABEL:
			// intentionally set nothing
			break;
		default:
			throw new IllegalStateException(nextPersonalField + " is not a valid field name");
		}
	}

	public Object[] getAsArray() {
		List<String> result = new ArrayList<>();
		result.add(nullSafe(name));
		result.add(nullSafe(age));
		result.add(nullSafe(yearsOfService));
		result.add(nullSafe(gender));
		result.add(nullSafe(firstExperienceDescription));
		result.add(nullSafe(rolePreferenceComment));
		return result.toArray();
	}

	private String nullSafe(String text) {
		return (text == null) ? "" : text;
	}

}
