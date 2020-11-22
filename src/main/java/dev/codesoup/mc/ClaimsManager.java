package dev.codesoup.mc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ClaimsManager extends Manager {

	public static class Pair {
		
		public int A, B;
		
		public Pair(int A, int B) {
			this.A = A; 
			this.B = B;
		}
		
		@Override
		public int hashCode() {
			return A * 256 + B;
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof Pair) {
				Pair otherPair = (Pair)other;
				return otherPair.A == A && otherPair.B == B;
			} else {
				return false;
			}
		}
		
	}
	
	private Map<Pair, UUID> claims;
	
	public ClaimsManager(CustomMod mod) {
		super(mod);
		claims = new HashMap<Pair, UUID>();
	}
	
	// x and z are chunk coordinates
	public UUID getClaim(int x, int z) {
		return claims.get(new Pair(x, z));
	}
	
	public void setClaim(int x, int z, UUID uuid) {
		claims.put(new Pair(x, z), uuid);
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
	
}