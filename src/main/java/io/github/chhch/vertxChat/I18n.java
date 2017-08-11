package io.github.chhch.vertxChat;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by ch on 11.08.17.
 */
public enum I18n {
    INSTANCE;

    private ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", Locale.US);

    public String getString(String key) {
        return messages.getString(key);
    }
}