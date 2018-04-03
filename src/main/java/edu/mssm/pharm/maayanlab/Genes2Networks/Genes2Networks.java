package edu.mssm.pharm.maayanlab.Genes2Networks;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Logger;

import edu.mssm.pharm.maayanlab.common.core.FileUtils;
import edu.mssm.pharm.maayanlab.common.core.Settings;
import edu.mssm.pharm.maayanlab.common.core.SettingsChanger;
import edu.mssm.pharm.maayanlab.common.graph.NetworkModelWriter;
import edu.mssm.pharm.maayanlab.common.graph.PajekNETWriter;
import edu.mssm.pharm.maayanlab.common.graph.ShapeNode.Shape;
import edu.mssm.pharm.maayanlab.common.graph.XGMMLWriter;
import edu.mssm.pharm.maayanlab.common.graph.yEdGraphMLWriter;

public class Genes2Networks implements SettingsChanger {

	static Logger log = Logger.getLogger(Genes2Networks.class.getSimpleName());
	
	// paths to sig files
	private final static String BIND_LOC = "res/BIND.sig";
	private final static String BIOCARTA_LOC = "res/Biocarta.sig";
	private final static String BIOGRID_LOC = "res/BioGRID.sig";
	private final static String BIOPLEX_LOC = "res/BioPlex.sig";
	private final static String DIP_LOC = "res/DIP.sig";
	private final static String FIGEYS_LOC = "res/figeys.sig";
	private final static String HPRD_LOC = "res/HPRD.sig";
	private final static String HUMAP_LOC = "res/huMAP.sig";
	private final static String IREF_LOC = "res/iREF.sig";
	private final static String INNATEDB_LOC = "res/InnateDB.sig";	
	private final static String INTACT_LOC = "res/IntAct.sig";
	private final static String KEA_LOC = "res/KEA.sig";
	private final static String KEGG_LOC = "res/KEGG.sig";
	private final static String MINT_LOC = "res/MINT.sig";
	private final static String MIPS_LOC = "res/MIPS.sig";
	private final static String MURPHY_LOC = "res/murphy.sig";
	private final static String PDZBASE_LOC = "res/pdzbase.sig";
	private final static String PPID_LOC = "res/ppid.sig";
	private final static String PREDICTEDPPI_LOC = "res/predictedPPI.sig";
	private final static String SNAVI_LOC = "res/SNAVI.sig";
	private final static String STELZL_LOC = "res/Stelzl.sig";
	private final static String VIDAL_LOC = "res/vidal.sig";
	
	// Instance variables
	private HashMap<String, NetworkNode> nodes;
	private HashMap<String, HashSet<Interaction>> edges, publications;
	private HashSet<String> seeds;
	private HashSet<NetworkNode> network;
	private int pathLength = 2;
	private int articleMin = 0, interactionMax = 0, edgeFilter = 0;
	private boolean useInternalBackground = true;
	
