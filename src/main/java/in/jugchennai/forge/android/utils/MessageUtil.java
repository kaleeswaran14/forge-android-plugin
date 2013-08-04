package in.jugchennai.forge.android.utils;

import java.util.ResourceBundle;

/**
 * The Class Messages.
 */
public final class MessageUtil {

    /** The Constant INSTANCE. */
    public static final MessageUtil INSTANCE = new MessageUtil();

    /** The bundle. */
    private final ResourceBundle bundle;

    /**
     * Instantiates a new messages.
     */
    private MessageUtil() {
        bundle = ResourceBundle.getBundle("ResourceBundle");
    }

    /**
     * Gets the key value.
     * 
     * @param key the key
     * @return the key value
     */
    public String getKeyValue(final String key) {
        return bundle.getString(key);
    }
    
    /**
     * Gets the message.
     * 
     * @param key the key
     * @return the message
     */
    public String getMessage(final String key) {
        return bundle.getString("message."+key);
    }

    /**
     * Gets the message.
     * 
     * @param key the key
     * @param args the args
     * @return the message
     */
    public String getMessage(final String key, final Object... args) {
        return String.format(bundle.getString("message."+key), args);

    }
}