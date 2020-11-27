package dev.codesoup.mc;

import java.util.List;
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
	
	private boolean dynmapExists;
	private DynmapCommonAPI dynmapAPI;
	private MarkerAPI markerAPI;
	private MarkerSet markerSet;
	
	public MapManager(CustomMod mod) {
		
		super(mod);
	
		this.dynmapExists = Loader.isModLoaded("dynmap");
		
		if(dynmapExists) {
			DynmapCommonAPIListener.register(new DynmapListener());
		} else {
			mod.logger.error("Dynmap not installed, map will not work!");
		}
		
	}
	
	private class DynmapListener extends DynmapCommonAPIListener {
		
		@Override
		public void apiEnabled(DynmapCommonAPI api) {
			if(api != null) {
				
				dynmapAPI = api;
				markerAPI = api.getMarkerAPI();
				
				createMarkerLayer();
				initClaims(mod.getClaims());
				
			} else {
				mod.logger.error("No API?");
			}
		}
		
	}
	
	private void addMarker(int x, int z, UUID uuid) {
		
		Alliance alliance = mod.getAllianceManager().getAlliance(uuid);
		int color;
		if(alliance != null) 
			color = Colors.toRGB(alliance.getColor());
		else
			color = 0xffffff;
		
		String markerID = String.format("%d-%d", x, z);
		String tooltip = "test";
		
		double[] xlist = new double[] {x * 16, x * 16 + 16};
		double[] zlist = new double[] {z * 16, z * 16 + 16};
		
		AreaMarker marker = markerSet.createAreaMarker(markerID, tooltip, true, "earth", xlist, zlist, false);
		if(marker != null) {
			marker.setLineStyle(1, 0, color);
			marker.setFillStyle(0.7, color);
		} else {
			mod.logger.error("Failed to create marker");
		}
		
	}
	
	public void initClaims(ClaimsManager manager) {
	
		if(dynmapExists) {
			for(AreaMarker marker: markerSet.getAreaMarkers()) {
				marker.deleteMarker();
			}
			
			for(Pair pair: manager.getClaims().keySet()) {
				addMarker(pair.A, pair.B, mod.getClaims().getClaim(pair.A, pair.B));
			}
		}
		
	}
	
	public void doClaim(int x, int z, UUID uuid) {
		if(dynmapExists) {
			addMarker(x, z, uuid);
		}
	}
	
	public void doUnclaim(int x, int z) {
		if(dynmapExists) {
			AreaMarker marker = markerSet.findAreaMarker(String.format("%d-%d", x, z));
			if(marker != null) {
				marker.deleteMarker();
			}
		}
	}
	
	public void refreshClaims(Alliance alliance) {
		if(dynmapExists) {
			for(UUID uuid: alliance.getMembers()) {
				refreshClaims(uuid);
			}
		}
	}
	
	public void refreshClaims(UUID uuid) {
		if(dynmapExists) {
			refreshClaims(mod.getClaims().getClaims(uuid), uuid);
		}
	}
	
	public void refreshClaims(Map<Pair, UUID> claims) {
		if(dynmapExists) {
			for(Pair pair: claims.keySet()) {
				doUnclaim(pair.A, pair.B);
				addMarker(pair.A, pair.B, claims.get(pair));
			}
		}
	}
	
	public void refreshClaims(List<Pair> pairs, UUID uuid) {
		if(dynmapExists) {
			for(Pair pair: pairs) {
				doUnclaim(pair.A, pair.B);
				addMarker(pair.A, pair.B, uuid);
			}
		}
	}
	
	private void createMarkerLayer() {
		
		if(this.markerAPI != null) {
			markerSet = this.markerAPI.getMarkerSet(MARKER_SET_NAME);
			if(markerSet == null) {
				markerSet = this.markerAPI.createMarkerSet(MARKER_SET_NAME, CLAIMS_LAYER_NAME, null, false);
			}
			
			markerSet.setMarkerSetLabel(CLAIMS_LAYER_NAME);
		} else {
			mod.logger.error("Marker API is null!");
		}
		
	}
	
}
