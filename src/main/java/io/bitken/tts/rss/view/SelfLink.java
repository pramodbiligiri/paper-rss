package io.bitken.tts.rss.view;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class SelfLink {

    //    <atom:link href="https://rss.papertime.app" rel="self" type="application/rss+xml"/>

    @JacksonXmlProperty(localName = "href", isAttribute = true)
    private String href;

    @JacksonXmlProperty(localName = "rel", isAttribute = true)
    private String rel;

    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;

    public SelfLink() {
        this.href = "https://rss.papertime.app";
        this.rel = "self";
        this.type = "application/rss+xml";
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
