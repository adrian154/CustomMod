package dev.codesoup.mc.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.Nation;
import dev.codesoup.mc.PowerManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TopCommand extends ModCommandBase {

	private final static String USAGE = "/top [players|nations]";
	
	public TopCommand(CustomMod mod) {
		super(mod, "top", 0);
	}
	
	private void addToTop(List<Pair<String, Integer>> top, String str, int power, int max) {
	
		if(top.size() == 0) {
			top.add(new ImmutablePair<>(str, power));
			return;
		} else {
			for(int i = 0; i < top.size(); i++) {
				if(top.get(i).getRight() <= power) {
					top.add(i, new ImmutablePair<>(str, power));
					return;
				}
			}
		}
			
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		_assert(params.length <= 1 && params.length == 0 || (params[0].equals("players") || params[0].equals("nations")), ERR_INCORRECT_USAGE + USAGE);
		
		PowerManager pwm = mod.getPowerManager();
		
		List<Pair<String, Integer>> top = new ArrayList<>();
		if(params.length ==  0 || params[0].equals("players")) {
			for(UUID uuid: pwm.getKeys()) {
				addToTop(
					top,
					mod.getNationManager().getName(mod.getProfile(uuid)), 
					pwm.getTotalPower(uuid),
					5
				);
			}
		}
		
		if(params.length == 0 || params[0].equals("nations")) {
			for(Nation nation: mod.getNationManager().getNations()) {
				addToTop(
					top,
					nation.getFmtName(),
					pwm.getTotalPower(nation),
					5
				);
			}
		}
		
		String str = IntStream.range(0, top.size())
			.mapToObj(i -> {
				Pair<String, Integer> pair = top.get(i);
				return String.format(
					"%s#%d - %s%s%s (%d)",
					TextFormatting.GRAY,
					i + 1,
					TextFormatting.RESET,
					pair.getLeft(),
					TextFormatting.GRAY,
					pair.getRight()
				);
			})
			.collect(Collectors.joining("\n"));
		
		sender.sendMessage(new TextComponentString(String.format("%s=== Top %d %s ===", TextFormatting.GRAY, 5, params.length == 0 ? "overall" : params[0])));
		sender.sendMessage(new TextComponentString(str));
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
