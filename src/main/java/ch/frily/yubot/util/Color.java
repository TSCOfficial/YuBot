package ch.frily.yubot.util;

import lombok.Getter;

@Getter
public class Color {

    private final Integer red;

    private final Integer green;

    private final Integer blue;

    public static final java.awt.Color RED = new java.awt.Color(231, 76, 60);
    public static final java.awt.Color GREEN = new java.awt.Color(46, 204, 113);

    public Color(Integer red, Integer green, Integer blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(String hexCode){
        red = Integer.valueOf(hexCode.substring(0, 2), 16);
        green = Integer.valueOf(hexCode.substring(2, 4), 16);
        blue = Integer.valueOf(hexCode.substring(4, 6), 16);
    }

    public java.awt.Color get(){
        return new java.awt.Color(red, green, blue);
    }
}
