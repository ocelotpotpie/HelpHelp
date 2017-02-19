package nu.nerd.help;

import net.md_5.bungee.api.ChatColor;

// ----------------------------------------------------------------------------
/**
 * Records the colour and style of text, and knows how to emit the corresponding
 * Minecraft formatting codes.
 * 
 * The colour code and all style codes will always be read as fully translated
 * into Minecraft format codes.
 */
public class TextStyle {
    /**
     * The style bit code for bold text.
     */
    public static final int BOLD = 1;

    /**
     * The style bit code for strikethrough text.
     */
    public static final int STRIKETHROUGH = 2;

    /**
     * The style bit code for underline text.
     */
    public static final int UNDERLINE = 4;

    /**
     * The style bit code for italicised text.
     */
    public static final int ITALIC = 8;

    /**
     * Bit mask where only those bits used to denote style are set.
     */
    public static final int STYLE_BITS = 15;

    /**
     * The default text style, regular white text.
     */
    public static final TextStyle DEFAULT = new TextStyle("&f");

    // ------------------------------------------------------------------------
    /**
     * A bit of interactive testing.
     */
    public static void main(String[] args) {
        System.out.println(asFormatCodes(null, new TextStyle("&e&o&9&n&l")));
        System.out.println(asFormatCodes(TextStyle.DEFAULT, new TextStyle("&e&o&9&n&l")));
        System.out.println(asFormatCodes(TextStyle.DEFAULT, new TextStyle("&e&o")));
        System.out.println(asFormatCodes(TextStyle.DEFAULT, new TextStyle("&f&l&m&n&o")));
        System.out.println(asFormatCodes(TextStyle.DEFAULT, new TextStyle("&f&m&n&o")));
        System.out.println(asFormatCodes(TextStyle.DEFAULT, new TextStyle("&f&l&o")));
        System.out.println(asFormatCodes(TextStyle.DEFAULT, new TextStyle("&f&o")));
        System.out.println(asFormatCodes(new TextStyle("&f&o"), new TextStyle("&f&n")));
    }

    // ------------------------------------------------------------------------
    /**
     * Construct a style from a Minecraft format string, possibly containing
     * either colour codes, or style codes, or both.
     * 
     * Other characters in the string are ignored.
     * 
     * @param formatCode the Minecraft format string.
     */
    public TextStyle(String formatCode) {
        if (formatCode == null) {
            _colourCode = null;
        } else {
            for (int i = 0; i < formatCode.length(); ++i) {
                if (formatCode.charAt(i) == '&' || formatCode.charAt(i) == '§') {
                    if (i + 1 >= formatCode.length()) {
                        break;
                    } else {
                        char code = Character.toLowerCase(formatCode.charAt(i + 1));
                        if (Character.isDigit(code) || (code >= 'a' && code <= 'f')) {
                            _colourCode = "§" + code;
                            _styleBits = 0;
                        } else if (code == 'k') {
                            // Obfuscated. Not currently implemented.
                        } else if (code == 'l') {
                            _styleBits |= BOLD;
                        } else if (code == 'm') {
                            _styleBits |= STRIKETHROUGH;
                        } else if (code == 'n') {
                            _styleBits |= UNDERLINE;
                        } else if (code == 'o') {
                            _styleBits |= ITALIC;
                        } else if (code == 'r') {
                            _styleBits = 0;
                        }
                    }
                }
            } // for
        }
    } // ctor

    // ------------------------------------------------------------------------
    /**
     * Construct a style from separate colour and style bits arguments.
     * 
     * @param colourCode the colour code, which can contain untranslated
     *        alternate colour codes.
     * @param styleBits
     */
    public TextStyle(String colourCode, int styleBits) {
        setColourCode(colourCode);
        _styleBits = styleBits;
    }

    // ------------------------------------------------------------------------
    /**
     * @see {Object{@link #toString()}
     */
    @Override
    public String toString() {
        return asFormatCodes();
    }

    // ------------------------------------------------------------------------
    /**
     * Return this style as a minimal set of Minecraft format codes.
     * 
     * If this style does not override the colour, then the returned codes will
     * not contain a colour sequence.
     * 
     * @return this style as a minimal set of Minecraft format codes.
     */
    public String asFormatCodes() {
        StringBuilder s = new StringBuilder();
        if (getColourCode() != null) {
            s.append(getColourCode());
        }
        s.append(asFormatCodes(getStyleBits()));
        return ChatColor.translateAlternateColorCodes('&', s.toString());
    }

