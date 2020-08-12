package com.loohp.discordbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.security.auth.login.LoginException;

import com.loohp.discordbot.Music.GuildMusicManager;
import com.loohp.discordbot.Music.PlayerManager;
import com.loohp.discordbot.Music.TrackInQueue;
import com.loohp.discordbot.Music.TrackScheduler;
import com.loohp.discordbot.Utils.CustomStringUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class DiscordBot extends ListenerAdapter {
	
	public static BufferedReader in;
	public static JDA jda;
	
	public static void main(String[] args) throws LoginException {
		if (args.length != 1) {
			return;
		}
		jda = JDABuilder.createDefault(args[0]).build();
		jda.addEventListener(new DiscordBot());
		StickerManager.generate();
		
		in = new BufferedReader(new InputStreamReader(System.in));
		
		Thread t1 = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	try {
					Local.main();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		});  
		t1.start();
		
		Thread t2 = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	Status.run();
		    }
		});  
		t2.start();
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		System.out.println("Bot is ready!");
		System.out.println("System Time: " + new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss' 'zzz").format(new Date()));
		System.out.println("Joined guilds:");
		jda.getGuilds().forEach(each -> System.out.println(each.getName()));
		
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				BiggyMusicStatus.run();
			}
		});
		t3.start();
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (!event.getChannelType().equals(ChannelType.TEXT)) {
			return;
		}
		
		Guild guild = event.getGuild();
		Message message = event.getTextChannel().retrieveMessageById(event.getMessageId()).complete();
		MessageChannel channel = event.getChannel();
		ReactionEmote reaction = event.getReactionEmote();
		if (!reaction.isEmoji()) {
			return;
		}
		
		String emote = reaction.getEmoji();
		
		if (!message.getAuthor().equals(jda.getSelfUser())) {
			return;
		}
		
		if (event.getUser().equals(jda.getSelfUser())) {
			return;
		}
		
		if (message.getContentRaw().contains("**Current Track**")) {
			if (emote.equals("⏸️")) {
				PlayerManager manager = PlayerManager.getInstance();
				AudioPlayer player = manager.getGuildMusicManager(guild).player;
				player.setPaused(!player.isPaused());
				channel.sendMessage(player.isPaused() ? "Paused the music player!" : "Resumed the music player!").queue();
			} else if (emote.equals("⏭️")) {
				PlayerManager manager = PlayerManager.getInstance();
				TrackScheduler scheduler = manager.getGuildMusicManager(guild).scheduler;
				scheduler.getRawPosition().incrementAndGet();
				scheduler.nextTrack();
				channel.sendMessage("Skipping to the next track!").queue();
			} else if (emote.equals("⏮️")) {
				PlayerManager manager = PlayerManager.getInstance();
				TrackScheduler scheduler = manager.getGuildMusicManager(guild).scheduler;
				scheduler.getRawPosition().decrementAndGet();
				scheduler.nextTrack();
				channel.sendMessage("Going back to the previous track!").queue();
			} else if (emote.equals("🔀")) {
				PlayerManager manager = PlayerManager.getInstance();
				TrackScheduler scheduler = manager.getGuildMusicManager(guild).scheduler;
				Collections.shuffle(scheduler.getQueue());
				channel.sendMessage("Randomized the position of tracks in the playlist!").queue();
			} else if (emote.equals("🛑")) {
				AudioManager audioManager = event.getGuild().getAudioManager();

		        if (!audioManager.isConnected()) {
		            channel.sendMessage("I'm not connected to a voice channel").queue();
		            return;
		        }

		        VoiceChannel voiceChannel = audioManager.getConnectedChannel();

		        if (!voiceChannel.getMembers().contains(event.getMember())) {
		            channel.sendMessage("You have to be in the same voice channel as me to use this").queue();
		            return;
		        }
		        PlayerManager manager = PlayerManager.getInstance();
		        manager.getGuildMusicManager(guild).player.destroy();
		        Iterator<TrackInQueue> itr = manager.getLoadingQueue().iterator();
		        while (itr.hasNext()) {
		        	TrackInQueue track = itr.next();
		        	if (track.getTextChannel().getGuild().getIdLong() == guild.getIdLong()) {
		        		itr.remove();
		        	}
		        }
		        manager.getGuildMusicManager(guild).scheduler.clear();
		        audioManager.closeAudioConnection();
		        channel.sendMessage("Disconnected from your channel").queue();
		        message.removeReaction("⏸️", jda.getSelfUser()).queue();
		        message.removeReaction("⏭️", jda.getSelfUser()).queue();
		        message.removeReaction("🛑", jda.getSelfUser()).queue();
		        message.removeReaction("⏮️", jda.getSelfUser()).queue();
		        message.removeReaction("🔀", jda.getSelfUser()).queue();
		        message.removeReaction("❌", jda.getSelfUser()).queue();
			}
			message.removeReaction(emote, event.getUser()).queue();
		}
		
		if (emote.equals("❌")) {
			message.delete().queueAfter(2, TimeUnit.SECONDS);
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {		
		if (!event.getChannelType().equals(ChannelType.TEXT)) {
			return;
		}
		
		Message message = event.getMessage();
		String[] args = CustomStringUtils.splitStringToArgs(message.getContentRaw());
		User user = event.getAuthor();
		Guild guild = message.getGuild();
		MessageChannel channel = event.getChannel();
		if (args.length == 0) {
			return;
		}
		
		if (message.getContentRaw().toLowerCase().contains("apple")) {
			int index = message.getContentRaw().toLowerCase().indexOf("apple");
			if (index >= 0) {
				char before = (index - 1) < 0 ? ' ' : message.getContentRaw().charAt(index - 1);
				char after = (index + "apple".length()) >= message.getContentRaw().length() ? ' ' : message.getContentRaw().charAt(index + "apple".length());
				if (String.valueOf(before).matches("[^a-zA-Z0-9]") && String.valueOf(after).matches("[^a-zA-Z0-9]")) {
					message.addReaction("🍎").queue();
				}
			}
		}
		if (message.getContentRaw().toLowerCase().contains("pie")) {
			int index = message.getContentRaw().toLowerCase().indexOf("pie");
			if (index >= 0) {
				char before = (index - 1) < 0 ? ' ' : message.getContentRaw().charAt(index - 1);
				char after = (index + "pie".length()) >= message.getContentRaw().length() ? ' ' : message.getContentRaw().charAt(index + "pie".length());
				if (String.valueOf(before).matches("[^a-zA-Z0-9]") && String.valueOf(after).matches("[^a-zA-Z0-9]")) {
					message.addReaction("🎃").queue();
				}
			}
		}
		
		if (event.getAuthor().isBot()) {
			return;
		}
		
		if (args[0].toLowerCase().equalsIgnoreCase("==sticker")) {
			if (args.length == 2) {
				if (args[1].equalsIgnoreCase("list")) {
					channel.sendMessage("All stickers are fetched from LOOHP's github repository\nhttps://github.com/LOOHP/StickerBase\n\nListing all stickers:\n```\n" + StickerManager.getStickerList() + "\n```\n").queue();
				} else {
					File file = StickerManager.getSticker(args[1]);
					if (file == null) {
						channel.sendMessage(user.getAsMention() + " Sticker `" + args[1] + "` does not exist on the StickerBase!").delay(3, TimeUnit.SECONDS, null).flatMap(Message::delete).queue();
						message.delete().queue();
					} else {
						channel.sendMessage(user.getAsMention() + " sent a sticker!").addFile(file).queue();
						message.delete().queue();
					}
				}
			}
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==henryip")) {
			channel.sendMessage("Nobody will ever remember it: \n```\nonlyyou.asuscomm.com\n```").queue();
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==ping")) {
            long time = System.currentTimeMillis();
            channel.sendMessage("Pong!").queue(response -> {
            	response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
            });
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==discordbday")) {
			if (args.length == 1) {
				String date = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss' 'zzz").format(new Date(user.getTimeCreated().toInstant().toEpochMilli()));
	            channel.sendMessage("Your discord birthday is " + date + "!").queue();
			} else if (args.length == 2) {
				User query = message.getMentionedMembers().get(0).getUser();
				String date = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss' 'zzz").format(new Date(query.getTimeCreated().toInstant().toEpochMilli()));
	            channel.sendMessage(query.getAsMention() + "'s discord birthday is " + date + "!").queue();
			}
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==avatar")) {
			try {
				if (args.length == 1) {
					URL website = new URL(user.getAvatarUrl());
					URLConnection connection = website.openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla 5.0 (Windows; U; " + "Windows NT 5.1; en-US; rv:1.8.0.11) ");
					ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
					File avatar = new File("avatar");
					File output = new File(avatar, user.getId() + ".png");
					avatar.mkdirs();
					FileOutputStream fos = new FileOutputStream(output);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					fos.close();
					channel.sendMessage("Here is your awesome avatar!").addFile(output).queue(response -> {
						output.delete();
					});
				} else if (args.length == 2) {
					if (args[1].toLowerCase().equals("server")) {
						Guild query = message.getGuild();
						URL website = new URL(query.getIconUrl());
						URLConnection connection = website.openConnection();
						connection.setRequestProperty("User-Agent", "Mozilla 5.0 (Windows; U; " + "Windows NT 5.1; en-US; rv:1.8.0.11) ");
						ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
						File avatar = new File("avatar");
						File output = new File(avatar, user.getId() + ".png");
						avatar.mkdirs();
						FileOutputStream fos = new FileOutputStream(output);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						fos.close();
						channel.sendMessage("Here is " + query.getName() + "'s awesome icon!").addFile(output).queue(response -> {
							output.delete();
						});
					} else {
						User query = message.getMentionedMembers().get(0).getUser();
						URL website = new URL(query.getAvatarUrl());
						URLConnection connection = website.openConnection();
						connection.setRequestProperty("User-Agent", "Mozilla 5.0 (Windows; U; " + "Windows NT 5.1; en-US; rv:1.8.0.11) ");
						ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
						File avatar = new File("avatar");
						File output = new File(avatar, user.getId() + ".png");
						avatar.mkdirs();
						FileOutputStream fos = new FileOutputStream(output);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						fos.close();
						channel.sendMessage("Here is " + query.getAsMention() + "'s awesome avatar!").addFile(output).queue(response -> {
							output.delete();
						});
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==play")) {
			
			AudioManager audioManager = guild.getAudioManager();

	        GuildVoiceState memberVoiceState = event.getMember().getVoiceState();
	        VoiceChannel voiceChannel = memberVoiceState.getChannel();
	        Member selfMember = event.getGuild().getSelfMember();
	        
	        if (!audioManager.isConnected()) {
	        	if (!memberVoiceState.inVoiceChannel()) {
		            channel.sendMessage("Please join a voice channel first").queue();
		            return;
		        }
	        	
	        	audioManager.openAudioConnection(voiceChannel);
		        channel.sendMessage("Joining your voice channel").queue();			   
	        }	  

	        if (!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
	            channel.sendMessageFormat("I am missing permission to join %s", voiceChannel).queue();
	            return;
	        }
			
			PlayerManager manager = PlayerManager.getInstance();
			
			if (args.length == 2) {
				String link = args[1];
		        manager.loadAndPlay(message.getTextChannel(), link);
		        int volume = manager.getGuildMusicManager(event.getGuild()).player.getVolume();
		        if (args.length > 2) {
		        	try {
		        		volume = Integer.parseInt(args[2]);
		        	} catch (Exception ignore) {}
		        }
		        manager.getGuildMusicManager(event.getGuild()).player.setVolume(volume);
			} else if (!message.getAttachments().isEmpty()) {
				for (Attachment attachment : message.getAttachments()) {
					String link = attachment.getUrl();
			        manager.loadAndPlay(message.getTextChannel(), link);
			        int volume = manager.getGuildMusicManager(event.getGuild()).player.getVolume();
			        if (args.length > 2) {
			        	try {
			        		volume = Integer.parseInt(args[2]);
			        	} catch (Exception ignore) {}
			        }
			        manager.getGuildMusicManager(event.getGuild()).player.setVolume(volume);
				}
			} else {
				channel.sendMessage("Please provide a audio source!").queue();
			}
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==volume")) {
            if (args.length > 1) {
            	PlayerManager manager = PlayerManager.getInstance();
		        int volume = 100;
		        if (args.length > 1) {
		        	try {
		        		volume = Integer.parseInt(args[1]);
		        	} catch (Exception ignore) {}
		        }
		        manager.getGuildMusicManager(event.getGuild()).player.setVolume(volume);
		        channel.sendMessage("Set the volume to " + volume).queue();
            } else {
				channel.sendMessage("Please provide a volume!").queue();
			}
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==save")) {
			List<Member> mentionList = message.getMentionedMembers();
			String userId = mentionList.isEmpty() ? user.getId() : mentionList.get(0).getUser().getId();
			List<AudioTrack> queue = PlayerManager.getInstance().getGuildMusicManager(guild).scheduler.getQueueCopy();
			File folder = new File("playlist");
			folder.mkdirs();
			File file = new File(folder, userId + ".txt");
			try {
				PrintWriter writer = new PrintWriter(new FileOutputStream(file, false));
				queue.forEach(song -> writer.println(song.getInfo().uri));
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			channel.sendMessage("Saved " + queue.size() + " songs into personal playlist!").queue();
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==saveappend")) {
			List<Member> mentionList = message.getMentionedMembers();
			String userId = mentionList.isEmpty() ? user.getId() : mentionList.get(0).getUser().getId();
			List<AudioTrack> queue = PlayerManager.getInstance().getGuildMusicManager(guild).scheduler.getQueueCopy();
			File folder = new File("playlist");
			folder.mkdirs();
			File file = new File(folder, userId + ".txt");
			try {
				PrintWriter writer = new PrintWriter(new FileOutputStream(file, true));			
				queue.forEach(song -> writer.println(song.getInfo().uri));
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			channel.sendMessage("Appended " + queue.size() + " songs into personal playlist!").queue();
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==load")) {
			List<Member> mentionList = message.getMentionedMembers();
			String userId = mentionList.isEmpty() ? user.getId() : mentionList.get(0).getUser().getId();
			File folder = new File("playlist");
			folder.mkdirs();
			File file = new File(folder, userId + ".txt");
			if (file.exists()) {			
				AudioManager audioManager = guild.getAudioManager();
	
		        GuildVoiceState memberVoiceState = event.getMember().getVoiceState();
		        VoiceChannel voiceChannel = memberVoiceState.getChannel();
		        Member selfMember = event.getGuild().getSelfMember();
		        
		        if (!audioManager.isConnected()) {
		        	if (!memberVoiceState.inVoiceChannel()) {
			            channel.sendMessage("Please join a voice channel first").queue();
			            return;
			        }
		        	
		        	audioManager.openAudioConnection(voiceChannel);
			        channel.sendMessage("Joining your voice channel").queue();			   
		        }	  
	
		        if (!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
		            channel.sendMessageFormat("I am missing permission to join %s", voiceChannel).queue();
		            return;
		        }
				
				PlayerManager manager = PlayerManager.getInstance();
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
				    for (String link; (link = br.readLine()) != null;) {
				        manager.loadAndPlay(message.getTextChannel(), link);
				    }
				    br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				channel.sendMessageFormat("No playlists created!").queue();
			}
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==pause")) {
			PlayerManager manager = PlayerManager.getInstance();
			AudioPlayer player = manager.getGuildMusicManager(guild).player;
			player.setPaused(!player.isPaused());
			channel.sendMessage(player.isPaused() ? "Paused the music player!" : "Resumed the music player!").queue();
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==remove")) {
			try {
				PlayerManager manager = PlayerManager.getInstance();
				TrackScheduler scheduler = manager.getGuildMusicManager(guild).scheduler;
				AudioTrack track = scheduler.getQueue().remove(Integer.parseInt(args[1]) - 1);
				channel.sendMessage("Removed track " + track.getInfo().title).queue();
			} catch (Exception e) {
				channel.sendMessage("Invalid arguments!").queue();
			}
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==skip")) {
			if (args.length == 1) {
				PlayerManager manager = PlayerManager.getInstance();
				TrackScheduler scheduler = manager.getGuildMusicManager(guild).scheduler;
				scheduler.getRawPosition().incrementAndGet();
				scheduler.nextTrack();
				channel.sendMessage("Skipping to the next track!").queue();
			} else {
				try {
					PlayerManager manager = PlayerManager.getInstance();
					TrackScheduler scheduler = manager.getGuildMusicManager(guild).scheduler;
					int pos = Integer.parseInt(args[1]) - 1;
					if (pos >= 0 || pos < scheduler.getQueue().size()) {
						scheduler.getRawPosition().set(pos);
						scheduler.nextTrack();
						channel.sendMessage("Skipping to the track " + (pos + 1) + "!").queue();
					} else {
						channel.sendMessage("Track position out of range!").queue();
					}
				} catch (NumberFormatException e) {
					channel.sendMessage("Invalid input! Integer expected!").queue();
				}
			}
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==shuffle")) {
			PlayerManager manager = PlayerManager.getInstance();
			TrackScheduler scheduler = manager.getGuildMusicManager(guild).scheduler;
			Collections.shuffle(scheduler.getQueue());
			channel.sendMessage("Randomized the position of tracks in the playlist!").queue();
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==current")) {
			AudioManager audioManager = event.getGuild().getAudioManager();

	        if (!audioManager.isConnected()) {
	            channel.sendMessage("I'm not connected to a voice channel").queue();
	            return;
	        }
	        
			PlayerManager manager = PlayerManager.getInstance();
			AudioPlayer player = manager.getGuildMusicManager(guild).player;
			TrackScheduler scheduler = manager.getGuildMusicManager(guild).scheduler;
			String currentPos = String.format("%02d:%02d", 
				    TimeUnit.MILLISECONDS.toMinutes(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getPosition()),
				    TimeUnit.MILLISECONDS.toSeconds(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getPosition()) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getPosition()))
			);
			String finalPos = String.format("%02d:%02d", 
				    TimeUnit.MILLISECONDS.toMinutes(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getDuration()),
				    TimeUnit.MILLISECONDS.toSeconds(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getDuration()) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getDuration()))
			);
			Message info = channel.sendMessage(
					"**Current Track** (Playlist #" + (scheduler.getCurrentPosition() + 1) + ")\n"
					+ "```"
					+ "Title: " + scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getInfo().title + "\n"
					+ "Author: " + scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getInfo().author + "\n"
					+ "Volume: " + player.getVolume() + "\n"
					+ "Current Position: " + currentPos + "/" + finalPos + "\n"
					+ "```").complete();
			
			info.addReaction("🛑").queue(response -> {
				info.addReaction("⏮️").queue(response2 -> {
					info.addReaction("⏸️").queue(response3 -> {
						info.addReaction("⏭️").queue(response4 -> {
							info.addReaction("🔀").queue(response5 -> {
								info.addReaction("❌").queue();
							});
						});
					});
				});
            });
			
			Thread t1 = new Thread(new Runnable() {
			    @Override
			    public void run() {
			    	while (!scheduler.getQueueCopy().isEmpty()) {
			    		try {
							try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException ignore) {}
							String currentPos = String.format("%02d:%02d", 
								    TimeUnit.MILLISECONDS.toMinutes(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getPosition()),
								    TimeUnit.MILLISECONDS.toSeconds(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getPosition()) - 
								    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getPosition()))
							);
							String finalPos = String.format("%02d:%02d", 
								    TimeUnit.MILLISECONDS.toMinutes(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getDuration()),
								    TimeUnit.MILLISECONDS.toSeconds(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getDuration()) - 
								    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getDuration()))
							);
							try {
							info.editMessage(
									"**Current Track** (Playlist #" + (scheduler.getCurrentPosition() + 1) + ")\n"
								  + "```"
								  + "Title: " + scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getInfo().title + "\n"
								  + "Author: " + scheduler.getQueueCopy().get(scheduler.getCurrentPosition()).getInfo().author + "\n"
								  + "Volume: " + player.getVolume() + "\n"
								  + "Current Position: " + currentPos + "/" + finalPos + "\n"
								  + "```").complete(false);
							} catch (RateLimitedException ignore) {}
			    		} catch (Exception e) {
			    			break;
			    		}
					}
			    	try {
			    		info.editMessage("Track Ended!").queue();
			    		info.removeReaction("⏸️", jda.getSelfUser()).queue();
			    		info.removeReaction("⏭️", jda.getSelfUser()).queue();
			    		info.removeReaction("🛑", jda.getSelfUser()).queue();
			    		info.removeReaction("⏮️", jda.getSelfUser()).queue();
			    		info.removeReaction("🔀", jda.getSelfUser()).queue();
			    		info.removeReaction("❌", jda.getSelfUser()).queue();
			    	} catch (Exception ignore) {}
			    }
			});  
			t1.start();
		}
		
		if (args[0].toLowerCase().equalsIgnoreCase("==playlist")) {
	        AudioManager audioManager = event.getGuild().getAudioManager();

	        if (!audioManager.isConnected()) {
	            channel.sendMessage("I'm not connected to a voice channel").queue();
	            return;
	        } 

	        PlayerManager manager = PlayerManager.getInstance();
	        GuildMusicManager guildMusicManager = manager.getGuildMusicManager(guild);
	        StringBuilder sb = new StringBuilder();
	        int pos = 1;
	        int currentpos = guildMusicManager.scheduler.getCurrentPosition() + 1;
	        for (AudioTrack each : guildMusicManager.scheduler.getQueueCopy()) {
	        	sb.append(pos + ". " + each.getInfo().title + " ");
	        	if (pos == currentpos) {
	        		sb.append("<-- Current Playing!");
	        	}
	        	sb.append("\n");
	        	pos++;
	        }
	        AtomicLong total = new AtomicLong(0);
	        guildMusicManager.scheduler.getQueueCopy().stream().forEach(each -> total.addAndGet(each.getInfo().length));
	        
	        AtomicLong current = new AtomicLong(0);
	        int position = 1;
	        for (AudioTrack each : guildMusicManager.scheduler.getQueueCopy()) {
	        	if (position == currentpos) {
	        		current.addAndGet(each.getPosition());
	        		break;
	        	} else {
	        		current.addAndGet(each.getInfo().length);
	        	}
	        	position++;
	        }
	        
	        String totalString = String.format("%02d:%02d", 
				    TimeUnit.MILLISECONDS.toMinutes(total.get()),
				    TimeUnit.MILLISECONDS.toSeconds(total.get()) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(total.get()))
			);
			String currentString = String.format("%02d:%02d", 
				    TimeUnit.MILLISECONDS.toMinutes(current.get()),
				    TimeUnit.MILLISECONDS.toSeconds(current.get()) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(current.get()))
			);
			
			String leftString = String.format("%02d:%02d", 
				    TimeUnit.MILLISECONDS.toMinutes(total.get() - current.get()),
				    TimeUnit.MILLISECONDS.toSeconds(total.get() - current.get()) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(total.get() - current.get()))
			);
	        
	        channel.sendMessage(
					  "**Current Playlist** " + (guildMusicManager.scheduler.isRepeat() ? "(Playlist repeat **ON**!)" : "") + "\n"
					+ "__Total Duration: " + currentString + "/" + totalString + " (" + leftString + " left)__\n"
					+ "```"
					+ sb.toString()
					+ "```").queue(response -> {
						response.addReaction("❌").queue();
					});
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==repeat")) {
	        AudioManager audioManager = event.getGuild().getAudioManager();

	        if (!audioManager.isConnected()) {
	            channel.sendMessage("I'm not connected to a voice channel").queue();
	            return;
	        } 

	        PlayerManager manager = PlayerManager.getInstance();
	        GuildMusicManager guildMusicManager = manager.getGuildMusicManager(guild);
	        boolean value = !guildMusicManager.scheduler.isRepeat();
	        guildMusicManager.scheduler.setRepeat(value);
	        
	        channel.sendMessage("Set playlist repeat mode to " + value).queue();
		}
		if (args[0].toLowerCase().equalsIgnoreCase("==stop")) {
	        AudioManager audioManager = event.getGuild().getAudioManager();

	        if (!audioManager.isConnected()) {
	            channel.sendMessage("I'm not connected to a voice channel").queue();
	            return;
	        }

	        VoiceChannel voiceChannel = audioManager.getConnectedChannel();

	        if (!voiceChannel.getMembers().contains(event.getMember())) {
	            channel.sendMessage("You have to be in the same voice channel as me to use this").queue();
	            return;
	        }
	        PlayerManager manager = PlayerManager.getInstance();
	        manager.getGuildMusicManager(guild).player.destroy();
	        Iterator<TrackInQueue> itr = manager.getLoadingQueue().iterator();
	        while (itr.hasNext()) {
	        	TrackInQueue track = itr.next();
	        	if (track.getTextChannel().getGuild().getIdLong() == guild.getIdLong()) {
	        		itr.remove();
	        	}
	        }
	        manager.getGuildMusicManager(guild).scheduler.clear();
	        audioManager.closeAudioConnection();
	        channel.sendMessage("Disconnected from your channel").queue();
		}
		
	}

}
