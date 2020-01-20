package ui;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;

import java.awt.*;

public class MapMarkerMy extends MapMarkerCircle {
    private static final double defaultMarkerSize = 15.0;
    private Image image;
    private String tooltipText;
    private String profileImageUrl;

    public MapMarkerMy(
            Layer layer,
            Coordinate coord,
            Color color,
            Image image,
            String tooltipText,
            String profileImageUrl) {
        super(layer, null, coord, defaultMarkerSize, STYLE.FIXED, getDefaultStyle());
        this.image = image;
        this.tooltipText = tooltipText;
        setColor(Color.BLACK);
        setBackColor(color);
        this.profileImageUrl = profileImageUrl;
    }

    @Override
    public void paint(Graphics g, Point position, int radio) {
        double r = this.getRadius();
        super.paint(g, position, (int) r);
        int width = (int) r + 5;
        int height = (int) r + 5;
        int w2 = width / 2;
        int h2 = height / 2;
        g.drawImage(this.image, position.x - w2, position.y - h2, width, height, null);
        this.paintText(g, position);
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
