package io.github.chhch.vertxChat;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by ch on 11.08.17.
 */
public enum I18n {
    INSTANCE;

    private ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", Locale.US);

    public static String getString(String key) {
        return INSTANCE.messages.getString(key);
    }
}