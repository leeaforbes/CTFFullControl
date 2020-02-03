package com.javabean.ctf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CTFCommandTabCompleter implements TabCompleter{
	
	private CTFGameManager gameManager;
	private HashMap<String, Arena> arenaMap = new HashMap<String, Arena>();
	
	public CTFCommandTabCompleter(CTFGameManager ctfgm, HashMap<String, Arena> am){
		gameManager = ctfgm;
		arenaMap = am;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		LinkedList<String> options = new LinkedList<String>();
		if(sender instanceof Player){
			Player player = (Player)sender;
			if(args.length > 0){
				if(args[0].equalsIgnoreCase("info")){
					//do nothing
				}
				else if(args[0].equalsIgnoreCase("join")){
					if(args.length > 1){
						for(String arenaName : arenaMap.keySet()){
							options.add(arenaName);
						}
					}
				}
				else if(args[0].equalsIgnoreCase("leave")){
					//do nothing
				}
				else if(args[0].equalsIgnoreCase("start")){
					//do nothing
				}
				else if(args[0].equalsIgnoreCase("team")){
					if(args.length > 1){
						Arena playerArena = gameManager.getPlayerGameArena(player);
						if(playerArena != null){
							for(String teamName : playerArena.getTeams().keySet()){
								options.add(teamName);
							}
						}
					}
				}
				else if(args[0].equalsIgnoreCase("timeleft")){
					//do nothing
				}
				else if(player.isOp()){
					if(args[0].equalsIgnoreCase("addarena")){
						//do nothing
					}
					else if(args[0].equalsIgnoreCase("arena")){
						if(args.length > 1){
							if(args[1].equalsIgnoreCase("addflag")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										for(String teamName : arena.getTeams().keySet()){
											if(teamName.startsWith(args[3])){
												options.add(teamName);
											}
										}
									}
								}
							}
							else if(args[1].equalsIgnoreCase("addspawn")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										for(String teamName : arena.getTeams().keySet()){
											if(teamName.startsWith(args[3])){
												options.add(teamName);
											}
										}
									}
								}
							}
							else if(args[1].equalsIgnoreCase("addteam")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										for(String teamName : arena.getTeams().keySet()){
											if(teamName.startsWith(args[3])){
												options.add(teamName);
											}
										}
									}
								}
							}
							else if(args[1].equalsIgnoreCase("createsign") && args.length == 3){
								for(String arenaName : arenaMap.keySet()){
									options.add(arenaName);
								}
							}
							else if(args[1].equalsIgnoreCase("info") && args.length == 3){
								for(String arenaName : arenaMap.keySet()){
									options.add(arenaName);
								}
							}
							else if(args[1].equalsIgnoreCase("removeteam")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										for(String teamName : arena.getTeams().keySet()){
											if(teamName.startsWith(args[3])){
												options.add(teamName);
											}
										}
									}
								}
							}
							else if(args[1].equalsIgnoreCase("removespawn")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										for(String teamName : arena.getTeams().keySet()){
											if(teamName.startsWith(args[3])){
												options.add(teamName);
											}
										}
									}
								}
								else if(args.length == 5){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										Team team = arena.getTeam(args[3]);
										if(team != null){
											for(String spawnName : team.getSpawns().keySet()){
												if(spawnName.startsWith(args[4])){
													options.add(spawnName);
												}
											}
										}
									}
								}
							}
							else if(args[1].equalsIgnoreCase("removeflag")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										for(String teamName : arena.getTeams().keySet()){
											if(teamName.startsWith(args[3])){
												options.add(teamName);
											}
										}
									}
								}
								else if(args.length == 5){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										Team team = arena.getTeam(args[3]);
										if(team != null){
											for(String flagName : team.getFlags().keySet()){
												if(flagName.startsWith(args[4])){
													options.add(flagName);
												}
											}
										}
									}
								}
							}
							else if(args[1].equalsIgnoreCase("removesign")){
								//do nothing
							}
							else if(args[1].equalsIgnoreCase("setbound")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4){
									options.add("0");
									options.add("1");
								}
							}
							else if(args[1].equalsIgnoreCase("teamcolor")){
								if(args.length == 3){
									for(String arenaName : arenaMap.keySet()){
										if(arenaName.startsWith(args[2])){
											options.add(arenaName);
										}
									}
								}
								else if(args.length == 4){
									Arena arena = arenaMap.get(args[2]);
									if(arena != null){
										for(String teamName : arena.getTeams().keySet()){
											if(teamName.startsWith(args[3])){
												options.add(teamName);
											}
										}
									}
								}
								else if(args.length == 5){
									options.add("#000000");
									return options;
								}
							}
							else{
								String[] possible = {"addflag", "addspawn", "addteam", "createsign", "info", "removeteam", "removespawn", "removeflag", "removesign", "setbound", "teamcolor"};
								addIfStartsWith(options, possible, args[1]);
								return options;
							}
						}
					}
					else if(args[0].equalsIgnoreCase("removearena")){
						if(args.length == 2){
							for(String arenaName : arenaMap.keySet()){
								if(arenaName.startsWith(args[1])){
									options.add(arenaName);
								}
							}
						}
						else if(args.length == 3){
							Arena arena = arenaMap.get(args[2]);
							if(arena != null){
								for(String teamName : arena.getTeams().keySet()){
									if(teamName.startsWith(args[3])){
										options.add(teamName);
									}
								}
							}
						}
					}
					else if(args[0].equalsIgnoreCase("spawn")){
						if(args.length == 2){
							String[] possible = {"disable", "enable", "set"};
							addIfStartsWith(options, possible, args[1]);
						}
						return options;
					}
					else{
						String[] possible = {"info", "join", "leave", "start", "team", "timeleft"};
						addIfStartsWith(options, possible, args[0]);
						if(player.isOp()){
							String[] opPossible = {"addarena", "arena", "removearena", "spawn"};
							addIfStartsWith(options, opPossible, args[0]);
						}
						return options;
					}
				}
			}
		}
		return options;
	}
	
	private void addIfStartsWith(LinkedList<String> options, String[] possible, String arg){
		for(String option : possible){
			if(option.startsWith(arg)){
				options.add(option);
			}
		}
	}
}
