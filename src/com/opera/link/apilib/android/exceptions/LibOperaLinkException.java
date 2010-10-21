package com.opera.link.apilib.android.exceptions;



public class LibOperaLinkException extends Exception {
	
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 401;
	public static final int NOT_FOUND = 404;

	
	private static final long serialVersionUID = 1L;
	public LibOperaLinkException(Exception innerException) {
		super();
	}
	public LibOperaLinkException(String message) {
		super(message);
	}
	public static void throwCommunicationExeption(int httpStatusCode, Exception innerException)
			throws LinkItemNotFound, LinkAccessDeniedException, LibOperaLinkException {
		switch(httpStatusCode) {
		case NOT_FOUND:
			throw new LinkItemNotFound();
		case UNAUTHORIZED:
			throw new LinkAccessDeniedException(innerException);
		}
		throw new LibOperaLinkException(innerException);
	}
}
