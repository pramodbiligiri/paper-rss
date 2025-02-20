package io.bitken.tts.rss.view;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.bitken.tts.model.domain.CategoryInfo;

import javax.servlet.http.HttpServletRequest;

@JacksonXmlRootElement(localName = "rss")
public class RssFeedView {

//    <rss
//    xmlns:dc="http://purl.org/dc/elements/1.1/"
//    xmlns:content="http://purl.org/rss/1.0/modules/content/"
//    xmlns:atom="http://www.w3.org/2005/Atom"
//    version="2.0"
//    xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">

    @JacksonXmlProperty(namespace = "xmlns", localName = "dc", isAttribute = true)
    private String dc;

    @JacksonXmlProperty(namespace = "xmlns", localName = "content", isAttribute = true)
    private String content;

    @JacksonXmlProperty(namespace = "xmlns", localName = "atom", isAttribute = true)
    private String atom;

    @JacksonXmlProperty(namespace = "xmlns", localName = "version", isAttribute = true)
    private String version;

    @JacksonXmlProperty(namespace = "xmlns", localName = "itunes", isAttribute = true)
    private String itunes;

    @JacksonXmlProperty(localName = "channel")
    private RssChannel channel;

    public RssFeedView() {
    }

    public RssFeedView(CategoryInfo cat, String imgBaseUrl) {
        this.dc = "http://purl.org/dc/elements/1.1/";
        this.content = "http://purl.org/rss/1.0/modules/content/";
        this.atom = "http://www.w3.org/2005/Atom";
        this.version = "2.0";
        this.itunes = "http://www.itunes.com/dtds/podcast-1.0.dtd";
        this.channel = new RssChannel(cat, imgBaseUrl);
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAtom() {
        return atom;
    }

    public void setAtom(String atom) {
        this.atom = atom;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getItunes() {
        return itunes;
    }

    public void setItunes(String itunes) {
        this.itunes = itunes;
    }

    public RssChannel getChannel() {
        return channel;
    }

    public void setChannel(RssChannel channel) {
        this.channel = channel;
    }

    public static RssFeedView createForCategory(CategoryInfo cat, String imgBaseUrl) {
        RssFeedView feedView = new RssFeedView(cat, imgBaseUrl);

        RssChannel channel = feedView.getChannel();
        channel.setTitle(cat.getDescriptiveLabel() + ": Paper Time");
        channel.setDescription("Abstracts of latest research papers in " + cat.getDescriptiveLabel());

        return feedView;
    }
}