	// default settings
	private final Settings settings = new Settings() {
		{
			// Integer: the starting path length (number of edges between nodes) to do network expansion. [>=1]
			set(PATH_LENGTH, 2);
			// Integer: limit the maximum number of interactions that a particular protein has to exclude hub proteins. 0 means disabled. [>0]
			set(MAXIMUM_NUMBER_OF_EDGES, 0);
			// Integer: limit the maximum number of interactions that a particular article contributes to exclude high-thoroughput studies. 0 means disabled. [>0] 
			set(MAXIMUM_NUMBER_OF_INTERACTIONS, 0);
			// Integer: require a minimum number of articles to report a specific interaction to strengthen the validity of the interaction. 0 means disabled. [>0] 
			set(MINIMUM_NUMBER_OF_ARTICLES, 0);
			// Boolean: enable BIND database. [true/false]
			set(ENABLE_BIND, true);
			// Boolean: enable Biocarta database. [true/false]
			set(ENABLE_BIOCARTA, true);
			// Boolean: enable BioGRID database. [true/false]
			set(ENABLE_BIOGRID, true);
			// Boolean: enable BioPlex database. [true/false]
			set(ENABLE_BIOPLEX, true);			
			// Boolean: enable DIP database. [true/false]
			set(ENABLE_DIP, true);
			// Boolean: enable figeys database. [true/false]
			set(ENABLE_FIGEYS, false);
			// Boolean: enable HPRD database. [true/false]
			set(ENABLE_HPRD, true);
			// Boolean: enable huMAP database. [true/false]
			set(ENABLE_HUMAP, true);
			// Boolean: enable iREF database. [true/false]			
			set(ENABLE_IREF, true);
			// Boolean: enable InnateDB database. [true/false]
			set(ENABLE_INNATEDB, true);
			// Boolean: enable IntAct database. [true/false]
			set(ENABLE_INTACT, true);
			// Boolean: enable KEA database. [true/false]
			set(ENABLE_KEA, false);
			// Boolean: enable KEGG database. [true/false]
			set(ENABLE_KEGG, true);
			// Boolean: enable MINT database. [true/false]
			set(ENABLE_MINT, true);
			// Boolean: enable MIPS database. [true/false]
			set(ENABLE_MIPS, true);
			// Boolean: enable murphy database. [true/false]
			set(ENABLE_MURPHY, false);
			// Boolean: enable pdzbase database. [true/false]
			set(ENABLE_PDZBASE, true);
			// Boolean: enable ppid database. [true/false]
			set(ENABLE_PPID, true);
			// Boolean: enable top 3000 of predicted PPI database. [true/false]
			set(ENABLE_PREDICTEDPPI, false);
			// Boolean: enable SNAVI database. [true/false]
			set(ENABLE_SNAVI, true);
			// Boolean: enable Stelzl database. [true/false]
			set(ENABLE_STELZL, false);
			// Boolean: enable vidal database. [true/false]
			set(ENABLE_VIDAL, false);
			// Boolean: output a yEd graphml file for network visualization of the expansion. [true/false]
			set(ENABLE_YED_OUTPUT, true);
			// Boolean: output a Cytoscape XGMML file for network visualization of the expansion. [true/false]
			set(ENABLE_CYTOSCAPE_OUTPUT, false);
			// Boolean: output a Pajek NET file for network visualization of the expansion. [true/false]
			set(ENABLE_PAJEK_OUTPUT, false);
			// String: web color of the seed genes in the Cytoscape and yEd network expansion outputs. [#000000 - #FFFFFF]
			set(SEED_NODE_COLOR, "#FF0000");
			// String: web color of the expanded protein network in the Cytoscape and yEd network expansions outputs. [#000000 - #FFFFFF]
			set(EXPANDED_NODE_COLOR, "#00FF00");
		}
	};
	
	// constants for settings
	public final static String PATH_LENGTH = "path_length";
	public final static String MAXIMUM_NUMBER_OF_EDGES = "max_number_of_interactions_per_protein";
	public final static String MAXIMUM_NUMBER_OF_INTERACTIONS = "max_number_of_interactions_per_article";
	public final static String MINIMUM_NUMBER_OF_ARTICLES = "min_number_of_articles_supporting_interaction";
	
	public final static String ENABLE_BIND = "enable_BIND";
	public final static String ENABLE_BIOCARTA = "enable_Biocarta";
	public final static String ENABLE_BIOGRID = "enable_BioGRID";
	public final static String ENABLE_BIOPLEX = "enable_BioPlex";	
	public final static String ENABLE_DIP = "enable_DIP";
	public final static String ENABLE_FIGEYS = "enable_figeys";	
	public final static String ENABLE_HPRD = "enable_HPRD";
	public final static String ENABLE_HUMAP = "enable_huMAP";
	public final static String ENABLE_IREF = "enable_iREF";
	public final static String ENABLE_INNATEDB = "enable_InnateDB";	
	public final static String ENABLE_INTACT = "enable_IntAct";
	public final static String ENABLE_KEA = "enable_KEA";
	public final static String ENABLE_KEGG = "enable_KEGG";
	public final static String ENABLE_MINT = "enable_MINT";
	public final static String ENABLE_MIPS = "enable_MIPS";
	public final static String ENABLE_MURPHY = "enable_murphy";
	public final static String ENABLE_PDZBASE = "enable_pdzbase";
	public final static String ENABLE_PPID = "enable_ppid";
	public final static String ENABLE_PREDICTEDPPI = "enable_predictedPPI";
	public final static String ENABLE_SNAVI = "enable_SNAVI";
	public final static String ENABLE_STELZL = "enable_Stelzl";
	public final static String ENABLE_VIDAL = "enable_vidal";
	
