package nu.nerd.help;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.ins.Ins;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;

// ----------------------------------------------------------------------------
/**
 * Renders a CommonMark DOM to Minecraft Help text.
 */
public class HelpRenderer {
    // ------------------------------------------------------------------------
    // TODO: make these styles configuration settings.
    static final TextStyle QUOTE_STYLE = new TextStyle("&a&o");
    static final TextStyle CODE_STYLE = new TextStyle("&e&o");
    static final TextStyle LINK_STYLE = new TextStyle("&b");
    static final TextStyle BOLD_STYLE = new TextStyle("&e&l");
    static final TextStyle STRIKETHROUGH_STYLE = new TextStyle("&m");
    static final TextStyle UNDERLINE_STYLE = new TextStyle("&n");
    static final TextStyle ITALICS_STYLE = new TextStyle("&o");

    // ------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        MessageSink sink = s -> System.out.println(s);
        HelpLoader loader = new HelpLoader();
        loader.loadURI("file:pve.md");

        YamlConfiguration config = new YamlConfiguration();
        config.load(new File("config.yml"));

        YamlConfiguration output = new YamlConfiguration();
        HelpRenderer help = new HelpRenderer();
        help.renderBoilerPlate(output, config);
        help.renderIndexTopics(output, loader, sink);
        help.renderGeneralTopics(output, loader, sink);