    // ------------------------------------------------------------------------
    /**
     * Return this TextStyle as a Minecraft colour code sequence with an
     * explicit colour code that resets all formatting, even if this style does
     * not specify a colour.
     * 
     * When this TextStyle does not specify a colour, the default colour, white,
     * is emitted.
     * 
     * @return this TextStyle as a sequence of Minecraft colour codes with an
     *         explicit colour.
     */
    public String asResetCodes() {
        StringBuilder s = new StringBuilder();
        if (getColourCode() != null) {
            s.append(getColourCode());
        } else {
            s.append(TextStyle.DEFAULT.getColourCode());
        }
        s.append(asFormatCodes(getStyleBits()));
        return ChatColor.translateAlternateColorCodes('&', s.toString());
    }

    // ------------------------------------------------------------------------
    /**
     * Return the specified style bits expressed as Minecraft format codes.
     * 
     * @param styleBits a bitset of integer style constants.
     * @return the corresponding Minecraft format codes.
     */
    public static String asFormatCodes(int styleBits) {
        StringBuilder s = new StringBuilder();
        if ((styleBits & BOLD) != 0) {
            s.append("&l");
        }
        if ((styleBits & STRIKETHROUGH) != 0) {
            s.append("&m");
        }
        if ((styleBits & UNDERLINE) != 0) {
            s.append("&n");
        }
        if ((styleBits & ITALIC) != 0) {
            s.append("&o");
        }
        return ChatColor.translateAlternateColorCodes('&', s.toString());
    }

    // ------------------------------------------------------------------------
    /**
     * Return the minimal set of Minecraft format codes required to go from the
     * "oldStyle" {@link TextStyle} to the "newStyle" {@link TextStyle}.
     * 
     * If newStyle is a different colour to oldStyle, or if oldStyle has style
     * bits set that are absent in newStyle, then there is no option but to emit
     * the format codes corresponding to newStyle in full, without consideration
     * of oldStyle. Conversely, if oldStyle and newStyle are the same colour (or
     * newStyle does not specify a colour) and if newStyle only adds style bits
     * to oldStyle and does not clear any, then the newStyle can be expressed as
     * a minimal delta of formatting codes added to oldStyle.
     * 
     * @param oldStyle the old style of the text; if null, newStyle is emitted
     *        in full with an explicit colour code.
     * @param newStyle the new style of the text.
     * @return a minimal sequence of Minecraft format codes to go from the old
     *         style to the new.
     */
    public static String asFormatCodes(TextStyle oldStyle, TextStyle newStyle) {
        if (oldStyle == null) {
            return newStyle.asResetCodes();
        } else {
            String oldColour = (oldStyle.getColourCode() != null) ? oldStyle.getColourCode()
                                                                  : TextStyle.DEFAULT.getColourCode();

            // If the colour has not changed...
            if (newStyle.getColourCode() == null || oldColour.equals(newStyle.getColourCode())) {
                int changedBits = (oldStyle.getStyleBits() ^ newStyle.getStyleBits());
                int clearedBits = (changedBits & oldStyle.getStyleBits());
                if (clearedBits == 0) {
                    return TextStyle.asFormatCodes(changedBits);
                }
            }

            // Colour has changed, or bits have been cleared.
            return newStyle.asResetCodes();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Set the colour of the text, given a Minecraft colour code.
     * 
     * @param colourCode the two-character Minecraft colour code.
     */
    public void setColourCode(String colourCode) {
        _colourCode = (colourCode != null) ? ChatColor.translateAlternateColorCodes('&', colourCode.toLowerCase())
                                           : null;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the two-character Minecraft colour code, or null if not set.
     * 
     * @return the two-character Minecraft colour code, or null if not set.
     */
    public String getColourCode() {
        return _colourCode;
    }

    // ------------------------------------------------------------------------
    /**
     * Add the specified style bits to the set of styles.
     * 
     * @param styleBits a bit set of style attributes.
     */
    public void addStyleBits(int styleBits) {
        _styleBits |= styleBits;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the bit set of style attributes.
     * 
     * @return the bit set of style attributes.
     */
    public int getStyleBits() {
        return _styleBits;
    }

    // ------------------------------------------------------------------------
    /**
     * The two-character colour code of the text.
     */
    private String _colourCode;

    /**
     * A bit set of the style attributes of the style.
     */
    private int _styleBits;
} // class TextStyle