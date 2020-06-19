package com.loohp.discordbot.Music;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    AudioPlayer player;
    List<AudioTrack> queue;
    AtomicInteger currentpos;
    boolean repeat;
    Guild guild;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.queue = new LinkedList<>();
        this.repeat = false;
        this.currentpos = new AtomicInteger(0);
        this.guild = guild;
    }
    
    public boolean isRepeat() {
    	return repeat;
    }
    
    public void setRepeat(boolean repeat) {
    	this.repeat = repeat;
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
    	if (currentpos.get() >= queue.size()) {
    		currentpos.set(0);
    	}
    	queue.add(track);
    	player.startTrack(track, true);
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
    	if (currentpos.get() >= queue.size()) {
    		if (repeat) {
    			currentpos.set(0);
    			queue = queue.stream().map(each -> each.makeClone()).collect(Collectors.toCollection(LinkedList::new));
    			player.startTrack(queue.get(0), false);
    		} else {
    			player.startTrack(null, false);
    			clear();
    			Iterator<TrackInQueue> itr = PlayerManager.getInstance().getLoadingQueue().iterator();
    	        while (itr.hasNext()) {
    	        	TrackInQueue track = itr.next();
    	        	if (track.getTextChannel().getGuild().getIdLong() == guild.getIdLong()) {
    	        		itr.remove();
    	        	}
    	        }
    			PlayerManager.getInstance().getGuildMusicManager(guild).player.destroy();
    			AudioManager audioManager = guild.getAudioManager();
    			audioManager.closeAudioConnection();
    		}
    	} else {
    		queue.set(currentpos.get(), queue.get(currentpos.get()).makeClone());
    		player.startTrack(queue.get(currentpos.get()), false);
    	}
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
        	currentpos.incrementAndGet();
            nextTrack();
        }
    }
    
    public AtomicInteger getRawPosition() {
    	return currentpos;
    }
    
    public int getCurrentPosition() {
    	return currentpos.get();
    }
    
    public List<AudioTrack> getQueueCopy() {
    	return new ArrayList<AudioTrack>(queue);
    }
    
    public List<AudioTrack> getQueue() {
    	return queue;
    }
    
    public void clear() {
    	this.queue.clear();
    }
}