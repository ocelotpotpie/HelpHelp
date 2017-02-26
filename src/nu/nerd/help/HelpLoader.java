package nu.nerd.help;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

// ----------------------------------------------------------------------------
/**
 * Loads help from a Markdown file specified as a URL and provides access to the
 * various sections of it.
 */
public class HelpLoader {
    // ------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        MessageSink sink = s -> System.out.println(s);
        HelpLoader loader = new HelpLoader();
        loader.loadURI("file:pve.md");

        for (Entry<String, Object> entry : loader.getSubstitutions().entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        System.out.println("--------");
        System.out.println(loader.getIndexSectionMarkup());
        System.out.println("--------");
        System.out.println(loader.getTopicSectionMarkup());
    }

    // ------------------------------------------------------------------------
    /**
     * Load help from the specified URI.
     * 
     * @param path the URI.
     * @throws InvalidConfigurationException
     * @throws IOException
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public void loadURI(String path)
    throws InvalidConfigurationException, MalformedURLException,
    IOException, URISyntaxException {
        URI uri = new URI(path);
        URLConnection connection = uri.toURL().openConnection();

        String text = IOUtils.toString(connection.getInputStream());
        String[] sections = text.replace("\r", "").split("\n===+\n");
        if (sections.length != 3) {
            throw new IOException("The input must be divided into 3 sections by === on a line by itself.");
        }

        YamlConfiguration settings = new YamlConfiguration();
        settings.loadFromString(sections[0]);
        _substitutions = settings.getValues(true);
        _indexSectionMarkup = substitute(sections[1], _substitutions);
        _topicSectionMarkup = substitute(sections[2], _substitutions);

        Parser parser = createMarkdownParser();
        _indexSectionNode = parser.parse(getIndexSectionMarkup());
        _topicSectionNode = parser.parse(getTopicSectionMarkup());
    }

    // ------------------------------------------------------------------------
    /**
     * Return the substitutions from variable names to values.
     * 
     * @return the substitutions from variable names to values.
     */
    public Map<String, Object> getSubstitutions() {
        return _substitutions;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the variable-substituted Markdown markup of the index section.
     * 
     * @return the variable-substituted Markdown markup of the index section.
     */
    public String getIndexSectionMarkup() {
        return _indexSectionMarkup;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the variable-substituted Markdown markup of the topic section.
     * 
     * @return the variable-substituted Markdown markup of the topic section.
     */
    public String getTopicSectionMarkup() {
        return _topicSectionMarkup;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the Node corresponding to the parsed Markdown of the index
     * section.
     * 
     * @return the Node corresponding to the parsed Markdown of the index
     *         section.
     */
    public Node getIndexSectionNode() {
        return _indexSectionNode;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the Node corresponding to the parsed Markdown of the topic
     * section.
     * 
     * @return the Node corresponding to the parsed Markdown of the topic
     *         section.
     */
    public Node getTopicSectionNode() {
        return _topicSectionNode;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the heading level of the first heading in the topics section.
     *
     * It is assumed that the topics all have the same heading level, and
     * therefore subheadings of a topic will have a higher level.
     *
     * @return the heading level of the first heading in the topics section.
     */
    public int getTopicHeadingLevel() {
        Heading firstHeading = Nodes.firstChildByType(getTopicSectionNode(), Heading.class);
        return firstHeading != null ? firstHeading.getLevel() : 1;
    }

    // ------------------------------------------------------------------------
    /**
     * Return a Markdown parser initialised with the extensions.
     * 
     * @return the parser.
     */
    public Parser createMarkdownParser() {
        return Parser.builder().extensions(_extensions).build();
    }

    // ------------------------------------------------------------------------
    /**
     * Create an HtmlRenderer initialised with the extensions.
     * 
     * @return the HtmlRenderer.
     */
    public HtmlRenderer createHtmlRenderer() {
        return HtmlRenderer.builder().extensions(_extensions).build();
    }

    // ------------------------------------------------------------------------
    /**
     * Return the result of replacing all variables of the form ${name} with the
     * corresponding value in a format string.
     * 
     * @param format the format string.
     * @param substitutions a map from variable names to values.
     * @return the result of string substitution.
     */
    protected String substitute(String format, Map<String, Object> substitutions) {
        final Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher m = pattern.matcher(format);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(result, substitutions.get(m.group(1)).toString());
        }
        m.appendTail(result);
        return result.toString();
    }

    // ------------------------------------------------------------------------
    /**
     * Common mark Extensions used to initialise the parser and HTML renderer.
     */
    protected List<Extension> _extensions = Arrays.asList(AutolinkExtension.create(),
                                                          StrikethroughExtension.create(),
                                                          TablesExtension.create(),
                                                          HeadingAnchorExtension.create(),
                                                          InsExtension.create());

    /**
     * The substitutions from variable names to values.
     */
    protected Map<String, Object> _substitutions;

    /**
     * The variable-substituted Markdown markup of the index section.
     */
    protected String _indexSectionMarkup;

    /**
     * The variable-substituted Markdown markup of the topic section.
     */
    protected String _topicSectionMarkup;

    /**
     * The Node corresponding to the parsed Markdown of the index section.
     */
    protected Node _indexSectionNode;

    /**
     * The Node corresponding to the parsed Markdown of the topic section.
     */
    protected Node _topicSectionNode;

} // class HelpLoader