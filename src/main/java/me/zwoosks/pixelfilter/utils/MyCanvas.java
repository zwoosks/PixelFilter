package me.zwoosks.pixelfilter.utils;

import java.awt.Image;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapFont;
import org.bukkit.map.MapView;

public class MyCanvas implements MapCanvas {

    private final MapView mapView;
    private final byte[][] pixels = new byte[128][128];

    private MapCursorCollection cursors;

    public MyCanvas(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public void drawImage(int arg0, int arg1, Image arg2) {
    }

    @Override
    public void drawText(int arg0, int arg1, MapFont arg2, String arg3) {
    }

    @Override
    public byte getBasePixel(int x, int y) {
        return this.getPixel(x, y);
    }

    @Override
    public MapCursorCollection getCursors() {
        return cursors == null ? cursors = new MapCursorCollection() : cursors;
    }

    @Override
    public MapView getMapView() {
        return this.mapView;
    }

    @Override
    public byte getPixel(int x, int y) {
        return this.pixels[x][y];
    }

    @Override
    public void setCursors(MapCursorCollection cursors) {
        this.cursors = cursors;
    }

    @Override
    public void setPixel(int x, int y, byte pixel) {
        this.pixels[x][y] = pixel;
    }

    public byte[][] getPixels() {
        return this.pixels;
    }

}
