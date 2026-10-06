import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class GenerateRedlineFontAtlas {

    private static final int CELL_SIZE = 8;
    private static final int CELLS_PER_ROW = 16;
    private static final int SCALE = 16;

    private GenerateRedlineFontAtlas() {
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 2) {
            throw new IllegalArgumentException("Usage: GenerateRedlineFontAtlas <font.ttf> <output.png>");
        }

        Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(arguments[0]));
        Font font = baseFont.deriveFont(Font.PLAIN, (float) CELL_SIZE);
        int imageSize = CELL_SIZE * CELLS_PER_ROW * SCALE;
        BufferedImage highResolution = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = highResolution.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.scale(SCALE, SCALE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        FontMetrics metrics = graphics.getFontMetrics();
        for (int character = 32; character < 256; ++character) {
            if (character == 127) {
                continue;
            }
            int x = character % CELLS_PER_ROW * CELL_SIZE;
            int y = character / CELLS_PER_ROW * CELL_SIZE;
            int width = metrics.charWidth((char) character);
            int glyphX = x + Math.max(0, (CELL_SIZE - width) / 2);
            graphics.drawString(String.valueOf((char) character), glyphX, y + CELL_SIZE - 1);
        }
        graphics.dispose();

        BufferedImage atlas = new BufferedImage(CELL_SIZE * CELLS_PER_ROW, CELL_SIZE * CELLS_PER_ROW,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D downsample = atlas.createGraphics();
        downsample.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        downsample.drawImage(highResolution, 0, 0, atlas.getWidth(), atlas.getHeight(), null);
        downsample.dispose();

        Path output = Path.of(arguments[1]);
        Files.createDirectories(output.getParent());
        ImageIO.write(atlas, "png", output.toFile());
    }
}
