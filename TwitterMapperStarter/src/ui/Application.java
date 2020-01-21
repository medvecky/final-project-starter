package ui;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import query.Query;
import query.QueryManager;
import twitter.LiveTwitterSource;
import twitter.PlaybackTwitterSource;
import twitter.TwitterSource;
import util.SphericalGeometry;
import util.Util;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * The Twitter viewer application
 * Derived from a JMapViewer demo program written by Jan Peter Stotz
 */
public class Application extends JFrame {
    // The content panel, which contains the entire UI
    private final ContentPanel contentPanel;
    // The provider of the tiles for the map, we use the Bing source
    private BingAerialTileSource bing;
    // All of the active queries
    private QueryManager queryManager;
    // The source of tweets, a TwitterSource, either live or playback
    private TwitterSource twitterSource;

    private void initialize() {
        // To use the live twitter stream, use the following line
        twitterSource = new LiveTwitterSource();

        // To use the recorded twitter stream, use the following line
        // The number passed to the constructor is a speedup value:
        //  1.0 - play back at the recorded speed
        //  2.0 - play back twice as fast
//        twitterSource = new PlaybackTwitterSource(60.0);
        queryManager = new QueryManager();
    }

    /**
     * A new query has been entered via the User Interface
     *
     * @param query The new query object
     */
    public void addQuery(Query query) {
        queryManager.addQuery(query);
        twitterSource.setFilterTerms(queryManager.getQueryTerms());
        contentPanel.addQuery(query);
        twitterSource.addObserver(query);
    }

    /**
     * Constructs the {@code Application}.
     */
    public Application() {
        super("Twitter content viewer");
        setSize(300, 300);
        initialize();

        bing = new BingAerialTileSource();

        // Do UI initialization
        contentPanel = new ContentPanel(this);
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Always have map markers showing.
        map().setMapMarkerVisible(true);
        // Always have zoom controls showing,
        // and allow scrolling of the map around the edge of the world.
        map().setZoomContolsVisible(true);
        map().setScrollWrapEnabled(true);

        // Use the Bing tile provider
        map().setTileSource(bing);

        //NOTE This is so that the map eventually loads the tiles once Bing attribution is ready.
        Coordinate coord = new Coordinate(0, 0);

        Timer bingTimer = new Timer();
        TimerTask bingAttributionCheck = new TimerTask() {
            @Override
            public void run() {
                // This is the best method we've found to determine when the Bing data has been loaded.
                // We use this to trigger zooming the map so that the entire world is visible.
                if (!bing.getAttributionText(0, coord, coord).equals("Error loading Bing attribution data")) {
                    map().setZoom(2);
                    bingTimer.cancel();
                }
            }
        };
        bingTimer.schedule(bingAttributionCheck, 100, 200);

        // Set up a motion listener to create a tooltip showing the tweets at the pointer position
        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                ICoordinate pos = map().getPosition(p);
                List<MapMarker> markers = getMarkersCovering(pos, pixelWidth(p));
                String text = "";
                for (MapMarker m : markers) {
                    MapMarkerMy ma = (MapMarkerMy) m;
                    text = getHtml(ma.getProfileImageUrl(), ma.getTooltipText());
                }
                map().setToolTipText(text);
            }
        });
    }


    public String getHtml(String imgUrl, String text) {
        return "<html><style>html{background-color: #F7D358; }</style><body> <img src = "
                + imgUrl + ">" + text + "</body></html>";
    }

    // How big is a single pixel on the map?  We use this to compute which tweet markers
    // are at the current most position.
    private double pixelWidth(Point p) {
        ICoordinate center = map().getPosition(p);
        ICoordinate edge = map().getPosition(new Point(p.x + 1, p.y));
        return SphericalGeometry.distanceBetween(center, edge);
    }

    // Get those layers (of tweet markers) that are visible because their corresponding query is enabled
    private Set<Layer> getVisibleLayers() {
       return queryManager.getVisibleLayers();
    }

    // Get all the markers at the given map position, at the current map zoom setting
    private List<MapMarker> getMarkersCovering(ICoordinate pos, double pixelWidth) {
        List<MapMarker> ans = new ArrayList<>();
        Set<Layer> visibleLayers = getVisibleLayers();
        for (MapMarker m : map().getMapMarkerList()) {
            if (!visibleLayers.contains(m.getLayer())) continue;
            double distance = SphericalGeometry.distanceBetween(m.getCoordinate(), pos);
            if (distance < m.getRadius() * pixelWidth) {
                ans.add(m);
            }
        }
        return ans;
    }

    public JMapViewer map() {
        return contentPanel.getViewer();
    }

    /**
     * @param args Application program arguments (which are ignored)
     */
    public static void main(String[] args) {
        new Application().setVisible(true);
    }

    // Update which queries are visible after any checkBox has been changed
    public void updateVisibility() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println("Recomputing visible queries");
                for (Query q : queryManager) {
                    JCheckBox box = q.getCheckBox();
                    Boolean state = box.isSelected();
                    q.setVisible(state);
                }
                map().repaint();
            }
        });
    }

    // A query has been deleted, remove all traces of it
    public void terminateQuery(Query query) {
        queryManager.removeQuery(query);
        twitterSource.setFilterTerms(queryManager.getQueryTerms());
        twitterSource.deleteObserver(query);
    }
}
