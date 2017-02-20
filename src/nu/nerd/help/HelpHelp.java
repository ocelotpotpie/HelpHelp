package nu.nerd.help;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
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
     * Plugin initialisation.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("help-dump")) {
            if (args.length == 0) {
                dumpTopics(sender);
                return true;
            } else {
                return false;
            }

        } else if (command.getName().equals("help-reload")) {
            if (args.length == 0) {
                reloadHelp(sender);
                // TODO: Implement configuration reloading.
                return true;
            } else {
                return false;
            }
        } else if (command.getName().equals("help-load")) {
            if (args.length == 1) {
                loadHelp(sender, args[0]);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * Reload "help.yml".
     *
     * This method uses reflection to access version-specific CraftBukkit
     * methods that probably won't go away.
     *
     * @param sender the command sender.
     */
    protected void reloadHelp(CommandSender sender) {
        HelpMap helpMap = Bukkit.getServer().getHelpMap();
        Class<? extends HelpMap> simpleHelpMapClass = helpMap.getClass();
        try {
            Method clear = simpleHelpMapClass.getMethod("clear");
            Method initializeGeneralTopics = simpleHelpMapClass.getMethod("initializeGeneralTopics");
            Method initializeCommands = simpleHelpMapClass.getMethod("initializeCommands");
            clear.invoke(helpMap);
            initializeGeneralTopics.invoke(helpMap);
            initializeCommands.invoke(helpMap);
            sender.sendMessage(ChatColor.GOLD + "Help reloaded.");
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Unable to reload help: " + ex.toString());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Load help from the specified URI.
     *
     * @param uri the URI as a string; generally a remote file URL.
     */
    protected void loadHelp(CommandSender sender, String uri) {
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                String errorMessage = null;
                try {
                    MessageSink sink = s -> sender.sendMessage(ChatColor.RED + s);
                    HelpLoader loader = new HelpLoader();
                    loader.loadURI(uri);

                    YamlConfiguration output = new YamlConfiguration();
                    HelpRenderer help = new HelpRenderer();
                    help.renderBoilerPlate(output, getConfig());
                    help.renderIndexTopics(output, loader, sink);
                    help.renderGeneralTopics(output, loader, sink);

                    output.save(new File("help.yml"));
                    Bukkit.getScheduler().runTask(HelpHelp.this, new Runnable() {
                        @Override
                        public void run() {
                            reloadHelp(sender);
                        }
                    });
                } catch (URISyntaxException | MalformedURLException ex) {
                    errorMessage = ChatColor.RED + "Error loading help: malformed URL: " + ex.getMessage();
                } catch (InvalidConfigurationException ex) {
                    errorMessage = ChatColor.RED + "Error loading help: the YAML substitution section is malformed: " + ex.getMessage();
                } catch (IllegalArgumentException | IOException ex) {
                    errorMessage = ChatColor.RED + "Error loading help: " + ex.getMessage();
                }
                if (errorMessage != null) {
                    final String finalMessage = errorMessage;
                    Bukkit.getScheduler().runTask(HelpHelp.this, new Runnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(finalMessage);
                        }
                    });
                }
            }
        });
    }

    // ------------------------------------------------------------------------
    /**
     * Dump all topics to a file in the plugin directory.
     *
     * @param sender issuer of the command to dump topics, or null.
     */
    protected void dumpTopics(CommandSender sender) {
        YamlConfiguration output = new YamlConfiguration();
        ConfigurationSection topics = output.createSection("general-topics");
        ConfigurationSection indices = output.createSection("index-topics");

        HelpTopic defaultTopic = Bukkit.getServer().getHelpMap().getHelpTopic("");
        if (defaultTopic != null) {
            if (defaultTopic instanceof IndexHelpTopic) {
                dumpIndexHelpTopic(indices, (IndexHelpTopic) defaultTopic, sender);
            } else {
                dumpHelpTopic(topics, defaultTopic);
            }
        }
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