package edu.mssm.pharm.maayanlab.Genes2Networks;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import edu.mssm.pharm.maayanlab.common.core.FileUtils;
import edu.mssm.pharm.maayanlab.common.core.SettingsChanger;
import edu.mssm.pharm.maayanlab.common.swing.FileDrop;
import edu.mssm.pharm.maayanlab.common.swing.UIUtils;

public class G2NPanel extends JPanel {

	private static final long serialVersionUID = -7399777657232503800L;
	
	static Logger log = Logger.getLogger(G2NPanel.class.getSimpleName());
	
	// G2N process holder to call from nested class
	private Genes2Networks g2n;
	
	// JPanels
	private JPanel panel;
	
	// UI elements
	private JFileChooser openChooser, saveChooser;
	private JTextField openPath, savePath;
	private JTextArea inputTextArea, outputTextArea;
	private JButton openButton, viewButton, runButton;
	private JSlider pathLengthSlider;
	private JCheckBox maxNodeLinksCheckBox, maxInteractionsCheckBox, minArticlesCheckBox;
	private JTextField maxNodeLinks, maxInteractions, minArticles;

	// Checkboxes for the different databases
	private JCheckBox bindCheckBox, biocartaCheckBox, biogridCheckBox;
	private JCheckBox bioplexCheckBox;
	private JCheckBox dipCheckBox, figeysCheckBox, hprdCheckBox;
	private JCheckBox humapCheckBox;
	private JCheckBox irefCheckBox;
	private JCheckBox innatedbCheckBox, intactCheckBox, keggCheckBox;
	private JCheckBox mintCheckBox, mipsCheckBox, murphyCheckBox;
	private JCheckBox pdzbaseCheckBox, ppidCheckBox, keaCheckBox;
	private JCheckBox snaviCheckBox, stelzlCheckBox, vidalCheckBox;
	private JCheckBox predictedPPICheckBox;
	private JCheckBox yedOutput, cytoscapeOutput, pajekOutput;
	
	// Output
	private String output;
	private HashSet<NetworkNode> networkSet;	// hold results to generate graph
	
