package com.javabean.ctf;

import java.util.HashMap;

import org.bukkit.Location;

public class Team {
	private String teamName;
	private String hexColor = "#000000";
	
	//flag name, Flag
	//used for O(1) get flag
	private HashMap<String, Flag> flags = new HashMap<String, Flag>();
	
	//spawn name, Spawn
	//used for O(1) get spawn
	private HashMap<String, Spawn> spawns = new HashMap<String, Spawn>();
	
	//for use with addteam
	public Team(String name){
		teamName = name;
	}
	
	//loading from file
	public Team(String name, HashMap<String, Flag> f, HashMap<String, Spawn> s, String color){
		teamName = name;
		flags = f;
		spawns = s;
		hexColor = color;
	}
	
	public void addSpawn(Spawn spawn){
		spawns.put(spawn.getName(), spawn);
	}
	
	public void removeSpawn(String spawnName){
		spawns.remove(spawnName);
	}
	
	public void addFlag(Flag flag){
		flags.put(flag.getName(), flag);
	}
	
	public void removeFlag(String flagName){
		flags.remove(flagName);
	}
	
	public String getName(){
		return teamName;
	}
	
	public void setName(String name){
		teamName = name;
	}
	
	public String getHexColor(){
		return hexColor;
	}
	
	public void setHexColor(String color){
		hexColor = color;
	}
	
	public Flag getFlag(String name){
		return flags.get(name);
	}
	
	public Flag getFlagAtLocation(Location location){
		for(String flagName : flags.keySet()){
			if(flags.get(flagName).getLocation().equals(location)){
				return flags.get(flagName);
			}
		}
		return null;
	}
	
	public boolean hasFlagAt(Location location){
		for(String flagName : flags.keySet()){
			if(flags.get(flagName).getLocation().equals(location)){
				return true;
			}
		}
		return false;
	}
	
	public HashMap<String, Flag> getFlags(){
		return flags;
	}
	
	public Spawn getSpawn(String name){
		return spawns.get(name);
	}
	
	public HashMap<String, Spawn> getSpawns(){
		return spawns;
	}
	
	public boolean isPlayable(){
		return flags.size() >= 1 && spawns.size() >= 1;
	}
	
	public int numFlags(){
		return flags.size();
	}
	
	public int numSpawns(){
		return spawns.size();
	}
	
	public Spawn getRandomSpawn(){
		int random = (int)(Math.random() * spawns.size());
		int count = 0;
		for(String spawnName : spawns.keySet()){
			if(random == count){
				return spawns.get(spawnName);
			}
			count++;
		}
		//should never happen
		return null;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Team: ");
		sb.append(teamName);
		sb.append("(" + hexColor + ")");
		sb.append(", Flags: ");
		for(String flag: flags.keySet()){
			sb.append(flags.get(flag).toString());
			sb.append(", ");
		}
		sb.append("Spawns: ");
		for(String spawn: spawns.keySet()){
			sb.append(spawns.get(spawn).toString());
			sb.append(", ");
		}
		return sb.toString();
	}
}
