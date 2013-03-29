package com.nguyenmp.puushforjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.nguyenmp.puushforjava.parser.AccountParser;
import com.nguyenmp.puushforjava.parser.DisplayedPoolParser;
import com.nguyenmp.puushforjava.parser.PoolParser;
import com.nguyenmp.puushforjava.parser.SettingsParser;
import com.nguyenmp.puushforjava.things.Account;
import com.nguyenmp.puushforjava.things.Application;
import com.nguyenmp.puushforjava.things.DisplayedPool;
import com.nguyenmp.puushforjava.things.Pool;

@SuppressWarnings("deprecation")
public class PuushClient {
	
	/**
	 * Tries to log into puush.me using the given credentials.
	 * @param email the email of the user
	 * @param password the password of the user
	 * @return a <code>CookieStore</code> with the cookie of a logged in user.  
	 * <code>null</code> of logging in failed.
	 * @throws URISyntaxException should never occur.
	 * @throws IOException if an I/O error occurs, like during an opening connection.
	 * @throws ClientProtocolException in case of an http protocol error
	 */
	public static CookieStore login(String email, String password) throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		client.setRedirectHandler(new MyRedirectHandler());
		HttpContext context = getContext(null);
		
		HttpPost post = new HttpPost();
		post.setURI(new URI("http://puush.me/login/go"));
		
		List<NameValuePair> args = new ArrayList<NameValuePair>();
		args.add(new BasicNameValuePair("email", email));
		args.add(new BasicNameValuePair("password", password));
		args.add(new BasicNameValuePair("login", "Login →"));
		
		HttpEntity postEntity = new UrlEncodedFormEntity(args);
		
		post.setEntity(postEntity);
		
		HttpResponse response = client.execute(post, context);
		
		post.abort();
		client.getConnectionManager().shutdown();
		
		if (response.containsHeader("Location") && response.getFirstHeader("Location").getValue().equals("http://puush.me/login/retry/")) return null;
		
