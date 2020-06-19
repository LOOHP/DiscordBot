package com.loohp.discordbot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.loohp.discordbot.Music.PlayerManager;
import com.loohp.discordbot.ObjectHolders.StatusHolder;
import com.loohp.discordbot.Utils.CustomStringUtils;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class Local {
	
	public static TextChannel currentText;
	
	public static void main() throws IOException {
		while (true) {
			System.out.print("> ");
			String[] args = CustomStringUtils.splitStringToArgs(DiscordBot.in.readLine());
			
			if (args.length != 0) {
				switch (args[0].toLowerCase()) {
				case "status":
					if (args.length == 1) {
						System.out.println(Status.overrideStatus.isPresent() ? "Override ON" : "Override OFF");
						try {
							System.out.println(DiscordBot.jda.getPresence().getStatus().name() + " -> " + DiscordBot.jda.getPresence().getActivity().getType().name() + " : " + DiscordBot.jda.getPresence().getActivity().getName());
						} catch (Exception e) {
							System.out.println("No valid status is currently set");
						}
					} else {
						if (args[1].equalsIgnoreCase("clear")) {
							Status.overrideStatus = Optional.empty();
							System.out.println("Cleared custom override status!");
						} else {
							try {
								List<String> list = new LinkedList<>(Arrays.asList(args));
								list.remove(0);
								list.remove(0);
								list.remove(0);
								String text = String.join(" ", list);
								Status.overrideStatus = Optional.of(new StatusHolder(OnlineStatus.valueOf(args[1].toUpperCase()), Activity.of(ActivityType.valueOf(args[2].toUpperCase()), text)));
								Status.update = true;
								System.out.println("Set override status!");
							} catch (Exception e) {
								System.out.println("Invalid arguments!");
							}
						}
					}
					break;
				case "listtextchannels":
					for (TextChannel text : DiscordBot.jda.getTextChannelCache().asList()) {
						System.out.println(text.getId() + " -> " + text.getGuild().getName() + " : " + text.getName());
					}
					break;
				case "jointextchannel":
					if (args.length > 1) {
						try {
							Long.parseLong(args[1]);
							currentText = DiscordBot.jda.getTextChannelById(args[1]);
							if (currentText != null) {
								System.out.println("Joined " + currentText.getGuild().getName() + " : " + currentText.getName());
							} else {
								System.out.println("Channel not found!");
							}
						} catch (NumberFormatException e) {
							List<TextChannel> list = DiscordBot.jda.getTextChannelsByName(args[1], true);
							if (!list.isEmpty()) {
								currentText = list.get(0);
								System.out.println("Joined " + currentText.getGuild().getName() + " : " + currentText.getName());
							} else {
								System.out.println("Channel not found!");
							}
						}
					}
					break;
				case "listchannelmention":
					for (Member member : currentText.getMembers()) {
						System.out.println(member.getUser().getName() + " -> " + member.getAsMention());
					}
					break;
				case "currenttextchannel":
					if (currentText != null) {
						System.out.println(currentText.getName());
					} else {
						System.out.println("Not yet joined a text channel");
					}
					break;
				case "chat":
					if (currentText != null) {
						List<String> messageList = new ArrayList<String>(Arrays.asList(args));
						messageList.remove(0);
						String message = String.join(" ", messageList);
						currentText.sendMessage(message).queue();
					} else {
						System.out.println("Not yet joined a text channel");
					}
					break;
				case "file":
					if (currentText != null) {
						List<String> messageList = new ArrayList<String>(Arrays.asList(args));
						messageList.remove(0);
						String message = String.join(" ", messageList);
						File file = new File(message);
						if (file.isFile()) {
							currentText.sendFile(file).queue();
						} else {
							System.out.println("That is not a file");
						}
					} else {
						System.out.println("Not yet joined a text channel");
					}
					break;
				case "stop":
					for (Guild guild : DiscordBot.jda.getGuilds()) {
						AudioManager audioManager = guild.getAudioManager();
	
				        if (!audioManager.isConnected()) {
				            continue;
				        }
	
				        PlayerManager manager = PlayerManager.getInstance();
				        manager.getGuildMusicManager(guild).player.destroy();
				        audioManager.closeAudioConnection();
					}
					System.out.println("Shutting down..");
					DiscordBot.jda.shutdownNow();
					FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {
				        @Override
				        public Object call() throws Exception {
							System.runFinalization();
				            return null;
				        }
				    }) {
				    };
				    try {
				        task.get(5, TimeUnit.SECONDS);
				    } catch (Exception e) {
				    	Runtime.getRuntime().halt(0);
				    }
				    System.exit(0);
					break;
				case "":
					break;
				default:
					System.out.println("Typo?");
					break;
				}
			}
		}
	}

}
