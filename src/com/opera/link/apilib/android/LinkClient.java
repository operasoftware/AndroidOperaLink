package com.opera.link.apilib.android;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient3.HttpClient3;

import org.json.JSONArray;
import org.json.JSONException;

import com.opera.link.apilib.android.exceptions.LibOperaLinkException;
import com.opera.link.apilib.android.exceptions.LinkAccessDeniedException;
import com.opera.link.apilib.android.exceptions.LinkItemNotFound;
import com.opera.link.apilib.android.exceptions.LinkResponseFormatException;
import com.opera.link.apilib.android.items.BookmarkFolderEntry;
import com.opera.link.apilib.android.items.Element;
import com.opera.link.apilib.android.items.FolderEntry;
import com.opera.link.apilib.android.items.FolderInterface;
import com.opera.link.apilib.android.items.NoteFolderEntry;
import com.opera.link.apilib.android.items.SpeedDial;

import net.oauth.OAuth;

/**
 * 
 * The main class handling connection with Opera Link server. It supports user
 * authorisation and methods for getting and manipulating Opera Link elements.
 * 
 * <p>
 * Before some data with bookmarks, notes or speeddials can be exchanged with
 * server, the application must be granted access. To do so OAuth 1.0 protocol
 * based authorization method is performed. The application must be registered
 * at http://auth.opera.com/service/oauth/applications/ and have its consumerKey
 * and consumerSecret
 * </p>
 * 
 * @author pjarzebowski
 */
public class LinkClient {

	private String consumerKey;
	private String consumerSecret;

	private OAuthAccessor accessor;
	private OAuthServiceProvider provider;
	private OAuthClient client;
	private boolean isAuthorized = false;

	private static final String URL_SEPARATOR = "/";

	public static final String OAUTH_URL = "https://auth.opera.com/service/oauth/";
	public static final String URL_PREFIX = "https://link.api.opera.com/rest/";
	public static final String OOB_CALLBACK = "oob";
	public static final String OAUTH_CALLBACK = "oauth_callback";
	public static final String OAUTH_VERIFIER = "oauth_verifier";

	/**
	 * Creates object for making requests to the Opera Link server. Connection
	 * is not authorised after the object creation, the user must grant access
	 * to his data on the authorisation website - to generat the url use {@code
	 * getAuthorizationURL} method.
	 * 
	 * @param consumerKey
	 *            Application key
	 * @param consumerSecret
	 *            Application secret key
	 */
	public LinkClient(String consumerKey, String consumerSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;

		setLinkOAuthConnnection();
	}

	private LinkClient(String consumerKey, String consumerSercret,
			String requestToken, String accessToken, String tokenSecret) {
		this(consumerKey, consumerSercret);
		accessor.accessToken = accessToken;
		accessor.requestToken = requestToken;
		accessor.tokenSecret = tokenSecret;
	}

	/**
	 * Creates object for making requests to the Opera Link server. Connection
	 * is not authorised, but OAuth request tokens where generated before.
	 * 
	 * @param consumerKey
	 *            Application key
	 * @param consumerSecret
	 *            Application secret key
	 * @param requestToken
	 *            connection request token
	 * @param tokenSecret
	 *            connection secret token
	 */
	public static LinkClient createFromRequestToken(String consumerKey,
			String consumerSercret, String requestToken, String tokenSecret) {
		return new LinkClient(consumerKey, consumerSercret, requestToken, null,
				tokenSecret);
	}

	/**
	 * Creates object for making requests to the Opera Link server. Connection
	 * is authorised.
	 * 
	 * @param consumerKey
	 *            Application key
	 * @param consumerSecret
	 *            Application secret key
	 * @param accessToken
	 *            generated access token for connection
	 * @param tokenSecret
	 *            connection secret token
	 * @return
	 */
	public static LinkClient createFromAccessToken(String consumerKey,
			String consumerSecret, String accessToken, String tokenSecret) {
		return new LinkClient(consumerKey, consumerSecret, null, accessToken,
				tokenSecret);
	}

	private void setLinkOAuthConnnection() {
		provider = new OAuthServiceProvider(OAUTH_URL + "request_token",
				OAUTH_URL + "authorize", OAUTH_URL + "access_token");
		OAuthConsumer consumer = new OAuthConsumer(OOB_CALLBACK, consumerKey,
				consumerSecret, provider);
		accessor = new OAuthAccessor(consumer);
		client = new OAuthClient(new HttpClient3());
	}

