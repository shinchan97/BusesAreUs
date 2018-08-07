package ca.ubc.cs.cpsc210.translink.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.R;
import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// A plotter for bus stop locations
public class BusStopPlotter extends MapViewOverlay {
    /**
     * clusterer
     */
    private RadiusMarkerClusterer stopClusterer;
    /**
     * maps each stop to corresponding marker on map
     */
    private Map<Stop, Marker> stopMarkerMap = new HashMap<>();
    /**
     * marker for stop that is nearest to user (null if no such stop)
     */
    private Marker nearestStnMarker;
    private Activity activity;
    private StopInfoWindow stopInfoWindow;

    /**
     * Constructor
     *
     * @param activity the application context
     * @param mapView  the map view on which buses are to be plotted
     */
    public BusStopPlotter(Activity activity, MapView mapView) {
        super(activity.getApplicationContext(), mapView);
        this.activity = activity;
        nearestStnMarker = null;
        stopInfoWindow = new StopInfoWindow((StopSelectionListener) activity, mapView);
        newStopClusterer();
    }

    public RadiusMarkerClusterer getStopClusterer() {
        return stopClusterer;
    }

    /**
     * Mark all visible stops in stop manager onto map.
     */
    public void markStops(Location currentLocation) { //task 5
        updateVisibleArea();
        Drawable stopIconDrawable = activity.getResources().getDrawable(R.drawable.stop_icon);
        stopClusterer.getItems().clear();
        for (Stop s: StopManager.getInstance()) {
            if (Geometry.rectangleContainsPoint(northWest, southEast, s.getLocn())) {
                Marker m1 = stopMarkerMap.get(s);
                if (m1 == null) {
                    m1 = newMarker(stopIconDrawable, s);
                }
                setMarker(s, m1);
                stopClusterer.add(m1);
            }
        }
        markTheNearest(currentLocation);
        stopClusterer.invalidate();
    }

    private Marker newMarker(Drawable stopIconDrawable, Stop s) {
        Marker m1 = new Marker(mapView);
        GeoPoint g1 = new GeoPoint(s.getLocn().getLatitude(), s.getLocn().getLongitude());
        m1.setPosition(g1);
        m1.setInfoWindow(stopInfoWindow);
        m1.setRelatedObject(s);
        m1.setIcon(stopIconDrawable);
        makeBusNo(s, m1);
        return m1;
    }

    private void makeBusNo(Stop s, Marker m1) {
        ArrayList<String> busNo = new ArrayList<>();
        for (Route r: s.getRoutes()) {
            busNo.add(r.getNumber());
        }
        String busNos = new String();
        for (String str: busNo) {
            busNos = busNos + "\n" + str;
        }
        m1.setTitle(s.getNumber() + " " + s.getName() + " " + busNos);
    }


    private void markTheNearest(Location currentLocation) {
        if (currentLocation != null) {
            LatLon userloc = new LatLon(currentLocation.getLatitude(), currentLocation.getLongitude());
            Stop closest = StopManager.getInstance().findNearestTo(userloc);
            updateMarkerOfNearest(closest);
        }
    }

    /**
     * Create a new stop cluster object used to group stops that are close by to reduce screen clutter
     */
    private void newStopClusterer() {
        stopClusterer = new RadiusMarkerClusterer(activity);
        stopClusterer.getTextPaint().setTextSize(20.0F * BusesAreUs.dpiFactor());
        int zoom = mapView == null ? 16 : mapView.getZoomLevel();
        if (zoom == 0) {
            zoom = MapDisplayFragment.DEFAULT_ZOOM;
        }
        int radius = 1000 / zoom;

        stopClusterer.setRadius(radius);
        Drawable clusterIconD = activity.getResources().getDrawable(R.drawable.stop_cluster);
        Bitmap clusterIcon = ((BitmapDrawable) clusterIconD).getBitmap();
        stopClusterer.setIcon(clusterIcon);
    }

    /**
     * Update marker of nearest stop (called when user's location has changed).  If nearest is null,
     * no stop is marked as the nearest stop.
     *
     * @param nearest stop nearest to user's location (null if no stop within StopManager.RADIUS metres)
     */
    public void updateMarkerOfNearest(Stop nearest) { //task 6
        Drawable stopIconDrawable = activity.getResources().getDrawable(R.drawable.stop_icon);
        Drawable closestStopIconDrawable = activity.getResources().getDrawable(R.drawable.closest_stop_icon);

        if (nearestStnMarker != null) {
            nearestStnMarker.setIcon(stopIconDrawable);
//            nearestStnMarker = null;
        }

        if (nearest != null) {
            nearestStnMarker = getMarker(nearest);
            if (nearestStnMarker != null) {
                nearestStnMarker.setIcon(closestStopIconDrawable);
            }
        }
    }


    /**
     * Manage mapping from stops to markers using a map from stops to markers.
     * The mapping in the other direction is done using the Marker.setRelatedObject() and
     * Marker.getRelatedObject() methods.
     */
    private Marker getMarker(Stop stop) {
        return stopMarkerMap.get(stop);
    }

    private void setMarker(Stop stop, Marker marker) {
        stopMarkerMap.put(stop, marker);
    }

    private void clearMarker(Stop stop) {
        stopMarkerMap.remove(stop);
    }

    private void clearMarkers() {
        stopMarkerMap.clear();
    }
}
