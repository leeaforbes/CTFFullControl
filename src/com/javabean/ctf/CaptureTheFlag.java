package com.javabean.ctf;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CaptureTheFlag extends JavaPlugin{
	
	private HashMap<String, Arena> arenaMap = new HashMap<String, Arena>();
	private CTFGameManager gameManager = new CTFGameManager(this);
	
	//spawn location when joining server
	public static Location hubSpawn;
	//spawn hub spawn enabled
	public static boolean hubEnabled;
	
	// Fired when plugin is first enabled
	@Override
	public void onEnable() {
		//read arena data from XML file
		parseXMLArenaData();
		
		//read sign data from XML file
		parseXMLSignData();
		
		//read various game data from XML file
		parseXMLGameData();
		
		//creates commands
		getCommand("ctf").setExecutor(new TheCTFCommand(arenaMap, gameManager, this));
		//getCommand("ctf").setTabCompleter(new CTFCommandTabCompleter());
		
		//event listener
		getServer().getPluginManager().registerEvents(new CTFListener(gameManager, arenaMap), this);
		
		//TODO commit to git from command line
		//THIS IS NOW DONE IN parseXMLGameData()
		//hubSpawn = new Location(Bukkit.getServer().getWorlds().get(0), -285.5, 76.00, -132.5, 0, 0);
		
		//plugin enabled successfully
		getLogger().info("-------------------------------------");
		getLogger().info(getClass().getSimpleName() + " enabled!");
		getLogger().info("-------------------------------------");
	}
	
	// Fired when plugin is disabled
	@Override
	public void onDisable() {
		//rewrite arena data to XML file
		writeArenaDataToXML();
		
		//rewrite sign data to XML file
		writeSignDataToXML();
		
		//rewrite game data to XML file
		writeGameDataToXML();
		
		//plugin disabled successfully
		getLogger().info("-------------------------------------");
		getLogger().info(getClass().getSimpleName() + " disabled!");
		getLogger().info("-------------------------------------");
	}
	
	private World getWorldOfName(List<World> worlds, String name){
		for(World world : worlds){
			if(world.getName().equals(name)){
				return world;
			}
		}
		return null;
	}
	
	private void parseXMLArenaData(){
		//set up file location
		String arenaFileName = "arena.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\CaptureTheFlag directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<World> worlds = Bukkit.getWorlds();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(arenaInfoFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		
		//gets the worlds
		NodeList worldList = doc.getElementsByTagName("world");
		for (int worldIndex = 0; worldIndex < worldList.getLength(); worldIndex++) {
			Node worldNode = worldList.item(worldIndex);
			if (worldNode.getNodeType() == Node.ELEMENT_NODE) {
				Element worldElement = (Element)worldNode;
				String worldName = worldElement.getAttribute("name");
				World world = getWorldOfName(worlds, worldName);
				
				//gets arenas for world
				NodeList arenaList = worldElement.getElementsByTagName("arena");
				for (int arenaIndex = 0; arenaIndex < arenaList.getLength(); arenaIndex++) {
					Node arenaNode = arenaList.item(arenaIndex);
					if (arenaNode.getNodeType() == Node.ELEMENT_NODE) {
						Element arenaElement = (Element)arenaNode;
						String arenaName = arenaElement.getAttribute("name");
						
						//gets the bounds for arena
						Location[] bounds = new Location[2];
						NodeList boundsList = arenaElement.getElementsByTagName("bound");
						//set to 2 because only 2 possible bounds, so it gets first two
						for(int boundIndex = 0; boundIndex < 2; boundIndex++){
							Node boundNode = boundsList.item(boundIndex);
							if(boundNode.getNodeType() == Node.ELEMENT_NODE){
								Element boundElement = (Element)boundNode;
								Location location = new Location(world,
										Double.parseDouble(boundElement.getAttribute("x")),
										Double.parseDouble(boundElement.getAttribute("y")),
										Double.parseDouble(boundElement.getAttribute("z")));
								bounds[boundIndex] = location;
							}
						}
						
						//gets the teams for arena
						HashMap<String, Team> teams = new HashMap<String, Team>();
						NodeList teamsList = arenaElement.getElementsByTagName("team");
						for(int teamIndex = 0; teamIndex < teamsList.getLength(); teamIndex++){
							Node teamNode = teamsList.item(teamIndex);
							if(teamNode.getNodeType() == Node.ELEMENT_NODE){
								Element teamElement = (Element)teamNode;
								String teamName = teamElement.getAttribute("name");
								String teamColor = teamElement.getAttribute("color");
								
								//gets the flags for team
								HashMap<String, Flag> flags = new HashMap<String, Flag>();
								NodeList flagsList = teamElement.getElementsByTagName("flag");
								for(int flagIndex = 0; flagIndex < flagsList.getLength(); flagIndex++){
									Node flagNode = flagsList.item(flagIndex);
									if(flagNode.getNodeType() == Node.ELEMENT_NODE){
										Element flagElement = (Element)flagNode;
										String flagName = flagElement.getAttribute("name");
										//new flag created
										flags.put(flagName, new Flag(flagName, new Location(world,
												Double.parseDouble(flagElement.getAttribute("x")),
												Double.parseDouble(flagElement.getAttribute("y")),
												Double.parseDouble(flagElement.getAttribute("z")))));
									}
								}
								
								//gets the spawns for team
								HashMap<String, Spawn> spawns = new HashMap<String, Spawn>();
								NodeList spawnsList = teamElement.getElementsByTagName("spawn");
								for(int spawnIndex = 0; spawnIndex < spawnsList.getLength(); spawnIndex++){
									Node spawnNode = spawnsList.item(spawnIndex);
									if(spawnNode.getNodeType() == Node.ELEMENT_NODE){
										Element spawnElement = (Element)spawnNode;
										String spawnName = spawnElement.getAttribute("name");
										//new spawn created
										spawns.put(spawnName, new Spawn(spawnName, new Location(world,
												Double.parseDouble(spawnElement.getAttribute("x")),
												Double.parseDouble(spawnElement.getAttribute("y")),
												Double.parseDouble(spawnElement.getAttribute("z")),
												Float.parseFloat(spawnElement.getAttribute("yaw")),
												Float.parseFloat(spawnElement.getAttribute("pitch")))));
									}
								}
								//new team created
								teams.put(teamName, new Team(teamName, flags, spawns, teamColor));
							}
						}
						//new arena created
						arenaMap.put(arenaName, new Arena(arenaName, bounds, teams));
					}
				}
			}
		}
	}
	
	private void writeArenaDataToXML(){
		//set up file location
		String arenaFileName = "arena.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\CaptureTheFlag directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//write to XML
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			
			//create worlds root tag
			Element worldsRootElement = doc.createElement("worlds");
			doc.appendChild(worldsRootElement);
			for(String arenaName : arenaMap.keySet()){
				Arena arena = arenaMap.get(arenaName);
				World arenaWorld = arena.getBounds()[0].getWorld();
				
				//get or create world element
				Element worldElement = getOrCreateWorldElement(doc, worldsRootElement, arenaWorld.getName());
				
				//create element for current arena
				Element arenaElement = doc.createElement("arena");
				arenaElement.setAttribute("name", arena.getName());
				worldElement.appendChild(arenaElement);
				
				//create elements for arena info
				//only two elements but this is cleaner
				for(Location bound : arena.getBounds()){
					Element boundElement = doc.createElement("bound");
					boundElement.setAttribute("x", "" + bound.getBlockX());
					boundElement.setAttribute("y", "" + bound.getBlockY());
					boundElement.setAttribute("z", "" + bound.getBlockZ());
					arenaElement.appendChild(boundElement);
				}
				
				//create elements for team info
				for(String teamName : arena.getTeams().keySet()){
					Team team = arena.getTeam(teamName);
					Element teamElement = doc.createElement("team");
					teamElement.setAttribute("name", teamName);
					teamElement.setAttribute("color", team.getHexColor());
					arenaElement.appendChild(teamElement);
					
					//create elements for flag info
					for(String flagName : team.getFlags().keySet()){
						Element flagElement = doc.createElement("flag");
						flagElement.setAttribute("name", flagName);
						flagElement.setAttribute("x", "" + team.getFlag(flagName).getLocation().getBlockX());
						flagElement.setAttribute("y", "" + team.getFlag(flagName).getLocation().getBlockY());
						flagElement.setAttribute("z", "" + team.getFlag(flagName).getLocation().getBlockZ());
						teamElement.appendChild(flagElement);
					}
					
					//create elements for spawn info
					for(String spawnName : team.getSpawns().keySet()){
						Element spawnElement = doc.createElement("spawn");
						spawnElement.setAttribute("name", spawnName);
						spawnElement.setAttribute("x", "" + team.getSpawn(spawnName).getLocation().getX());
						spawnElement.setAttribute("y", "" + team.getSpawn(spawnName).getLocation().getY());
						spawnElement.setAttribute("z", "" + team.getSpawn(spawnName).getLocation().getZ());
						spawnElement.setAttribute("yaw", "" + team.getSpawn(spawnName).getLocation().getYaw());
						spawnElement.setAttribute("pitch", "" + team.getSpawn(spawnName).getLocation().getPitch());
						teamElement.appendChild(spawnElement);
					}
				}
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(arenaInfoFile);
			
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
	
	private Element getOrCreateWorldElement(Document doc, Element worldsRootElement, String worldName){
		NodeList worldList = worldsRootElement.getChildNodes();
		for(int i = 0; i < worldList.getLength(); i++){
			Element worldElement = (Element)worldList.item(i);
			if(worldElement.getAttributeNode("name").getValue().equals(worldName)){
				//element for this world already exists, return it
				return worldElement;
			}
		}
		
		//element for this world DNE, create new one
		Element worldElement = doc.createElement("world");
		worldElement.setAttribute("name", worldName);
		worldsRootElement.appendChild(worldElement);
		return worldElement;
	}
	
	public static Location getBlockLookingAt(Player player){
		Set<Material> ignoreBlocks = new HashSet<Material>();
		ignoreBlocks.add(Material.AIR);
		ignoreBlocks.add(Material.WATER);
		ignoreBlocks.add(Material.LAVA);
		return player.getTargetBlock(ignoreBlocks, 100).getLocation();
	}
	
	private void parseXMLSignData(){
		//set up file location
		String arenaFileName = "signs.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\CaptureTheFlag directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<World> worlds = Bukkit.getWorlds();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(arenaInfoFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		
		//get the list of signs
		NodeList signList = doc.getElementsByTagName("sign");
		for (int signIndex = 0; signIndex < signList.getLength(); signIndex++) {
			Node signNOde = signList.item(signIndex);
			if (signNOde.getNodeType() == Node.ELEMENT_NODE) {
				Element signElement = (Element)signNOde;
				String worldName = signElement.getAttribute("world");
				Location signLocation = new Location(getWorldOfName(worlds, worldName),
						Double.parseDouble(signElement.getAttribute("x")),
						Double.parseDouble(signElement.getAttribute("y")),
						Double.parseDouble(signElement.getAttribute("z")));
				String arenaName = signElement.getAttribute("joinarena");
				arenaMap.get(arenaName).createJoinSign(signLocation, this);
			}
		}
	}
	
	private void writeSignDataToXML() {
		//set up file location
		String arenaFileName = "signs.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\CaptureTheFlag directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//write to XML
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			
			//create signs root tag
			Element signsRootElement = doc.createElement("signs");
			doc.appendChild(signsRootElement);
			for(String arenaName : arenaMap.keySet()){
				for(Location signLocation : arenaMap.get(arenaName).getSigns()){
					Element signElement = doc.createElement("sign");
					signElement.setAttribute("world", signLocation.getWorld().getName());
					signElement.setAttribute("x", "" + signLocation.getX());
					signElement.setAttribute("y", "" + signLocation.getY());
					signElement.setAttribute("z", "" + signLocation.getZ());
					signElement.setAttribute("joinarena", arenaName);
					signsRootElement.appendChild(signElement);
				}
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(arenaInfoFile);
			
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
	
	private void parseXMLGameData(){
		//set up file location
		String arenaFileName = "gamedata.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\CaptureTheFlag directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<World> worlds = Bukkit.getWorlds();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(arenaInfoFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		
		//get various game data
		Node hubNode = doc.getElementsByTagName("hubspawn").item(0);
		if (hubNode.getNodeType() == Node.ELEMENT_NODE) {
			Element hubElement = (Element)hubNode;
			String worldName = hubElement.getAttribute("world");
			hubSpawn = new Location(getWorldOfName(worlds, worldName),
					Double.parseDouble(hubElement.getAttribute("x")),
					Double.parseDouble(hubElement.getAttribute("y")),
					Double.parseDouble(hubElement.getAttribute("z")));
			hubEnabled = Boolean.parseBoolean(hubElement.getAttribute("enabled"));
		}
	}
	
	public void writeGameDataToXML(){
		//set up file location
		String arenaFileName = "gamedata.xml";
		File arenaInfoFile = getDataFolder();
		if(arenaInfoFile.mkdir()){
			getLogger().info("Created \\CaptureTheFlag directory.");
		}
		
		arenaInfoFile = new File(arenaInfoFile.toString() + "\\" + arenaFileName);
		try {
			if(arenaInfoFile.createNewFile()){
				getLogger().info("Created new " + arenaFileName + " file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//write to XML
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			
			//create data root tag
			Element signsRootElement = doc.createElement("data");
			doc.appendChild(signsRootElement);
			Element hubSpawnElement = doc.createElement("hubspawn");
			hubSpawnElement.setAttribute("world", hubSpawn.getWorld().getName());
			hubSpawnElement.setAttribute("x", "" + hubSpawn.getX());
			hubSpawnElement.setAttribute("y", "" + hubSpawn.getY());
			hubSpawnElement.setAttribute("z", "" + hubSpawn.getZ());
			hubSpawnElement.setAttribute("yaw", "" + hubSpawn.getYaw());
			hubSpawnElement.setAttribute("pitch", "" + hubSpawn.getPitch());
			hubSpawnElement.setAttribute("enabled", "" + hubEnabled);
			signsRootElement.appendChild(hubSpawnElement);
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(arenaInfoFile);
			
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}