	public final static String ENABLE_YED_OUTPUT = "enable_expansion_yEd_output";
	public final static String ENABLE_CYTOSCAPE_OUTPUT = "enable_expansion_cytoscape_output";
	public final static String ENABLE_PAJEK_OUTPUT = "enable_expansion_Pajek_output";
	public final static String SEED_NODE_COLOR = "seed_node_color";
	public final static String EXPANDED_NODE_COLOR = "expanded_node_color";
		
	// Global temporary variables
	private HashSet<NetworkNode> visited;
	
	public static void main(String[] args) {
		if (args.length == 2) {
			Genes2Networks g2n = new Genes2Networks();
			g2n.run(args[0]);
			g2n.writeFile(args[1]);
			g2n.writeNetworks(args[1].replaceFirst(".sig", ""));
		}
		else if (args.length > 2) {
			String[] sigFiles = new String[args.length - 2];
			System.arraycopy(args, 2, sigFiles, 0, args.length-2);
			Genes2Networks g2n = new Genes2Networks();
			g2n.run(args[0], sigFiles);
			g2n.writeFile(args[1]);
			g2n.writeNetworks(args[1].replaceFirst(".sig", ""));
		}
		else
			log.warning("Usage: java -jar Genes2Networks.jar input output [sigFiles...]");
	}
	
	// Load default settings
	public Genes2Networks() {
		settings.loadSettings();
	}
	
	// Inherit settings
	public Genes2Networks(Settings externalSettings) {
		settings.loadSettings(externalSettings);
	}
	
	public void setSetting(String key, String value) {
		settings.set(key, value);
	}
	
	// Run for CLI with nonstandard background
	public void run(String listFile, String[] sigFiles) {
		useInternalBackground = false;
		readSigs(sigFiles);
		run(listFile);
	}

