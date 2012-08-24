package se.kth.webtex;

import java.io.Serializable;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class PageBean implements Serializable {
    private static final ResourceBundle properties = ResourceBundle.getBundle("webtex");

    public static final String getVersion() {
        return properties.getString("webtex.version");
    }
}