	public static void main(String[] args) {
		if(args.length == 0) {
			// Schedule a job for the EDT
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowGUI();
				}
			});
		}
		else{
			Genes2Networks.main(args);
		}		
	}
	
	private static void createAndShowGUI() {
		// Try to use Nimbus look and feel
		try {            
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
           log.warning("Nimbus: " + e);
        }
        
        // Create and set up the window
        JFrame appFrame = new JFrame("Genes2Networks");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Add content to the window
        G2NPanel appPanel = new G2NPanel();
        appFrame.setContentPane(appPanel);
        
        // Display the window
        appFrame.setResizable(false);
        appFrame.pack();
        appFrame.setVisible(true);
	}
	
	public G2NPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		if (!Boolean.getBoolean("verbose"))
            log.setLevel(Level.WARNING);
		
		// Attach instance to variable so nested classes can reference it
		panel = this;
		
		// File choosers
		openChooser = new JFileChooser(System.getProperty("user.dir"));
		openChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = openChooser.getSelectedFile();
				if (file.canRead() && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
					setupIO(file);
			}
		});
		saveChooser = new JFileChooser(System.getProperty("user.dir"));
		saveChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = saveChooser.getSelectedFile();
				if (file != null && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
					if (!file.getName().endsWith(".sig")) {
						file = new File(file.getAbsolutePath() + ".sig");
						saveChooser.setSelectedFile(file);
					}
					
					savePath.setText(file.getAbsolutePath());
				}
			}
		});
		
		// Select input file button
		JButton openFileButton = new JButton("Input TFs");
		openFileButton.setPreferredSize(new Dimension(300, 30));
		openFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openChooser.showOpenDialog(panel);
			}
		});
		JButton saveFileButton = new JButton("Output Network");
		saveFileButton.setPreferredSize(new Dimension(300, 30));
		saveFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveChooser.showSaveDialog(panel);
			}
		});
		
		// Text Fields
		openPath = new JTextField();
		savePath = new JTextField();
		
		// File Drop
		new FileDrop(openPath, new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				if (files[0].canRead()) {
					setupIO(files[0]);
					openChooser.setSelectedFile(files[0]);
				}
			}
		});
		
		// Scroll panes
		inputTextArea = new JTextArea(20, 20);
		JScrollPane inputTextPane = new JScrollPane(inputTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		outputTextArea = new JTextArea(20, 20);
		JScrollPane outputTextPane = new JScrollPane(outputTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		// File Drop
		new FileDrop(inputTextArea, new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				if (files[0].canRead()) {
					setupIO(files[0]);
					openChooser.setSelectedFile(files[0]);
				}
			}
		});
		
		// Open results
		openButton = new JButton("View Results");
		openButton.setEnabled(false);
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(new File(output));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(panel, "Unable to open " + output, "Unable to open file", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		// View network
		viewButton = new JButton("View Network");
		viewButton.setEnabled(false);
		viewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NetworkViewer viewer = new NetworkViewer(networkSet);
				viewer.pack();
				viewer.setVisible(true);
			}
		});
		
		// Start button
		runButton = new JButton("Expand Network");
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				output = savePath.getText();
				ArrayList<String> inputList = UIUtils.getTextAreaText(inputTextArea);
				
				try {
					if (!output.equals("") && FileUtils.validateList(inputList)) {
						g2n = new Genes2Networks();
						
						setSettings(g2n);
						g2n.run(inputList);
						setOutputTextArea(g2n.getNetwork());
						setNetworkSet(g2n.getNetworkSet());
						g2n.writeFile(output);
						g2n.writeNetworks(output.replaceFirst(".sig", ""));
						enableOutput(output);
					}
					else {
						JOptionPane.showMessageDialog(panel, "No save location specified.", "No Save Location", JOptionPane.WARNING_MESSAGE);
					}
				} catch (ParseException e1) {
					if (e1.getErrorOffset() == -1)
						JOptionPane.showMessageDialog(panel, "Input list is empty.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
					else
						JOptionPane.showMessageDialog(panel, e1.getMessage() + " at line " + (e1.getErrorOffset() + 1) +" is not a valid Entrez Gene Symbol.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		// Advanced Settings
		JLabel pathLengthLabel = new JLabel("Path Length");
		pathLengthLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		pathLengthSlider = new JSlider(JSlider.HORIZONTAL, 1, 4, 2);
		pathLengthSlider.setMajorTickSpacing(1);
		pathLengthSlider.setSnapToTicks(true);
		pathLengthSlider.setPaintTicks(true);
		pathLengthSlider.setPaintLabels(true);
		JPanel pathLengthBox = new JPanel();
		pathLengthBox.add(pathLengthLabel);
		pathLengthBox.add(pathLengthSlider);
		
		JLabel bgLabel = new JLabel("<html>Select<br>PPI<br>networks<br>to include:</html>");
		bgLabel.setHorizontalAlignment(JLabel.LEFT);
		bindCheckBox = new JCheckBox("BIND", true);
		biocartaCheckBox = new JCheckBox("Biocarta", true);
		biogridCheckBox = new JCheckBox("BioGRID", true);
		bioplexCheckBox = new JCheckBox("BioPlex", true);
		dipCheckBox = new JCheckBox("DIP", true);
		figeysCheckBox = new JCheckBox("figeys", false);
		hprdCheckBox = new JCheckBox("HPRD", true);
		humapCheckBox = new JCheckBox("huMAP", true);
		irefCheckBox = new JCheckBox("iREF", true);
		innatedbCheckBox = new JCheckBox("InnateDB", true);
		intactCheckBox = new JCheckBox("IntAct", true);
		keaCheckBox = new JCheckBox("KEA", false);
		keggCheckBox = new JCheckBox("KEGG", true);
		mintCheckBox = new JCheckBox("MINT", true);
		mipsCheckBox = new JCheckBox("MIPS", true);
		murphyCheckBox = new JCheckBox("murphy", false);
		pdzbaseCheckBox = new JCheckBox("pdzbase", true);
		ppidCheckBox = new JCheckBox("ppid", true);
		predictedPPICheckBox = new JCheckBox("Predicted PPI", false);
		snaviCheckBox = new JCheckBox("SNAVI", true);
		stelzlCheckBox = new JCheckBox("Stelzl", false);
		vidalCheckBox = new JCheckBox("vidal", false);
		JPanel bgBox = new JPanel();
		JPanel checkBoxes = new JPanel(new GridLayout(4, 0));
		checkBoxes.add(bindCheckBox);
		checkBoxes.add(biocartaCheckBox);
		checkBoxes.add(biogridCheckBox);
		checkBoxes.add(bioplexCheckBox);
		checkBoxes.add(dipCheckBox);
		checkBoxes.add(figeysCheckBox);
		checkBoxes.add(hprdCheckBox);
		checkBoxes.add(humapCheckBox);
		checkBoxes.add(irefCheckBox);
		checkBoxes.add(innatedbCheckBox);
		checkBoxes.add(intactCheckBox);
		checkBoxes.add(keaCheckBox);
		checkBoxes.add(keggCheckBox);
		checkBoxes.add(mintCheckBox);
		checkBoxes.add(mipsCheckBox);
		checkBoxes.add(murphyCheckBox);
		checkBoxes.add(pdzbaseCheckBox);
		checkBoxes.add(ppidCheckBox);	
		checkBoxes.add(predictedPPICheckBox);
		checkBoxes.add(snaviCheckBox);
		checkBoxes.add(stelzlCheckBox);
		checkBoxes.add(vidalCheckBox);
		bgBox.add(bgLabel);	
		bgBox.add(checkBoxes);
		
		maxNodeLinksCheckBox = new JCheckBox("Allow a maximum of", false);
		maxNodeLinksCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				maxNodeLinks.setEnabled(maxNodeLinksCheckBox.isSelected());
			}
		});
		maxNodeLinks = new JTextField("100000");
		maxNodeLinks.setEnabled(false);
		JLabel maxNodeLinksLabel = new JLabel("node links from a node");
		JPanel maxNodeLinksBox = new JPanel();
		maxNodeLinksBox.add(maxNodeLinksCheckBox);
		maxNodeLinksBox.add(maxNodeLinks);
		maxNodeLinksBox.add(maxNodeLinksLabel);
		
		maxInteractionsCheckBox = new JCheckBox("Allow a maximum of", false);
		maxInteractionsCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				maxInteractions.setEnabled(maxInteractionsCheckBox.isSelected());
			}
		});
		maxInteractions = new JTextField("100000");
		maxInteractions.setEnabled(false);
		JLabel maxInteractionsLabel = new JLabel("interactions from an article");
		JPanel maxInteractionsBox = new JPanel();
		maxInteractionsBox.add(maxInteractionsCheckBox);
		maxInteractionsBox.add(maxInteractions);
		maxInteractionsBox.add(maxInteractionsLabel);
		
		minArticlesCheckBox = new JCheckBox("Allow a minimum of", false);
		minArticlesCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				minArticles.setEnabled(minArticlesCheckBox.isSelected());
			}
		});
		minArticles = new JTextField("1");
		minArticles.setEnabled(false);
		JLabel minArticlesLabel = new JLabel("articles reporting a specific interaction");
		JPanel minArticlesBox = new JPanel();
		minArticlesBox.add(minArticlesCheckBox);
		minArticlesBox.add(minArticles);
		minArticlesBox.add(minArticlesLabel);
		
		JLabel outputLabel = new JLabel("Select desired outputs: ");
		yedOutput = new JCheckBox("yEd Network", true);
		cytoscapeOutput = new JCheckBox("Cytoscape Network", false);
		pajekOutput = new JCheckBox("Pajek Network", false);
		JPanel outputBox = new JPanel();
		outputBox.add(outputLabel);
		outputBox.add(yedOutput);
		outputBox.add(cytoscapeOutput);
		outputBox.add(pajekOutput);
		
		// Input and output box
		JPanel ioBox = new JPanel();
		ioBox.setLayout(new GridLayout(2,2));
		ioBox.add(openFileButton);
		ioBox.add(saveFileButton);
		ioBox.add(openPath);	
		ioBox.add(savePath);
		
		// Panes
		JPanel textPanesBox = new JPanel();
		textPanesBox.setLayout(new BoxLayout(textPanesBox, BoxLayout.LINE_AXIS));
		textPanesBox.add(inputTextPane);
		textPanesBox.add(outputTextPane);
		
		// Button box
		JPanel buttonBox = new JPanel();
		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.LINE_AXIS));
		buttonBox.add(runButton);
		buttonBox.add(openButton);
		buttonBox.add(viewButton);
		
		// Advanced settings box
		JPanel advancedSettingsBox = new JPanel();
		advancedSettingsBox.setLayout(new BoxLayout(advancedSettingsBox, BoxLayout.PAGE_AXIS));
		advancedSettingsBox.setBorder(BorderFactory.createTitledBorder("Advanced Settings"));
		advancedSettingsBox.add(pathLengthBox);
		advancedSettingsBox.add(bgBox);
		advancedSettingsBox.add(maxNodeLinksBox);
		advancedSettingsBox.add(maxInteractionsBox);
		advancedSettingsBox.add(minArticlesBox);
		advancedSettingsBox.add(outputBox);

		this.add(ioBox);
		this.add(textPanesBox);
		this.add(Box.createRigidArea(new Dimension(0,10)));
		this.add(buttonBox);
		this.add(advancedSettingsBox);
	}
	
	public void setSettings(SettingsChanger changer) {
		if (maxNodeLinks.isEnabled()) 
			changer.setSetting(Genes2Networks.MAXIMUM_NUMBER_OF_EDGES, maxNodeLinks.getText());
		if (minArticles.isEnabled())
			changer.setSetting(Genes2Networks.MINIMUM_NUMBER_OF_ARTICLES, minArticles.getText());
		if (maxInteractions.isEnabled())
			changer.setSetting(Genes2Networks.MAXIMUM_NUMBER_OF_INTERACTIONS, maxInteractions.getText());
		changer.setSetting(Genes2Networks.PATH_LENGTH, Integer.toString(pathLengthSlider.getValue()));
		changer.setSetting(Genes2Networks.ENABLE_BIND, Boolean.toString(bindCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_BIOCARTA, Boolean.toString(biocartaCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_BIOGRID, Boolean.toString(biogridCheckBox.isSelected()));		
		changer.setSetting(Genes2Networks.ENABLE_BIOPLEX, Boolean.toString(bioplexCheckBox.isSelected()));				
		changer.setSetting(Genes2Networks.ENABLE_DIP, Boolean.toString(dipCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_FIGEYS, Boolean.toString(figeysCheckBox.isSelected()));		
		changer.setSetting(Genes2Networks.ENABLE_HPRD, Boolean.toString(hprdCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_HUMAP, Boolean.toString(humapCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_IREF, Boolean.toString(irefCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_INNATEDB, Boolean.toString(innatedbCheckBox.isSelected()));		
		changer.setSetting(Genes2Networks.ENABLE_INTACT, Boolean.toString(intactCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_KEA, Boolean.toString(keaCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_KEGG, Boolean.toString(keggCheckBox.isSelected()));		
		changer.setSetting(Genes2Networks.ENABLE_MINT, Boolean.toString(mintCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_MIPS, Boolean.toString(mipsCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_MURPHY, Boolean.toString(murphyCheckBox.isSelected()));		
		changer.setSetting(Genes2Networks.ENABLE_PDZBASE, Boolean.toString(pdzbaseCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_PPID, Boolean.toString(ppidCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_PREDICTEDPPI, Boolean.toString(predictedPPICheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_SNAVI, Boolean.toString(snaviCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_STELZL, Boolean.toString(stelzlCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_VIDAL, Boolean.toString(vidalCheckBox.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_YED_OUTPUT, Boolean.toString(yedOutput.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_CYTOSCAPE_OUTPUT, Boolean.toString(cytoscapeOutput.isSelected()));
		changer.setSetting(Genes2Networks.ENABLE_PAJEK_OUTPUT, Boolean.toString(pajekOutput.isSelected()));
	}
	
	public void setInputTextArea(Collection<String> list) {
		UIUtils.setTextAreaText(inputTextArea, list);
	}
	
	public void setOutputTextArea(Collection<String> list) {
		UIUtils.setTextAreaText(outputTextArea, list);
	}
	
	public void setNetworkSet(HashSet<NetworkNode> networkSet) {
		this.networkSet = networkSet;
	}
	
	private void setupIO(File inputFile) {
		openPath.setText(inputFile.getAbsolutePath());
		setInputTextArea(FileUtils.readFile(inputFile));
		
		File outputFile = new File(System.getProperty("user.dir"), FileUtils.stripFileExtension(inputFile.getName()) + ".results_network.sig");
		saveChooser.setSelectedFile(outputFile);
		savePath.setText(outputFile.getAbsolutePath());
	}
	
	// Check if okay to enable view results button
	public void enableOutput(String output) {
		savePath.setText(output);
		this.output = output;
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
			openButton.setEnabled(true);
		viewButton.setEnabled(true);
	}
}
