package com.javabean.ctf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class Arena {
	private String arenaName;
	private Location[] bounds = new Location[2];
	
	//team name, Team
	//used for O(1) get team
	private HashMap<String, Team> teams = new HashMap<String, Team>();
	
	//sign locations
	private LinkedList<Location> signLocations = new LinkedList<Location>();
	
	public static Material[] wallSignTypes = {Material.OAK_WALL_SIGN, Material.ACACIA_WALL_SIGN, Material.BIRCH_WALL_SIGN, Material.DARK_OAK_WALL_SIGN, Material.JUNGLE_WALL_SIGN, Material.SPRUCE_WALL_SIGN};
	
	//for use with addarena
	public Arena(String name, Location[] b){
		//might need to add some default info to avoid errors later
		this(name, b, new HashMap<String, Team>());
	}
	
	//loading from file
	public Arena(String name, Location[] b, HashMap<String, Team> t){
		arenaName = name;
		bounds = b;
		teams = t;
	}
	
	public void setBound(Location loc, int index){
		bounds[index] = loc;
	}
	
	public void addTeam(Team team){
		teams.put(team.getName(), team);
	}
	
	public void removeTeam(String teamName){
		teams.remove(teamName);
	}
	
	public Team getTeam(String name){
		return teams.get(name);
	}
	
	public String getName(){
		return arenaName;
	}
	
	public Location[] getBounds(){
		return bounds;
	}
	
	public HashMap<String, Team> getTeams(){
		return teams;
	}
	
	//call this before allowing a players to play in this arena
	public boolean isPlayable(){
		if(teams.size() < 2){
			return false;
		}
		for(String teamName : teams.keySet()){
			if(!teams.get(teamName).isPlayable()){
				return false;
			}
		}
		return true;
	}
	
	public int numTeams(){
		return teams.size();
	}
	
	public int numFlags(){
		int count = 0;
		for(String teamName : teams.keySet()){
			count += teams.get(teamName).numFlags();
		}
		return count;
	}
	
	public Team getTeamOfFlagAt(Location location){
		for(String teamName : teams.keySet()){
			if(teams.get(teamName).hasFlagAt(location)){
				return teams.get(teamName);
			}
		}
		return null;
	}
	
	public int numSpawns(){
		int count = 0;
		for(String teamName : teams.keySet()){
			count += teams.get(teamName).numSpawns();
		}
		return count;
	}
	
	public void createJoinSign(Player player, Plugin plugin){
		if(createJoinSign(CaptureTheFlag.getBlockLookingAt(player), plugin)){
			player.sendMessage(ChatColor.GREEN + "Sign created successfully.");
		}
		else{
			player.sendMessage(ChatColor.RED + "You must be looking at an existing sign.");
		}
	}
	
	//returns true if it is a sign and created successfully
	public boolean createJoinSign(Location lookingAt, Plugin plugin){
		//if is a sign
		if(isASign(lookingAt.getBlock().getType())){
			Block signBlock = lookingAt.getBlock();
			BlockState signBlockState = signBlock.getState();
			Sign sign = (Sign)signBlockState;
			sign.setLine(0, ChatColor.DARK_RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Join");
			sign.setLine(1, ChatColor.AQUA + "" + ChatColor.BOLD + "" + getName());
			sign.setLine(2, ChatColor.DARK_GREEN + "" + numTeams() + " teams");
			sign.setLine(3, ChatColor.DARK_GREEN + "" + numFlags() + " flags");
			sign.setMetadata("joinarena", new FixedMetadataValue(plugin, getName()));
			sign.update();
			signBlockState.update();
			signLocations.add(lookingAt);
			return true;
		}
		return false;
	}
	
	public void removeJoinSign(Location lookingAt, Plugin plugin){
		Iterator<Location> signIterator = signLocations.iterator();
		while(signIterator.hasNext()){
			Location signLocation = signIterator.next();
			if(lookingAt.getWorld().getName() == signLocation.getWorld().getName() && lookingAt.getX() == signLocation.getX() && lookingAt.getY() == signLocation.getY() && lookingAt.getZ() == signLocation.getZ()){
				signLocation.getBlock().getState().removeMetadata("joinarena", plugin);
				signIterator.remove();
			}
		}
	}
	
	public boolean isAJoinSign(Location lookingAt){
		for(Location signLocation : signLocations){
			if(lookingAt.getWorld().getName() == signLocation.getWorld().getName() && lookingAt.getX() == signLocation.getX() && lookingAt.getY() == signLocation.getY() && lookingAt.getZ() == signLocation.getZ()){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isASign(Material materialInQuestion){
		for(Material wallSignType : wallSignTypes){
			if(wallSignType == materialInQuestion){
				return true;
			}
		}
		return false;
	}
	
	public LinkedList<Location> getSigns(){
		
		return signLocations;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Arena: ");
		sb.append(arenaName);
		sb.append(", Bounds: ");
		for(Location loc : bounds){
			sb.append(loc);
			sb.append(", ");
		}
		sb.append("Teams: ");
		for(String team : teams.keySet()){
			sb.append(teams.get(team).toString());
			sb.append(", ");
		}
		return sb.toString();
	}
}
