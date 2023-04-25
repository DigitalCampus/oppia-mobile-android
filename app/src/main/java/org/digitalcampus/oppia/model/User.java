/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.utils.CryptoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

	public static final String API_KEY = "api_key";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String EMAIL = "email";
	public static final String ORGANISATION = "organisation";
	public static final String JOB_TITLE = "job_title";
	public static final String SCORING_ENABLED = "scoring";
	public static final String BADGING_ENABLED = "badging";
	public static final String POINTS = "points";
	public static final String BADGES = "badges";
	public static final String COHORTS = "cohorts";

	private long userId;
	private String username;
	private String email;
	private String password;
	private String passwordAgain;
	private String firstname;
	private String lastname;
	private String apiKey;
	private String jobTitle;
	private String organisation;
	private String phoneNo;
	private String passwordEncrypted;
	private boolean scoringEnabled = true;
	private boolean badgingEnabled = true;
	private int points = 0;
	private int badges = 0;
	private boolean offlineRegister = false;
	private List<Integer> cohorts = new ArrayList<>();

	private Map<String, CustomValue> userCustomFields = new HashMap<>();

	private boolean localUser;

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPasswordAgain() {
		return passwordAgain;
	}
	public void setPasswordAgain(String passwordAgain) {
		this.passwordAgain = passwordAgain;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getDisplayName() {
		return firstname + " " + lastname;
	}

	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public int getBadges() {
		return badges;
	}
	public void setBadges(int badges) {
		this.badges = badges;
	}
	public boolean isScoringEnabled() {
		return scoringEnabled;
	}
	public void setScoringEnabled(boolean scoringEnabled) {
		this.scoringEnabled = scoringEnabled;
	}

	public boolean isBadgingEnabled() {
		return badgingEnabled;
	}
	public void setBadgingEnabled(boolean badgingEnabled) {
		this.badgingEnabled = badgingEnabled;
	}

	public String getPasswordEncrypted() {
		if (this.passwordEncrypted == null){
			this.passwordEncrypted = CryptoUtils.encryptLocalPassword(this.password);
		}
		return this.passwordEncrypted;
	}

	public void setPasswordEncrypted(String pwEncrypted){
		this.passwordEncrypted = pwEncrypted;
	}

	public String getPasswordHashed(){
		if (this.password != null){
			return CryptoUtils.encryptExternalPassword(this.password);
		}
		else return "";
	}

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getJobTitle() {
		return jobTitle;
	}
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	public String getOrganisation() {
		return organisation;
	}
	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}
	public String getPhoneNo() {
		return phoneNo;
	}
	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}


	public boolean isOfflineRegister() {
		return offlineRegister;
	}

	public void setOfflineRegister(boolean offlineRegister) {
		this.offlineRegister = offlineRegister;
	}

	public Map<String, CustomValue> getUserCustomFields() {
		return userCustomFields;
	}

	public CustomValue getCustomField(String key){
		return userCustomFields.get(key);
	}

	public void putCustomField(String key, CustomValue value){
		userCustomFields.put(key, value);
	}

	public void setUserCustomFields(Map<String, CustomValue> userCustomFields) {
		this.userCustomFields = userCustomFields;
	}

	private void setCustomFieldsFromJSON(Context ctx, JSONObject json) throws JSONException {
		List<CustomField> cFields = DbHelper.getInstance(ctx).getCustomFields();
		for (CustomField field : cFields) {
			String key = field.getKey();
			if (json.has(key)) {
				if (field.isString()) {
					String value = json.getString(key);
					this.putCustomField(key, new CustomValue<>(value));
				} else if (field.isBoolean()) {
					boolean value = json.getBoolean(key);
					this.putCustomField(key, new CustomValue<>(value));
				} else if (field.isInteger()) {
					int value = json.getInt(key);
					this.putCustomField(key, new CustomValue<>(value));
				} else if (field.isFloat()) {
					float value = (float) json.getDouble(key);
					this.putCustomField(key, new CustomValue<>(value));
				}
			}
		}
	}

	public void setCohorts(List<Integer> cohorts) {
		this.cohorts = cohorts;
	}

	public void setCohortsFromJSONArray(JSONArray cohortsJson) throws JSONException {
		List<Integer> cohorts = new ArrayList<>();
		for (int i = 0; i < cohortsJson.length(); i++) {
			cohorts.add(cohortsJson.getInt(i));
		}
		this.setCohorts(cohorts);
	}

	public void updateFromJSON(Context ctx, JSONObject json) throws JSONException {

		this.setFirstname(json.getString(User.FIRST_NAME));
		this.setLastname(json.getString(User.LAST_NAME));
		if (json.has(User.API_KEY)){
			this.setApiKey(json.getString(User.API_KEY));
		}
		if (json.has(User.EMAIL)) {
			this.setEmail(json.getString(User.EMAIL));
		}
		if (json.has(User.ORGANISATION)) {
			this.setOrganisation(json.getString(User.ORGANISATION));
		}
		if (json.has(User.JOB_TITLE)) {
			this.setJobTitle(json.getString(User.JOB_TITLE));
		}
		this.setCustomFieldsFromJSON(ctx, json);

		// Set user cohorts
		if (json.has(User.COHORTS)) {
			JSONArray cohortsJson = json.getJSONArray(User.COHORTS);
			this.setCohortsFromJSONArray(cohortsJson);
		}

		// Set badging and scoring data
		try {
			this.setPoints(json.getInt(User.POINTS));
			this.setBadges(json.getInt(User.BADGES));
			this.setScoringEnabled(json.getBoolean(User.BADGING_ENABLED));
			this.setBadgingEnabled(json.getBoolean(User.BADGING_ENABLED));
		} catch (JSONException e) {
			this.setPoints(0);
			this.setBadges(0);
			this.setScoringEnabled(true);
			this.setBadgingEnabled(true);
		}

	}


	public List<Integer> getCohorts() {
		return this.cohorts;
	}

	public boolean isLocalUser() {
		return localUser;
	}

	public void setLocalUser(boolean localUser) {
		this.localUser = localUser;
	}
}
