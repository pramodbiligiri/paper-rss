package io.bitken.tts.rss.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.bitken.tts.model.entity.PaperAudio;
import io.bitken.tts.model.entity.PaperData;
import io.bitken.tts.model.entity.converter.IAudioFile;
import io.bitken.tts.model.entity.converter.LocalAudioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RssItem {

    private static final Logger LOG = LoggerFactory.getLogger(RssItem.class);

    public static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMM");

//<title><![CDATA[Data-Driven Abductive Inference of Library Specifications]]></title>
//<description><![CDATA[Programmers often leverage data structure libraries that provide useful and reusable abstractions. Modular verification of programs that make use of these libraries naturally rely on specifications that capture important properties about how the library expects these data structures to be accessed and manipulated. However, these specifications are often missing or incomplete, making it hard for clients to be confident they are using the library safely. ]]></description>
//<link>https://arxiv.org/abs/2108.04783</link>
//<guid isPermaLink="false">cca0fdac-226b-4d95-9241-0c3e29166548</guid>
//<dc:creator><![CDATA[Zhe Zhou, Robert Dickerson, Benjamin Delaware, Suresh Jagannathan]]></dc:creator>
//<pubDate>Tue, 10 Aug 2021 19:11:13 GMT</pubDate>

    @JacksonXmlCData
    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlCData
    @JacksonXmlProperty(localName = "description")
    private String description;

    @JacksonXmlProperty(localName = "link")
    private String link;

    @JacksonXmlProperty(localName = "guid")
    private String guid;

    @JacksonXmlCData
    @JacksonXmlProperty(namespace = "dc", localName = "creator")
    private String creator;

    @JacksonXmlProperty(localName = "pubDate")
    private String pubDate;

    @JacksonXmlProperty(localName = "audioCreateTime")
    private String audioCreateTime;

    @JacksonXmlProperty(localName = "enclosure")
    private Enclosure enclosure;

    public RssItem() {
        this.enclosure = new Enclosure();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getAudioCreateTime() {
        return audioCreateTime;
    }

    public void setAudioCreateTime(String audioCreateTime) {
        this.audioCreateTime = audioCreateTime;
    }

    @JsonIgnore
    public Optional<Date> getAudioCreateTimeParsed() {
        if (audioCreateTime == null || audioCreateTime.isBlank()) {
            return Optional.empty();
        }

        try {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return Optional.of(sdf.parse(audioCreateTime));
        } catch (ParseException e) {
            return Optional.empty();
        }

    }

    @JsonIgnore
    public Optional<Date> getPubDateParsed() {
        if (pubDate == null || pubDate.isBlank()) {
            return Optional.empty();
        }

        try {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return Optional.of(sdf.parse(pubDate));
        } catch (ParseException e) {
            return Optional.empty();
        }

    }

    public Enclosure getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(Enclosure enclosure) {
        this.enclosure = enclosure;
    }

    public static RssItem from(PaperData pd) {
        RssItem pi = new RssItem();

        String formattedTitle = formatTitle(pd.getTitle()).toString();
        pi.setTitle(formattedTitle);

        String paperLink = "https://arxiv.org/abs/" + pd.getArxivId();

        sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
        pi.setDescription(
            pd.getAuthors() +
            "\n\nAbstract\n" +
            getAbstract(pd.getAbstractt()) +
            "\n\nLink: " + paperLink +
            "\nTitle: " + formattedTitle +
            "\n\nhttps://papertime.app"
        );

        List<PaperAudio> audios = pd.getAudio();
        PaperAudio audio = audios.get(audios.size() - 1);
        String fullPath = resolvePath(audio.getAudio());

        pi.setLink(paperLink);
        pi.setCreator(pd.getAuthors());
        pi.setGuid(paperLink);

        setPubdate(pi, pd);
        setAudioCreateTime(pi, audio);

        Enclosure enclosure = new Enclosure();
        enclosure.setType("audio/mpeg");
        enclosure.setUrl(fullPath);
        enclosure.setLength(audio.getDuration().toString());

        pi.setEnclosure(enclosure);

        return pi;
    }

    static StringBuilder getAbstract(String text) {
        StringBuilder buf = new StringBuilder();

        String newText = text.trim();
        int len = newText.length();

        for (int i = 0; i < len; i++) {
            char ch = newText.charAt(i);

            // Since the text goes out as CDATA, including newlines in it interferes with
            // any wrapping the client can do. So remove newlines from the content and let the RSS
            // client do the wrapping.
            if (ch == '\n' && i < (len-1) && newText.charAt(i+1) != '\n') {
                if (newText.charAt(i+1) != ' '){
                    buf.append(" ");
                }

                continue;
            }

            buf.append(ch);
        }

        return buf;
    }

    static StringBuilder formatTitle(String title) {
        String newText = title.trim();

        StringBuilder buf = new StringBuilder();
        int len = newText.length();

        for (int i = 0; i < len; i++) {
            char ch = newText.charAt(i);

            if (ch == ' ' && i < len-1 && newText.charAt(i+1) == ' ') {
                continue;
            }

            if (ch == '\n') {
                continue;
            }

            buf.append(ch);
        }

        return buf;
    }

    private static void setAudioCreateTime(RssItem pi, PaperAudio audio) {
        if (audio.getCreateTime() == null) {
            return;
        }

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        pi.setAudioCreateTime(sdf.format(audio.getCreateTime().getTime()));
    }

    private static void setPubdate(RssItem pi, PaperData pd) {
        Timestamp pubDate = pd.getPubDate();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (pubDate == null) {
            pi.setPubDate(sdf.format(new Date()));
        } else {
            pi.setPubDate(sdf.format(pubDate));
        }
    }

    private static String resolvePath(IAudioFile file) {
        if (file instanceof LocalAudioFile) {
            return "/file/" + file.getFilename();
        }

        return file.getFullPath();
    }

}