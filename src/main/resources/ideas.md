TorrentTunes

* Reverse engineer the YTS site, and make your own tracker, based on musicbrainz categorization(MBID's and so forth). Your own open-source tracker and website, simple and powerful, like YTS, with an API. Build a torrent server

* Reverse engineer some of the grooveshark newer html5 front end for the local application, find out where its hosted. Use chrome-embedded for the application

* For the back end, use mpetazzoni for both the tracker and client code.

* Have users provide a music library location, and scan each individual track, mp3s and flacs, identify the track with musicbrainz, and create a torrent that gives it an uploaderid of some kind, and the MBID. 



Sample queries:
https://musicbrainz.org/doc/Search_Server

https://musicbrainz.org/doc/Indexed_Search_Syntax#Search_Fields_4

https://musicbrainz.org/recording/MBID
https://musicbrainz.org/recording/13dd61c7-ce73-4e97-9f0c-9f0e53144411

search terms for recording:
recording(title)
artist
number(track#)
release(album)
date - yep, do the year
fmt=json
limit=1

http://musicbrainz.org/ws/2/recording/?query=recording:Closer+number:5+artist:Nine%Inch%Nails+dur:372666+release:The%Downward%Spiral&date:1994limit=10

Spaces are percents, the + is for query stuff
http://musicbrainz.org/ws/2/recording/?query=recording:now%at%last+dur:195489+artist:feist+number:11&limit=1



http://musicbrainz.org/ws/2/recording/?query=recording:Closer+artist:Nine%Inch%Nails&limit=5

http://musicbrainz.org/ws/2/recording/?query=recording:The%Entertainer+artist:Scott%Joplin+dur:156891+number:4+release:Frog%Legs%Ragtime%Era%Favorites&limit=10

http://musicbrainz.org/ws/2/recording/?query=recording:The%Entertainer%(1902,%piano%roll)+artist:Scott%Joplin+dur:156891+number:4+release:Frog%Legs&limit=1

http://musicbrainz.org/ws/2/recording/?query=recording:Closer+AND+artist:%22Nine%20Inch%20Nails+AND+date

http://musicbrainz.org/ws/2/recording/?query=recording:Closer+AND+artist:%22Nine%20Inch%20Nails%22+AND+dur:%5B369666%20TO%20375666%5D+AND+number:5+AND+release:%22The%20Downward%20Spiral%22+AND+date:1994*&limit=1&fmt=json




http://musicbrainz.org/ws/2/recording/?query=recording:The Entertainer (1902, piano roll) AND artist:Scott Joplin AND dur:[153891 TO 159891] AND number:4 AND release:Frog Legs: Ragtime Era Favorites&limit=1&fmt=json

http://musicbrainz.org/ws/2/recording/?query=recording:%22The%20Entertainer%22%20AND%20artist:%22Scott%20Joplin%22%20AND%20dur:[155391%20TO%20158391]%20AND%20number:4%20AND%20release:%22Frog%20Legs:%20Ragtime%20Era%20Favorites%22&limit=1&fmt=json

http://musicbrainz.org/ws/2/recording/?query=recording:%22Blueprint%22%20AND%20artist:%22Fugazi%22%20AND%20dur:[231382%20TO%20234382]%20AND%20number:5%20AND%20release:%22Repeater%20%2B%203%20Songs%22%20AND%20date:1990*&limit=1&fmt=json





