package dev.codesoup.mc;

import java.util.Map;
import java.util.UUID;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import net.minecraftforge.fml.common.Loader;

public class MapManager extends RequiresMod {

	public static final String MARKER_SET_NAME = "custommod.claims.markerset";
	public static final String CLAIMS_LAYER_NAME = "Claims";
	
	private DynmapCommonAPI dynmapAPI;
	private MarkerAPI markerAPI;
	private MarkerSet markerSet;
	
	public MapManager(CustomMod mod) {
		
		super(mod);
	
		if(!Loader.isModLoaded("dynmap")) {
			throw new RuntimeException("Dynmap is not installed!");
		}
		
		DynmapCommonAPIListener.register(new DynmapListener());
		
	}
	
	private class DynmapListener extends DynmapCommonAPIListener {
		
		@Override
		public void apiEnabled(DynmapCommonAPI api) {
			if(api != null) {
				
				dynmapAPI = api;
				markerAPI = api.getMarkerAPI();
				
				createMarkerLayer();
				initClaims(mod.getClaims());
				
			}
		}
		
	}
	
	private void addMarker(int x, int z) {
		
		String markerID = String.format("%d-%d", x, z);
		String tooltip = "test";
		
		double[] xlist = new double[] {x * 16, x * 16 + 15};
		double[] zlist = new double[] {z * 16, z * 16 + 15};
		
		AreaMarker marker = markerSet.createAreaMarker(markerID, tooltip, true, "world", xlist, zlist, false);
		if(marker != null) {
			marker.setLineStyle(3, 1, 0xffff00);
			marker.setFillStyle(0.5, 0xffff00);
		}
		
	}
	
	public void initClaims(ClaimsManager manager) {
		
		Map<Pair, UUID> claims = manager.getClaims();
		
		for(Pair pair: claims.keySet()) {
			addMarker(pair.A, pair.B);
		}
		
	}
	
	public void doClaim(int x, int z) {
		addMarker(x, z);
	}
	
	public void doUnclaim(int x, int z) {
		AreaMarker marker = markerSet.findAreaMarker(String.format("%d-%d", x, z));
		if(marker != null) {
			marker.deleteMarker();
		}
	}
	
	private void createMarkerLayer() {
		
		if(this.markerAPI != null) {
			markerSet = this.markerAPI.getMarkerSet(MARKER_SET_NAME);
			if(markerSet == null) {
				markerSet = this.markerAPI.createMarkerSet(MARKER_SET_NAME, CLAIMS_LAYER_NAME, null, false);
			}
			
			markerSet.setMarkerSetLabel(CLAIMS_LAYER_NAME);
		}
		
	}
	
}
