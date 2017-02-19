package nu.nerd.help;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.commonmark.renderer.html.HtmlRenderer;

// ----------------------------------------------------------------------------
/**
 * Renders help as HTML.
 */
public class HTMLHelpRenderer {
    // ------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        MessageSink sink = s -> System.out.println(s);
        HelpLoader loader = new HelpLoader();
        loader.loadURI("file:pve.md", sink);

        renderHTML(new File("index.html"), loader);
    }

    // ------------------------------------------------------------------------

    public static void renderHTML(File outputFile, HelpLoader loader) throws IOException {
        HtmlRenderer renderer = loader.createHtmlRenderer();
        StringBuilder buffer = new StringBuilder();
        renderer.render(loader.getIndexSectionNode(), buffer);
        renderer.render(loader.getTopicSectionNode(), buffer);
        PrintWriter w = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
        w.println(buffer.toString());
        w.close();
    }
} // class HTMLHelpRenderer