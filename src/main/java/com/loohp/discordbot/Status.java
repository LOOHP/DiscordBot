package com.loohp.discordbot;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.loohp.discordbot.ObjectHolders.StatusHolder;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class Status {
	
	public static Optional<StatusHolder> overrideStatus = Optional.empty();
	public static boolean update = false;
	public static boolean showingPumpkin = false;
	public static boolean showingCustom = false;
	
	public static void run() {
		Random random = new Random();
		int lastranhour = -1;
		while (true) {
			try {
				if (overrideStatus.isPresent()) {
					if (update) {
						DiscordBot.jda.getPresence().setPresence(overrideStatus.get().getOnlineStatus(), overrideStatus.get().getActivity());
						update = false;
						showingCustom = true;
					}
				} else {				
					Guild home = DiscordBot.jda.getGuildById("639738086934446080");
					if (home != null) {
						Member pumpkin = home.getMemberById("671016178722013224");
						if (pumpkin != null && !(pumpkin.getOnlineStatus().equals(OnlineStatus.INVISIBLE) || pumpkin.getOnlineStatus().equals(OnlineStatus.OFFLINE) || pumpkin.getOnlineStatus().equals(OnlineStatus.UNKNOWN))) {
							if (!showingPumpkin) {
								DiscordBot.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.DEFAULT, "with my best friend Pumpkin Pie!!"));
								showingPumpkin = true;
							}
							continue;
						}
					}
					
					LocalDateTime now = LocalDateTime.now();
					if (now.getHour() != lastranhour || showingPumpkin || showingCustom) {
						showingPumpkin = false;
						showingCustom = false;
						lastranhour = now.getHour();
					
						String date = now.getDayOfMonth() + "/" + now.getMonthValue();
						if (date.equals("1/1")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.WATCHING, "people celebrate the year " + now.getYear() + "!"));
						} else if (date.equals("14/2")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "people flashing everywhere.."));
						} else if (date.equals("20/2")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "my master telling me what happened on this day"));
						} else if (date.equals("29/2")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "on the day which only happens once per 4 years"));
						} else if (date.equals("17/5")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "Happy Birthday to Henry!"));
						} else if (date.equals("23/5")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "Happy Birthday to my best friend, Pie!!"));
						} else if (date.equals("27/5")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "Happy Birthday to Biggy!"));
						} else if (date.equals("31/5")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.WATCHING, "Happy Birthday to Carcarcaacacacaracaracrr!"));
						} else if (date.equals("4/6")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.WATCHING, "candle lights on 4/6"));
						} else if (date.equals("12/6")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "what happened on this day in Hong Kong.."));
						} else if (date.equals("21/7")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "what happened on this day in Hong Kong.."));
						} else if (date.equals("2/8")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "Happy Birthday to MYSELF!! (I think)"));
						} else if (date.equals("31/8")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "what happened on this day in Hong Kong.."));
						} else if (date.equals("20/9")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "games ignoring my master's special day"));
						} else if (date.equals("23/10")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "with Nana because it is her birthday!"));
						} else if (date.equals("25/12")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "with my friends on christmas day!"));
						} else if (date.equals("26/12")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "with my friends on boxing day!"));
						} else if (date.equals("31/12")) {
							DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "the countdown and saying goodbye to " + now.getYear() + "!"));
						} else {
							if (now.getDayOfWeek().getValue() <= 5){
								switch (now.getHour()) {
								case 0:
									DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "my master sleep.."));
									break;
								case 1:
								case 2:
								case 3:
								case 4:
								case 5:
								case 6:
									DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "my internet friends on the other side!"));
									break;
								case 7:
									DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "my master wake up and hurry"));
									break;
								case 8:
								case 9:
								case 10:
								case 11:
								case 12:
									DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.LISTENING, "Biggy's beautiful music"));
									break;
								case 13:
								case 14:
								case 15:
									DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.LISTENING, "my master's lessons"));
									break;
								case 16:
								case 17:
								case 18:
								case 19:
								case 20:
								case 21:
								case 22:
								case 23:
									switch (random.nextInt(6)) {
						    		case 0:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP complaining"));
						    			break;
						    		case 1:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP talking about his best friend"));
						    			break;
						    		case 2:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.WATCHING, "Nana's videos ignoring LOOHP"));
						    			break;
						    		case 3:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "with Henry while LOOHP is busy"));
						    			break;
						    		case 4:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP talk about the physical realm"));
						    			break;
						    		case 5:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "Stand with Hong Kong 🇭🇰"));
						    			break;
						    		default:
										break;
									}
									break;
								default:
									break;
								}
							} else {
								switch (now.getHour()) {
								case 0:
									DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "my master sleep.."));
									break;
								case 1:
								case 2:
								case 3:
								case 4:
								case 5:
								case 6:
									DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "my internet friends on the other side!"));
									break;
								case 7:
								case 8:
								case 9:
								case 10:
									DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "my master sleep wondering when he is going to wakeup."));
									break;
								case 11:
								case 12:
								case 13:
								case 14:
								case 15:
								case 16:
								case 17:
								case 18:
								case 19:
									switch (random.nextInt(8)) {
						    		case 0:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP complaining"));
						    			break;
						    		case 1:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP talking about his best friend"));
						    			break;
						    		case 2:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.WATCHING, "Nana's videos ignoring LOOHP"));
						    			break;
						    		case 3:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "with Henry while LOOHP is busy"));
						    			break;
						    		case 4:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP talk about the physical realm"));
						    			break;
						    		case 5:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.LISTENING, "Biggy's beautiful music"));
						    			break;
						    		case 6:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.WATCHING, "whatever Car is watching"));
						    			break;
						    		case 7:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "Stand with Hong Kong 🇭🇰"));
						    			break;
						    		default:
						    			break;
						    		}
									break;
								case 20:
								case 21:
								case 22:
								case 23:
									switch (random.nextInt(2)) {
						    		case 0:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "Minecraft with my master"));
						    			break;
						    		case 1:
						    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "FIFA with my master"));
						    			break;
						    		default:
						    			break;
						    		}
									break;
								default:
									break;
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {TimeUnit.SECONDS.sleep(120);} catch (InterruptedException ignore) {}
		}
		
		/*
    	while (true) {
    		switch (random.nextInt(12)) {
    		case 0:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP complaining"));
    			break;
    		case 1:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP talking about his best friend"));
    			break;
    		case 2:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.WATCHING, "Nana's videos ignoring LOOHP"));
    			break;
    		case 3:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.DEFAULT, "with Henry while LOOHP is busy"));
    			break;
    		case 4:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.IDLE, Activity.of(ActivityType.LISTENING, "LOOHP talk about the physical realm"));
    			break;
    		case 5:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.LISTENING, "Biggy's beautiful music"));
    			break;
    		case 6:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.WATCHING, "whatever Car is watching"));
    			break;
    		case 7:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(ActivityType.WATCHING, "Stand with Hong Kong 🇭🇰"));
    			break;
    		case 8:
    		case 9:
    		case 10:
    		case 11:
    			DiscordBot.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.of(ActivityType.DEFAULT, "with my best friend Pumpkin Pie!!"));
    			break;
    		default:
    			break;
    		}
    		try {TimeUnit.MINUTES.sleep(random.nextInt(5) + 1);} catch (InterruptedException ignore) {}
    	}
    	*/
	}

}
