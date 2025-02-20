package io.bitken.tts.rss.view;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Enclosure {

// <enclosure url="https://storage.googleapis.com/paper-time/paper-audio/cca0fdac-226b-4d95-9241-0c3e29166548.mp3"
// length="0" type="audio/mpeg"/>

    @JacksonXmlProperty(localName = "url", isAttribute = true)
    private String url;

    @JacksonXmlProperty(localName = "length", isAttribute = true)
    private String length;

    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
