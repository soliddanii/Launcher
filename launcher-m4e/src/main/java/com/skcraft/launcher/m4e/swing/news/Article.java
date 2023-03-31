package com.skcraft.launcher.m4e.swing.news;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Article {

	private String title;
	private String link;
	private String description;
	private String date;
	private String author;

}
