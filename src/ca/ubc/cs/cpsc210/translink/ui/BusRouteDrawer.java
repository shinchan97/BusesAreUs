package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /**
     * overlay used to display bus route legend text on a layer above the map
     */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /**
     * overlays used to plot bus routes
     */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     *
     * @param context the application context
     * @param mapView the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) { // task 7
        Stop selected = getArrivals();
        if (selected != null) {
            Set<Route> routes = selected.getRoutes();
            for (Route r : routes) {
                List<RoutePattern> rp1 = getRoutePatterns(r);
                for (RoutePattern rp: rp1) {
                    List<LatLon> ll1 = rp.getPath();
                    for (int i = 0; i < ll1.size() - 1; i++) {
                        if (Geometry.rectangleIntersectsLine(northWest, southEast, ll1.get(i), ll1.get(i + 1))) {
                            drawline(zoomLevel, r, ll1, i);
                        }
                    }
                }
            }
        }
    }

    private Stop getArrivals() {
        Stop selected = StopManager.getInstance().getSelected();
        updateVisibleArea();
        busRouteLegendOverlay.clear();
        busRouteOverlays.clear();
        return selected;
    }

    private List<RoutePattern> getRoutePatterns(Route r) {
        List<RoutePattern> rp1 = r.getPatterns();
        busRouteLegendOverlay.add(r.getNumber());
        return rp1;
    }

    private void drawline(int zoomLevel, Route r, List<LatLon> ll1, int i) {
        List<GeoPoint> geo1 = new ArrayList<GeoPoint>();
        GeoPoint firstgeo = new GeoPoint(ll1.get(i).getLatitude(), ll1.get(i).getLongitude());
        GeoPoint secondgeo = new GeoPoint(ll1.get(i + 1).getLatitude(), ll1.get(i + 1).getLongitude());
        geo1.add(firstgeo);
        geo1.add(secondgeo);
        Polyline pol1 = new Polyline(mapView.getContext());
        pol1.setColor(busRouteLegendOverlay.getColor(r.getNumber()));
        pol1.setWidth(getLineWidth(zoomLevel));
        pol1.setPoints(geo1);
        busRouteOverlays.add(pol1);
    }

    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     *
     * @param zoomLevel the zoom level of the map
     * @return width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if (zoomLevel > 14) {
            return 7.0f * BusesAreUs.dpiFactor();
        } else if (zoomLevel > 10) {
            return 5.0f * BusesAreUs.dpiFactor();
        } else {
            return 2.0f * BusesAreUs.dpiFactor();
        }
    }
}
