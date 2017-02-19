package nu.nerd.help;

// ----------------------------------------------------------------------------
/**
 * Receives messages to be displayed or logged.
 */
public interface MessageSink {
    // ------------------------------------------------------------------------
    /**
     * Process a message.
     * 
     * @param text the message text.
     */
    public void message(String text);
} // class MessageSink