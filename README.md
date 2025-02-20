paper-rss
=========
Generates RSS feeds for recent paper data in the database. Can be used to generate
topic specific podcasts

To run on developer machine:  
This might work:
```
mvn -Dspring-boot.run.fork=false -Dspring.profiles.active=dev spring-boot:run -Dstart-class=io.bitken.tts.rss.main.RssMain
```