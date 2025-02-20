package io.bitken.tts.rss.view;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.bitken.tts.model.domain.CategoryInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class RssChannel {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

//  <channel>
//    <title><![CDATA[Paper Time - tune in to CS Research]]></title>
//    <description><![CDATA[Abstracts of the latest research papers in Computer Science]]></description>
//    <link>https://papertime.app</link>
//    <image>
//      <url>https://papertime.app/pt-icon-1.ico</url>
//      <title>Paper Time - tune in to CS Research</title>
//      <link>https://papertime.app</link>
//    </image>
//
//    <generator>Paper Time</generator>
//    <lastBuildDate>Sun, 08 Aug 2021 19:11:13 GMT</lastBuildDate>
//    <atom:link href="https://rss.papertime.app" rel="self" type="application/rss+xml"/>
//    <!--<author><![CDATA[Pramod Biligiri]]></author>
//    <copyright><![CDATA[Pramod Biligiri]]></copyright>-->
//    <language><![CDATA[en]]></language>

    @JacksonXmlCData
    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlCData
    @JacksonXmlProperty(localName = "description")
    private String description;

    @JacksonXmlProperty(localName = "link")
    private String link;

    @JacksonXmlProperty(localName = "image")
    private ChannelImage channelImage;

    @JacksonXmlProperty(localName = "generator")
    private String generator;

    @JacksonXmlProperty(localName = "lastBuildDate")
    private String lastBuildDate;

    @JacksonXmlProperty(localName = "pubDate")
    private String pubDate;

//    @JacksonXmlProperty(localName = "link", namespace = "atom")
//    private SelfLink selfLink;

    @JacksonXmlProperty(localName = "item")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<RssItem> rssItems = new ArrayList<>();

    public RssChannel() {

    }

    public RssChannel(CategoryInfo cat, String imgBaseUrl) {
        this.title = cat.getDescriptiveLabel() +" abstracts from papertime.app";
        this.description = "Listen to abstracts of the latest research papers in Computer Science";
        this.link = "https://papertime.app";
        this.channelImage = new ChannelImage(cat, imgBaseUrl);
        this.generator = "Paper Time";

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.lastBuildDate = sdf.format(new Date());
        this.pubDate = this.lastBuildDate;
//        this.selfLink = new SelfLink();
    }

    public void addRssItems(List<RssItem> items) {
        this.rssItems.addAll(items);
    }

    public List<RssItem> getRssItems() {
        return rssItems;
    }

    public void setRssItems(List<RssItem> rssItems) {
        this.rssItems = rssItems;
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

    @JacksonXmlProperty(localName = "link")
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ChannelImage getChannelImage() {
        return channelImage;
    }

    public void setChannelImage(ChannelImage channelImage) {
        this.channelImage = channelImage;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    //    @JacksonXmlProperty(localName = "link", namespace = "atom")
//    public SelfLink getSelfLink() {
//        return selfLink;
//    }

//    public void setSelfLink(SelfLink selfLink) {
//        this.selfLink = selfLink;
//    }

}
