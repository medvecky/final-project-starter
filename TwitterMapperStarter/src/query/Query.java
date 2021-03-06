package query;

import filters.Filter;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Layer;
import twitter4j.Status;
import ui.MapMarkerMy;
import util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

/**
 * A query over the twitter stream.
 */
public class Query implements Observer {
    // The map on which to display markers when the query matches
    private final JMapViewer map;
    // Each query has its own "layer" so they can be turned on and off all at once
    private Layer layer;
    // The color of the outside area of the marker
    private final Color color;
    // The string representing the filter for this query
    private final String queryString;
    // The filter parsed from the queryString
    private final Filter filter;
    // The checkBox in the UI corresponding to this query (so we can turn it on and off and delete it)
    private JCheckBox checkBox;

    public Color getColor() {
        return color;
    }

    public String getQueryString() {
        return queryString;
    }

    public Filter getFilter() {
        return filter;
    }

    public Layer getLayer() {
        return layer;
    }

    public JCheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public void setVisible(boolean visible) {
        layer.setVisible(visible);
    }

    public boolean getVisible() {
        return layer.isVisible();
    }

    public Query(String queryString, Color color, JMapViewer map) {
        this.queryString = queryString;
        this.filter = Filter.parse(queryString);
        this.color = color;
        this.layer = new Layer(queryString);
        this.map = map;
    }

    @Override
    public String toString() {
        return "Query: " + queryString;
    }

    @Override
    public void update(Observable observable, Object o) {
        Status twitStatus = (Status) o;
        Image image = null;
        try {
            image = ImageIO.read(new URL(twitStatus.getUser().getMiniProfileImageURL()));
        } catch (IOException e) {
            System.out.println("Profile image not loaded. ");
            System.out.println(e.getMessage());
        }
        if (filter.matches(twitStatus)) {
            map.addMapMarker(new MapMarkerMy(
                    layer,
                    Util.statusCoordinate(twitStatus),
                    color,
                    image,
                    twitStatus.getText(),
                    twitStatus.getUser().getBiggerProfileImageURL()
            ));
        }
    }
}

