package com.kartoflane.superluminal.ui;

import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import com.kartoflane.superluminal.core.ConfigIO;
import com.kartoflane.superluminal.core.Main;
import com.kartoflane.superluminal.core.ShipIO;
import com.kartoflane.superluminal.elements.FTLShip;


public class ShipBrowser
{
	public static Shell shell;
	public boolean abort;
	DirectoryDialog dialog;
	
	private Text dataDir;
	private Text resDir;
	
	// === GUI ELEMENTS' VARIABLES
	Button btnConfirm;
	static Tree tree;
	public static TreeItem trtmPlayer;
	public static TreeItem trtmEnemy;
	public static TreeItem trtmOther;
	
	static HashSet<FTLShip> loadedShips;
	//static HashSet<FTLWeapon> loadedWeapons;
	
	public static HashSet<TreeItem> ships;
	static String selectedShip;

	public ShipBrowser(Shell sh)
	{
		shell = new Shell(sh, SWT.BORDER | SWT.TITLE);
		dialog = new DirectoryDialog(Main.shell, SWT.OPEN);
		ships = new HashSet<TreeItem>();
		
		createContents();
		
		Main.shell.setEnabled(false);
		shell.setLocation(Main.shell.getLocation().x+100, Main.shell.getLocation().y+50);
		tree.setEnabled(false);
		
		if (!isNull(Main.dataPath) && !isNull(Main.resPath)) {
			tree.setEnabled(true);
			
			ShipIO.loadTree();
		}
		if (!isNull(Main.dataPath)) {
			dataDir.setText(Main.dataPath);
		}
		if (!isNull(Main.resPath)) {
			resDir.setText(Main.resPath);
		}
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents()
	{
		shell.setText(Main.APPNAME + " - Ship Browser");
		shell.setFont(Main.appFont);
		shell.setSize(400, 400);
		shell.setLayout(new GridLayout(2, false));
		
		Composite composite_2 = new Composite(shell, SWT.NONE);
		composite_2.setLayout(new GridLayout(2, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		dataDir = new Text(composite_2, SWT.BORDER);
		dataDir.setFont(Main.appFont);
		GridData gd_dataDir = new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1);
		gd_dataDir.minimumWidth = 290;
		gd_dataDir.widthHint = 290;
		dataDir.setLayoutData(gd_dataDir);
		dataDir.setText("Data-unpacked directory");
		
		Button btnData = new Button(composite_2, SWT.NONE);
		btnData.setFont(Main.appFont);
		GridData gd_btnData = new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1);
		gd_btnData.minimumWidth = 70;
		gd_btnData.widthHint = 70;
		btnData.setLayoutData(gd_btnData);
		btnData.setText("Browse...");
		
		resDir = new Text(composite_2, SWT.BORDER);
		resDir.setFont(Main.appFont);
		GridData gd_resDir = new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1);
		gd_resDir.minimumWidth = 290;
		gd_resDir.widthHint = 290;
		resDir.setLayoutData(gd_resDir);
		resDir.setText("Resources-unpacked directory");
		
		Button btnRes = new Button(composite_2, SWT.NONE);
		btnRes.setFont(Main.appFont);
		GridData gd_btnRes = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRes.minimumWidth = 70;
		gd_btnRes.widthHint = 70;
		btnRes.setLayoutData(gd_btnRes);
		btnRes.setText("Browse...");
		
				
		tree = new Tree(shell, SWT.BORDER);
		tree.setFont(Main.appFont);
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
		gd_tree.heightHint = 339;
		tree.setLayoutData(gd_tree);
		
		trtmPlayer = new TreeItem(tree, SWT.NONE);
		trtmPlayer.setText("Player ships");
		
		trtmPlayer.setExpanded(false);
		
		trtmEnemy = new TreeItem(tree, SWT.NONE);
		trtmEnemy.setText("Enemy ships");
		
		trtmOther = new TreeItem(tree, SWT.NONE);
		trtmOther.setText("Other");
		trtmOther.setExpanded(true);
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayout(null);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1);
		gd_composite_1.heightHint = 25;
		gd_composite_1.widthHint = 419;
		composite_1.setLayoutData(gd_composite_1);
		
		btnConfirm = new Button(composite_1, SWT.NONE);
		btnConfirm.setFont(Main.appFont);
		btnConfirm.setBounds(228, 0, 75, 25);
		btnConfirm.setText("Load");
		btnConfirm.setEnabled(false);
		
		Button btnCancel = new Button(composite_1, SWT.NONE);
		btnCancel.setFont(Main.appFont);
		btnCancel.setBounds(309, 0, 75, 25);
		btnCancel.setText("Cancel");
		
	//=====================================
	// === BOOKMARK LISTENERS

		btnData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = dialog.open();
				if (!isNull(s)) {
					Main.dataPath = s;
					dataDir.setText(Main.dataPath);
					
					// check if the other path is set too
					if (!isNull(Main.resPath)) {
						clearTrees();
						ShipIO.reloadBlueprints();
						tree.setEnabled(true);
						ConfigIO.saveConfig();
					}
				}
			}
		});
		
		btnRes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = dialog.open();
				if (!isNull(s)) {
					Main.resPath = s;
					resDir.setText(Main.resPath);
					
					// check if the other path is set too
					if (!isNull(Main.dataPath)) {
						ShipIO.reloadBlueprints();
						tree.setEnabled(true);
						ConfigIO.saveConfig();
					}
				}
			}
		});
		
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event)
			{
				Main.shell.setEnabled(true);
				shell.dispose();
			}
		});
		
		tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String s;
		        TreeItem[] selection = tree.getSelection();
		        
		        for (int i = 0; i < selection.length; i++) {
		        	if (ships.contains(selection[i])) {
		        		btnConfirm.setEnabled(true);
		        		
		        		s = selection[i].getText();
		        		s = s.substring(s.indexOf("(")+1, s.indexOf(")"));

		        		selectedShip = s;
		        	} else {
		        		btnConfirm.setEnabled(false);
		        	}
		        }
			}
		});

		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Main.shell.setEnabled(true);
				shell.dispose();
			}
		});
		
		btnConfirm.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Main.shell.setEnabled(true);
				Main.idList.clear();
				
				ShipIO.loadShip(selectedShip, true);

				//Main.ship.updateReactor();
				
				if (ShipIO.errors.size() == 0 && Main.ship != null) {
					Main.print(((Main.ship.shipName!=null)?(Main.ship.shipClass + " - " + Main.ship.shipName):(Main.ship.shipClass)) + " [" + Main.ship.blueprintName + "] loaded successfully.");
				} else {
					Main.print("Errors occured during ship loading; some data may be missing.");
					if (Main.debug) {
						for (String s : ShipIO.errors) {
							Main.debug(s);
						}
					}
					
					ShipIO.errors.clear();
				}
				ConfigIO.saveConfig();
				shell.dispose();
			}
		});
	}
	

	// AUXILIARY
	public static boolean isNull(String path) {
		return path == null || (path != null && (path.equals("") || path.equals("null")));
	}
	
	public static void clearTrees() {
		for (TreeItem trtm : trtmPlayer.getItems()) {
			trtm.dispose();
		}
		trtmPlayer.clearAll(true);
		for (TreeItem trtm : trtmEnemy.getItems()) {
			trtm.dispose();
		}
		trtmEnemy.clearAll(true);
		for (TreeItem trtm : trtmOther.getItems()) {
			trtm.dispose();
		}
		trtmOther.clearAll(true);
	}
}