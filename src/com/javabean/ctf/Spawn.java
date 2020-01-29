package com.javabean.ctf;

import org.bukkit.Location;

public class Spawn {
	private String spawnName;
	private Location location;
	
	//addspawn and loading from a file
	public Spawn(String name, Location loc){
		spawnName = name;
		location = loc;
	}
	
	public String getName(){
		return spawnName;
	}
	
	public void setName(String name){
		spawnName = name;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public void setLocation(Location loc){
		location = loc;
	}
	
	@Override
	public String toString(){
		return "Spawn: " + spawnName + " " + location.toString();
	}
}
