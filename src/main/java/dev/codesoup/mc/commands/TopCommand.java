package dev.codesoup.mc.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.Nation;
import dev.codesoup.mc.PowerManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class TopCommand extends ModCommandBase {

	private final static String USAGE = "/top <players|nations>";
	
	public TopCommand(CustomMod mod) {
		super(mod, "top", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		_assert(params.length == 1 && (params[0].equals("players") || params[0].equals("nations")), ERR_INCORRECT_USAGE + USAGE);
		
		PowerManager pwm = mod.getPowerManager();
		
		if(params[0].equals("players")) {
			
			List<Pair<GameProfile, Integer>> top = new ArrayList<>();
			for(UUID uuid: pwm.getKeys()) {
				
				GameProfile profile = mod.getServer().getPlayerProfileCache().getProfileByUUID(uuid);
				int power = pwm.getTotalPower(uuid);
				
				if(top.size() == 0) {
					top.add(new ImmutablePair<>(profile, power));
				} else {
					
					for(int i = 0; i < top.size(); i++) {
						if(power > top.get(i).getRight()) {
							top.add(i, new ImmutablePair<>(profile, power));
							break;
						}
					}
					
					if(top.size() > 5) {
						top.remove(top.size() - 1);
					}
					
				}
				
			}

			String list = top.stream().map(pair -> String.format("%s (%d)", pair.getLeft().getName(), pair.getRight())).collect(Collectors.joining(", "));			
			sender.sendMessage(new TextComponentString(list));
			
		} else if(params[0] == "nations") {
		
			List<Pair<Nation, Integer>> top = new ArrayList<>();
			for(Nation nation: mod.getNationManager().getNations()) {
				
				int power = pwm.getTotalPower(nation);
				
				if(top.size() == 0) {
					top.add(new ImmutablePair<>(nation, power));
				} else {
					
					for(int i = 0; i < top.size(); i++) {
						if(power > top.get(i).getRight()) {
							top.add(i, new ImmutablePair<>(nation, power));
							break;
						}
					}
					
					if(top.size() > 5) {
						top.remove(top.size() - 1);
					}
					
				}
				
			}

			String list = top.stream().map(pair -> String.format("%s (%d)%s", pair.getLeft().getFmtName(), pair.getRight())).collect(Collectors.joining(", "));
			sender.sendMessage(new TextComponentString(list));
			
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
