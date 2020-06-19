package com.loohp.discordbot.Music;

import net.dv8tion.jda.api.entities.TextChannel;

public class TrackInQueue {
	
	TextChannel channel;
	String url;
	
	public TrackInQueue (TextChannel channel, String url) {
		this.channel = channel;
		this.url = url;
	}
	
	public TextChannel getTextChannel() {
		return channel;
	}
	
	public String getUrl() {
		return url;
	}

}
