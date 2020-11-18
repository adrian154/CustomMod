package dev.codesoup.mc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ClaimsManager {

	private static final String PATH = "claims.dat";
	
	private Map<Pair<Integer, Integer>, UUID> claims;
	
	public ClaimsManager() throws IOException {
		claims = new HashMap<Pair<Integer, Integer>, UUID>();
		loadFromFile();
	}
	
	private void loadFromFile() throws IOException {
		
		File file = new File(PATH);
		if(!file.exists()) {
			file.createNewFile();
		}
		
	}
	
	// x and z are chunk coordinates
	public UUID getClaim(int x, int z) {
		Pair<Integer, Integer> pair = new MutablePair<Integer, Integer>(x, z);
		return claims.get(pair);
	}
	
	public boolean shouldProtect(World world, BlockPos pos, UUID uuid) {
		
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		UUID claim = getClaim(chunk.x, chunk.z);
		
		if(claim == null) {
			return false;
		} else {
			
		}
		
	}
	
}