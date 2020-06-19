package com.loohp.discordbot;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.loohp.discordbot.ObjectHolders.StatusHolder;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.managers.AudioManager;

public class BiggyMusicStatus {
	
	public static boolean active = false;
	public static AtomicInteger failsCounter = new AtomicInteger(0);
	
	public static void run() {
		while (true) {
			try {TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {}
					
			Guild home = DiscordBot.jda.getGuildById(639738086934446080L);
			if (home != null) {
				//Member boralt = home.getMemberById(218924754923552770L); <-- test LOOHP#9328
				Member boralt = home.getMemberById(500644923712864258L);
				if (boralt != null) {
					GuildVoiceState voiceState = boralt.getVoiceState();
					if (voiceState != null) {
						if (voiceState.isStream()) {
							Member self = home.getMember(DiscordBot.jda.getSelfUser());
							if (self != null) {
								GuildVoiceState selfVoiceState = self.getVoiceState();
								if (!selfVoiceState.inVoiceChannel()) {
									AudioManager audioManager = home.getAudioManager();
									audioManager.openAudioConnection(voiceState.getChannel());
									
									Status.overrideStatus = Optional.of(new StatusHolder(OnlineStatus.ONLINE, Activity.of(ActivityType.LISTENING, "Biggy's lovely music LIVE!🔴")));
									Status.update = true;
									active = true;
								}
								continue;
							}
						}
					} 
				}
			}
			
			if (active) {
				if (failsCounter.incrementAndGet() > 5) {
					home.getAudioManager().closeAudioConnection();
					Status.overrideStatus = Optional.empty();
					active = false;
					failsCounter.set(0);
				}
			}
		}
	}

}
