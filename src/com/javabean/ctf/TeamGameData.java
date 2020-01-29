package com.javabean.ctf;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class TeamGameData {
	
	private Team team;
	
	//enemy flag taken and brought to friendly flag
	private int flagsCaptured = 0;
	
	//player name, player
	HashMap<String, Player> players = new HashMap<String, Player>();
	
	public TeamGameData(Team t){
		team = t;
	}
	
	public Team getTeam(){
		return team;
	}
	
	public void addPlayer(Player player){
		players.putIfAbsent(player.getName(), player);
	}
	
	public void removePlayer(Player player){
		players.remove(player.getName());
	}
	
	public Player getPlayer(String playerName){
		return players.get(playerName);
	}
	
	public int getNumPlayers(){
		return players.size();
	}
	
	public int getFlagsCaptured(){
		return flagsCaptured;
	}
	
	public void captureFlag(){
		flagsCaptured++;
	}
	
	public void reset(){
		flagsCaptured = 0;
	}
}
