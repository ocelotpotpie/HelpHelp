package nu.nerd.help;

import java.util.ArrayList;

// ----------------------------------------------------------------------------
/**
 * Records {@link TextStyle} changes as a stack, with the most recent change -
 * the most nested formatting - on top.
 * 
 * The stack contains at least one style (the default - white regular text) at
 * all times.
 */
public class TextStyleStack {
    // ------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public TextStyleStack() {
        reset();
    }

    // ------------------------------------------------------------------------
    /**
     * Reset the stack to the default state, containing the default format
     * (white, regular text).
     */
    public void reset() {
        _styles.clear();
        _styles.add(TextStyle.DEFAULT);
    }

    // ------------------------------------------------------------------------
    /**
     * Push the specified style onto the stack, overriding any previous colour
     * if the style specifies one, and combining the new style's style bits with
     * the previously active style.
     * 
     * For example, if the previously active style included bold text and the
     * new style included italicised text, then the combined style would be bold
     * and italicised.
     * 
     * @param style the new style to add.
     */
    public void combine(TextStyle style) {
        String combinedColour = (style.getColourCode() != null) ? style.getColourCode()
                                                                : top().getColourCode();
        _styles.add(new TextStyle(combinedColour, top().getStyleBits() + style.getStyleBits()));
    }

    // ------------------------------------------------------------------------
    /**
     * Convert the {@link TextStyle} on top of the stack into a minimal sequence
     * of Minecraft format codes, taking into account the previous format of the
     * text.
     * 
     * @return the minimal sequence of Minecraft format codes corresponding to
     *         the top of the stack.
     */
    public String asFormatCodes() {
        return TextStyle.asFormatCodes(previous(), top());
    }

    // ------------------------------------------------------------------------
    /**
     * Remove the most recent style change, but always retain at least the
     * default style.
     */
    public void pop() {
        if (_styles.size() > 1) {
            _styles.set(_styles.size() - 1, null);
            _styles.remove(_styles.size() - 1);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return the {@link TextStyle} on top of the stack.
     * 
     * @return the {@link TextStyle} on top of the stack.
     */
    public TextStyle top() {
        return _styles.get(_styles.size() - 1);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the {@link TextStyle} that preceded the top of the stack.
     * 
     * @return the {@link TextStyle} that preceded the top of the stack.
     */
    public TextStyle previous() {
        if (_styles.size() >= 2) {
            return _styles.get(_styles.size() - 2);
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Stack of text styles with the default style on the bottom.
     */
    protected ArrayList<TextStyle> _styles = new ArrayList<TextStyle>();
} // class TextStyleStack