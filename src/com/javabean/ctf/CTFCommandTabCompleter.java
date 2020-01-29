package com.javabean.ctf;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CTFCommandTabCompleter implements TabCompleter{
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		LinkedList<String> options = new LinkedList<String>();
		if(args.length == 1){
			if(args[0].equals("arena")){
				if(args.length == 2){
					if(args[1].equals("setbound")){
						options.add("0");
						options.add("1");
						return options;
					}
					else if(args[1].equals("team")){
						options.add("addflag");
						options.add("addspawn");
						options.add("removeflag");
						options.add("removespawn");
						return options;
					}
					else{
						return options;
					}
				}
				else{
					options.add("addteam");
					options.add("info");
					options.add("removeteam");
					options.add("setbound");
					options.add("team");
					return options;
				}
			}
			else if(args[0].equals("addarena")){
				if(args.length == 0){
					return options;
				}
			}
			else if(args[0].equals("removearena")){
				if(args.length == 0){
					return options;
				}
			}
			else{
				options.add("addarena");
				options.add("arena");
				options.add("removearena");
				return options;
			}
		}
		return options;
	}
}
