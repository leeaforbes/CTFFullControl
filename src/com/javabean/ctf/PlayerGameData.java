package com.javabean.ctf;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerGameData{
	private Player player;
	private Location locationBeforeGame;
	private Team team;
	
	//enemy flag taken and brought to friendly flag
	private int flagsCaptured = 0;
	
	//enemy flags taken
	private int flagsStolen = 0;
	
	//friendly flag taken by enemy and slain enemy to return
	private int flagsSaved = 0;
	//different enemy flag intercepted by slaying enemy to steal
	private int flagsIntercepted = 0;
	private int kills = 0;
	private int deaths = 0;
	private double damageDealt = 0;
	
	private HashMap<String, Flag> holdingFlags = new HashMap<String, Flag>();
	
	public PlayerGameData(Player p, Team t){
		player = p;
		locationBeforeGame = new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), p.getLocation().getYaw(), p.getLocation().getPitch());
		team = t;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public Location getLocationBeforeGame(){
		return locationBeforeGame;
	}
	
	public Team getTeam(){
		return team;
	}
	
	public void setTeam(Team t){
		team = t;
	}
	
	public HashMap<String, Flag> getHoldingFlags(){
		return holdingFlags;
	}
	
	public void transferFlagsTo(PlayerGameData killer){
		ArrayList<String> saveFlags = new ArrayList<String>();
		ArrayList<String> interceptFlags = new ArrayList<String>();
		for(String flagName : holdingFlags.keySet()){
			//flag belongs to killer player's team, save it
			if(killer.getTeam().getFlag(flagName) != null){
				saveFlags.add(flagName);
			}
			//flag belongs to a different enemy, intercept it
			else{
				interceptFlags.add(flagName);
			}
		}
		//saves flags for team
		for(String flagName : saveFlags){
			dropFlag(holdingFlags.get(flagName));
			killer.saveFlag();
		}
		//sends reference of flags to killer, removes flags from this player
		for(String flagName : interceptFlags){
			killer.grabFlag(holdingFlags.remove(flagName));
			killer.interceptFlag();
		}
	}
	
	public void dropAllFlags(){
		//drop all flags
		ArrayList<String> avoidModificationWhileIterating = new ArrayList<String>();
		for(String flagName : holdingFlags.keySet()){
			avoidModificationWhileIterating.add(flagName);
		}
		for(String flagName: avoidModificationWhileIterating){
			Flag flag = holdingFlags.get(flagName);
			dropFlag(flag);
		}
	}
	
	//drops the flag by replacing the material of the block where the flag is located
	public void dropFlag(Flag flag){
		flag.getLocation().getBlock().setType(flag.getBlockMaterial());
		holdingFlags.remove(flag.getName());
	}
	
	//grab the flag by hitting the block or intercepting it from another player
	public void grabFlag(Flag flag){
		holdingFlags.put(flag.getName(), flag);
		flag.getLocation().getBlock().setType(Material.AIR);
	}
	
	public int numFlagsHolding(){
		return holdingFlags.size();
	}
	
	public int getFlagsCaptured(){
		return flagsCaptured;
	}
	
	public void captureFlag(){
		flagsCaptured++;
	}
	
	public int getFlagsStolen(){
		return flagsStolen;
	}
	
	public void stealFlag(){
		flagsStolen++;
	}
	
	public int getFlagsSaved() {
		return flagsSaved;
	}
	
	public void saveFlag(){
		flagsSaved++;
	}
	
	public int getFlagsIntercepted() {
		return flagsIntercepted;
	}
	
	public void interceptFlag(){
		flagsIntercepted++;
	}
	
	public int getKills() {
		return kills;
	}
	
	public void killSomeone(){
		kills++;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public void die(){
		deaths++;
	}
	
	public double getDamageDealt() {
		return damageDealt;
	}
	
	public void dealDamage(double amount){
		damageDealt += amount;
	}
	
	public void reset(){
		flagsCaptured = 0;
		flagsStolen = 0;
		flagsSaved = 0;
		flagsIntercepted = 0;
		kills = 0;
		deaths = 0;
		damageDealt = 0;
		holdingFlags.clear();
	}
}
