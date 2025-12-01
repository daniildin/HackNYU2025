package com.nova.parsing;

import java.util.List;

public class Article {
	private String headline;
	private String content;
	private String publisher;
	private List<String> writers;
	private String date;

	public Article() {}
	public Article(String headline, String content) {
		this.headline = headline; this.content = content;
	}

	public String getHeadline() { return headline; }
	public void setHeadline(String headline) { this.headline = headline; }
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	public String getPublisher() { return publisher; }
	public void setPublisher(String publisher) { this.publisher = publisher; }
	public List<String> getWriters() { return writers; }
	public void setWriters(List<String> writers) { this.writers = writers; }
	public String getDate() { return date; }
	public void setDate(String date) { this.date = date; }
}
