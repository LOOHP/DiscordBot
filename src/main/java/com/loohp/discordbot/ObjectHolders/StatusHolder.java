package com.loohp.discordbot.ObjectHolders;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class StatusHolder {
	
	OnlineStatus status;
	Activity activity;
	
	public StatusHolder(OnlineStatus status, Activity activity) {
		this.status = status;
		this.activity = activity;
	}
	
	public OnlineStatus getOnlineStatus() {
		return status;
	}
	
	public Activity getActivity() {
		return activity;
	}

}
