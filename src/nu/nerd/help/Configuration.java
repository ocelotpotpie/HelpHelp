package nu.nerd.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

// ----------------------------------------------------------------------------
/**
 * Reads and exposes the plugin configuration.
 */
public class Configuration {
    // ------------------------------------------------------------------------
    /**
     * Minecraft formatting codes for block quotes.
     */
    public TextStyle QUOTE_STYLE = new TextStyle("&a&o");

    /**
     * Minecraft formatting codes for code blocks.
     */
    public TextStyle CODE_STYLE = new TextStyle("&e&o");

    /**
     * Minecraft formatting codes for hyperlinks.
     */
    public TextStyle LINK_STYLE = new TextStyle("&b");

    /**
     * Minecraft formatting codes for bold text.
     */
    public TextStyle BOLD_STYLE = new TextStyle("&e&l");

    /**
     * Minecraft formatting codes for strikethrough text.
     */
    public TextStyle STRIKETHROUGH_STYLE = new TextStyle("&m");

    /**
     * Minecraft formatting codes for underlined text.
     */
    public TextStyle UNDERLINE_STYLE = new TextStyle("&n");

    /**
     * Minecraft formatting codes for italicised text.
     */
    public TextStyle ITALICS_STYLE = new TextStyle("&o");

    /**
     * Minecraft formatting codes for headings.
     *
     * Array entry i contains the style for heading level (i+1), where the most
     * significant heading is level 1, and subsequent levels signify sub-topics.
     */
    public ArrayList<TextStyle> HEADING_STYLES = new ArrayList<>();

    /**
     * Name of generated HTML output file.
     */
    public String HTML_OUTPUT_FILE;

    /**
     * Replacement title of the "Default" index topic.
     */
    public String HTML_DEFAULT_TOPIC_TITLE;

    /**
     * Set of amended permission strings that topic nodes must have to be
     * visible in the generated HTML (can include the empty string).
     */
    public HashSet<String> HTML_VISIBLE_PERMISSIONS = new HashSet<>();

    /**
     * HTML text to be prepended before any HTML generated from topics.
     */
    public String HTML_HEADER;

    /**
     * HTML text to be appended after any HTML generated from topics.
     */
    public String HTML_FOOTER;

    // ------------------------------------------------------------------------
    /**
     * Load the plugin configuration.
     */
    public void reload() {
        HelpHelp.PLUGIN.reloadConfig();
        FileConfiguration config = HelpHelp.PLUGIN.getConfig();

        QUOTE_STYLE = new TextStyle(config.getString("style.quote"));
        CODE_STYLE = new TextStyle(config.getString("style.code"));
        LINK_STYLE = new TextStyle(config.getString("style.link"));
        BOLD_STYLE = new TextStyle(config.getString("style.bold"));
        STRIKETHROUGH_STYLE = new TextStyle(config.getString("style.strikethrough"));
        UNDERLINE_STYLE = new TextStyle(config.getString("style.underline"));
        ITALICS_STYLE = new TextStyle(config.getString("style.italics"));

        HEADING_STYLES.clear();
        List<String> headingStyles = config.getStringList("style.headings");
        if (headingStyles.isEmpty()) {
            headingStyles = Arrays.asList("&c&o", "&d&o", "&a&o", "&6&o", "&d&o");
        }
        for (String style : headingStyles) {
            HEADING_STYLES.add(new TextStyle(style));
        }

        HTML_OUTPUT_FILE = config.getString("html.output-file");
        HTML_DEFAULT_TOPIC_TITLE = config.getString("html.default-topic-title");
        HTML_VISIBLE_PERMISSIONS.clear();
        HTML_VISIBLE_PERMISSIONS.addAll(config.getStringList("html.visible-permissions"));
        HTML_HEADER = config.getString("html.header");
        HTML_FOOTER = config.getString("html.footer");
    }

    // ------------------------------------------------------------------------
    /**
     * Return the TextStyle for formatting a heading of the specified level as
     * Minecraft help text.
     *
     * Styles repeat, cyclically if the level exceeds the number of styles that
     * have been defined.
     *
     * @param level the level, beginning at 1.
     * @return the TextStyle corresponding to the level.
     */
    public TextStyle getHeadingStyle(int level) {
        int modLevel = Math.max(0, level - 1) % HEADING_STYLES.size();
        return HEADING_STYLES.get(modLevel);
    }
} // class Configuration