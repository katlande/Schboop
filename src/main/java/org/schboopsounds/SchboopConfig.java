package org.schboopsounds;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup("Schboop")
public interface SchboopConfig extends Config
{

	@ConfigItem(
			keyName = "masterVolume",
			name = "Master Volume",
			description = "Sets the master volume of all ground item sounds",
			position = 0
	)
	default int masterVolume()
	{
		return 100;
	}

	@ConfigItem(
			keyName = "Schboop_says_Moo",
			name = "Moo for raw beef",
			description = "Configure whether or not Schboop should say moo when you get raw beef as loot.",
			position = 1
	)
	default boolean Schboop_says_Moo()
	{
		return true;
	}

	@ConfigItem(
			keyName = "Pops_Died",
			name = "'What happened?' on death",
			description = "Configure whether or not Pops should say 'what happened?' when you die.",
			position = 2
	)
	default boolean Pops_Died()
	{
		return true;
	}

	@ConfigItem(
			keyName = "lowHP",
			name = "Low HP Warnings",
			description = "Configure whether or not Schboop reminds you to drink a yellow when you health is low.",
			position = 2
	)
	default boolean lowHP()
	{
		return true;
	}

	@ConfigItem(
			keyName = "lowpray",
			name = "Low Prayer Warning",
			description = "Configure whether or not Schboop reminds you to drink a pink at 20% prayer.",
			position = 2
	)
	default boolean lowpray()
	{
		return true;
	}
}



