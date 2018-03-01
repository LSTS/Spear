package com.example.nachito.spear;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import pt.lsts.coverage.GeoCoord;
import pt.lsts.imc.FollowPath;
import pt.lsts.imc.Goto;
import pt.lsts.imc.Maneuver;
import pt.lsts.imc.PathPoint;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.neptus.messages.listener.Periodic;
import pt.lsts.util.PlanUtilities;

import static android.os.Build.VERSION_CODES.M;
import static com.example.nachito.spear.MainActivity.altitude;
import static com.example.nachito.spear.MainActivity.depth;
import static com.example.nachito.spear.MainActivity.isDepthSelected;
import static com.example.nachito.spear.MainActivity.isRPMSelected;
import static com.example.nachito.spear.MainActivity.localizacao;
import static com.example.nachito.spear.MainActivity.speed;
import static com.example.nachito.spear.MainActivity.startBehaviour;
import static com.example.nachito.spear.MainActivity.swath_width;
import static com.example.nachito.spear.MainActivity.zoomLevel;
import static pt.lsts.coverage.AreaCoverage.computeCoveragePath;

/**
 *
 * Created by ines on 11/13/17.
 */
@EActivity
public class Area extends AppCompatActivity {

    static double lat;
    static double lon;
    static boolean iscircleDrawn;
    static Polyline polyline;
    static Polygon circle;
    static ArrayList<GeoPoint> markers = new ArrayList<>();
    static ArrayList<Maneuver> maneuverArrayList;
    static ArrayList<GeoPoint> nullArray = new ArrayList<>();
    IMapController mapController;
    Button done;
    Button preview;
    MapView mapArea;
    Button erase;
    Drawable nodeIcon2;
    int numberOfPointsPressed;
    Drawable nodeIcon;
    Marker startMarker;
    ArrayList<GeoPoint> otherVehiclesPosition;
    GeoPoint centerInSelectedVehicle;
    final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centerInSelectedVehicle);
    Goto area2;
    boolean isdoneClicked = false;
    Button eraseAll;
    String selected;
    List<Marker> markerList = new ArrayList<>();
    List<Marker> markerArea = new ArrayList<>();
    List<Polyline> poliList = new ArrayList<>();
    Maneuver maneuverFromArea;
    ArrayList<GeoPoint> areaWaypoints = new ArrayList<>();
    Collection<PlanUtilities.Waypoint> waypointsFromArea;
    Marker pointsFromArea;
    Polyline areaWaypointPolyline;
    ArrayList<Maneuver> maneuvers;


    public static ArrayList<GeoPoint> getPointsArea() {
        return markers;
    }

    public static boolean getCircle() {
        return iscircleDrawn;
    }


    public static List<Maneuver> sendmList() {
        return maneuverArrayList;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.area);

        mapArea = findViewById(R.id.mapArea);
        done = findViewById(R.id.doneArea);
        nodeIcon = getResources().getDrawable(R.drawable.orangeled);
        erase = findViewById(R.id.eraseArea);
        eraseAll = findViewById(R.id.eraseAllArea);
        isdoneClicked = false;
        if (pointsFromArea != null) {
            for (Marker l : markerArea) {
                l.remove(mapArea);
                mapArea.invalidate();
            }
        }

        if (areaWaypointPolyline != null) {

            areaWaypointPolyline.setPoints(nullArray);
            mapArea.getOverlays().remove(areaWaypointPolyline);
            mapArea.invalidate();
        }
        if (areaWaypoints != null)
            areaWaypoints.clear();


        if (maneuverArrayList != null)
            maneuverArrayList.clear();
        markerList.clear();
        markerArea.clear();
        markers.clear();
        numberOfPointsPressed = 0;
        Toast.makeText(this, " Long click on the map to choose an area", Toast.LENGTH_SHORT).show();
        getIntentSelected();
        nodeIcon2 = getResources().getDrawable(R.drawable.reddot);
        preview = findViewById(R.id.previewArea);

        if (MainActivity.isOfflineSelected) {
            mapArea.setTileSource(new XYTileSource("4uMaps", 2, 18, 256, ".png", new String[]{}));
        }
        mapController = mapArea.getController();
        mapController.setZoom(zoomLevel);
        centerInSelectedVehicle = MainActivity.getVariables();
        if (centerInSelectedVehicle != null)
            mapController.setCenter(centerInSelectedVehicle);
        else
            mapController.setCenter(localizacao());

        drawRed();
        drawBlue();
        drawGreen();


        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                lat = p.getLatitude();
                lon = p.getLongitude();
                markers.add(p);
                startMarker = new Marker(mapArea);
                startMarker.setPosition(p);
                mapArea.getOverlays().add(startMarker);
                startMarker.setIcon(getResources().getDrawable(R.drawable.orangeled));
                markerList.add(startMarker);
                mapArea.invalidate();
                numberOfPointsPressed++;
                erase.setOnClickListener(v -> {
                    if (!isdoneClicked) {
                        for (int i = 0; i < numberOfPointsPressed; i++) {
                            mapArea.getOverlays().remove(startMarker);
                            startMarker.remove(mapArea);
                            markers.remove(startMarker.getPosition());
                            numberOfPointsPressed--;
                        }

                        if (polyline != null)
                            polyline.setPoints(markers);
                        if (circle != null)
                            circle.setPoints(markers);
                        mapArea.invalidate();
                        erase.setClickable(false);
                    }
                });


                eraseAll.setOnClickListener(v -> {
                    if (pointsFromArea != null) {
                        for (Marker l : markerArea) {
                            l.remove(mapArea);
                            mapArea.invalidate();
                        }
                    }

                    if (areaWaypointPolyline != null) {
                        for (Polyline line : poliList) {
                            line.setPoints(nullArray);
                            mapArea.getOverlays().remove(areaWaypointPolyline);
                            mapArea.invalidate();

                        }

                    }
                    if (areaWaypoints != null)
                        areaWaypoints.clear();
                    if (maneuverArrayList != null)
                        maneuverArrayList.clear();
                    if (maneuvers != null)
                        maneuvers.clear();

                    if (!isdoneClicked) {
                        for (Marker m : markerList) {
                            m.remove(mapArea);
                            mapArea.invalidate();

                        }
                        markerList.clear();
                        markerArea.clear();
                        markers.clear();
                        numberOfPointsPressed = 0;
                        drawGreen();
                        drawBlue();
                        drawRed();

                    }
                });

                done.setOnClickListener(v -> {
                    if (markers.size() <= 1) {

                        if (selected == null) {
                            Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            Go(p);
                            isdoneClicked = true;
                        }
                    } else if (markers.size() > 1) {
                        if (selected == null) {
                            Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {
                            isdoneClicked = true;
                            iscircleDrawn = true;
                            followArea();

                        }
                    }
                });


                preview.setOnClickListener(v -> {
                    if (markers.size() <= 1) {
                        Toast.makeText(Area.this, "Add more points", Toast.LENGTH_SHORT).show();
                    } else if (markers.size() > 1) {
                        if (selected == null) {
                            Toast.makeText(Area.this, "Select a vehicle first", Toast.LENGTH_SHORT).show();
                        } else {

                            followArea();
                        }
                    }
                });
                return false;
            }
        };
        MapEventsOverlay OverlayEventos = new MapEventsOverlay(this.getBaseContext(), mReceive);
        mapArea.getOverlays().add(OverlayEventos);
        //Refreshing the map to draw the new overlay
        mapArea.invalidate();

    }

    public void getIntentSelected() {
        Intent intent = getIntent();
        selected = intent.getExtras().getString("selected");
    }




    public void followArea() {
        LinkedHashSet<String> noRepetitions = new LinkedHashSet<>();
        Iterator<GeoPoint> it = markers.iterator();
        while (it.hasNext()) {
            String val = it.next().toString();
            if (noRepetitions.contains(val)) {
                it.remove();
            } else
                noRepetitions.add(val);
        }

        ArrayList<GeoCoord> coords = new ArrayList<>();
        maneuvers = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++) {
            coords.add(new GeoCoord(markers.get(i).getLatitude(), markers.get(i).getLongitude()));
        }
        Vector<PathPoint> points = new Vector<>();
        GeoCoord primPonto = new GeoCoord(markers.get(0).getLatitude(), markers.get(0).getLongitude());
        for (GeoCoord coord : computeCoveragePath(coords, swath_width)) {
            double[] offsets = coord.getOffsetFrom(primPonto);
            PathPoint pt = new PathPoint();
            pt.setX(offsets[0]);
            pt.setY(offsets[1]);
            pt.setZ(offsets[2]);
            points.add(pt);

        }
        FollowPath area = new FollowPath();
        double lat = Math.toRadians(markers.get(0).getLatitude()); //primeiro
        double lon = Math.toRadians(markers.get(0).getLongitude());
        area.setLat(lat);
        area.setLon(lon);
        area.setSpeed(speed);
        if (!isRPMSelected) {
            area.setSpeedUnits(SpeedUnits.METERS_PS);
        } else {
            area.setSpeedUnits(SpeedUnits.RPM);
        }
        if (isDepthSelected) {
            area.setZ(depth);
            area.setZUnits(ZUnits.DEPTH);
        } else {
            area.setZ(altitude);
            area.setZUnits(ZUnits.ALTITUDE);
        }
        area.setPoints(points);
        for (int i = 0; i < area.getPoints().size(); i++) {
            maneuvers.add(area);
            maneuverArrayList = new ArrayList<>();
            maneuverArrayList.addAll(maneuvers);

        }
        if (isdoneClicked) {
            MainActivity.areNewWaypointsFromAreaUpdated = false;
            MainActivity.hasEnteredServiceMode = false;
            startBehaviour("SpearArea-" + selected, PlanUtilities.createPlan("SpearArea-" + selected, maneuvers.toArray(new Maneuver[0])));
            sendmList();
            onBackPressed();
        } else {
            drawPreview(maneuverArrayList);
        }
    }

    public void drawPreview(List<Maneuver> maneuverListArea) {

        for (int i = 0; i < maneuverListArea.size(); i++) {
            wayPointsArea(maneuverListArea.get(i));
        }
    }

    public void wayPointsArea(final Maneuver maneuver) {
        maneuverFromArea = maneuver;
        makePointsArea();
    }

    public void makePointsArea() {
        GeoPoint pontoArea;
        waypointsFromArea = PlanUtilities.computeWaypoints(maneuverFromArea);
        for (PlanUtilities.Waypoint point : waypointsFromArea) {
            double valueOfLatitude = point.getLatitude();
            double valueOfLongitude = point.getLongitude();
            pontoArea = new GeoPoint(valueOfLatitude, valueOfLongitude);
            if (!(areaWaypoints.contains(pontoArea))) {
                areaWaypoints.add(pontoArea);
            }
        }

        for (int i = 0; i < areaWaypoints.size(); i++) {
            pointsFromArea = new Marker(mapArea);
            pointsFromArea.setPosition(areaWaypoints.get(i));
            pointsFromArea.setIcon(nodeIcon2);
            pointsFromArea.setDraggable(true);
            mapArea.getOverlays().add(pointsFromArea);
            markerArea.add(pointsFromArea);
        }
        areaWaypointPolyline = new Polyline();
        areaWaypointPolyline.setWidth(5);
        areaWaypointPolyline.setPoints(areaWaypoints);
        mapArea.getOverlays().add(areaWaypointPolyline);
        mapArea.invalidate();
        poliList.add(areaWaypointPolyline);


    }

    public void Go(GeoPoint p) {
        Goto go = new Goto();
        double lat = Math.toRadians(p.getLatitude());
        double lon = Math.toRadians(p.getLongitude());
        go.setLat(lat);
        go.setLon(lon);
        if (isDepthSelected) {
            go.setZ(depth);
            go.setZUnits(ZUnits.DEPTH);
        } else {
            go.setZ(altitude);
            go.setZUnits(ZUnits.ALTITUDE);
        }
        go.setSpeed(speed);
        if (!isRPMSelected) {
            go.setSpeedUnits(SpeedUnits.METERS_PS);
        } else {
            go.setSpeedUnits(SpeedUnits.RPM);
        }
        String planid = "SpearGoto-" + selected;
        MainActivity.hasEnteredServiceMode = false;
        startBehaviour(planid, go);
        onBackPressed();

    }

    @Periodic()
    public void drawBlue() {
        otherVehiclesPosition = MainActivity.drawOtherVehicles();

        for (int i = 0; i < otherVehiclesPosition.size(); i++) {
            if (otherVehiclesPosition.get(i) != centerInSelectedVehicle) {
                final ArrayList<OverlayItem> itemsPoints = new ArrayList<>();
                OverlayItem markerPoints = new OverlayItem("markerTitle", "markerDescription", otherVehiclesPosition.get(i));
                markerPoints.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
                itemsPoints.add(markerPoints);
                Resources resources = this.getResources();
                Bitmap source2;
                if (android.os.Build.VERSION.SDK_INT <= M) {
                    source2 = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2));

                } else

                    source2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.downarrow2), 50, 50, false);

                Bitmap target = MainActivity.RotateMyBitmap(source2, MainActivity.orientationOtherVehicles.get(i));
                Drawable marker_ = new BitmapDrawable(getResources(), target);
                ItemizedIconOverlay markersOverlay_ = new ItemizedIconOverlay<>(itemsPoints, marker_, null, this);
                mapArea.getOverlays().add(markersOverlay_);

            }
        }
    }

    @Periodic()
    public void drawRed() {
        final GeoPoint loc = MainActivity.localizacao();
        final ArrayList<OverlayItem> items2 = new ArrayList<>();
        final OverlayItem marker2 = new OverlayItem("markerTitle", "markerDescription", loc);
        marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
        items2.add(marker2);
        Resources resources = this.getResources();

        Bitmap newMarker2;
        if (android.os.Build.VERSION.SDK_INT <= M) {

            newMarker2 = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowred2));

        } else {

            newMarker2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.arrowred2), 0, 0, true);

        }
        Bitmap target = MainActivity.RotateMyBitmap(newMarker2, MainActivity.bearingMyLoc);
        Drawable markerLoc = new BitmapDrawable(getResources(), target);
        final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items2, markerLoc, null, this);
        mapArea.getOverlays().add(markersOverlay2);

    }


    @Periodic()
    public void drawGreen() {
        if (centerInSelectedVehicle != null) {
            final ArrayList<OverlayItem> items = new ArrayList<>();
            final OverlayItem marker = new OverlayItem("markerTitle", "markerDescription", centerInSelectedVehicle);
            marker.setMarkerHotspot(OverlayItem.HotspotPlace.TOP_CENTER);
            items.add(marker);
            Bitmap newMarker;
            Resources resources = this.getResources();

            if (android.os.Build.VERSION.SDK_INT <= M) {
                newMarker = Bitmap.createBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2));

            } else

                newMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrowgreen2), 0, 0, true);
            Bitmap target = MainActivity.RotateMyBitmap(newMarker, MainActivity.orientationSelected);
            Drawable markerLoc = new BitmapDrawable(getResources(), target);
            final ItemizedIconOverlay markersOverlay2 = new ItemizedIconOverlay<>(items, markerLoc, null, this);
            mapArea.getOverlays().add(markersOverlay2);


        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapController.setZoom(zoomLevel);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}