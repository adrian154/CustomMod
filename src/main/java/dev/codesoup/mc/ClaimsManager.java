package dev.codesoup.mc;

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
	}
	
	// x and z are chunk coordinates
	public UUID getClaim(int x, int z) {
		return claims.get(new Pair(x, z));
	}
	
	public void setClaim(int x, int z, UUID uuid) {
		Pair pair = new Pair(x, z);
		claims.put(pair, uuid);
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
		
		if(claim == null) {
			return false;
		} else {
			return mod.getServer().getPlayerList().getPlayerByUUID(claim) == null;
		}
		
	}
	
	public List<Pair> getClaims(UUID uuid) {
		return playerClaims.get(uuid);
	}
	
	public Pair unclaimLast(UUID uuid) {
		List<Pair> claims = playerClaims.get(uuid);
		return claims.get(claims.size() - 1);
	}
	
}