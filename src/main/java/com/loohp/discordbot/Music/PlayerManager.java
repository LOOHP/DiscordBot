package com.loohp.discordbot.Music;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;    
    private final ConcurrentLinkedQueue<TrackInQueue> loadqueue;
    private final AtomicLong lastqueue;
    private final PlayerManager runnableINSTANCE;
    private final Timer runnable;

    private PlayerManager() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        
        this.loadqueue = new ConcurrentLinkedQueue<>();
        this.lastqueue = new AtomicLong(0);
        this.runnableINSTANCE = this;
        this.runnable = run();
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, guild);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadAndPlay(TextChannel channel, String trackUrl) {
        loadqueue.add(new TrackInQueue(channel, trackUrl));
    }
    
    public ConcurrentLinkedQueue<TrackInQueue> getLoadingQueue() {
    	return loadqueue;
    }
    
    private Timer run() {
    	Timer timer = new Timer();
    	timer.schedule(new TimerTask() {
    		public void run() {
    			
    			if (!runnableINSTANCE.equals(getInstance())) {
    				runnable.cancel();
    				return;
    			}
    			
    			if (lastqueue.get() < (System.currentTimeMillis() - 1000)) {
    			
	    			TrackInQueue tiq = loadqueue.poll();
	
	    			if (tiq != null) {
	    				
	    				TextChannel channel = tiq.getTextChannel();
	    				String trackUrl = tiq.getUrl();
	    				GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
	    				
		    			playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
		    	            @Override
		    	            public void trackLoaded(AudioTrack track) {
		    	                channel.sendMessage("Adding to queue " + track.getInfo().title + (loadqueue.isEmpty() ? "" : " (" + loadqueue.size() + " left in loading queue)")).queue();
		    	                play(musicManager, track);
		    	                lastqueue.set(System.currentTimeMillis());
		    	            }
		
		    	            @Override
		    	            public void playlistLoaded(AudioPlaylist playlist) {
		    	            	AudioTrack selectedTrack = playlist.getSelectedTrack();
		    	            	
		    	            	if (selectedTrack == null) {
		    	            		selectedTrack = playlist.getTracks().get(0);
		    	                }
		    	            	
		    	            	boolean doAdd = false;
		    	            	AtomicInteger left = new AtomicInteger(playlist.getTracks().size());
		    	            	for (AudioTrack track : playlist.getTracks()) {
		    	            		int remaining = left.decrementAndGet();
		    	            		if (!doAdd && track.equals(selectedTrack)) {
		    	            			doAdd = true;
		    	            		}
		    	            		if (doAdd) {
		    			                channel.sendMessage("Adding to queue " + track.getInfo().title + " from the playlist " + playlist.getName() + ((remaining + loadqueue.size()) <= 0 ? "" : " (" + (remaining + loadqueue.size()) + " left in loading queue)")).queue();
		    			                play(musicManager, track);
		    	            		}		    	         
		    	            		lastqueue.set(System.currentTimeMillis());
		    	            	}
		    	            }
		
		    	            @Override
		    	            public void noMatches() {
		    	                channel.sendMessage("Nothing found by " + trackUrl).queue();
		    	            }
		
		    	            @Override
		    	            public void loadFailed(FriendlyException e) {
		    	                channel.sendMessage("Could not play: " + e.getMessage()).queue();
		    	                System.out.println("Error while loading track in " + channel.getName());
		    	                e.printStackTrace();
		    	            }
		    	        });
	    			}
    			}
    		}
    	}, 0, 2000);
    	return timer;
    }
    
    private void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

    public static synchronized PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }
}