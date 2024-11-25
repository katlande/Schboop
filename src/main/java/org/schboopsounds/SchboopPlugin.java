package org.schboopsounds;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.ItemComposition;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.RuneLite;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.*;
import static net.runelite.api.Skill.HITPOINTS;
import static net.runelite.api.Skill.PRAYER;
import net.runelite.api.events.ChatMessage;

import java.util.regex.Pattern;
import okhttp3.OkHttpClient;

@Slf4j
@PluginDescriptor(
	name = "Schboop"
)
public class SchboopPlugin extends Plugin
{
	@Inject
	private ItemManager itemManager;
	private static final File CUSTOM_SOUNDS_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "custom-sounds");
	private static final File SchboopMoo = new File(CUSTOM_SOUNDS_DIR, "moo.wav");
	private static final File WhaHappen = new File(CUSTOM_SOUNDS_DIR, "what_happened_long.wav");
	private static final File yellowv1 = new File(CUSTOM_SOUNDS_DIR, "yellow1.wav");
	private static final File yellowv2 = new File(CUSTOM_SOUNDS_DIR, "yellow2.wav");
	private static final File yellowv3 = new File(CUSTOM_SOUNDS_DIR, "yellow3.wav");
	private static final File yellowv5 = new File(CUSTOM_SOUNDS_DIR, "yellow5.wav");
	private static final File yellowv6 = new File(CUSTOM_SOUNDS_DIR, "yellow6.wav");
	private static final File pink = new File(CUSTOM_SOUNDS_DIR, "pink.wav");
	private static final File[] SOUND_FILES = new File[]{
			SchboopMoo,
			WhaHappen,
			yellowv1,
			yellowv2,
			yellowv3,
			yellowv5,
			yellowv6,
			pink
	};

	// runelite haves the what_happened.wav file... need to convert to something it can stand

    //private List<String> highlightedItemsList = new CopyOnWriteArrayList<>();
	private static final long CLIP_TIME_UNLOADED = -2;

	private long lastClipTime = CLIP_TIME_UNLOADED;
	private Clip clip = null;

	@Inject
	private Client client;

	@Inject
	private SchboopConfig config;

	@Inject
	private OkHttpClient okHttpClient;
	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log:.*");
	// https://github.com/evaan/tedious-collection-log/blob/master/src/main/java/xyz/evaan/TediousCollectionLogPlugin.java
	// collection log related example -- this functionality may not work as I can't test it

	@Override
	protected void startUp() throws Exception
	{
		initSoundFiles();
		//playSound(WhaHappen); // for testing purposes
	}


	@Override
	protected void shutDown()
	{
		clip.close();
		clip = null;
	}

	@Subscribe
	public void onLootReceived(LootReceived lootReceived) {
		for (ItemStack stack : lootReceived.getItems()) {
			handleItem(stack.getId(), stack.getQuantity());
		}
	}


	@Subscribe
	public void onActorDeath(ActorDeath actorDeath) {
		Actor actor = actorDeath.getActor();

		if (!(actor instanceof Player)) {
			return;
		}

		Player player = (Player) actor;
		deathSound(player);
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged){
		if (statChanged.getSkill() != HITPOINTS)
		{
			if(statChanged.getSkill() != PRAYER){
				return;
			} else {
				float currentPray = client.getBoostedSkillLevel(PRAYER);
				int PRAYthresh50 = (int) ((float) client.getRealSkillLevel(HITPOINTS) * 0.4f);
				if (currentPray <= PRAYthresh50 && config.lowpray() && org.schboopsounds.counter.PRAYCOUNTER < 1)
				{
					playSound(pink);
					org.schboopsounds.counter.PRAYCOUNTER = 1;
				}
				if (currentPray > PRAYthresh50 && config.lowpray() && org.schboopsounds.counter.PRAYCOUNTER > 0)
				{
					org.schboopsounds.counter.PRAYCOUNTER = 0;
				}
			}
		} else {
			float currentHP = client.getBoostedSkillLevel(HITPOINTS);
			//int counter = 0;

			int HPthresh50 = (int) ((float) client.getRealSkillLevel(HITPOINTS) * 0.4f);
			int HPthresh40 = (int) ((float) client.getRealSkillLevel(HITPOINTS) * 0.3f );
			int HPthresh30 = (int) ((float) client.getRealSkillLevel(HITPOINTS) * 0.2f);
			int HPthresh20 = (int) ((float) client.getRealSkillLevel(HITPOINTS) * 0.1f);
			int HPthresh10 = (int) ((float) client.getRealSkillLevel(HITPOINTS) * 0.05f);

			//log.warn("Apparently the clip is playing");

			if (currentHP <= HPthresh50 && currentHP > HPthresh40 && config.lowHP() && org.schboopsounds.counter.HPCOUNTER < 1)
			{
				playSound(yellowv1);
				org.schboopsounds.counter.HPCOUNTER = 1;
			}
			if (currentHP <= HPthresh40 && currentHP > HPthresh30 && config.lowHP() && org.schboopsounds.counter.HPCOUNTER < 2)
			{
				playSound(yellowv2);
				org.schboopsounds.counter.HPCOUNTER = 2;
			}
			if (currentHP <= HPthresh30 && currentHP > HPthresh20 && config.lowHP() && org.schboopsounds.counter.HPCOUNTER < 3)
			{
				playSound(yellowv3);
				org.schboopsounds.counter.HPCOUNTER = 3;
			}
			if (currentHP <= HPthresh20 && currentHP > HPthresh10 && config.lowHP() && org.schboopsounds.counter.HPCOUNTER < 4)
			{
				playSound(yellowv5);
				org.schboopsounds.counter.HPCOUNTER = 4;
			}
			if (currentHP <= HPthresh10 && config.lowHP() && org.schboopsounds.counter.HPCOUNTER < 5)
			{
				playSound(yellowv6);
				org.schboopsounds.counter.HPCOUNTER = 5;
			}
			if (currentHP > HPthresh50 && config.lowHP() && org.schboopsounds.counter.HPCOUNTER > 0)
			{
				org.schboopsounds.counter.HPCOUNTER = 0;
			}
		}

	}
	// this works but it plays at the wrong time
	// check and see what it thinks the HPthresh and currentHP variables are


	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) {
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM) {
			return;
		}
		if (COLLECTION_LOG_ITEM_REGEX.matcher(chatMessage.getMessage()).matches()) {
			playSound(SchboopMoo);
		}

	}


    private void handleItem(int id, int quantity) {
		final ItemComposition itemComposition = itemManager.getItemComposition(id);
		final String name = itemComposition.getName().toLowerCase();
		//playSound(SchboopMoo);
		if (config.Schboop_says_Moo() && name.contains("raw beef")) {
			playSound(SchboopMoo);
		}
	}



	private void playSound(File f)
	{
		long currentTime = System.currentTimeMillis();
		if (clip == null || !clip.isOpen() || currentTime != lastClipTime) {
			lastClipTime = currentTime;
			try
			{
				// making sure last clip closes so we don't get multiple instances
				if (clip != null && clip.isOpen()) clip.close();

				AudioInputStream is = AudioSystem.getAudioInputStream(f);
				AudioFormat format = is.getFormat();
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				clip = (Clip) AudioSystem.getLine(info);
				clip.open(is);
				setVolume(config.masterVolume());
				clip.start();
				//log.warn("Apparently the clip is playing");
			}
			catch (LineUnavailableException | UnsupportedAudioFileException | IOException e)
			{
				log.warn("Sound file error", e);
				lastClipTime = CLIP_TIME_UNLOADED;
			}
		}
	}



	private void deathSound(Player player) {
		if (player == null) {
			return;
		}
		if (player == client.getLocalPlayer() && config.Pops_Died()) {
			playSound(WhaHappen);
		}
		if (player != client.getLocalPlayer()) {
			return;
		}

	}


	// sets volume using dB to linear conversion
	private void setVolume(int volume)
	{
		float vol = volume/100.0f;
		vol *= config.masterVolume()/100.0f;
		FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(20.0f * (float) Math.log10(vol));
	}


	public class counter{
		public int HPcounter = 0;
	}

	@Provides
	SchboopConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SchboopConfig.class);
	}

	// initialize sound files if they haven't been created yet
	private void initSoundFiles()
	{
		if (!CUSTOM_SOUNDS_DIR.exists())
		{
			CUSTOM_SOUNDS_DIR.mkdirs();
		}

		for (File f : SOUND_FILES)
		{
			try
			{
				if (f.exists()) {
					continue;
				}
				InputStream stream = SchboopPlugin.class.getClassLoader().getResourceAsStream(f.getName());
				OutputStream out = new FileOutputStream(f);
				byte[] buffer = new byte[8 * 1024];
				int bytesRead;
				while ((bytesRead = stream.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
				out.close();
				stream.close();
			}  catch (Exception e) {
				log.debug(e + ": " + f);
			}
		}
	}
}
