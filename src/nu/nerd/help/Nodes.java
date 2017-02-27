package nu.nerd.help;

import java.util.ArrayList;
import java.util.List;

import org.commonmark.node.Code;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

// --------------------------------------------------------------------------
/**
 * Markdown Node utility functions.
 */
public class Nodes {
    // ------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        MessageSink sink = s -> System.out.println(s);
        HelpLoader loader = new HelpLoader();
        loader.loadURI("file:pve.md");
        Nodes.dumpNode(loader.getIndexSectionNode());
        System.out.println("--------");
        Nodes.dumpNode(loader.getTopicSectionNode());

        ArrayList<ArrayList<Node>> indexTopics = Nodes.getTopicNodes(loader.getIndexSectionNode(), 0);
        for (ArrayList<Node> topic : indexTopics) {
            Heading heading = (Heading) topic.get(0);
            System.out.println(firstChildByType(heading, Text.class).getLiteral());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return an ArrayList<> of all topics under the root node of a section.
     * 
     * Each topic is represented as an ArrayList<Node>, beginning with the
     * Heading at the start of the topic.
     * 
     * @param root the root Node of the section.
     * @param level the expected heading level of the headings beginning the
     *        topics, or 0 for any heading level.
     * @return an array of arrays of Nodes.
     */
    public static ArrayList<ArrayList<Node>> getTopicNodes(Node root, int level) {
        ArrayList<ArrayList<Node>> allTopics = new ArrayList<ArrayList<Node>>();
        ArrayList<Node> topic = new ArrayList<Node>();
        Node node = root.getFirstChild();
        while (node != null) {
            if (node instanceof Heading) {
                Heading heading = (Heading) node;
                if (level == 0 || heading.getLevel() == level) {
                    if (!topic.isEmpty()) {
                        allTopics.add(topic);
                    }
                    topic = new ArrayList<Node>();
                }
            }
            topic.add(node);
            node = node.getNext();
        }
        if (!topic.isEmpty()) {
            allTopics.add(topic);
        }
        return allTopics;
    }

    // ------------------------------------------------------------------------
    /**
     * Remove the first element from the list of nodes and return it, or return
     * null if there is no element to remove.
     * 
     * @param nodes the list of nodes.
     * @return the first element from the list of nodes, or null if there is no
     *         element to remove.
     */
    public static Node take(List<Node> nodes) {
        return nodes.isEmpty() ? null : nodes.remove(0);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the first element from the list of nodes but do not remove it.
     * 
     * @param nodes the list of nodes.
     * @return the first element from the list of nodes, or null if there is no
     *         element.
     */
    public static Node peek(List<Node> nodes) {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    // ------------------------------------------------------------------------
    /**
     * Return a list of all children of the specified non-null node with the
     * specified class.
     * 
     * @param node the starting node.
     * @param type the type of the children to return.
     * @return a list of all children of the specified non-null node with the
     *         specified class.
     */
    public static <T extends Node> ArrayList<T> childrenByType(Node node, Class<T> type) {
        ArrayList<T> matchingChildren = new ArrayList<T>();
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (type.isInstance(child)) {
                matchingChildren.add((T) child);
            }
        }
        return matchingChildren;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the first child of a node with the specified type.
     *
     * @param node the parent node.
     * @param type the Node subclass to look for.
     * @return the first matching child, or null if not found.
     */
    public static <T extends Node> T firstChildByType(Node node, Class<T> type) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (type.isInstance(child)) {
                return (T) child;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    /**
     * Traverse the descendants of a Node that match a list of types and return
     * the matching descendant, or null if not found.
     * 
     * @param node the node to start descending the hierarchy from.
     * @param types an array of Node subclasses that specifies the path to the
     *        sought descendant. The first entry is the type of the immediate
     *        child of node. The next entry is the type of the descendant of
     *        that child (i.e the type of the grand-child of node). The last
     *        entry in the types array is the type of the returned Node
     *        instance, if found.
     * @return a descendant of Node that can be reached by traversing only those
     *         descendants with the types specified in the types argument, in
     *         the order they are listed there.
     */
    @SafeVarargs
    public static Node childByPath(Node node, Class<? extends Node>... types) {
        for (int i = 0; i < types.length; ++i) {
            if (node == null) {
                return null;
            }
            node = firstChildByType(node, types[i]);
        }
        return node;
    }

    // ------------------------------------------------------------------------
    /**
     * Dump a Markdown node to standard output.
     * 
     * @param node the node.
     */
    public static void dumpNode(Node node) {
        dumpNode(node, 0);
    }

    // ------------------------------------------------------------------------
    /**
     * Dump the specified node at the specified depth of nesting.
     * 
     * @param node the node.
     * @param depth the nesting depth or number of parents of node.
     */
    protected static void dumpNode(Node node, int depth) {
        while (node != null) {
            StringBuilder s = new StringBuilder();
            s.append(indent(depth));
            if (node instanceof Code) {
                s.append("Code{literal=").append(((Code) node).getLiteral()).append("}");
            } else {
                s.append(node.toString());
            }
            System.out.println(s.toString());
            dumpNode(node.getFirstChild(), depth + 1);
            node = node.getNext();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return a string used to indent output for the specified nesting depth.
     * 
     * @param depth the nesting depth.
     * @return a string used to indent output for the specified nesting depth.
     */
    protected static String indent(int depth) {
        int indentLength = depth * 2;
        int mod = indentLength % INDENT.length();
        int div = indentLength / INDENT.length();
        if (div == 0) {
            return INDENT.substring(INDENT.length() - mod);
        } else {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < div; ++i) {
                s.append(INDENT);
            }
            s.append(INDENT.substring(mod));
            return s.toString();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * A string used to compose indentation strings returned by indent().
     */
    private static final String INDENT = "                                        ";
} // class Nodes