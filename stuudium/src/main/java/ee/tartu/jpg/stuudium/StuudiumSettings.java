package ee.tartu.jpg.stuudium;

import android.content.Context;

public abstract class StuudiumSettings {
	private static final String DEFAULT_PROTOCOL = "https";
	private static final String DEFAULT_API_HOST = "api.ope.ee";
	private static final String DEFAULT_API_VERSION = "v1";
	private static final String DEFAULT_REQUEST_PATTERN = "%s://%s/%s/%s";
	private static final String DEFAULT_AUTH_REDIRECT_URI = "stuudium://response";

	/**
	 * 
	 * @return the protocol used to communicate with Stuudium API (default
	 *         "https")
	 */
	public String getProtocol() {
		return DEFAULT_PROTOCOL;
	}

	/**
	 * 
	 * @return the host used to communicate to Stuudium service (default
	 *         "api.ope.ee")
	 */
	public String getAPIHost() {
		return DEFAULT_API_HOST;
	}

	/**
	 * 
	 * @return version of the API to use (default "v1")
	 */
	public String getAPIVersion() {
		return DEFAULT_API_VERSION;
	}

	/**
	 * 
	 * @return pattern used to generate the request URL (default "%s://%s/%s/%s"
	 *         -> protocol://host/version/request)
	 */
	public String getRequestPattern() {
		return DEFAULT_REQUEST_PATTERN;
	}

	/**
	 * 
	 * @return the URI to redirect to after authenticating (default
	 *         "stuudium://response")
	 */
	public String getAuthRedirectUri() {
		return DEFAULT_AUTH_REDIRECT_URI;
	}

	/**
	 * 
	 * @return the unique ID provided by Stuudium to access the API
	 */
	public abstract String getClientId();

	/**
	 * 
	 * @return the subdomain of your school in Stuudium
	 */
	public abstract String getSubdomain();

	/**
	 * 
	 * @return a String containing the identifier application for the API
	 */
	public abstract String getUserAgent();

	/**
	 * 
	 * @return the context used by the UI of the application, used to create
	 *         login dialog.
	 */
	public abstract Context getContext();



}