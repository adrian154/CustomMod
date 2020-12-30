package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.codesoup.mc.commands.ProtectCommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ClaimsManager extends RequiresMod {
	
	private transient Map<XZPair, UUID> claims;
	private Map<UUID, List<XZPair>> playerClaims;
	
	public ClaimsManager(CustomMod mod) {
		super(mod);
		claims = new HashMap<XZPair, UUID>();
		playerClaims = new HashMap<UUID, List<XZPair>>();
	}

	public void postInit() {
		for(UUID uuid: playerClaims.keySet()) {
			List<XZPair> list = playerClaims.get(uuid);
			for(XZPair pair: list) {
				claims.put(pair, uuid);
			}
		}
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
	
	public boolean shouldProtect(World world, BlockPos pos, EntityPlayer player) {
		
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		UUID claim = getClaim(chunk.x, chunk.z);
		
		// obviously if no one has claimed it it's up for grabs
		if(claim == null || mod.getCommands().GOD_COMMAND.isGodmode(player)) {
			return false;
		}
		
		// chunks claimed by a special UUID are accessible only to ops
		if(claim.equals(ProtectCommand.PROTECTED_UUID)) {
			return !player.canUseCommand(4, "");
		}
		
		// If the player is offline...
		if(mod.getPlayer(claim) == null) {
		
			// If they are allied, don't protect. Otherwise, if they are allied, protect.
			return !mod.getNationManager().sameNation(player.getUniqueID(), claim);
		
		} else {
			
			// otherwise, there are no protections
			return false;
			
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

	public void claim(EntityPlayer player, Chunk chunk, boolean passive) {
		
		if(getClaim(chunk.x, chunk.z) == null && mod.getPowerManager().removeClaimPower(player, 1)) {
			setClaim(chunk.x, chunk.z, player.getUniqueID());
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Chunk claimed!"));
		} else {
			if(!passive) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "This chunk is already claimed."));
			}
		}
		
	}
	
}