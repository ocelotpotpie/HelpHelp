package nu.nerd.help;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.java.JavaPlugin;

// ----------------------------------------------------------------------------
/**
 * Main plugin class.
 */
public class HelpHelp extends JavaPlugin {
    // ------------------------------------------------------------------------
    /**
     * Insert custom help topics and programmatically generated indices
     * according to the configuration.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        // TODO: insert topics.
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("helphelp")) {
            if (args.length == 1 && args[0].equals("reload")) {
                reloadHelp();
                // TODO: Implement configuration reloading.

                return true;
            } else if (args.length == 1 && args[0].equals("dump")) {
                dumpTopics(sender);
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    /**
     * Reload "help.yml".
     * 
     * This method uses reflection to access version-specific CraftBukkit
     * methods that probably won't go away.
     */
    protected void reloadHelp() {
        HelpMap helpMap = Bukkit.getServer().getHelpMap();
        Class<? extends HelpMap> simpleHelpMapClass = helpMap.getClass();
        try {
            Method clear = simpleHelpMapClass.getMethod("clear");
            Method initializeGeneralTopics = simpleHelpMapClass.getMethod("initializeGeneralTopics");
            Method initializeCommands = simpleHelpMapClass.getMethod("initializeCommands");
            clear.invoke(helpMap);
            initializeGeneralTopics.invoke(helpMap);
            initializeCommands.invoke(helpMap);
        } catch (Exception ex) {
            getLogger().severe("Unable to reload help: " + ex.toString());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Dump all topics to a file in the plugin directory.
     *
     * @param sender issuer of the command to dump topics, or null.
     */
    protected void dumpTopics(CommandSender sender) {
        YamlConfiguration output = new YamlConfiguration();
        ConfigurationSection topics = output.createSection("topics");
        ConfigurationSection indices = output.createSection("indices");

        for (HelpTopic topic : Bukkit.getServer().getHelpMap().getHelpTopics()) {
            if (topic instanceof IndexHelpTopic) {
                dumpIndexHelpTopic(indices, (IndexHelpTopic) topic, sender);
            } else {
                dumpHelpTopic(topics, topic);
            }
        }

        File outputFile = new File(getDataFolder(), "dump.yml");
        try {
            output.save(outputFile);
        } catch (IOException ex) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "Error dumping topics: " + ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Dump a non-index help topic into the ConfigurationSection.
     *
     * @param section the ConfigurationSection.
     * @param topic the HelpTopic.
     */
    protected void dumpHelpTopic(ConfigurationSection section, HelpTopic topic) {
        ConfigurationSection topicSection = section.createSection(topic.getName());
        topicSection.set("shortText", topic.getShortText());
        topicSection.set("fullText", topic.getFullText(Bukkit.getConsoleSender()));
    }

    // ------------------------------------------------------------------------
    /**
     * Dump an IndexHelpTopic into the specified ConfigurationSection.
     *
     * @param section the ConfigurationSection.
     * @param topic the IndexHelpTopic.
     * @param sender the CommandSender.
     */
    @SuppressWarnings("unchecked")
    protected void dumpIndexHelpTopic(ConfigurationSection section, IndexHelpTopic topic, CommandSender sender) {
        ConfigurationSection topicSection = section.createSection(topic.getName());

        Class<IndexHelpTopic> clazz = IndexHelpTopic.class;
        try {
            Field preamble = clazz.getDeclaredField("preamble");
            Field permission = clazz.getDeclaredField("permission");
            Field allTopics = clazz.getDeclaredField("allTopics");
            preamble.setAccessible(true);
            permission.setAccessible(true);
            allTopics.setAccessible(true);
            topicSection.set("shortText", topic.getShortText());
            topicSection.set("fullText", topic.getFullText(sender));
            topicSection.set("preamble", preamble.get(topic));
            topicSection.set("permission", permission.get(topic));

            ConfigurationSection entriesSection = topicSection.createSection("entries");
            for (HelpTopic subTopic : (Collection<HelpTopic>) allTopics.get(topic)) {
                if (subTopic instanceof IndexHelpTopic) {
                    dumpIndexHelpTopic(entriesSection, (IndexHelpTopic) subTopic, sender);
                } else {
                    dumpHelpTopic(entriesSection, subTopic);
                }
            }

        } catch (Exception ex) {
            getLogger().warning(ex.getMessage());
        }
    }
} // class HelpHelp