        output.save(new File("help.yml"));
        System.out.println(output.saveToString());
    }

    // ------------------------------------------------------------------------
    /**
     * Reset the state of this HelpRenderer to render more in-game help.
     */
    public void reset() {
        _currentOrderedListIndex.clear();
    }

    // ------------------------------------------------------------------------
    /**
     * Write the boiler-plate sections copied from the specified configuration
     * to the output YAML configuration.
     * 
     * @param output the output help configuration.
     * @param config the YAML configuration that contains the boiler-plate
     *        sections.
     */
    public void renderBoilerPlate(FileConfiguration output, FileConfiguration config) {
        ConfigurationSection copied = config.getConfigurationSection("boiler-plate");
        for (Entry<String, Object> entry : copied.getValues(true).entrySet()) {
            if (entry.getValue() instanceof MemorySection) {
                output.createSection(entry.getKey());
            } else {
                output.set(entry.getKey(), entry.getValue());
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Write the index-topics section to the output YAML configuration.
     * 
     * @param output the output help configuration.
     * @param loader the Markdown help file loader containing loaded help
     *        markup.
     * @param sink the MessageSink used to log messages.
     */
    public void renderIndexTopics(FileConfiguration output, HelpLoader loader, MessageSink sink) {
        ConfigurationSection indexTopicsSection = output.createSection("index-topics");
        Splitter splitter = new Splitter();

        for (ArrayList<Node> topic : Nodes.getTopicNodes(loader.getIndexSectionNode())) {
            Node headingNode = Nodes.take(topic);
            Text text = Nodes.firstChildByType(headingNode, Text.class);
            String topicName = text.getLiteral();
            ConfigurationSection section = indexTopicsSection.createSection(topicName);

            Node node = Nodes.take(topic);
            if (node instanceof Paragraph) {
                Code code = Nodes.firstChildByType(node, Code.class);
                if (code != null) {
                    section.set("permission", code.getLiteral());
                }
                node = Nodes.take(topic);
            }

            if (node instanceof BlockQuote) {
                section.set("preamble", splitter.split(StringUtils.stripStart(renderFlow(node), null) + '\n'));
                node = Nodes.take(topic);
            }

            if (node instanceof BulletList) {
                ArrayList<String> items = new ArrayList<String>();
                int droppedItems = 0;
                for (ListItem listItem : Nodes.childrenByType(node, ListItem.class)) {
                    String formattedItemText = renderFlow(Nodes.firstChildByType(listItem, Paragraph.class));
                    items.add(ChatColor.stripColor(formattedItemText).trim());
                }
                section.set("commands", items);
                if (droppedItems != 0) {
                    sink.message("Index topic " + topicName + " dropped " + droppedItems +
                                 " items that could not be parsed.");
                }
            } else {
                StringBuilder s = new StringBuilder();
                sink.message("In index topic " + topicName +
                             " we were expecting a bullet list but instead got " +
                             (node != null ? node.getClass().getName() : "nothing") + ".");

            }
        }
    } // renderIndexTopics

    // ------------------------------------------------------------------------
    /**
     * Write the general-topics section to the output YAML configuration.
     * 
     * @param output the output help configuration.
     * @param loader the Markdown help file loader containing loaded help
     *        markup.
     * @param sink the MessageSink used to log messages.
     */
    public void renderGeneralTopics(FileConfiguration output, HelpLoader loader, MessageSink sink) {
        ConfigurationSection indexTopicsSection = output.createSection("general-topics");
        Splitter splitter = new Splitter();

        for (ArrayList<Node> topic : Nodes.getTopicNodes(loader.getTopicSectionNode())) {
            Node headingNode = Nodes.take(topic);
            Text text = Nodes.firstChildByType(headingNode, Text.class);
            String topicName = text.getLiteral();
            ConfigurationSection section = indexTopicsSection.createSection(topicName);

            Node node = Nodes.take(topic);
            if (node instanceof Paragraph) {
                Code code = Nodes.firstChildByType(node, Code.class);
                if (code != null) {
                    section.set("permission", code.getLiteral());
                }
                node = Nodes.take(topic);
            }

            if (node instanceof BlockQuote) {
                // Remove leading and trailing newlines to prevent
                // IndexHelpTopic replacing them with ". ".
                section.set("shortText", splitter.split(renderFlow(node)).trim());
                node = Nodes.take(topic);
            }

            StringBuilder fullText = new StringBuilder();
            while (node != null) {
                fullText.append(renderFlow(node));
                node = Nodes.take(topic);
            }
            if (fullText.length() != 0) {
                section.set("fullText", splitter.split(fullText.toString()));
            }
        }
    } // renderGeneralTopics

    // ------------------------------------------------------------------------
    /**
     * Render some arbitrary Node as help text.
     * 
     * This is the top level interface for rendering nodes.
     * 
     * @param node the Node.
     */
    protected String renderFlow(Node node) {
        TextStyleStack stack = new TextStyleStack();
        StringBuilder s = new StringBuilder();
        renderNode(s, stack, node);
        return s.toString();
    }

    // ------------------------------------------------------------------------
    /**
     * Render all children of the specified Node to the StringBuilder.
     * 
     * @param s the StringBuilder that holds rendered help text.
     * @param stack holds the history of text styles.
     * @param node the parent Node.
     */
    protected void renderChildren(StringBuilder s, TextStyleStack stack, Node node) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            renderNode(s, stack, child);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Render the specified Node to the StringBuilder.
     * 
     * @param s the StringBuilder that holds rendered help text.
     * @param stack holds the history of text styles.
     * @param node the Node to render.
     */
    protected void renderNode(StringBuilder s, TextStyleStack stack, Node node) {
        if (node instanceof Text) {
            s.append(stack.asFormatCodes());
            s.append(((Text) node).getLiteral());

        } else if (node instanceof Paragraph) {
            // Suppress the double newline when rendering list items.
            if (!(node.getParent() instanceof ListItem)) {
                s.append('\n');
            }
            renderChildren(s, stack, node);
            s.append('\n');

        } else if (node instanceof HardLineBreak) {
            s.append('\n');

        } else if (node instanceof BlockQuote) {
            stack.combine(QUOTE_STYLE);
            renderChildren(s, stack, node);
            stack.pop();

        } else if (node instanceof Code) {
            stack.combine(CODE_STYLE);
            s.append(stack.asFormatCodes());
            s.append(((Code) node).getLiteral());
            stack.pop();

        } else if (node instanceof Link) {
            stack.combine(LINK_STYLE);
            s.append(stack.asFormatCodes());
            s.append(((Link) node).getDestination());
            stack.pop();

        } else if (node instanceof Emphasis) {
            stack.combine(ITALICS_STYLE);
            renderChildren(s, stack, node);
            stack.pop();

        } else if (node instanceof StrongEmphasis) {
            stack.combine(BOLD_STYLE);
            renderChildren(s, stack, node);
            stack.pop();

        } else if (node instanceof Ins) {
            stack.combine(UNDERLINE_STYLE);
            renderChildren(s, stack, node);
            stack.pop();

        } else if (node instanceof Strikethrough) {
            stack.combine(STRIKETHROUGH_STYLE);
            renderChildren(s, stack, node);
            stack.pop();

        } else if (node instanceof SoftLineBreak) {
            s.append(' ');

        } else if (node instanceof ListItem) {
            if (node.getParent() instanceof BulletList) {
                // NOTE: Alternatively, use ∙ for a square bullet.
                s.append("• ");
            } else if (node.getParent() instanceof OrderedList) {
                OrderedList list = (OrderedList) node.getParent();
                int number = _currentOrderedListIndex.getOrDefault(list, list.getStartNumber());
                s.append(number);
                s.append(". ");
                _currentOrderedListIndex.put(list, ++number);
            }
            renderChildren(s, stack, node);
        } else {
            renderChildren(s, stack, node);
        }
    } // renderNode

    // ------------------------------------------------------------------------
    /**
     * A map from OrderedList node to the number assigned to the next ListItem
     * that is its child.
     */
    protected HashMap<OrderedList, Integer> _currentOrderedListIndex = new HashMap<>();
} // class HelpRenderer