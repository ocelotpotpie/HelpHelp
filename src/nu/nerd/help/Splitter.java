package nu.nerd.help;

import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;

// ----------------------------------------------------------------------------
/**
 * Splits chat lines so that the client will not wrap text.
 * 
 * Bukkit's ChatPaginator class splits lines at the server, but does it badly.
 * In order to avoid ChatPaginator mangling help text, Splitter splits lines no
 * longer than ChatPaginator (in fact, slightly shorter), so that ChatPaginator
 * decides not to mess with the lines.
 * 
 * This class fixes the following problems with ChatPaginator:
 * <ul>
 * <li>ChatPaginator attempts to continue the last colour of a chat line before
 * a split on the newly created line, but doesn't understand that formatting
 * codes can change both colour and style. It only copies the very last
 * formatting code. So for example, if the line is formatted with &e&o (yellow,
 * italics), ChatPaginator would format the next line with &o only.</li>
 * <li>ChatPaginator believes that the client will never wrap a line that is 55
 * characters or less. That is not true. Wrapping at 54 characters is a safer
 * bet, but still may not be foolproof.</li>
 * <li>ChatPaginator sometimes jams the last word of a line up against the
 * preceding word, with no space in between them.</li>
 * </ul>
 */
public class Splitter {
    // ------------------------------------------------------------------------
    /**
     * Number of columns to split the line at, by default.
     */
    public static final int DEFAULT_COLUMNS = 54;

    // ------------------------------------------------------------------------

    public String split(String in) {
        return split(in, DEFAULT_COLUMNS);
    }

    // ------------------------------------------------------------------------

    public String split(String in, int columns) {
        reset();
        ArrayList<String> lines = new ArrayList<String>();

        // Sentinel (also protects against ending in format code).
        in += ' ';

        // Start by inserting the line breaks, without carrying forward formats.
        for (int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            if (c == '\n') {
                lines.add(_line.toString());
                _line.setLength(0);
                _formatCount = 0;
                _lastBreak = 0;
                continue;
            }

            if (c == ChatColor.COLOR_CHAR) {
                _formatCount += 2;
                _line.append(c);
                _line.append(in.charAt(++i));
                continue;
            }

            if (Character.isWhitespace(c)) {
                _line.append(c);

                // Record index in _line after most recent space.
                _lastBreak = _line.length();

                if (_line.length() - _formatCount > columns) {
                    // Drop the new space.
                    _line.setLength(columns + _formatCount);
                    String head = _line.toString();
                    lines.add(head);
                    String tail = new TextStyle(head).asFormatCodes();
                    _line.setLength(0);
                    _line.append(tail);
                    _formatCount = countCodes(tail);
                    _lastBreak = 0;
                }
                continue;
            }

            // Actual visible characters...
            _line.append(c);
            if (_line.length() - _formatCount > columns) {
                // If there were spaces in the line to break at, break there.
                int breakIndex = (_lastBreak > 0) ? _lastBreak : columns;
                String tail = _line.substring(breakIndex);
                String head = _line.substring(0, breakIndex);
                lines.add(head);
                _line.setLength(0);
                _line.append(new TextStyle(head).asFormatCodes());
                _line.append(tail);
                _lastBreak = 0;
                _formatCount = countCodes(_line.toString());
            }
        } // for

        return String.join("\n", lines);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the number of formatting characters in the specified String.
     * 
     * @param s the String.
     * @return the number of formatting characters, which will always be
     *         divisible by 2.
     */
    protected static int countCodes(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == ChatColor.COLOR_CHAR) {
                ++i;
                count += 2;
            }
        }
        return count;
    }

    // ------------------------------------------------------------------------
    /**
     * Prepare internal state for a split().
     */
    protected void reset() {
        _formatCount = 0;
        _lastBreak = 0;
        _line = new StringBuilder();
    }

    // ------------------------------------------------------------------------

    protected int _columns = DEFAULT_COLUMNS;
    protected int _formatCount = 0;

    /**
     * Start index of the part of line that would be broken off if it had to be
     * split.
     */
    protected int _lastBreak = 0;
    protected StringBuilder _line = new StringBuilder();
} // class Splitter
