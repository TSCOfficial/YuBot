package ch.frily.yubot.util;

import lombok.Getter;

@Getter
public class Color {

    private final Integer red;

    private final Integer green;

    private final Integer blue;

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
