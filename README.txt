==============================================
 AndroidOperaLink - Android Opera Link Client
==============================================

THIS LIBRARY IS OBSOLETE, YOU SHOULD BE USING

https://github.com/operasoftware/JavaOperaLinkClient

INSTEAD. IT HAS IMPROVEMENTS, MAVEN INTEGRATION, A NON-CONFUSING NAME
AND GENERALLY SPEAKING MOAR COWBELL.


Introduction
============

This is the Opera Link Public API client library for Android.
It provides utilities to get and manipulate Opera Bookmarks, Notes and
Speed Dials. The application which uses it should provide the library with an
application key and secret key received from link.opera.com.
The library takes care of authorizing the user and giving easy access to get and
modify his Opera Link data.

Copyright and license
=====================

The source code included in this distribution is released under the
BSD license:

Copyright © 2010, Opera Software
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

* Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
* Neither the name of Opera Software nor the names of its contributors
  may be used to endorse or promote products derived from this
  software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Library dependencies
====================
For convenience, we have included pre-compiled versions of several
libraries this code depends on:

oauth2.jar
commons-codec-1.4.jar
commons-httpclient-3.1.jar
jetty-6.1.11.jar
jetty-util-6.1.11.jar

You can download the source code for them from the following locations:

OAuth: http://code.google.com/p/oauth/
Commons: http://commons.apache.org/
Jetty: http://www.eclipse.org/jetty/

OAuth and Commons are distributed under the Apache License. See a copy
of the license in the LICENSE-2.0.txt file.

Jetty has a dual license (Apache License 2.0 or Eclipse Public License
1.0). See a copy of the Eclipse Public License in the Eclipse Public
License - Version 1.0.htm file. Also, see exceptions to the dual
license in the NOTICE.TXT file.


Usage description
=================

LinkClient is a class which handles connection with Opera Link server. It
supports user authorization and methods for getting and manipulating Opera Link
elements.

Before some data with bookmarks, notes or speeddials can be exchanged with
server, the application must be granted access. To do so OAuth 1.0a protocol
based authorization method is performed. The application must be registered
at https://auth.opera.com/service/oauth/applications/ and have its consumerKey
and consumerSecret. Register your app as a Web Application, this way you will
be able to specify a callback  URL - if you do that and add the URL to intent
filters then the Android browser will automatically redirect a user back
to the application. The example uses "notes://operalink.notes.com" as a callback 
URL.

1. Authorization
----------------

To authorize a new user LinkClient object must be created. It obtains a request
token and generates the authorization website address where the user must be
redirected:
    // create new connection
    link = new LinkClient(consumerKey, consumerSecret);

    // set the callback url, in this case the application has added a following data
    // specification to an intent filter in AndroidManifest.xml:
    //     <data android:scheme="notes" android:host="operalink.notes.com" />
    String callbackUrl = "notes://operalink.notes.com";
    try {
        String authorizeUrl = link.getAuthorizationURL(callbackUrl);

        // create intent which will redirect the user to a browser
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(authorizeUrl));

        // redirect user to the authorization website:
        startActivityForResult(i, REDIRECTION_ACTIVITY);
    } catch (LibOperaLinkException e) {
        e.printStackTrace();
    }

After the user has granted access to the application and it has been resumed,
authorization process can be finalized. The verification code is read from the
intent data which was passed in the url query and then is used to obtain an access token. 

    Uri uri = this.getIntent().getData();
    if (uri == null) {
        return;
    }

    String verifier = uri.getQueryParameter(LinkClient.OAUTH_VERIFIER);
    try {

        // obtain access tokens
        link.grantAccess(verifier);

        // save access token and secret for this user
        accessToken = link.getAccessToken();
        tokenSecret = link.getTokenSecret();
    } catch (LibOperaLinkException e) {
        e.printStackTrace();
    }

When OAuth access token was once generated this line of code is equivalent:
    LinkClient link = LinkClient.createFromAccessToken(consumerKey,
					    consumerSecret, accessToken, tokenSecret);



2. Accessing and manipulating data
----------------------------------

Use LinkClient object to get Opera Link data and to submit some changes to
it.

a) Bookmarks:
This example shows how to access data, create a new folder and bookmark, how to move
them around and finally how to delete them.

    // create and append a new sample folder to a root folder bookmarks list
    BookmarkFolder sample_folder = new BookmarkFolder('New folder');
    link.add(sample_folder);

    // create and add a new bookmark to the sample folder
    Bookmark bookmark = new Bookmark("Opera Link", "http://link.opera.com/");
    bookmark.visited = new Date();
    link.addToFolder(bookmark, sample_folder);

    // now see that the elements were added - get all of the bookmarks from the
    // server
    ArrayList<BookmarkFolderEntry> allBookmarks = link.getRootBookmarks(false);
    // and check if the last element is the sample folder added and it contains the 
    // bookmark
    int bookmarksSize = allBookmarks.size();
    assert(allBookmarks.get(bookmarksSize - 1).isFolder());
    assert(allBookmarks.get(bookmarksSize - 1).title.equals('New folder'));
    sample_folder = (BookmarkFolder) allBookmarks.get(bookmarksSize - 1);
    assert(sample_folder.getChildren().get(0).isBookmark());
    bookmark = (Bookmark) sample_folder.getChildren().get(0);
    assert(bookmark.title.equals('Opera Link')) ;

    // or just get from the server a content of the folder
    ArrayList<BookmarkFolderEntry> sampleFolderContent =
        link.getBookmarksFromFolder(sample_folder, false);
    assert(sampleFolderContent.get(0).title.equals('Opera Link')) ;


    // Now let's reorder the bookmarks:
    // create another folder where already created folder and bookmark can be
    // moved into
    BookmarkFolder folderForMovedElements = new BookmarkFolder('Folder with moved elements');
    link.add(folderForMovedElements);

    // move one folder into another
    link.moveInsideFolder(sample_folder, folderForMovedElements)

    // move the bookmark outside of sample folder and place it in a root folder
    // at the last position
    link.moveToRootFolder(bookmark);
    // or place it in a root folder before the added folder for moved elements
    link.moveBeforeElement(bookmark, folderForMovedElements);


    // Time to clean up:
    // trash added elements
    link.trash(bookmark);
    link.trash(sample_folder);
    link.trash(folderForMovedElements);
    // or delete them
    link.delete(bookmark);
    link.delete(sample_folder);
    link.delete(folderForMovedElements);


b) Notes:
Retrieving and manipulating notes data is analogical to bookmarks

c) Speed dials:
This example shows how to access, create, update and remove speed dials. In contrast to
notes and bookmarks speed dials can not be moved.

    // create a new speeddial and add it at first position
    SpeedDial dial = new SpeedDial("http://opera.com/", "Opera Main Page", 1);
    link.add(dial);

    // send updates to a dial
    dial.title = "Opera";
    link.update(dial);

    // access all of your speed dials
    ArrayList<SpeedDial> dials = link.getSpeedDials();

    // delete the new added one:
    link.delete(dial);


3. Folder properties
--------------------

BookmarkFolder and NoteFolder have some special properties you can access.
Those are related to their special use on the devices, for example Opera
Mini users can only access a folder for witch isTargetMini method returns true.
For more details see documentation.