	// Run for CLI
	public void run(String listFile) {
		ArrayList<String> inputList = FileUtils.readFile(listFile);
		
		try {
			if (FileUtils.validateList(inputList))
				run(inputList);
		} catch (ParseException e) {
			if (e.getErrorOffset() == -1)
				log.warning("Invalid Input: Input list is empty.");
			else
				log.warning("Invalid Input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
			System.exit(-1);			
		}
	}
	
	// Run for GUI and direct access
	public void run(ArrayList<String> listFile) {
		readInput(listFile);
		
		if (useInternalBackground) {
			ArrayList<String> sigList = new ArrayList<String>();
			if (settings.getBoolean(ENABLE_BIND))
				sigList.add(BIND_LOC);
			if (settings.getBoolean(ENABLE_BIOCARTA))
				sigList.add(BIOCARTA_LOC);
			if (settings.getBoolean(ENABLE_BIOGRID))
				sigList.add(BIOGRID_LOC);
			if (settings.getBoolean(ENABLE_BIOPLEX))
				sigList.add(BIOPLEX_LOC);			
			if (settings.getBoolean(ENABLE_DIP))
				sigList.add(DIP_LOC);
			if (settings.getBoolean(ENABLE_FIGEYS))
				sigList.add(FIGEYS_LOC);
			if (settings.getBoolean(ENABLE_HPRD))
				sigList.add(HPRD_LOC);
			if (settings.getBoolean(ENABLE_HUMAP))
				sigList.add(HUMAP_LOC);
			if (settings.getBoolean(ENABLE_IREF))
				sigList.add(IREF_LOC);			
			if (settings.getBoolean(ENABLE_INNATEDB))
				sigList.add(INNATEDB_LOC);
			if (settings.getBoolean(ENABLE_INTACT))
				sigList.add(INTACT_LOC);
			if (settings.getBoolean(ENABLE_KEA))
				sigList.add(KEA_LOC);
			if (settings.getBoolean(ENABLE_KEGG))
				sigList.add(KEGG_LOC);
			if (settings.getBoolean(ENABLE_MINT))
				sigList.add(MINT_LOC);
			if (settings.getBoolean(ENABLE_MIPS))
				sigList.add(MIPS_LOC);
			if (settings.getBoolean(ENABLE_MURPHY))
				sigList.add(MURPHY_LOC);
			if (settings.getBoolean(ENABLE_PDZBASE))
				sigList.add(PDZBASE_LOC);
			if (settings.getBoolean(ENABLE_PPID))
				sigList.add(PPID_LOC);
			if (settings.getBoolean(ENABLE_PREDICTEDPPI))
				sigList.add(PREDICTEDPPI_LOC);
			if (settings.getBoolean(ENABLE_SNAVI))
				sigList.add(SNAVI_LOC);
			if (settings.getBoolean(ENABLE_STELZL))
				sigList.add(STELZL_LOC);
			if (settings.getBoolean(ENABLE_VIDAL))
				sigList.add(VIDAL_LOC);
			String[] sigArray = new String[sigList.size()];
			readSigs(sigList.toArray(sigArray));
		}
		
		filter();
		computeNetwork();
	}
	
	public HashSet<String> getSeeds() {
		return seeds;
	}
	
	public HashSet<String> getNetwork() {
		
		HashSet<String> networkSet = new HashSet<String>();
		
		for (NetworkNode node : network)
			networkSet.add(node.getName());
		
		return networkSet;
	}
	
	public HashSet<NetworkNode> getNetworkSet() {
		return network;
	}
	
	public static String getEdgeKey(NetworkNode node1, NetworkNode node2) {
		String node1Name = node1.getName();
		String node2Name = node2.getName();
		
		if (node1Name.compareTo(node2Name) > 0)
			return node2Name + "::" + node1Name;
		else
			return node1Name + "::" + node2Name;		
	}
	
	public void removeEdgeFromNodes(String edgeKey) {
		String[] pair = edgeKey.split("::");
		
		NetworkNode source = nodes.get(pair[0]);
		NetworkNode target = nodes.get(pair[1]);
		
		source.removeNeighbor(target);
		target.removeNeighbor(source);
	}
	
	private void readInput(ArrayList<String> genes){
		seeds = new HashSet<String>();
		
		for (String gene : genes) {
			gene = gene.toUpperCase();
			seeds.add(gene);
		}		
	}
	
	private void readSigs(String[] sigFiles){
		// Initialize all variables
		nodes = new HashMap<String, NetworkNode>();
		edges = new HashMap<String, HashSet<Interaction>>();
		publications = new HashMap<String, HashSet<Interaction>>();
		
		// Loop all sig files
		for (String sigFile : sigFiles) {
			
			ArrayList<String> sig = FileUtils.readResource(sigFile);
			
			for (String s : sig) {				
				// Parse string
				String[] ss = s.split("\\s");
				
				// First name is source, second is target
				NetworkNode source, target;
				
				// Add both as nodes if they don't exist, otherwise retrieve from HashMap
				if (nodes.containsKey(ss[0])) {
					source = nodes.get(ss[0]);
					// If has outdated data, update type and location
					source.update(ss[3], ss[4]);
				}
				else {
					source = new NetworkNode(ss[0], "NA", "NA", ss[3], ss[4]);
					nodes.put(ss[0], source);
				}
				if (nodes.containsKey(ss[5])) {
					target = nodes.get(ss[5]);
					// If has outdated data, update type and location
					target.update(ss[3], ss[4]);
				}
				else {
					target = new NetworkNode(ss[5], "NA", "NA", ss[8], ss[9]);
					nodes.put(ss[5], target);
				}
				
				// Add each other as neighbors
				source.addNeighbor(target);
				target.addNeighbor(source);
				
				// Add network edge
				Interaction interaction = new Interaction(source, target, ss[10], ss[11], ss[12]);			
				
				// Generate common pair key
				String key = getEdgeKey(source, target);
				if (edges.containsKey(key)) {
					edges.get(key).add(interaction);
				}
				else {
					HashSet<Interaction> interactionList = new HashSet<Interaction>();
					interactionList.add(interaction);
					edges.put(key, interactionList);
				}
				// Store in publications
				if (publications.containsKey(ss[12])) {
					publications.get(ss[12]).add(interaction);
				}
				else {
					HashSet<Interaction> pubInteractionList = new HashSet<Interaction>();
					pubInteractionList.add(interaction);
					publications.put(ss[12], pubInteractionList);
				}
			}
		}
	}
	
	public void filter() {
		int articleMin = Integer.parseInt(settings.get(MINIMUM_NUMBER_OF_ARTICLES));
		int interactionMax = Integer.parseInt(settings.get(MAXIMUM_NUMBER_OF_INTERACTIONS));
		int edgeFilter = Integer.parseInt(settings.get(MAXIMUM_NUMBER_OF_EDGES));
		
		// If no change in filtering, return
		if (this.articleMin == articleMin && this.interactionMax == interactionMax && this.edgeFilter == edgeFilter)
			return;
		else {
			this.articleMin = articleMin;
			this.interactionMax = interactionMax;
			this.edgeFilter = edgeFilter;
		}
		
		// Each edge must be supported by a minimum number of interactions/articles
		if (articleMin > 1) {
			ArrayList<String> edgesToRemove = new ArrayList<String>();
			for (String key : edges.keySet())
				if (edges.get(key).size() < articleMin)
					edgesToRemove.add(key);
			
			for (String edgeKey : edgesToRemove) {
				HashSet<Interaction> interactions = edges.get(edgeKey);
				
				for (Interaction interaction : interactions)
					publications.get(interaction.getPmid()).remove(interaction);
				
				removeEdgeFromNodes(edgeKey);
				edges.remove(edgeKey);
			}
		}
		
		// Each publication can only contribute a maximum number of interactions
		if (interactionMax > 1) {
			ArrayList<String> publicationsToRemove = new ArrayList<String>();
			for (String key : publications.keySet())
				if (publications.get(key).size() > interactionMax)
					publicationsToRemove.add(key);
					
			for (String pmid : publicationsToRemove) {
				HashSet<Interaction> interactions = publications.get(pmid);
											
				for (Interaction interaction : interactions) {
					String edgeKey = getEdgeKey(interaction.getSource(), interaction.getTarget());
					edges.get(edgeKey).remove(interaction);
					if (edges.get(edgeKey).isEmpty())
						removeEdgeFromNodes(edgeKey);
				}
				
				publications.remove(pmid);
			}
		}
		
		// Each node can only have a maximum number of edges
		if (edgeFilter > 1) {
			for (NetworkNode node : nodes.values()) {
				if (node.getNeighbors().size() > edgeFilter) {
					for (NetworkNode neighbor : node.getNeighbors()) {
						String edgeKey = getEdgeKey(node, neighbor);
						HashSet<Interaction> interactions = edges.get(edgeKey);
						
						for (Interaction interaction : interactions)
							publications.get(interaction.getPmid()).remove(interaction);
						
						edges.remove(edgeKey);
					}
				
					node.removeSelf();
				}
			}
		}
	}
	
	// By default, don't include self-loops
	public void writeFile(String fileName) {
		writeFile(fileName, false);
	}
	
	public void writeFile(String fileName, boolean includeSelfLoops) {
		
		// Variables necessary to create intermediates rank
		ArrayList<String> nodeRank = new ArrayList<String>();
		int bgTotalLinks = edges.size();
		int subnetTotalLinks = 0;
		
		// Variables necessary to create sig file
		HashSet<Interaction> subNetInteractions = new HashSet<Interaction>();
		LinkedList<NetworkNode> pairingList = new LinkedList<NetworkNode>();
		pairingList.addAll(network);
		
		// Add edges for all pairs among the network
		NetworkNode node1 = null;
		while((node1 = pairingList.peek()) != null) {
			if (!includeSelfLoops)
				pairingList.pop();
			
			for (NetworkNode node2 : pairingList)
				if (edges.containsKey(getEdgeKey(node1, node2))) {
					subNetInteractions.addAll(edges.get(getEdgeKey(node1, node2)));
					subnetTotalLinks++;
				}
			
			if (includeSelfLoops)
				pairingList.pop();
		}

		// Write out
		FileUtils.writeFile(fileName, subNetInteractions);
		
		// Compute one-sample binomial proportion
		for (NetworkNode node : network) {
			HashSet<NetworkNode> neighbors = node.getNeighbors();
			
			int bgNodeLinks = neighbors.size();
			int subnetNodeLinks = 0;
			
			for (NetworkNode neighbor : neighbors)
				if (neighbor != node && network.contains(neighbor))
					subnetNodeLinks++;
			
			nodeRank.add(node.getName() + "," +
						 subnetNodeLinks + "," + subnetTotalLinks + "," + 
						 bgNodeLinks + "," + bgTotalLinks + "," +
						 computeBinomialProportion(bgNodeLinks, bgTotalLinks, subnetNodeLinks, subnetTotalLinks));
		}
		
		// Write out
		FileUtils.writeFile(fileName + ".txt", nodeRank);
	}
	
	public void writeNetworks(String outputPrefix) {
		NetworkModelWriter nmw = new NetworkModelWriter();
		
		for (String seed : seeds)
			nmw.addNode(seed, settings.get(SEED_NODE_COLOR), Shape.ELLIPSE);
		
		for (NetworkNode node : network)
			nmw.addNode(node.getName(), settings.get(EXPANDED_NODE_COLOR), Shape.ELLIPSE);
		
		for (NetworkNode node : network) {
			HashSet<NetworkNode> neighbors = node.getNeighbors();
			for (NetworkNode neighbor : neighbors)
				if (network.contains(neighbor))
					nmw.addInteraction(node.getName(), neighbor.getName());
		}
		
		if (settings.getBoolean(ENABLE_YED_OUTPUT)) {
			yEdGraphMLWriter ygw = new yEdGraphMLWriter(outputPrefix + ".graphml");
			ygw.open();
			nmw.writeGraph(ygw);			
			ygw.close();
		}
		
		if (settings.getBoolean(ENABLE_CYTOSCAPE_OUTPUT)) {
			XGMMLWriter xgw = new XGMMLWriter(outputPrefix + ".xgmml");
			xgw.open();
			nmw.writeGraph(xgw);
			xgw.close();
		}
		
		if (settings.getBoolean(ENABLE_PAJEK_OUTPUT)) {
			try {
				PajekNETWriter pnw = new PajekNETWriter(outputPrefix + ".net");
				pnw.open();
				nmw.writeGraph(pnw);
				pnw.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void computeNetwork() {
		pathLength = Integer.parseInt(settings.get(PATH_LENGTH));
		network = new HashSet<NetworkNode>();
		
		for (String seed : seeds) {
			if (nodes.containsKey(seed)) {
				NetworkNode start = nodes.get(seed);
				
				visited = new HashSet<NetworkNode>();
				
				walkPath(start, 0);
			}
		}
	}
	
	private boolean walkPath(NetworkNode node, int depth) {
		
		if (visited.contains(node))
			return false;
		else if (depth > pathLength)
			return false;
		else if (seeds.contains(node.getName())) {
			visited.add(node);
			network.add(node);
			for (NetworkNode neighbor : node.getNeighbors())
				walkPath(neighbor, depth+1);
			visited.remove(node);
			return true;
		}
		else {
			boolean orGate = false;
			visited.add(node);
			
			for (NetworkNode neighbor : node.getNeighbors())
				orGate |= walkPath(neighbor, depth+1);
			
			if (orGate)
				network.add(node);
			visited.remove(node);
			return orGate;
		}
	}
	
	private double computeBinomialProportion(int bgNodeLinks, int bgTotalLinks, int subnetNodeLinks, int subnetTotalLinks) {
		
		double nodeProportion = ((double) subnetNodeLinks) / bgNodeLinks;
		double linkProportion = ((double) subnetTotalLinks) / bgTotalLinks;
		double otherLinkProportion = 1 - linkProportion;
		double z = (nodeProportion - linkProportion) / Math.sqrt(linkProportion * otherLinkProportion / bgNodeLinks);
		
		return z;
	}
}