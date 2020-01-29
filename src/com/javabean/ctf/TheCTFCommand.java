package com.javabean.ctf;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class TheCTFCommand implements CommandExecutor{
	
	//arena name, arena
	private HashMap<String, Arena> arenaMap;
	private CTFGameManager gameManager;
	private Plugin plugin;
	
	public TheCTFCommand(HashMap<String, Arena> a, CTFGameManager ctfgm, Plugin p){
		arenaMap = a;
		gameManager = ctfgm;
		plugin = p;
	}
	
	//TODO update all argument possibilities on failure
	//TODO quickjoin to join the most full game, else random
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (sender instanceof Player) {
			Player player = (Player)sender;
			String commandSoFar = "/ctf";
			
			if(args.length == 0){
				if(player.isOp()){
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena | addarena | info | join | leave | removearena | start | timeleft>.");
				}
				else{
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <info | join | leave | start | timeleft>.");
				}
			}
			else if(args[0].equalsIgnoreCase("join")){
				if(args.length == 2){
					//arena does not exist
					if(arenaMap.get(args[1]) == null){
						player.sendMessage(ChatColor.RED + "Arena: " + args[1] + " does not exist.");
					}
					else{
						gameManager.attemptPlayerJoin(player, arenaMap.get(args[1]));
					}
				}
				else{
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " join command can have arguments <arena name>");
				}
			}
			else if(args[0].equalsIgnoreCase("leave")){
				Arena playerGameArena = gameManager.getPlayerGameArena(player);
				if(playerGameArena == null){
					player.sendMessage(ChatColor.RED + "You are not in a CTF game!");
				}
				else{
					//player leaves arena game
					gameManager.notifyPlayers(ChatColor.LIGHT_PURPLE + player.getName() + " left arena: " + playerGameArena.getName(), playerGameArena);
					gameManager.playerLeave(player, playerGameArena);
				}
			}
			else if(args[0].equalsIgnoreCase("team")){
				//gets what arena game they are in if they are in one
				Arena playerGameArena = gameManager.getPlayerGameArena(player);
				if(playerGameArena == null){
					player.sendMessage(ChatColor.RED + "You are not in a CTF game!");
				}
				else if(gameManager.isInProgress(playerGameArena)){
					player.sendMessage(ChatColor.RED + "You cannot switch teams while a game is in progress!");
				}
				else if(args.length == 2){
					//team does not exist
					if(playerGameArena.getTeam(args[1]) == null){
						player.sendMessage(ChatColor.RED + "Team: " + args[1] + " does not exist in arena" + playerGameArena + ".");
					}
					else{
						//switch player team
						gameManager.notifyPlayers(ChatColor.LIGHT_PURPLE + player.getName() + " joined team: " + args[1], playerGameArena);
						gameManager.setPlayerTeam(player, playerGameArena, playerGameArena.getTeam(args[1]));
					}
				}
				else{
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " team command can have arguments <team name>");
				}
			}
			else if(args[0].equalsIgnoreCase("start")){
				Arena playerGameArena = gameManager.getPlayerGameArena(player);
				if(playerGameArena == null){
					player.sendMessage(ChatColor.RED + "You are not in a CTF game!");
				}
				else if(gameManager.isInProgress(playerGameArena)){
					player.sendMessage(ChatColor.RED + "Game is already in progress!");
				}
//
//
//
//				TODO insert this back in upon further testing
//				COMMENTED OUT FOR PERSONAL TESING
//
//
//
//				else if(gameManager.getNumPlayersInArena(playerGameArena) < 2){
//					player.sendMessage(ChatColor.RED + "There must be at least two players to start a CTF game!");
//				}
				else{
					//start game
					gameManager.notifyPlayers(ChatColor.LIGHT_PURPLE + player.getName() + " started the game.", playerGameArena);
					gameManager.startArena(playerGameArena);
				}
			}
			else if(args[0].equalsIgnoreCase("timeleft")){
				Arena playerGameArena = gameManager.getPlayerGameArena(player);
				player.sendMessage(ChatColor.AQUA + "There is " + gameManager.timeLeft(playerGameArena) + " time left in arena: " + playerGameArena.getName() + ".");
			}
			else if(args[0].equalsIgnoreCase("info")){
				player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
				player.sendMessage(ChatColor.GOLD + "                 ***CTF Games Info***");
				if(gameManager.getNumGames() == 0){
					player.sendMessage(ChatColor.RED + "There are no games at the moment.");
					//TODO make this a quickjoin clickable message
				}
				else{
					gameManager.getInfo(player);
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Click to join!");
				}
				player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
			}
			else if(player.isOp()){
				if(args[0].equalsIgnoreCase("arena")){
					arenaSubCommand(player, commandSoFar + " " + args[0], args);
				}
				else if(args[0].equalsIgnoreCase("addarena")){
					addArenaSubCommand(player, commandSoFar + " " + args[0], args);
				}
				else if(args[0].equalsIgnoreCase("removearena")){
					removeArenaSubCommand(player, commandSoFar + " " + args[0], args);
				}
				else if(args[0].equalsIgnoreCase("spawn")){
					spawnSubCommand(player, commandSoFar + " " + args[0], args);
				}
				else{
					player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena | addarena | info | join | leave | removearena | start | timeleft>.");
				}
			}
			else{
				player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <info | join | leave | start | timeleft>.");
			}
		}
		return true;
	}
	
	private void arenaSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length >= 2){
			if(args[1].equalsIgnoreCase("addteam")){
				addTeamSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("info")){
				infoSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("removeteam")){
				removeTeamSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("setbound")){
				setBoundSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("addspawn")){
				addSpawnSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("removespawn")){
				removeSpawnSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("addflag")){
				addFlagSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("removeflag")){
				removeFlagSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("createsign")){
				createSignSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("removesign")){
				removeSignSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else if(args[1].equalsIgnoreCase("teamcolor")){
				teamColorSubCommand(player, commandSoFar + " " + args[1], args);
			}
			else{
				player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <addteam | info | removeteam | setbound | addspawn | removespawn | addflag | removeflag>.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <addteam | info | removeteam | setbound | addspawn | removespawn | addflag | removeflag>.");
		}
	}

	private void addArenaSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length == 1){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name>.");
		}
		else if(args.length == 2){
			//create new arena
			//name is specified, location is set to player location by default
			//arena does not exist yet, create it
			if(arenaMap.get(args[1]) == null){
				Location[] arenaLoc = {player.getLocation(), player.getLocation()};
				arenaMap.putIfAbsent(args[1], new Arena(args[1], arenaLoc));
				player.sendMessage(ChatColor.GREEN + "You added an arena named: " + args[1] + ".");
			}
			else{
				player.sendMessage(ChatColor.RED + "Arena: " + args[1] + " already exists.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void removeArenaSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length == 1){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name>.");
		}
		else if(args.length == 2){
			arenaMap.remove(args[1]);
			player.sendMessage(ChatColor.GREEN + "You removed the arena named: " + args[1] + ".");
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void addTeamSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 4){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <team name>.");
		}
		else if(args.length == 4){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//team in that arena does not exist, create it
			else if(arenaMap.get(args[2]).getTeam(args[3]) == null){
				arenaMap.get(args[2]).addTeam(new Team(args[3]));
				player.sendMessage(ChatColor.GREEN + "You added the team named: " + args[3] + " to arena: " + args[2] + ".");
			}
			//team in that arena already exists
			else{
				player.sendMessage(ChatColor.RED + "The team named: " + args[3] + " already exists in arena: " + args[2] + ".");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void infoSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length == 2){
			player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
			player.sendMessage(ChatColor.GOLD + "          ***General Arena Info***");
			if(arenaMap.size() == 0){
				player.sendMessage(ChatColor.RED + "No arenas exist.");
			}
			else{
				for(String arenaName : arenaMap.keySet()){
					player.sendMessage(ChatColor.GREEN + arenaName + ": " + arenaMap.get(arenaName).numTeams() + " teams, " +
							arenaMap.get(arenaName).numFlags() + " flags, and " +
							arenaMap.get(arenaName).numSpawns() + " spawns.");
				}
			}
			player.sendMessage(ChatColor.GOLD + "--------------------------------------------");
		}
		else if(args.length == 3){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			else{
				player.sendMessage(ChatColor.GREEN + args[2] + ": " + arenaMap.get(args[2]).numTeams() + " teams, " +
						arenaMap.get(args[2]).numFlags() + " flags, and " +
						arenaMap.get(args[2]).numSpawns() + " spawns.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void removeTeamSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 4){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <team name>.");
		}
		else if(args.length == 4){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//team does not exist
			else if(arenaMap.get(args[2]).getTeam(args[3]) == null){
				player.sendMessage(ChatColor.RED + "Team: " + args[3] + " does not exist in arena" + args[2] + ".");
			}
			else{
				arenaMap.get(args[2]).removeTeam(args[3]);
				player.sendMessage(ChatColor.GREEN + "You removed the team named: " + args[3] + " from arena: " + args[2] + ".");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void setBoundSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 4){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <0 | 1>.");
		}
		else if(args.length == 4){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			else{
				int bound = Integer.parseInt(args[3]);
				if(bound == 0 || bound == 1){
					arenaMap.get(args[2]).setBound(CaptureTheFlag.getBlockLookingAt(player), bound);
					player.sendMessage(ChatColor.GREEN + "You set bound: " + args[3] + " for arena: " + args[2] + ".");
				}
				else{
					player.sendMessage(ChatColor.RED + "Bound specified must be 0 or 1.");
				}
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void addSpawnSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 5){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <team name> <spawn name>.");
		}
		else if(args.length == 5){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//team does not exist
			else if(arenaMap.get(args[2]).getTeam(args[3]) == null){
				player.sendMessage(ChatColor.RED + "Team: " + args[3] + " does not exist for arena: " + args[2] + " does not exist.");
			}
			//spawn does not exist, create it
			else if(arenaMap.get(args[2]).getTeam(args[3]).getSpawn(args[4]) == null){
				arenaMap.get(args[2]).getTeam(args[3]).addSpawn(new Spawn(args[4], player.getLocation()));
				player.sendMessage(ChatColor.GREEN + "You added spawn: " + args[4] + " for team: " + args[3] + " for arena: " + args[2] + ".");
			}
			//spawn exists
			else{
				player.sendMessage(ChatColor.RED + "Spawn: " + args[4] + "already exists for team: " + args[3] + " does not exist for arena: " + args[2] + " does not exist.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void removeSpawnSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 5){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <team name> <spawn name>.");
		}
		else if(args.length == 5){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//team does not exist
			else if(arenaMap.get(args[2]).getTeam(args[3]) == null){
				player.sendMessage(ChatColor.RED + "Team: " + args[3] + " does not exist for arena: " + args[2] + " does not exist.");
			}
			//spawn does not exist
			else if(arenaMap.get(args[2]).getTeam(args[3]).getSpawn(args[4]) == null){
				player.sendMessage(ChatColor.RED + "Spawn: " + args[4] + "does not exist for team: " + args[3] + " does not exist for arena: " + args[2] + " does not exist.");
			}
			//spawn exists, delete it
			else{
				arenaMap.get(args[2]).getTeam(args[3]).removeSpawn(args[4]);
				player.sendMessage(ChatColor.GREEN + "You removed spawn: " + args[4] + " for team: " + args[3] + " for arena: " + args[2] + ".");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void addFlagSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 5){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <team name> <flag name>.");
		}
		else if(args.length == 5){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//team does not exist
			else if(arenaMap.get(args[2]).getTeam(args[3]) == null){
				player.sendMessage(ChatColor.RED + "Team: " + args[3] + " does not exist for arena: " + args[2] + " does not exist.");
			}
			//flag does not exist, create it
			else if(arenaMap.get(args[2]).getTeam(args[3]).getFlag(args[4]) == null){
				Location blockLookingAt = CaptureTheFlag.getBlockLookingAt(player);
				arenaMap.get(args[2]).getTeam(args[3]).addFlag(new Flag(args[4], blockLookingAt));
				player.sendMessage(ChatColor.GREEN + "You added flag: " + args[4] + "(" + blockLookingAt.getBlock().getType().name() + ") for team: " + args[3] + " for arena: " + args[2] + ".");
			}
			//flag exists
			else{
				player.sendMessage(ChatColor.RED + "Flag: " + args[4] + "already exists for team: " + args[3] + " does not exist for arena: " + args[2] + " does not exist.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void removeFlagSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length < 5){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <team name> <flag name>.");
		}
		else if(args.length == 5){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//team does not exist
			else if(arenaMap.get(args[2]).getTeam(args[3]) == null){
				player.sendMessage(ChatColor.RED + "Team: " + args[3] + " does not exist for arena: " + args[2] + " does not exist.");
			}
			//flag does not exist
			else if(arenaMap.get(args[2]).getTeam(args[3]).getFlag(args[4]) == null){
				player.sendMessage(ChatColor.RED + "Flag: " + args[4] + "does not exist for team: " + args[3] + " does not exist for arena: " + args[2] + " does not exist.");
			}
			//flag exists, delete it
			else{
				arenaMap.get(args[2]).getTeam(args[3]).removeFlag(args[4]);
				player.sendMessage(ChatColor.GREEN + "You removed flag: " + args[4] + " for team: " + args[3] + " for arena: " + args[2] + ".");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}

	private void createSignSubCommand(Player player, String commandSoFar, String[] args) {
		if(args.length == 2){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name>.");
		}
		else if(args.length == 3){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//create sign where the player is looking at
			else{
				arenaMap.get(args[2]).createJoinSign(player, plugin);
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void removeSignSubCommand(Player player, String commandSoFar, String[] args) {
		if(args.length == 2){
			Location lookingAt = CaptureTheFlag.getBlockLookingAt(player);
			boolean removed = false;
			for(String arenaName : arenaMap.keySet()){
				if(arenaMap.get(arenaName).isAJoinSign(lookingAt)){
					arenaMap.get(arenaName).removeJoinSign(lookingAt, plugin);
					removed = true;
				}
			}
			if(removed){
				player.sendMessage(ChatColor.GREEN + "Sign removed successfully.");
			}
			else{
				player.sendMessage(ChatColor.RED + "There is not join sign there.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void teamColorSubCommand(Player player, String commandSoFar, String[] args) {
		if(args.length == 2){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <arena name> <team name> <#000000>.");
		}
		else if(args.length == 3){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + args[2] + " command can have arguments: <team name> <#000000>.");
		}
		else if(args.length == 4){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + args[2] + args[3] + " command can have arguments: <#000000>.");
		}
		else if(args.length == 5){
			//arena does not exist
			if(arenaMap.get(args[2]) == null){
				player.sendMessage(ChatColor.RED + "Arena: " + args[2] + " does not exist.");
			}
			//team does not exist
			else if(arenaMap.get(args[2]).getTeam(args[3]) == null){
				player.sendMessage(ChatColor.RED + "Team: " + args[3] + " does not exist for arena: " + args[2] + ".");
			}
			//set team color
			else{
				arenaMap.get(args[2]).getTeam(args[3]).setHexColor(args[4]);
				player.sendMessage(ChatColor.GREEN + "Color set to " + args[4] + " team: " + args[3] + " for arena: " + args[2]
						+ ". Ensure the format of your color is the pound symbol and six hexadecimal digits! Ex: #000000");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
	
	private void spawnSubCommand(Player player, String commandSoFar, String[] args){
		if(args.length == 1){
			player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <disable | enable | set>.");
		}
		else if(args.length == 2){
			if(args[1].equalsIgnoreCase("disable")){
				CaptureTheFlag.hubEnabled = false;
				player.sendMessage(ChatColor.GREEN + "CTF hub spawn disabled.");
			}
			else if(args[1].equalsIgnoreCase("enable")){
				CaptureTheFlag.hubEnabled = true;
				player.sendMessage(ChatColor.GREEN + "CTF hub spawn enabled.");
			}
			else if(args[1].equalsIgnoreCase("set")){
				CaptureTheFlag.hubSpawn = new Location(player.getWorld(),
						player.getLocation().getX(),
						player.getLocation().getY(),
						player.getLocation().getZ(),
						player.getLocation().getYaw(),
						player.getLocation().getPitch());
				player.sendMessage(ChatColor.GREEN + "CTF hub spawn set.");
			}
			else{
				player.sendMessage(ChatColor.RED + "The " + commandSoFar + " command can have arguments: <disable | enable | set>.");
			}
		}
		else{
			player.sendMessage(ChatColor.RED + "Too many arguments specified for " + commandSoFar + " command.");
		}
	}
}
