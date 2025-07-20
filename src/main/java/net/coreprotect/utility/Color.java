package net.coreprotect.utility;

public final class Color {

    // we define our own constants here to eliminate string concatenation
    // javadoc taken from org.bukkit.ChatColor

    /**
     * Represents black.
     */
    public static final String BLACK = "<black>";

    /**
     * Represents dark blue.
     */
    public static final String DARK_BLUE = "<dark_blue>";

    /**
     * Represents dark green.
     */
    public static final String DARK_GREEN = "<dark_green>";

    /**
     * Represents dark blue (aqua).
     */
    public static final String DARK_AQUA = "<#31b0e8>";

    /**
     * Represents dark red.
     */
    public static final String DARK_RED = "<dark_red>";

    /**
     * Represents dark purple.
     */
    public static final String DARK_PURPLE = "<dark_purple>";

    /**
     * Represents gold.
     */
    public static final String GOLD = "<gold>";

    /**
     * Represents grey.
     */
    public static final String GREY = "<gray>";

    /**
     * Represents dark grey.
     */
    public static final String DARK_GREY = "<dark_gray>";

    /**
     * Represents blue.
     */
    public static final String BLUE = "<blue>";

    /**
     * Represents green.
     */
    public static final String GREEN = "<green>";

    /**
     * Represents aqua.
     */
    public static final String AQUA = "<aqua>";

    /**
     * Represents red.
     */
    public static final String RED = "<red>";

    /**
     * Represents light purple.
     */
    public static final String LIGHT_PURPLE = "<light_purple>";

    /**
     * Represents yellow.
     */
    public static final String YELLOW = "<yellow>";

    /**
     * Represents white.
     */
    public static final String WHITE = "<white>";

    /**
     * Represents magical characters that change around randomly.
     */
    public static final String MAGIC = "<obfuscated>";

    /**
     * Makes the text bold.
     */
    public static final String BOLD = "<bold>";

    /**
     * Makes a line appear through the text.
     */
    public static final String STRIKETHROUGH = "<strikethrough>";

    /**
     * Makes the text appear underlined.
     */
    public static final String UNDERLINE = "<underlined>";

    /**
     * Makes the text italic.
     */
    public static final String ITALIC = "<italic>";

    /**
     * Resets all previous chat colors or formats.
     */
    public static final String RESET = "<reset>";

    private Color() {
        throw new IllegalStateException("Utility class");
    }

}
