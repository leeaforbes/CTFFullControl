package com.javabean.ctf;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CTFListener implements Listener{
	
	private CTFGameManager gameManager;
	private HashMap<String, Arena> arenaMap = new HashMap<String, Arena>();
	
	public CTFListener(CTFGameManager ctfgm, HashMap<String, Arena> am){
		gameManager = ctfgm;
		arenaMap = am;
	}
	
	//prevents the player from losing hunger
	@EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event){
        event.setCancelled(true);
    }
	
	//item durability will not decrease on use
	@EventHandler
	public void onPlayerItemDamage(PlayerItemDamageEvent event){
		event.setCancelled(true);
	}
	
	//TODO going out of bounds kills you
	
	//only allow damage between two players if both in game and not on the same team
	@EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntityType() == EntityType.PLAYER){
			Player victim = (Player) event.getEntity();
			Arena victimGameArena = gameManager.getPlayerGameArena(victim);
			if (event.getDamager() instanceof Player ||event.getDamager() instanceof Arrow){
				Player attacker = event.getDamager() instanceof Player ? (Player) event.getDamager() : (Player)(((Arrow)event.getDamager()).getShooter());
//                attacker.sendMessage(ChatColor.GREEN + "You attacked " + victim.getName() + ".");
//                victim.sendMessage(ChatColor.RED + attacker.getName() + " attacked you!");
                Arena attackerGameArena = gameManager.getPlayerGameArena(attacker);
                //attacker and victim not in game
				if(attackerGameArena == null && attackerGameArena == null){
					event.setCancelled(true);
				}
				//game not in progress yet
				else if(!gameManager.isInProgress(attackerGameArena)){
					event.setCancelled(true);
				}
				//attacker and victim on the same team
				else if(gameManager.getPlayerGameData(attacker, victimGameArena).getTeam().getName().equals(gameManager.getPlayerGameData(victim, victimGameArena).getTeam().getName())){
					event.setCancelled(true);
				}
				//attack deals damage to victim
				else{
					gameManager.getPlayerGameData(attacker, attackerGameArena).dealDamage(event.getDamage());
				}
            }
        }
    }
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event){
		if (event.getEntityType() == EntityType.PLAYER){
			Player victim = (Player) event.getEntity();
			Arena victimGameArena = gameManager.getPlayerGameArena(victim);
			if (event.getCause() == DamageCause.FALL){
	        	if(victimGameArena == null || !gameManager.isInProgress(victimGameArena)){
	        		event.setCancelled(true);
	        	}
	        }
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getHand() == EquipmentSlot.HAND && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)){
			flagClickEvent(event);
			signClickEvent(event);
		}
	}
	
	private void flagClickEvent(PlayerInteractEvent event){
		Arena playerArena = gameManager.getPlayerGameArena(event.getPlayer());
		if(playerArena != null && gameManager.isInProgress(playerArena)){
			Block block = event.getClickedBlock();
			Team teamOfBlockHit = playerArena.getTeamOfFlagAt(block.getLocation());
			PlayerGameData playerGameData = gameManager.getPlayerGameData(event.getPlayer(), playerArena);
			Team playerTeam = playerGameData.getTeam();
			if(teamOfBlockHit != null){
				//if they hit their own flag
				if(playerTeam.getName().equals(teamOfBlockHit.getName())){
					//have enemy flag
					if(playerGameData.numFlagsHolding() > 0){
						captureFlags(playerGameData);
					}
					else{
						event.getPlayer().sendMessage(ChatColor.GREEN + "This is your flag! Hit again when you have the enemy flag with you!");
					}
				}
				//if they hit enemy flag
				else{
					Flag flagHit = teamOfBlockHit.getFlagAtLocation(block.getLocation());
					playerGameData.grabFlag(flagHit);
					playerGameData.stealFlag();
					gameManager.notifyPlayers(ChatColor.GOLD + "" + ChatColor.BOLD + playerGameData.getPlayer().getName() + " took " + teamOfBlockHit.getName() + " team's " + flagHit.getName() + " flag!", playerArena);
					event.getPlayer().sendMessage(ChatColor.GREEN + "Bring the flag back to your team's flag!");
				}
			}
		}
	}
	
	private void signClickEvent(PlayerInteractEvent event){
		if(Arena.isASign(event.getClickedBlock().getType())){
			BlockState signBlockState = event.getClickedBlock().getState();
			if(signBlockState.hasMetadata("joinarena")){
				//send player to join that arena if possible
				String arenaName = signBlockState.getMetadata("joinarena").get(0).asString();
				gameManager.attemptPlayerJoin(event.getPlayer(), arenaMap.get(arenaName));
			}
		}
	}
	
	@EventHandler
	public void onBreakBlock(BlockBreakEvent event){
		//if player is not OP
		if(!event.getPlayer().isOp()){
			event.setCancelled(true);
		}
		//player is op and in game
		else if(gameManager.getPlayerGameArena(event.getPlayer()) != null){
			event.setCancelled(true);
		}
		//player not in creative mode
		else if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlaceBlock(BlockPlaceEvent event){
		//if player is not OP
		if(!event.getPlayer().isOp()){
			event.setCancelled(true);
		}
		//player is op and in game
		else if(gameManager.getPlayerGameArena(event.getPlayer()) != null){
			event.setCancelled(true);
		}
	}
	
	public void captureFlags(PlayerGameData playerGameData){
		Arena playerArena = gameManager.getPlayerGameArena(playerGameData.getPlayer());
		for(String flagName : playerGameData.getHoldingFlags().keySet()){
			Flag flag = playerGameData.getHoldingFlags().get(flagName);
			Team teamFlagCaptured = playerArena.getTeamOfFlagAt(flag.getLocation());
			gameManager.notifyPlayers(ChatColor.GOLD + "" + ChatColor.BOLD + playerGameData.getPlayer().getName() + " captured " + teamFlagCaptured.getName() + " team's " + flag.getName() + " flag!", playerArena);
			//update player stats for flag capture
			playerGameData.captureFlag();
			//update team stats for flag capture
			gameManager.getGame(playerArena).getTeamGameData(playerGameData.getTeam().getName()).captureFlag();;
		}
		//drop all flags
		playerGameData.dropAllFlags();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		if(CaptureTheFlag.hubEnabled){
			event.getPlayer().teleport(CaptureTheFlag.hubSpawn);
		}
		event.setJoinMessage(ChatColor.GREEN + "" + event.getPlayer().getName() + "" + ChatColor.DARK_GREEN + " joined the server.");
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		//force player to leave game if they are in one to avoid combat logging and holding flags when disconnected
		if(gameManager.getPlayerGameArena(event.getPlayer()) != null){
			gameManager.playerLeave(event.getPlayer(), gameManager.getPlayerGameArena(event.getPlayer()));
		}
		event.setQuitMessage(ChatColor.RED + "" + event.getPlayer().getName() + "" + ChatColor.DARK_RED + " left the server.");
	}
	
	//drop flag on player death
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		if (event.getEntity() instanceof Player){
            Player victim = (Player) event.getEntity();
            Arena victimArena = gameManager.getPlayerGameArena(victim);
            PlayerGameData victimGameData = gameManager.getPlayerGameData(victim, victimArena);
            if (event.getEntity().getKiller() instanceof Player){
                Player attacker = (Player)event.getEntity().getKiller();
                PlayerGameData attackerGameData = gameManager.getPlayerGameData(attacker, victimArena);
                gameManager.notifyPlayers(ChatColor.DARK_GREEN + attacker.getName() + " killed " + victim.getName() + ".", victimArena);
                attackerGameData.killSomeone();
                victimGameData.die();
                if(victimGameData.numFlagsHolding() > 0){
                	victimGameData.transferFlagsTo(attackerGameData);
                	gameManager.notifyPlayers(ChatColor.DARK_GREEN + attacker.getName() + " intercepted " + victim.getName() + "'s flags!", victimArena);
                }
            }
            else{
            	event.setDeathMessage(ChatColor.LIGHT_PURPLE + victim.getName() + " died" + (victimGameData.numFlagsHolding() > 0 ? " and flags were returned" : "") + ".");
            }
            //force the player to drop all their flags if haven't already
            victimGameData.dropAllFlags();
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		//must call this so that player does not get sent to regular game spawn
		Location location = sendToHubOrGame(event.getPlayer());
		if(location != null){
			event.setRespawnLocation(location);
			event.getPlayer().teleport(location);
		}
	}
	
	private Location sendToHubOrGame(Player player){
		Arena playerArena = gameManager.getPlayerGameArena(player);
		//not in game, send to hub
		if(playerArena == null){
			return CaptureTheFlag.hubEnabled ? CaptureTheFlag.hubSpawn : null;
		}
		//in game, send to spawn in game
		else{
			player.getInventory().remove(Material.ARROW);
			player.getInventory().addItem(new ItemStack(Material.ARROW, 5));
			return gameManager.getPlayerGameData(player, playerArena).getTeam().getRandomSpawn().getLocation();
		}
	}
}