		return getCookieStore(context);
	}
	
	/**
	 * Logs out the session cookie, essentially expiring them.  the effects of this on other sessions is unknown.
	 * Failure will not be handled.
	 * @param cookies the cookies to expire.
	 * @throws URISyntaxException should never occur.
	 * @throws IOException if an I/O error occurs, like during an opening connection.
	 * @throws ClientProtocolException in case of an http protocol error
	 */
	public static void logout(CookieStore cookies) throws ClientProtocolException, IOException, URISyntaxException {
		HttpClient client = new DefaultHttpClient();
		HttpContext context = getContext(cookies);
		
		HttpGet get = new HttpGet();
		get.setURI(new URI("http://puush.me/logout/"));
		
		client.execute(get, context);
		
		get.abort();
		client.getConnectionManager().shutdown();
	}
	
	/**
	 * Resets the API key used by third party apps.
	 * @param cookies the cookies of the user who will reset their API key
	 * @return the new key.
	 * @throws URISyntaxException should never occur.
	 * @throws IOException if an I/O error occurs, like during an opening connection.
	 * @throws ClientProtocolException in case of an http protocol error
	 * @throws LoginException if the cookie is not valid or logged in
	 */
	public static String resetAPIKey(CookieStore cookies) throws URISyntaxException, ClientProtocolException, IOException, LoginException {
		DefaultHttpClient client = new DefaultHttpClient();
		client.setRedirectHandler(new MyRedirectHandler());
		
		HttpContext context = getContext(cookies);
		
		HttpGet get = new HttpGet();
		get.setURI(new URI("http://puush.me/account/reset_api_key"));
		
		HttpResponse response = client.execute(get, context);
		get.abort();
		client.getConnectionManager().shutdown();
		
		URL uri = null;
		try {
			uri = new URL(response.getHeaders("Location")[0].getValue());
		} catch (MalformedURLException e) {
			throw new LoginException();
		}
		
		String query = uri.getQuery();
		
		int start = query.indexOf("k=") + "k=".length();
		int end = query.indexOf("&", start);
		if (end == -1) end = query.length();
		
		return query.substring(start, end);
	}
	
	/**
	 * Change the password for a user.
	 * @param currentPassword The current password of the user
	 * @param newPassword The new password for the user.  Can be the same as the old one.
	 * @param cookies The cookie for user who will get their password changed
	 * @return <code>null</code> on success. On failure, string will contain the error message.
	 * @throws ClientProtocolException in case of an http protocol error
	 * @throws IOException connection could not be made or some other I/O error occurred.
	 * @throws URISyntaxException If the given string violates RFC 2396, as augmented by the above deviations. should never be thrown.
	 */
	public static String changePassword(String currentPassword, String newPassword, CookieStore cookies) throws ClientProtocolException, IOException, URISyntaxException {
		HttpClient client = new DefaultHttpClient();
		HttpContext context = getContext(cookies);
		//TODO:  Handle invalid cookies
		HttpPost post = new HttpPost();
		post.setURI(new URI("http://puush.me/ajax/change_password"));
		
		List<NameValuePair> args = new ArrayList<NameValuePair>();
		args.add(new BasicNameValuePair("c", currentPassword));
		args.add(new BasicNameValuePair("p", newPassword));
		
		HttpEntity postEntity = new UrlEncodedFormEntity(args);
		
		post.setEntity(postEntity);
		
		HttpResponse response = client.execute(post, context);
		HttpEntity responseEntity = response.getEntity();
		
		String contentString = readEntity(responseEntity);
		
		if (contentString.length() == 0) contentString = null;
		return contentString;
	}
	
	/**
	 * 
	 * @param poolID The new pool to set as default
	 * @param cookies The cookies of the user to change the default pool for.  Invalid cookies will 
	 * return false.
	 * @return <code>true</code> if response is the expected saved response.  <code>False</code> if 
	 * an unexpected response, including, but not limited to invalid cookies, was encountered.
	 * @throws URISyntaxException should never occur. safe to ignore.
	 * @throws ClientProtocolException in case of an http protocol error
	 * @throws IOException if the stream could not be created or some other I/O error occures
	 */
	public static boolean setDefaultPool(String poolID, CookieStore cookies) throws URISyntaxException, ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpContext context = getContext(cookies);
		
		HttpPost post = new HttpPost();
		post.setURI(new URI("http://puush.me/ajax/default_puush_pool"));
		
		List<NameValuePair> args = new ArrayList<NameValuePair>();
		args.add(new BasicNameValuePair("p", poolID));
		HttpEntity postEntity = new UrlEncodedFormEntity(args);
		post.setEntity(postEntity);
		
		HttpResponse response = client.execute(post, context);
		HttpEntity responseEntity = response.getEntity();
		
		String contentString = readEntity(responseEntity);
		
		post.abort();
		client.getConnectionManager().shutdown();
		
		return contentString.equals("<span class='success'>saved</span>");
	}
	
	/**
	 * Deletes the provided images.  A single bad ID will not ruin the batch call.  Only works for 
	 * images that belong to the cookie owner.
	 * @param imageIDs An array of Strings that contains the IDs of the images to delete
	 * @param cookies The cookie of the owner of the images.  Invalid cookies will not show an error.  
	 * Changes will simply not occur.
	 * @throws URISyntaxException should never occur. safe to ignore.
	 * @throws ClientProtocolException in case of an http protocol error
	 * @throws IOException if the stream could not be created or some other I/O error occures
	 */
	public static void deleteImages(String[] imageIDs, CookieStore cookies) throws URISyntaxException, ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpContext context = getContext(cookies);
		
		HttpPost post = new HttpPost();
		post.setURI(new URI("http://puush.me/ajax/delete_upload"));
		
		List<NameValuePair> args = new ArrayList<NameValuePair>();
		
		for (String imageID : imageIDs) {
			args.add(new BasicNameValuePair("i[]", imageID));
		}
		
		HttpEntity postEntity = new UrlEncodedFormEntity(args);
		
		post.setEntity(postEntity);
		
		client.execute(post, context);
		post.abort();
		client.getConnectionManager().shutdown();
	}
	
	/**
	 * Moves images to a new pool.
	 * @param imageIDs An array of Strings containing the IDs of the images to move.  
	 * Ex: <code>{ffffff, 0f0f0f, 000000}</code>
	 * @param poolID The id of the pool that is the destination for these images
	 * @param cookies The cookie of the owner of these images and pool.  Invalid cookies 
	 * will not throw an error.
	 * @throws URISyntaxException should never occur. safe to ignore.
	 * @throws ClientProtocolException in case of an http protocol error
	 * @throws IOException if the stream could not be created or some other I/O error occures
	 */
	public static void moveImages(String[] imageIDs, String poolID, CookieStore cookies) throws URISyntaxException, ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpContext context = getContext(cookies);
		
		HttpPost post = new HttpPost();
		post.setURI(new URI("http://puush.me/ajax/move_upload"));
		
		List<NameValuePair> args = new ArrayList<NameValuePair>();
		
		for (String imageID : imageIDs) {
			args.add(new BasicNameValuePair("i[]", imageID));
			args.add(new BasicNameValuePair("p", poolID));
		}
		
		HttpEntity postEntity = new UrlEncodedFormEntity(args);
		
		post.setEntity(postEntity);
		
		client.execute(post, context);
		
		post.abort();
		client.getConnectionManager().shutdown();
	}
	
	/**
	 * Gets the specified pool.
	 * @param pool The ID of the pool to get data from.  If the pool ID is invalid, does not 
	 * belong to the owner, or is <code>null</code>, then the default pool is provided instead.
	 * @param page The page of the pool to view.  If the page is less than 1, then the first page is 
	 * shown.  If the page is greater than the maximum pages, then the last page is shown.  
	 * If the page is invalid or missing, then the first page is shown.
	 * @param cookies The cookie of the owner of the pool.  If the cookie is invalid, a <code>
	 * NullPointerException</code> will be thrown.
	 * @return A payload containing the account, the pools owned, and the pool on display.
	 * @throws NullPointerException if I programmed poorly.  most likely, the cookie is invalid.
	 * @throws URISyntaxException should never occur. safe to ignore.
	 * @throws ClientProtocolException in case of an http protocol error
	 * @throws IOException if the stream could not be created or some other I/O error occures
	 * @throws SAXNotRecognizedException
	 * @throws SAXNotSupportedException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public static DisplayPayload getPool(String pool, int page, CookieStore cookies) throws URISyntaxException, ClientProtocolException, IOException, SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		HttpClient client = new DefaultHttpClient();
		HttpContext context = getContext(cookies);
		//TODO:  Handle invalid cookies
		//TODO:  Finish documenting XML parser exceptions
		HttpGet get = new HttpGet();
		get.setURI(new URI("http://puush.me/account?grid&pool=" + pool + "&page=" + page));
		
		HttpResponse response = client.execute(get, context);
		HttpEntity responseEntity = response.getEntity();
		
		String contentString = readEntity(responseEntity);
		
		get.abort();
		client.getConnectionManager().shutdown();
		
		return getDisplayPayload(contentString);
	}
	
	public static SettingsPayload getSettings(CookieStore cookies) throws URISyntaxException, ClientProtocolException, IOException, SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException {
		HttpClient client = new DefaultHttpClient();
		HttpContext context = getContext(cookies);
		
		HttpGet get = new HttpGet();
		get.setURI(new URI("http://puush.me/account/settings"));
		
		HttpResponse response = client.execute(get, context);
		
		String contentString = readEntity(response.getEntity());
		
		Account account = AccountParser.getAccountFromHtml(contentString);
		String apiKey = SettingsParser.getAPIKeyFromHtml(contentString);
		String password = SettingsParser.getPasswordFromHtml(contentString);
		Pool[] pools = SettingsParser.getPoolsFromHtml(contentString);
		Pool pool = SettingsParser.getSelectedPoolFromHtml(contentString);
		Application[] applications = SettingsParser.getThirdPartySupport(contentString);
		
		return new SettingsPayload(account, apiKey, password, pools, pool, applications);
	}
	
	private static DisplayPayload getDisplayPayload(String contentString) throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException, URISyntaxException {
		Account account = AccountParser.getAccountFromHtml(contentString);
		Pool[] pools = PoolParser.getPoolsFromHtml(contentString);
		DisplayedPool pool = DisplayedPoolParser.getDisplayedPoolFromHtml(contentString);
		
		return new DisplayPayload(account, pools, pool);
	}
	
	private static CookieStore getCookieStore(HttpContext context) {
		return (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
	}
	
	/**
	 * Creates a new HttpContext from the cookies in the given CookieStore.
	 * @param cookies The CookieStore containing the cookies that define the HttpContext.
	 * @return an HttpContext with the cookies from the given CookieStore.
	 */
	private static HttpContext getContext(CookieStore cookies) {
		HttpContext context = new BasicHttpContext();;
		
		if (cookies == null) cookies = new BasicCookieStore();
		
		context.setAttribute(ClientContext.COOKIE_STORE, cookies);
		
		return context;
	}
	
	/**
	 * Reads the content of the HttpEntity and stores it into a String
	 * @param entity The HttpEntity to read from
	 * @return The String representing the content of the HttpEntity
	 * @throws IOException if the stream could not be created or some other I/O error occures.
	 */
	private static String readEntity(HttpEntity entity) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
		char[] buffer = new char[1024];
		StringBuilder contentString = new StringBuilder();
		int bytesRead;
		while ((bytesRead = reader.read(buffer)) != -1) {
			contentString.append(buffer, 0, bytesRead);
		}
		
		return contentString.toString();
	}
	
	/**
	 * An object that represents the fragments of data from the Puush dashboard including:<br />
	 * <ul>
	 * <li>the account</li>
	 * <li>the pools owned by the account</li>
	 * <li>a page of the selected pool (or default)</li>
	 * </ul>
	 * @author Mark Nguyen
	 *
	 */
	public static class DisplayPayload implements Serializable {
		private final Account mAccount;
		private final Pool[] mPools;
		private final DisplayedPool mPool;
		
		private DisplayPayload(Account account, Pool[] pools, DisplayedPool pool) {
			mAccount = account;
			mPools = pools;
			mPool = pool;
		}
		
		public Account getAccount() {
			return mAccount;
		}
		
		public Pool[] getPools() {
			return mPools;
		}
		
		public DisplayedPool getDisplayedPool() {
			return mPool;
		}
	}
	
	public static class SettingsPayload {
		private final Account mAccount;
		private final String mApiKey;
		private final String mPassword;
		private final Pool[] mPools;
		private final Pool mSelectedPool;
		private final Application[] mApplications;
		
		private SettingsPayload(final Account account, final String apiKey, final String password, Pool[] pools, Pool selectedPool, Application[] applications) {
			mAccount = account;
			mApiKey = apiKey;
			mPassword = password;
			mPools = pools;
			mSelectedPool = selectedPool;
			mApplications = applications;
		}
		
		public Account getAccount() {
			return mAccount;
		}
		
		public String getAPIKey() {
			return mApiKey;
		}
		
		public String getPassword() {
			return mPassword;
		}
		
		public Pool[] getPools() {
			return mPools;
		}
		
		public Pool getSelectedPool() {
			return mSelectedPool;
		}
		
		public Application[] getApplications() {
			return mApplications;
		}
		
		
	}
	
//	public static DisplayPayload searchPool(String poolID, String searchQuery, CookieStore cookies) {
//		//TODO:  Support search
//		return null;
//	}
//	
//	public static String register(String email, String password, String confirmPassword) {
//		//TODO:  Support registration
//		return null;
//	}
//	
//	public static String resetPassword(String email) {
//		//TODO:  Support forgot passwords
//		return null;
//	}
	
	/**
	 * A RedirectHandler that will reject any redirection requests.
	 * @author Mark Nguyen
	 */
	private static class MyRedirectHandler implements RedirectHandler {
		@Override
		public URI getLocationURI(HttpResponse response, HttpContext context)
				throws ProtocolException {
			return null;
		}
		@Override
		public boolean isRedirectRequested(HttpResponse response,
				HttpContext context) {
			return false;
		}
	}
	
	/**
	 * An exception that shows that the user was not logged in during the action.
	 * @author Mark Nguyen
	 *
	 */
	public static class LoginException extends Exception {
		/**
		 * Serialization version UID
		 */
		private static final long serialVersionUID = -8723280022929073280L;

		LoginException() {
			super("User is not logged in");
		}
	}
	
}
