package io.bitken.tts.rss.view;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.bitken.tts.model.domain.CategoryInfo;

public class ChannelImage {

//      <url>https://papertime.app/pt-icon-1.ico</url>
//      <title>Paper Time - tune in to CS Research</title>
//      <link>https://papertime.app</link>

    @JacksonXmlProperty(localName = "url")
    private String url;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "link")
    private String link;

    public ChannelImage() {

    }

    public ChannelImage(CategoryInfo cat, String imgBaseUrl) {
        this.url = imgBaseUrl + (imgBaseUrl.endsWith("/") ? "" : "/") +  "ppt-" + cat.getDbTag() + "-podcast-1.png";
        this.title = cat.getDescriptiveLabel() + " abstracts: papertime.app";
        this.link = "https://papertime.app";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
