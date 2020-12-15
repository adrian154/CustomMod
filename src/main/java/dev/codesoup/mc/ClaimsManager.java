package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ClaimsManager extends RequiresMod {
	
	private Map<XZPair, UUID> claims;
	private Map<UUID, List<XZPair>> playerClaims;
	
	public ClaimsManager(CustomMod mod) {
		super(mod);
		claims = new HashMap<XZPair, UUID>();
		playerClaims = new HashMap<UUID, List<XZPair>>();
	}

	public UUID getClaim(int x, int z) {
		return claims.get(new XZPair(x, z));
	}
	
	public void setClaim(int x, int z, UUID uuid) {
		
		XZPair pair = new XZPair(x, z);
		claims.put(pair, uuid);
		
		if(playerClaims.get(uuid) == null)
			playerClaims.put(uuid, new ArrayList<XZPair>());
		playerClaims.get(uuid).add(pair);
		
		mod.getMapManager().doClaim(x, z, uuid);
		
	}
	
	public void unclaim(int x, int z) {
		
		XZPair pair = new XZPair(x, z);
		UUID claimer = claims.remove(pair);
		playerClaims.get(claimer).remove(pair);
		
		mod.getMapManager().doUnclaim(x, z);
		
	}
	
	public boolean shouldProtect(World world, BlockPos pos, UUID uuid) {
		
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		UUID claim = getClaim(chunk.x, chunk.z);
		
		// If the claim is unclaimed, it's up for grabs
		if(claim == null) {
			
			return false;
		
		} else {
			
			// If the player is offline...
			if(mod.getPlayer(claim) == null) {
			
				// If they are allied, don't protect. Otherwise, if they are allied, protect.
				return !mod.getNationManager().sameNation(uuid, claim);
			
			} else {
				
				// otherwise, there are no protections
				return false;
				
			}
			
		}
		
	}
	
	public Map<XZPair, UUID> getClaims() {
		return this.claims;
	}
	
	public List<XZPair> getClaims(UUID uuid) {
		if(playerClaims.get(uuid) == null)
			playerClaims.put(uuid, new ArrayList<XZPair>());
		return playerClaims.get(uuid);
	}
	
	public int getNumClaims(UUID uuid) {
		if(playerClaims.get(uuid) == null)
			playerClaims.put(uuid, new ArrayList<XZPair>());
		return playerClaims.get(uuid).size();
	}
	
	public XZPair unclaimLast(UUID uuid) {
		List<XZPair> claims = playerClaims.get(uuid);
		XZPair last = claims.get(claims.size() - 1);
		this.unclaim(last.A, last.B);
		claims.remove(claims.size() - 1); 
		return last;
	}
	
}