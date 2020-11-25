package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ClaimsManager extends Manager {
	
	private Map<Pair, UUID> claims;
	private Map<UUID, List<Pair>> playerClaims;
	
	public ClaimsManager(CustomMod mod) {
		super(mod);
		claims = new HashMap<Pair, UUID>();
		playerClaims = new HashMap<UUID, List<Pair>>();
	}
	
	// x and z are chunk coordinates
	public UUID getClaim(int x, int z) {
		return claims.get(new Pair(x, z));
	}
	
	public void setClaim(int x, int z, UUID uuid) {
		Pair pair = new Pair(x, z);
		claims.put(pair, uuid);
		
		if(playerClaims.get(uuid) == null)
			playerClaims.put(uuid, new ArrayList<Pair>());
		playerClaims.get(uuid).add(pair);
	}
	
	public void unclaim(int x, int z) {
		Pair pair = new Pair(x, z);
		UUID claimer = claims.remove(pair);
		playerClaims.get(claimer).remove(pair);
	}
	
	public boolean shouldProtect(World world, BlockPos pos, UUID uuid) {
		
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		UUID claim = getClaim(chunk.x, chunk.z);
		
		// If the claim is unclaimed, it's up for grabs
		if(claim == null) {
			
			return false;
		
		} else {
			
			// If the player is offline...
			if(mod.getServer().getPlayerList().getPlayerByUUID(claim) == null) {
			
				// If they are allied, don't protect. Otherwise, if they are allied, protect.
				return !mod.getAllianceManager().areAllied(uuid, claim);
			
			} else {
				
				// otherwise, there are no protections
				return false;
				
			}
			
		}
		
	}
	
	public List<Pair> getClaims(UUID uuid) {
		if(playerClaims.get(uuid) == null)
			playerClaims.put(uuid, new ArrayList<Pair>());
		return playerClaims.get(uuid);
	}
	
	public int getNumClaims(UUID uuid) {
		if(playerClaims.get(uuid) == null)
			playerClaims.put(uuid, new ArrayList<Pair>());
		return playerClaims.get(uuid).size();
	}
	
	public Pair unclaimLast(UUID uuid) {
		List<Pair> claims = playerClaims.get(uuid);
		return claims.get(claims.size() - 1);
	}
	
}