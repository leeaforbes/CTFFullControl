package com.javabean.ctf;

import org.bukkit.Location;
import org.bukkit.Material;

public class Flag {
	private String flagName;
	private Location location;
	private Material blockMaterial;
	
	//addflag and loading from file
	public Flag(String name, Location loc){
		setName(name);
		setLocation(loc);
		setBlockMaterial(location.getBlock().getType());
	}
	
	public String getName(){
		return flagName;
	}
	
	public void setName(String name){
		flagName = name;
	}
	
	public Material getBlockMaterial(){
		return blockMaterial;
	}
	
	public void setBlockMaterial(Material bm){
		blockMaterial = bm;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public void setLocation(Location loc){
		location = loc;
	}
	
	@Override
	public String toString(){
		return "Flag: " + flagName + " " + location.toString();
	}
}
