package com.javabean.ctf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Timer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CTFGameManager implements ActionListener{
	//map with arena name key, CTF game value to ensure only one game per arena
	private HashMap<String, CTFGame> games = new HashMap<String, CTFGame>();
	
	//the plugin
	Plugin plugin;
	
	//ticks once every 10 seconds to remove any empty games
	Timer gameGarbageCollector = new Timer(10000, this);
	
	public CTFGameManager(Plugin p){
		plugin = p;
		gameGarbageCollector.setRepeats(true);
		gameGarbageCollector.setInitialDelay(0);
		gameGarbageCollector.start();
	}
	
	public void createGame(Arena arena){
		//called whenever player joins and game does not exist yet
		games.putIfAbsent(arena.getName(), new CTFGame(arena, plugin));
	}
	
	//called whenever one player left or time runs out
	public void deleteGame(Arena arena){
		games.remove(arena.getName());
	}
	
	public void attemptPlayerJoin(Player player, Arena arena){
		//arena not playable
		if(!arena.isPlayable()){
			player.sendMessage(ChatColor.RED + "Arena: " + arena.getName() + " is not playable yet.");
		}
		//already in a game
		else if(getPlayerGameArena(player) != null){
			player.sendMessage(ChatColor.RED + "You are already in arena: " + arena.getName() + ". Leave with /ctf leave.");
		}
		//arena already has a game going
		else if(isInProgress(arena)){
			player.sendMessage(ChatColor.RED + "Arena: " + arena.getName() + " has a game in progress.");
		}
		else{
			//allow player to join arena game
			playerJoin(player, arena);
		}
	}
	
	public void playerJoin(Player player, Arena arena){
		if(games.get(arena.getName()) == null){
			createGame(arena);
		}
		games.get(arena.getName()).playerJoin(player);
		player.teleport(games.get(arena.getName()).getPlayerGameData(player.getName()).getTeam().getRandomSpawn().getLocation());
		notifyPlayers(ChatColor.LIGHT_PURPLE + player.getName() + " joined arena: " + arena.getName() + ".", arena);
	}
	
	public void playerLeave(Player player, Arena arena){
		games.get(arena.getName()).playerLeave(player);
		if(games.get(arena.getName()).getNumPlayers() == 0){
			//no more players playing, so delete the game
			deleteGame(arena);
		}
	}
	
	public void setPlayerTeam(Player player, Arena arena, Team team){
		games.get(arena.getName()).setPlayerTeam(player, team);
	}
	
	public CTFGame getGame(Arena arena){
		return games.get(arena.getName());
	}
	
	//returns arena of game player is in, otherwise null
	public Arena getPlayerGameArena(Player player){
		//check each arena with a game going on
		for(String arenaName : games.keySet()){
			if(games.get(arenaName).isPlayerInGame(player)){
				//player found in a game
				return games.get(arenaName).getArena();
			}
		}
		return null;
	}
	
	public PlayerGameData getPlayerGameData(Player player, Arena arena){
		return games.get(arena.getName()).getPlayerGameData(player.getName());
	}
	
	public String timeLeft(Arena arena){
		return games.get(arena.getName()).timeLeft();
	}
	
	public int getNumPlayersInArena(Arena arena){
		return games.get(arena.getName()).getNumPlayers();
	}
	
	public int getNumGames(){
		return games.size();
	}
	
	public void startArena(Arena arena){
		games.get(arena.getName()).start();
	}
	
	public boolean isInProgress(Arena arena){
		if(games.get(arena.getName()) == null){
			return false;
		}
		return games.get(arena.getName()).isInProgress();
	}
	
	public void notifyPlayers(String message, Arena arena){
		games.get(arena.getName()).notifyPlayers(message);
	}
	
	public void getInfo(Player player){
		for(String arenaName : games.keySet()){
			TextComponent message = new TextComponent((games.get(arenaName).isInProgress() ? ChatColor.RED : ChatColor.GREEN) + "\u25CF " + ChatColor.GREEN + arenaName + ": " + games.get(arenaName).getInfo());
			if(games.get(arenaName).isInProgress()){
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
						ChatColor.RED + arenaName + " is in progress").create()));
			}
			else{
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
						ChatColor.DARK_GREEN + "" + ChatColor.MAGIC + "|" + ChatColor.GREEN + "Join " + arenaName + ChatColor.DARK_GREEN + "" + ChatColor.MAGIC + "|").create()));
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ctf join " + arenaName));
			}
			player.spigot().sendMessage(message);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == gameGarbageCollector){
			for(String arenaName : games.keySet()){
				if(games.get(arenaName).getNumPlayers() == 0){
					deleteGame(games.get(arenaName).getArena());
				}
			}
		}
	}
}