	/**
	 * Generates and returns new URL address where an user can grant access to
	 * the application
	 * 
	 * @param callback
	 *            URL address where user should be redirected after granting
	 *            access. If no redirection needed pass {@code
	 *            LinkClient.OOB_CALLBACK} value
	 * @return URL address of authorisation website
	 */
	public String getAuthorizationURL(String callback)
			throws LibOperaLinkException {
		try {
			HashMap<String, String> requestParams = new HashMap<String, String>();
			requestParams.put(OAUTH_CALLBACK, OOB_CALLBACK);
			client.getRequestToken(accessor, OAuthMessage.POST, requestParams
					.entrySet());
		} catch (OAuthException e) {
			throw new LinkAccessDeniedException(e);
		} catch (Exception e) {
			throw new LibOperaLinkException(e);
		}
		return accessor.consumer.serviceProvider.userAuthorizationURL
				+ "?oauth_token=" + accessor.requestToken + "&oauth_callback="
				+ callback;
	}

	/**
	 * Performs the last step of OAauth authorisation - generates an access
	 * token.
	 * 
	 * @throws LibOperaLinkException
	 */
	public void grantAccess(String verifier) throws LibOperaLinkException {
		try {
			HashMap<String, String> requestParams = new HashMap<String, String>();
			requestParams.put(OAUTH_VERIFIER, verifier);
			client.getAccessToken(accessor, OAuthMessage.POST, requestParams
					.entrySet());
			/* client.getAccessToken(accessor, OAuthMessage.POST,
					OAuth.newList(OAUTH_VERIFIER, verifier.toString())); */
			isAuthorized = true;
		} catch (OAuthException e) {
			throw new LinkAccessDeniedException(e);
		} catch (Exception e) {
			throw new LibOperaLinkException(e);
		}
	}

	protected <T extends Element> JSONArray makeRequest(String requestUrl,
			String requestMethod, HashMap<String, String> params)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		// get server response
		String response = null;
		OAuthMessage responseMessage = null;
		try {
			responseMessage = client.invoke(accessor, requestMethod,
					requestUrl, addRequestParameters(params));
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new LibOperaLinkException("connection to server refused");
		} catch (OAuthException e1) {
			OAuthProblemException problemExep = (OAuthProblemException) e1;
			LibOperaLinkException.throwCommunicationExeption(problemExep
					.getHttpStatusCode(), e1);
		} catch (URISyntaxException e1) {
			throw new LibOperaLinkException(e1);
		}
		try {
			response = responseMessage.readBodyAsString();
		} catch (IOException e1) {
			throw new LinkResponseFormatException(e1);
		}

