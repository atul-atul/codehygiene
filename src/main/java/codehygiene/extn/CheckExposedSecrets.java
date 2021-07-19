package codehygiene.extn;

public class CheckExposedSecrets {
	private String userAllowedPatterns = "";

	public String getUserAllowedPatterns() {
		return userAllowedPatterns;
	}

	public void setUserAllowedPatterns(String userAllowedPatterns) {
		this.userAllowedPatterns = userAllowedPatterns;
	}
}
