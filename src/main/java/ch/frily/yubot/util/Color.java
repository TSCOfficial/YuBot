package ch.frily.yubot.util;

import lombok.Getter;

@Getter
public class Color {

    private final Integer red;

    private final Integer green;

    private final Integer blue;

    // Color source: https://discordlabs.org/tools/color-palette
    public static final java.awt.Color RED = new java.awt.Color(231, 76, 60);
    public static final java.awt.Color ORANGE = new java.awt.Color(230, 126, 34);
    public static final java.awt.Color YELLOW = new java.awt.Color(241, 196, 15);
    public static final java.awt.Color GREEN = new java.awt.Color(46, 204, 113);
    public static final java.awt.Color TURQUOISE = new java.awt.Color(26, 188, 156);
    public static final java.awt.Color PURPLE = new java.awt.Color(155, 89, 182);
    public static final java.awt.Color PLURPLE = new java.awt.Color(88, 101, 242);
    public static final java.awt.Color BLUE = new java.awt.Color(52, 152, 219);
    /** Discord subtext gray */
    public static final java.awt.Color LIGHT_GRAY = new java.awt.Color(171, 172, 178);

    // Custom Icons
    // Parameter:
    //      Icon size: 80
    //      Shadow:
    //          Color: 0, 0, 0, 0.5
    //          Offset: 2, 2
    //          Blur: 8
    // Icons without background: use size 140
    public static final java.awt.Color ICON_BACKGROUND = new java.awt.Color(32, 34, 37);
    public static final java.awt.Color ICON_FOREGROUND = new java.awt.Color(185, 187, 190); // neutral foreground color d1d4d7

    public Color(Integer red, Integer green, Integer blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Convert hexcode to color
     * @param hexCode The hexcode to convert (without the #)
     */
    public Color(String hexCode){
        red = Integer.valueOf(hexCode.substring(0, 2), 16);
        green = Integer.valueOf(hexCode.substring(2, 4), 16);
        blue = Integer.valueOf(hexCode.substring(4, 6), 16);
    }

    public java.awt.Color get(){
        return new java.awt.Color(red, green, blue);
    }
}
