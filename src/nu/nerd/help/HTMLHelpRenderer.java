package nu.nerd.help;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Heading;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.renderer.html.HtmlRenderer;

import com.google.common.collect.Sets;

// ----------------------------------------------------------------------------
/**
 * Renders help as HTML.
 */
public class HTMLHelpRenderer {
    // ------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        HelpLoader loader = new HelpLoader();
        loader.loadURI("file:pve.md");

        renderHTML(loader, new File("index.html"), "Index",
                   Sets.newHashSet("", "bukkit.command.help"),
                   "<html><body>", "</body></html>");
    }

    // ------------------------------------------------------------------------
    /**
     * Render the help topics as HTML in the specified file.
     *
     * The default index topic is amended to list only those topics that:
     * <ul>
     * <li>exist as Markdown markup. (HelpHelp makes no attempt to render HTML
     * directly from a plugin's Minecraft-formatted help text.)</li>
     * <li>and that have permissions that would make them visible to default
     * players (as opposed to staff).</li>
     * </ul>
     *
     * The entries in the bullet list of the default index are rendered as
     * hyperlinks to the corresponding topic on the same page. Only those topics
     * that are listed in the <i>thus amended</i> default index topic get
     * generated into the HTML; therefore, generated topics will have
     * permissions that make them visible.
     *
     * @param loader the Markdown help loader.
     * @param outputFile the HTML output file.
     * @param defaultTopicTitle the "Default" index topic will be changed to use
     *        this title in the generated HTML.
     * @param header HTML text to be prepended before any HTML generated from
     *        topics.
     * @param footer HTML text to be appended after any HTML generated from
     *        topics.
     */
    public static void renderHTML(HelpLoader loader,
                                  File outputFile,
                                  String defaultTopicTitle,
                                  Set<String> visiblePermissions,
                                  String header, String footer)
    throws IOException {
        HtmlRenderer renderer = loader.createHtmlRenderer();
        StringBuilder html = new StringBuilder(header);

        ArrayList<ArrayList<Node>> indexTopics = Nodes.getTopicNodes(loader.getIndexSectionNode(), 0);
        ArrayList<ArrayList<Node>> generalTopics = Nodes.getTopicNodes(loader.getTopicSectionNode(), loader.getTopicHeadingLevel());

        // Determine the set of all visible topics for which we have
        // Markdown nodes. Topic == ArrayList<Node>.
        HashMap<String, ArrayList<Node>> visibleTopics = new HashMap<>();
        addVisibleTopics(indexTopics, visibleTopics, visiblePermissions);
        ArrayList<Node> defaultIndexTopic = visibleTopics.get("Default");
        if (defaultIndexTopic == null) {
            throw new HTMLGenerationException("There is no index topic named Default!");
        }
        addVisibleTopics(generalTopics, visibleTopics, visiblePermissions);

        // Amend the title of the default index topic.
        Heading defaultIndexHeading = new Heading();
        defaultIndexHeading.setLevel(((Heading) defaultIndexTopic.get(0)).getLevel());
        defaultIndexHeading.appendChild(new Text(defaultTopicTitle));
        defaultIndexTopic.set(0, defaultIndexHeading);

        // Amend the bullet list to link to visible topics only.
        // Track which topics get linked to along the way.
        ArrayList<String> referencedTopics = new ArrayList<>();
        Node last = defaultIndexTopic.remove(defaultIndexTopic.size() - 1);
        if (last instanceof BulletList) {
            BulletList amendedIndexList = new BulletList();
            for (ListItem item : Nodes.childrenByType(last, ListItem.class)) {
                Node text = Nodes.childByPath(item, Paragraph.class, Text.class);
                if (text instanceof Text) {
                    String title = ((Text) text).getLiteral();
                    if (visibleTopics.containsKey(title)) {
                        ListItem amendedItem = new ListItem();
                        Link link = new Link("#" + anchorId(title), title);
                        amendedIndexList.appendChild(amendedItem);
                        amendedItem.appendChild(link);
                        link.appendChild(text);
                        referencedTopics.add(title);
                    }
                }
            }
            defaultIndexTopic.add(amendedIndexList);
        } else {
            throw new HTMLGenerationException("The default index topic does not end with a bullet list!");
        }

        // Render the default index topic inside a <nav>
        html.append("<nav>\n");
        for (Node node : defaultIndexTopic) {
            renderer.render(node, html);
        }
        html.append("</nav>\n");

        // Render those visible topics that were referenced in the index.
        // Enclosing <div class="topics">, each topic in <div class="topic">.
        // Also insert a back link to the top.
        html.append("<div class=\"topics\">\n");
        for (String topicName : referencedTopics) {
            html.append("<div class=\"topic\">\n");
            ArrayList<Node> topic = visibleTopics.get(topicName);
            for (Node node : topic) {
                renderer.render(node, html);
            }
            html.append("<a href=\"#top\"><span>Top</span></a>\n");
            html.append("</div>\n");
        }
        html.append("</div>\n");

        html.append(footer);

        PrintWriter w = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
        w.println(html.toString());
        w.close();
    } // renderHTML

    // ------------------------------------------------------------------------
    /**
     * Return the anchor ID corresponding to the specified title.
     *
     * This method generates the same anchor ID as the
     * commonmark-ext-heading-anchor extension. Specifically, it converts to
     * lower case, replaces spaces with '-' and then ignores all characters
     * except '-', '_' and word characters.
     *
     * @param title the heading title.
     * @return the corresponding anchor ID.
     */
    public static String anchorId(String title) {
        try {
            StringBuilder sb = new StringBuilder();
            Pattern pattern = Pattern.compile("[-_\\w]+", Pattern.UNICODE_CHARACTER_CLASS);
            Matcher matcher = pattern.matcher(title.toLowerCase().replace(" ", "-"));
            while (matcher.find()) {
                sb.append(matcher.group());
            }
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Examine all topics for which Markdown nodes have been supplied and
     * extract those that have a permission that is in the set of visible topic
     * permissions.
     *
     * Along the way, remove the Nodes that represent the topic permission.
     *
     * Topics are represented as an ArrayList<> of Markdown Nodes, the first of
     * which will be a Heading containing the topic title. If a permission is
     * specified, it will be as a Paragraph containing a Code Node in the next
     * Node.
     *
     * @param allTopics a list of all topics extracted from the Markdown.
     * @param visibleTopics a map from unformatted topic name to corresponding
     *        Nodes, with the permission markup removed.
     * @param visiblePermissions the set of permissions that render a topic
     *        visible; most topics will not have a permission node set so the
     *        configuration is pre-configured to include the empty permission
     *        node string.
     */
    protected static void addVisibleTopics(ArrayList<ArrayList<Node>> allTopics,
                                           HashMap<String, ArrayList<Node>> visibleTopics,
                                           Set<String> visiblePermissions) {
        for (ArrayList<Node> originalTopic : allTopics) {
            // Take from a copy of the topic list, so the original is unchanged.
            ArrayList<Node> reducedTopic = new ArrayList<Node>(originalTopic);

            Node headingNode = Nodes.take(reducedTopic);
            Text title = Nodes.firstChildByType(headingNode, Text.class);
            String topicName = title.getLiteral();
            String topicPermission = "";

            Node node = Nodes.peek(reducedTopic);
            if (node instanceof Paragraph) {
                Code code = Nodes.firstChildByType(node, Code.class);
                if (code != null) {
                    topicPermission = code.getLiteral();
                    Nodes.take(reducedTopic);
                }
            }

            // Exclude the topic permission from the added topic nodes.
            reducedTopic.add(0, headingNode);
            if (visiblePermissions.contains(topicPermission)) {
                visibleTopics.put(topicName, reducedTopic);
            }
        }
    } // addVisibleTopics
} // class HTMLHelpRenderer