		// load server response to json list
		if (response == null || response.length() == 0) {
			return null;
		}
		try {
			JSONArray items = new JSONArray(response);
			return items;
		} catch (JSONException e) {
			e.printStackTrace();
			throw new LinkResponseFormatException(e);
		}
	}

	protected Collection<Map.Entry<String, String>> addRequestParameters(
			HashMap<String, String> params) {
		if (params == null) {
			params = new HashMap<String, String>();
		}
		params.put(ApiParameters.API_OUTPUT_PARAM,
				ApiParameters.JSON_OUTPUT_PARAM);
		return params.entrySet();
	}

	protected <T extends Element> ArrayList<T> requestItems(Class<T> cls,
			String datatype, boolean recursive, String rootID)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		// create request URL
		String itemsDepth = ApiParameters.URL_GET_CHILDREN_PARAM;
		if (recursive) {
			itemsDepth = ApiParameters.URL_GET_DESCENDANTS_PARAM;
		}

		String requestUrl = createUrl(datatype, rootID) + itemsDepth;

		JSONArray items = makeRequest(requestUrl, HttpClient3.GET, null);
		try {
			return Element.elementsFactory(items, cls);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new LinkResponseFormatException(e);
		}
	}

	protected <T extends Element> T requestItem(Class<T> cls, String datatype,
			String rootID) throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		// create request URL
		String requestUrl = createUrl(datatype, rootID);

		JSONArray items = makeRequest(requestUrl, HttpClient3.GET, null);
		try {
			return Element.elementsFactory(items, cls).get(0);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new LibOperaLinkException(e);
		}
	}

	protected String createUrl(String datatype, String rootID) {
		StringBuilder sb = new StringBuilder(URL_PREFIX);
		sb.append(datatype);
		sb.append(URL_SEPARATOR);
		if (rootID != null) {
			sb.append(rootID);
			sb.append(URL_SEPARATOR);
		}
		return sb.toString();
	}

	/**
	 * Get public request token which is used during OAuth authorization process
	 * 
	 * @return request token
	 */
	public String getRequestToken() {
		return accessor.requestToken;
	}

	/**
	 * Get Public authorized token for OAuth communication
	 */
	public String getAccessToken() {
		return accessor.accessToken;
	}

	/**
	 * Secret token for OAuth communication
	 */
	public String getTokenSecret() {
		return accessor.tokenSecret;
	}

	/**
	 * True if authorization process is over and requests can be made to receive
	 * data from server
	 */
	public boolean isAuthorized() {
		return isAuthorized;
	}

	/**
	 * Fetches located in the OperaLink server's root folder bookmarks, bookmark
	 * folders and bookmark separators.
	 * 
	 * @param recursive
	 *            if true then all bookmarks are returned, otherwise only the
	 *            content of a root folder
	 * @return elements of bookmark datatype
	 * @throws LibOperaLinkException
	 *             thrown when the server communication exception occurs, see
	 *             its inner exception for details
	 */
	public ArrayList<BookmarkFolderEntry> getRootBookmarks(boolean recursive)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		return requestItems(BookmarkFolderEntry.class,
				BookmarkFolderEntry.DATATYPE, recursive, null);
	}

	/**
	 * Fetches from the OperaLink server and returns the content of folder
	 * (bookmarks, bookmark folders and bookmark separators)
	 * 
	 * @param folder
	 *            folder which content is returned
	 * @param recursive
	 *            if true then children are returned recursively, otherwise only
	 *            the direct content of a folder
	 * @return elements of bookmark datatype
	 * @throws LibOperaLinkException
	 *             thrown when the server communication exception occurs, see
	 *             its inner exception for details
	 */
	public ArrayList<BookmarkFolderEntry> getBookmarksFromFolder(
			String folderId, boolean recursive) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		return requestItems(BookmarkFolderEntry.class,
				BookmarkFolderEntry.DATATYPE, recursive, folderId);

	}

	/**
	 * Fetches located in the OperaLink server's root folder notes, note folders
	 * and note separators.
	 * 
	 * @param folderId
	 *            ID of folder which content should be returned
	 * @param recursive
	 *            if true then all bookmarks are returned, otherwise only the
	 *            content of a root folder
	 * @return elements of bookmark datatype
	 * @throws LibOperaLinkException
	 */
	public ArrayList<NoteFolderEntry> getRootNotes(boolean recursive)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		return requestItems(NoteFolderEntry.class, NoteFolderEntry.DATATYPE,
				recursive, null);
	}

	/**
	 * Fetches from the OperaLink server and returns the content of folder
	 * (notes, note folders and note separators)
	 * 
	 * @param folderId
	 *            ID of folder which content should be returned
	 * @param recursive
	 *            if true then all bookmarks are returned, otherwise only the
	 *            content of a root folder
	 * @return elements of bookmark datatype
	 * @throws LibOperaLinkException
	 */
	public ArrayList<NoteFolderEntry> getNotesFromFolder(String folderId,
			boolean recursive) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		return requestItems(NoteFolderEntry.class, NoteFolderEntry.DATATYPE,
				recursive, folderId);
	}

	/**
	 * Fetches from the OperaLink server and returns all of the Speed Dials
	 * 
	 * @return Speed Dials list
	 * @throws LibOperaLinkException
	 */
	public ArrayList<SpeedDial> getSpeedDials() throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		return requestItems(SpeedDial.class, SpeedDial.ITEM_TYPE, false, null);
	}

	/**
	 * Gets from the OperaLink server and returns element of datatype which has
	 * specified id
	 * 
	 * @param id
	 * @param datatype
	 *            can be one of BookmarkFolderEntry.getDatatype(),
	 *            NoteFolderEntry.getDatatype() or
	 *            SpeedDialFolderEntry.getDatatype()
	 * @return Element got from the server
	 * @throws LibOperaLinkException
	 *             thrown when the server communication exception occurs, see
	 *             its inner exception for details
	 */
	public Element getElement(String id, String datatype)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		return requestItem(Element.class, datatype, id);
	}

	/**
	 * Adds the element as a child of element with id=destination, it's appended
	 * at the end of destination's children list If destination is null then
	 * it's added to root folder (not valid for SpeedDial)
	 * 
	 * @param element
	 *            An element to be added
	 * @param destination
	 *            ID of an element which is going to be a parent of the added
	 *            element
	 * @throws LibOperaLinkException
	 *             thrown when the server communication exception occurs
	 */
	public void add(Element element, String destination)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {

		HashMap<String, String> params = element.createParamsDict();
		params.put(ApiParameters.API_METHOD_PARAM, ApiParameters.CREATE);
		params.put(Element.ITEM_TYPE_JSON_KEY, element.getItemType());

		JSONArray arrayResponse = makeRequest(createUrl(element.getDatatype(),
				destination), HttpClient3.POST, params);
		try {
			element.updateParameters(arrayResponse.getJSONObject(0));
		} catch (JSONException e) {
			e.printStackTrace();
			throw new LibOperaLinkException(e);
		}
	}

	/**
	 * Adds the element to the root folder
	 * 
	 * @param element
	 *            An element to be added
	 * @throws LibOperaLinkException
	 *             thrown when the server communication exception occurs
	 */
	public void add(FolderEntry<?> element) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		add(element, null);
	}

	/**
	 * Adds the element to the folder
	 * 
	 * @param <U>
	 * 
	 * @param element
	 *            An element to be added
	 * @param folder
	 *            The destination folder of the element
	 * @throws LibOperaLinkException
	 *             thrown when the server communication exception occurs or
	 *             folder parameter is not a folder element
	 */
	public <T extends FolderEntry<?>, U extends FolderInterface<T>> void addToFolder(
			FolderEntry<T> element, U folderElement) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {

		if (folderElement == null || folderElement.getId() == null) {
			throw new LinkItemNotFound();
		}

		add(element, folderElement.getId());

	}

	/**
	 * Adds the SpeedDial
	 * 
	 * @param speeddial
	 *            A SpeedDial to be added
	 * @throws LibOperaLinkException
	 *             thrown when the server communication exception occurs or
	 *             folder parameter is not a folder element
	 */
	public void add(SpeedDial speeddial) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		add(speeddial, String.valueOf(speeddial.getPosition()));
	}

	/**
	 * Sends all of changes to the element and updates it in case of any
	 * modifications made by other clients
	 * 
	 * @param element
	 *            Element to be updated
	 * @throws LibOperaLinkException
	 *             thrown when the server communication exception occurs or
	 *             folder parameter is not a folder element
	 */
	public void update(Element element) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		HashMap<String, String> params = element.createParamsDict();
		params.put(ApiParameters.API_METHOD_PARAM, ApiParameters.UPDATE);

		JSONArray arrayResponse = makeRequest(createUrl(element.getDatatype(),
				element.getId()), HttpClient3.POST, params);
		try {
			element.updateParameters(arrayResponse.getJSONObject(0));
		} catch (JSONException e) {
			e.printStackTrace();
			throw new LibOperaLinkException(e);
		}
	}

	/**
	 * Removes the element from Opera Link server
	 * 
	 * @param element
	 *            Element to be removed
	 * @throws LibOperaLinkException
	 */
	public void delete(Element element) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		delete(element.getId(), element.getDatatype());
	}

	/**
	 * Removes the element from Opera Link server
	 * 
	 * @param id
	 *            ID of element to be removed
	 * @param datatype
	 *            Datatype of element to be removed
	 * @throws LibOperaLinkException
	 */
	public void delete(String id, String datatype) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		HashMap<String, String> params = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put(ApiParameters.API_METHOD_PARAM, ApiParameters.DELETE);
			}
		};
		makeRequest(createUrl(datatype, id), HttpClient3.POST, params);
	}

	/**
	 * Moves the element to trash folder
	 * 
	 * @param id
	 *            ID of element to be trashed
	 * @param datatype
	 *            Datatype of element to be trashed
	 * @throws LibOperaLinkException
	 */
	public void trash(String id, String datatype) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		HashMap<String, String> params = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put(ApiParameters.API_METHOD_PARAM, ApiParameters.TRASH);
			}
		};
		makeRequest(createUrl(datatype, id), HttpClient3.POST, params);
	}

	/**
	 * Moves the element to trash folder
	 * 
	 * @param <T>
	 *            Type of the element - a subclass of FolderEntry
	 * @param element
	 *            Element to be trashed
	 * @throws LibOperaLinkException
	 */
	public <T extends FolderEntry<?>> void trash(FolderEntry<T> element)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		trash(element.getId(), element.getDatatype());
	}

	/**
	 * Moves the element into the specified folder at the last position
	 * 
	 * @param <T>
	 *            Type of the element - a subclass of FolderEntry
	 * @param element
	 *            Element to be moved
	 * @param folder
	 *            Destination folder
	 */
	public <T extends FolderEntry<?>, U extends FolderInterface<T>> void moveInsideFolder(
			FolderEntry<T> element, U folderElement) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {

		if (folderElement == null || folderElement.getId() == null) {
			throw new LinkItemNotFound();
		}
		moveInsideFolder(element.getId(), folderElement.getId(), element
				.getDatatype());
	}

	/**
	 * Moves the element into the root folder, it is appended at the last
	 * position
	 * 
	 * @param <T>
	 *            Type of the element - a subclass of FolderEntry
	 * @param element
	 *            Element to be moved
	 */
	public <T extends FolderEntry<?>> void moveToRootFolder(
			FolderEntry<T> element) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {

		moveInsideFolder(element.getId(), "", element.getDatatype());
	}

	/**
	 * Moves the element into the specified folder (element with
	 * ID=referenceItem) at the last position
	 * 
	 * @param id
	 *            ID of the element to be moved
	 * @param referenceItem
	 *            ID of the destination folder, if null then the element is
	 *            moved to a root folder
	 * @param datatype
	 *            Datatype of the element and folder
	 * @throws LibOperaLinkException
	 */
	public void moveInsideFolder(String id, String referenceItem,
			String datatype) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		move(id, referenceItem, datatype, ApiParameters.MOVE_POSITION_INTO);
	}

	/**
	 * Moves the id element to position relative to the referenceItem folder
	 * 
	 * @param referenceItem
	 *            if null then the element is moved to a root folder
	 * @throws LibOperaLinkException
	 */
	private void move(String id, String referenceItem, String datatype,
			final String relativePosition) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		final String requestUrl = createUrl(datatype, id);
		final String referenceItemID = referenceItem != null ? referenceItem
				: "";
		HashMap<String, String> params = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put(ApiParameters.API_METHOD_PARAM, ApiParameters.MOVE);
				put(ApiParameters.MOVE_REFERENCE_ITEM_PARAM, referenceItemID);
				put(ApiParameters.MOVE_RELATIVE_POSITION_PARAM,
						relativePosition);
			}
		};
		makeRequest(requestUrl, HttpClient3.POST, params);
	}

	/**
	 * Moves the element just after the specified referenceItem
	 * 
	 * @param <T>
	 *            Type of the element - a subclass of FolderEntry
	 * @param element
	 *            Element to be moved
	 * @param referenceItem
	 *            Element which is going to be followed by the moved element
	 * @throws LibOperaLinkException
	 */
	public <T extends FolderEntry<?>> void moveAfterElement(
			FolderEntry<T> element, FolderEntry<T> referenceItem)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		moveAfterElement(element.getId(), referenceItem.getId(), element
				.getDatatype());
	}

	/**
	 * Moves the element with ID=elId just after the specified element with
	 * ID=referenceElId
	 * 
	 * @param elId
	 *            ID of an element to be moved
	 * @param referenceElId
	 *            ID of an element which is going to be followed by the moved
	 *            element
	 * @param datatype
	 *            Datatype of the element and folder
	 * @throws LibOperaLinkException
	 */
	public void moveAfterElement(String elId, String referenceElId,
			String datatype) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		move(elId, referenceElId, datatype, ApiParameters.MOVE_POSITION_AFTER);
	}

	/**
	 * Moves the element with ID=elId just before the specified element with
	 * ID=referenceElId
	 * 
	 * @param Element
	 *            to be moved
	 * @param Element
	 *            which is going to be proceeded by the moved element
	 * @throws LibOperaLinkException
	 */
	public <T extends FolderEntry<?>> void moveBeforeElement(
			FolderEntry<T> element, FolderEntry<T> referenceItem)
			throws LinkItemNotFound, LinkAccessDeniedException,
			LinkResponseFormatException, LibOperaLinkException {
		moveBeforeElement(element.getId(), referenceItem.getId(), element
				.getDatatype());
	}

	/**
	 * Moves the element with ID=elId just before the specified element with
	 * ID=referenceElId
	 * 
	 * @param elId
	 *            ID of an element to be moved
	 * @param referenceElId
	 *            ID of an element which is going to be proceeded by the moved
	 *            element
	 * @param datatype
	 *            Datatype of the element and folder
	 * @throws LibOperaLinkException
	 */
	public void moveBeforeElement(String elId, String referenceElId,
			String datatype) throws LinkItemNotFound,
			LinkAccessDeniedException, LinkResponseFormatException,
			LibOperaLinkException {
		move(elId, referenceElId, datatype, ApiParameters.MOVE_POSITION_BEFORE);
	}
}
