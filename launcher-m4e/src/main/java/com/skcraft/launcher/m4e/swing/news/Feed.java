
package com.skcraft.launcher.m4e.swing.news;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.util.HttpRequest;

public class Feed {
	
	private ArrayList<Article> articles;
	
	public Feed(URL url, int numItems) {
		articles = new ArrayList<>();
		
		try {
			byte[] bytes = HttpRequest.post(url)
					.header("user-agent", "Mozilla/5.0 SKMCLauncher")
					.execute()
					.expectResponseCode(200)
					.returnContent()
					.asBytes();
			
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode feed = objectMapper.readTree(bytes);
			
			JsonNode items = feed.get("items");
			if (items.isArray()) {
				for (JsonNode item : items) {
					articles.add(
							new Article(
									item.get("title").asText(), 
									item.get("url").asText(),
									StringEscapeUtils.escapeHtml(item.get("content_html").asText()),
									formatDate(item.get("date_published").asText()), 
									item.get("author").get("name").asText()
								)
							);

					if (articles.size() == numItems) {
						break;
					}
				}
			}
		} catch (JsonProcessingException ex) {
			ex.printStackTrace(System.err);
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		} catch (InterruptedException ex) {
			ex.printStackTrace(System.err);
		}
	}

	public Article[] getArticles() {
		return (Article[]) this.articles.toArray();
	}

	public Article getArticle(int index) {
		return (index < articles.size()) ? articles.get(index) : null;
	}

	private String formatDate(String date) {
		OffsetDateTime odt = OffsetDateTime.parse(date);
		OffsetDateTime odtTruncatedToWholeSecond = odt.truncatedTo(ChronoUnit.SECONDS);

		return odtTruncatedToWholeSecond.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.getDefault()));
	}
}
