package nu.nerd.help;

import java.io.IOException;

// ----------------------------------------------------------------------------
/**
 * An exception raised to signify a problem generating HTML.
 */
public class HTMLGenerationException extends IOException {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param message the exception message to record.
     */
    public HTMLGenerationException(String message) {
        super(message);
    }

    // ------------------------------------------------------------------------
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 3348177233695264606L;
} // class HTMLGenerationException