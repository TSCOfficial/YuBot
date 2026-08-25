package ch.frily.yubot.util;

import java.awt.*;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class ProfileImageComposer {

    private static final int AVATAR_SIZE = 150;      // Ziel-Durchmesser des Avatars
    private static final int AVATAR_MARGIN = 40;      // Abstand zum linken Rand
    private static final int AVATAR_BORDER = 6;        // weißer Rand um den Avatar

    public static BufferedImage compose(BufferedImage banner, BufferedImage avatar) {
        BufferedImage canvas = new BufferedImage(
                banner.getWidth(), banner.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(banner, 0, 0, null);

        BufferedImage roundedAvatar = makeCircular(avatar, AVATAR_SIZE);

        // Position
        int x = AVATAR_MARGIN;
        int y = (canvas.getHeight() - AVATAR_SIZE) / 2;

        // White border behind the profile
        g.setColor(Color.WHITE);
        g.fillOval(x - AVATAR_BORDER, y - AVATAR_BORDER,
                AVATAR_SIZE + AVATAR_BORDER * 2, AVATAR_SIZE + AVATAR_BORDER * 2);

        g.drawImage(roundedAvatar, x, y, null);

        g.dispose();
        return canvas;
    }

    private static BufferedImage makeCircular(BufferedImage source, int targetSize) {
        // Make sure the image is square
        int side = Math.min(source.getWidth(), source.getHeight());
        BufferedImage square = source.getSubimage(
                (source.getWidth() - side) / 2, (source.getHeight() - side) / 2, side, side);

        BufferedImage circular = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = circular.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Cut out circle
        g.setClip(new Ellipse2D.Float(0, 0, targetSize, targetSize));
        g.drawImage(square, 0, 0, targetSize, targetSize, null);
        g.dispose();

        return circular;
    }
}
