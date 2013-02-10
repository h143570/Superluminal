package com.kartoflane.superluminal.core;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.graphics.*;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

import com.kartoflane.superluminal.elements.Anchor;
import com.kartoflane.superluminal.elements.CursorBox;
import com.kartoflane.superluminal.elements.FTLDoor;
import com.kartoflane.superluminal.elements.FTLItem;
import com.kartoflane.superluminal.elements.FTLMount;
import com.kartoflane.superluminal.elements.FTLRoom;
import com.kartoflane.superluminal.elements.FTLShip;
import com.kartoflane.superluminal.elements.Grid;
import com.kartoflane.superluminal.elements.GridBox;
import com.kartoflane.superluminal.elements.Slide;
import com.kartoflane.superluminal.elements.SystemBox;
import com.kartoflane.superluminal.elements.Systems;
import com.kartoflane.superluminal.painter.ColorBox;
import com.kartoflane.superluminal.painter.ImageBox;
import com.kartoflane.superluminal.painter.LayeredPainter;
import com.kartoflane.superluminal.painter.PaintBox;
import com.kartoflane.superluminal.ui.ErrorDialog;
import com.kartoflane.superluminal.ui.ExportDialog;
import com.kartoflane.superluminal.ui.NewShipWindow;
import com.kartoflane.superluminal.ui.PropertiesWindow;
import com.kartoflane.superluminal.ui.ShipBrowser;
import com.kartoflane.superluminal.ui.ShipPropertiesWindow;

public class Main
{
		// === CONSTANTS
	/**
	 * Frequency of canvas redrawing (if constantRedraw == true)
	 */
	private final static int INTERVAL = 25;
	/**
	 * Size of corner indicators on currently selected room
	 */
	private final static int CORNER = 10;
	/**
	 * Width of the drawing area, in grid cells
	 */
	public static int GRID_W = 26;
	public static int GRID_W_MAX = 30;
	/**
	 * Height of the drawing area, in grid cells
	 */
	public static int GRID_H = 20;
	public static int GRID_H_MAX = 24;
	
	public final static int REACTOR_MAX_PLAYER = 25;
	public final static int REACTOR_MAX_ENEMY = 32;
	
	public final static String APPNAME = "Superluminal";
	public final static String VERSION = "2013.02.7";
	
		// === Important objects
	public static Shell shell;
	public static Canvas canvas;
	public static FTLShip ship;
	public static ShipPropertiesWindow shipDialog;
	public static PropertiesWindow sysDialog;
	public static ExportDialog exDialog;
	public static MessageBox box;
	public static ErrorDialog erDialog;
	public static Transform currentTransform;
	public static LayeredPainter layeredPainter;
	public static CursorBox cursor;
	
		// === Preferences
		// ship explorer
	public static String dataPath = "null";
	public static String resPath = "null";
		// edit menu
	public static boolean removeDoor = true;
	public static boolean snapMounts = true;
	public static boolean snapMountsToHull = true;
	public static boolean arbitraryPosOverride = true;
		// view menu
	public static boolean showAnchor = true;
	public static boolean showMounts = true;
	public static boolean showRooms = true;
	public static boolean showHull = true;
	public static boolean showFloor = true;
	public static boolean showShield = true;
	public static boolean loadFloor = true;
	public static boolean loadShield = true;
	public static boolean loadSystem = true;
	public static boolean constantRedraw = true;
		// export dialog
	public static String exportPath = "null";
		// other
	public static String projectPath = "null";
	
		// ===  Mouse related
	public static Point mousePos = new Point(0,0);
	public static Point mousePosLastClick = new Point(0,0);
	public static Point dragRoomAnchor = new Point(0,0);
	public static boolean leftMouseDown = false;
	public static boolean rightMouseDown = false;
	public static boolean inBounds = false;
	
		// === Generic booleans
	private static boolean onCanvas = false;
	private static boolean moveSelected = false;
	private static boolean resizeSelected = false;
	private static boolean hullSelected = false;
	private static boolean shieldSelected = false;
	public static boolean canvasActive = false;
	public static boolean modShift = false;
	public static boolean modAlt = false;
	public static boolean moveAnchor = false;
	public static boolean allowRoomPlacement = true;
	
		// === Internal
	public static boolean debug = true;
	/**
	 * when set to true, all ship data is pre-loaded into hashmaps/sets when the ship browser is opened for the first time.
	 * no need to do this, only one ship is being used at any given time.
	 */
	public static boolean dataPreloading = false;
	/**
	 * Used when dataPreloading is enabled, set to true once data loading has been finished.
	 */
	public static boolean dataLoaded = false;
	
		// === Variables used to store a specific element out of a set (currently selected room, etc)
	public static FTLRoom selectedRoom = null;
	public static FTLDoor selectedDoor = null;
	public static FTLMount selectedMount = null;
	
		// === Rectangle variables, used for various purposes.
	private static FTLRoom parseRoom = null;
	private static Rectangle parseRect = null;
	private static Rectangle phantomRect = null;
	public static Rectangle mountRect = new Rectangle(0,0,0,0);
	public static Rectangle shieldEllipse = new Rectangle(0,0,0,0);
	
		// === Flags for Weapon Mounting tool
	private static Slide mountToolSlide = Slide.UP;
	private static boolean mountToolMirror = true;
	private static boolean mountToolHorizontal = true;
	
		// === Image holders
	public static Image hullImage = null;
	public static Image floorImage = null;
	public static Image shieldImage = null;
	public static Image cloakImage = null;
	public static Image tempImage;
	public static Image pinImage = null;
	public static Image tickImage = null;
	public static Image crossImage = null;
	public static Map<String, Integer> weaponStripMap = new HashMap<String, Integer>();
	public static Map<String, Image> weaponImgMap = new HashMap<String, Image>();
	public static Map<Integer, Rectangle> indexImgMapRotated = new HashMap<Integer, Rectangle>();
	public static Map<Integer, Rectangle> indexImgMapNormal = new HashMap<Integer, Rectangle>();
	
		// === Weapon image maps
	// it's probably better to just prepare all the images at once when loaded, instead of creating them as neccessary...
	public static LinkedList<Image> rotated = new LinkedList<Image>();
	public static LinkedList<Image> flipped = new LinkedList<Image>();
	public static LinkedList<Image> rotatedFlipped = new LinkedList<Image>();
	
		// === Miscellaneous
	public static Rectangle[] corners = new Rectangle[4];
	private static Color highlightColor = null;
	private static String lastMsg = "";
	/**
	 * Path of current project file, for quick saving via Ctrl+S
	 */
	public static String currentPath = null;

	/**
	 * Contains room IDs currently in use.
	 */
	public static HashSet<Integer> idList = new HashSet<Integer>();
	/**
	 * Preloaded data is stored in this map.
	 */
	public static HashMap<String, Object> loadedData = new HashMap<String, Object>();
	/**
	 * Images (tools and systems) are loaded once and then references are held in this map for easy access.
	 */
	public static HashMap<String, Image> toolsMap = new HashMap<String, Image>();
	/**
	 * Holds Image objects for easy reference, without the need to load them every time they're needed.
	 */
	//public static HashMap<Systems, Image> systemsMap = new HashMap<Systems, Image>();

	// === GUI elements' variables, for use in listeners and functions that reference them
	public static Menu menuSystem;
	//private static Label helpIcon;
	private static Label text;
	private static Label mGridPosText;
	private static Label shipInfoText;
	public static MenuItem mntmClose;
	private Text txtX;
	private Text txtY;
	private Label mPosText;
	private Button btnHull;
	private Button btnShields;
	private Button btnFloor;
	private Button btnCloak;
	private Button btnMiniship;
	private Label canvasBg;
	private Canvas bgCanvas;
	private FormData fd_canvas;
	private FormData fd_bgCanvas;
	private boolean shellStateChange;
	
	private MenuItem mntmUnload;
	private MenuItem mntmShowFile;
	private Menu menu_imageBtns;
	private Button sourceBtn;
	private MenuItem mntmPath;

	private MenuItem mntmShowFloor;
	private MenuItem mntmShowShield;
	private Button btnCloaked;
	private Button btnPirate;
	private Button btnXminus;
	private Button btnXplus;
	private Button btnYminus;
	private Button btnYplus;
	private MenuItem mntmConToPlayer;
	private MenuItem mntmConToEnemy;
	
	public static Font appFont;
	public static ToolItem tltmPointer;
	public static ToolItem tltmRoom;
	public static ToolItem tltmDoor;
	public static ToolItem tltmMount;
	public static ToolItem tltmSystem;

	
	public static Anchor anchor;
	public static GridBox gridBox;
	public static HashMap<Systems, SystemBox> systemsMap = new HashMap<Systems, SystemBox>();
	public static Grid grid;
	
	// =================================================================================================== //
	
	/*
	 * === TODO
	 * 
	 * - weaponArt odnosi sie do animations.xml, tam dopiero jest zdefiniowany uzywany image!
	 * - shell.pack w ship properties zamiast sztywnego ustalania wielkosci - inaczej jest clipping przyciskow na dole
	 * - usun width hint w hull/shield/etc images - zamiast tego daj minWidth (inaczej przyciski sie nie rozciagaja i tekst jest clippowany) 
	 * - w exportdialog text field nie jest ukrywany jak okno jest poczatkowo otwierane i ship jest player ship
	 * - przetestuj autoblueprints i blueprints na Macu
	 * 
	 * Perhaps re-allocate precision mode to ctrl-drag and change shift-drag to only move things along one axis, like it works it Photoshop.
	 * 
	 * ======
	 * Pirate version of ships viewable, similar to cloak?
	 * 
	 * =========================================================================
	 * - gibs -> male okienko gdzie ustawiasz kat (ko�o ze wskaznikiem), predkosc liniowa i katowa (slidery)
	 * - gibs -> ujemne angular velocity obraca w lewa strone
	 * 		  -> 10 angular velocty = pelen obrot
	 */
	
	// =================================================================================================== //
	
	public static void main(String[] args)
	{
		try {
			Main window = new Main();
			window.open();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open()
	{
		final Display display = Display.getDefault();
		
		shell = new Shell(SWT.SHELL_TRIM | SWT.BORDER);
		shell.setLayout(new GridLayout(2, false));
		shell.setText(APPNAME + " - Ship Editor");
		shell.setLocation(100,50);
		
		// resize the window as to not exceed screen dimensions, with maximum size being defined by GRID_W_MAX and GRID_H_MAX
		GRID_W = ((int) ((display.getBounds().width-35))/35);
		GRID_H = ((int) ((display.getBounds().height-150))/35);
		GRID_W = (GRID_W > GRID_W_MAX) ? GRID_W_MAX : GRID_W;
		GRID_H = (GRID_H > GRID_H_MAX) ? GRID_H_MAX : GRID_H;
		
		// create config file if it doesn't exist already
		if (!ConfigIO.configExists()) {
			ConfigIO.saveConfig();
		}
		
		// load values from config
		exportPath = ConfigIO.scourFor("exportPath");
		projectPath = ConfigIO.scourFor("projectPath");
		dataPath = ConfigIO.scourFor("dataPath");
		resPath = ConfigIO.scourFor("resPath");
		removeDoor = ConfigIO.getBoolean("removeDoor");
		snapMounts = ConfigIO.getBoolean("snapMounts");
		showAnchor = ConfigIO.getBoolean("showAnchor");
		showMounts = ConfigIO.getBoolean("showMounts");
		showRooms = ConfigIO.getBoolean("showRooms");
		showHull = ConfigIO.getBoolean("showHull");
		showFloor = ConfigIO.getBoolean("showFloor");
		showShield = ConfigIO.getBoolean("showShield");
		snapMounts = ConfigIO.getBoolean("snapMounts");
		snapMountsToHull = ConfigIO.getBoolean("snapMountsToHull");
		loadFloor = ConfigIO.getBoolean("loadFloor");
		loadShield = ConfigIO.getBoolean("loadShield");
		loadSystem = ConfigIO.getBoolean("loadSystem");
		constantRedraw = ConfigIO.getBoolean("constantRedraw");
		arbitraryPosOverride = ConfigIO.getBoolean("arbitraryPosOverride");
		
		appFont = new Font(Display.getCurrent(), "Monospaced", 9, SWT.NORMAL);
		if (appFont == null) {
			appFont = new Font(shell.getDisplay(), "Serif", 9, SWT.NORMAL);
		}
		if (appFont == null) {
			appFont = new Font(shell.getDisplay(), "Courier", 9, SWT.NORMAL);
		}
		
		// used as a default, "null" transformation to fall back to in order to do regular drawing.
		currentTransform = new Transform(shell.getDisplay());
		
		createContents();
		
		shell.setFont(appFont);
		
		if (!ShipIO.isNull(dataPath) && !ShipIO.isNull(resPath))
			ShipIO.fetchShipNames();
		
		shell.setMinimumSize(GRID_W*35, GRID_H*35);
		shell.open();
		
		sysDialog = new PropertiesWindow(shell);
		shipDialog = new ShipPropertiesWindow(shell);
		erDialog = new ErrorDialog(shell);
		
		shell.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				GRID_W = ((int) ((canvasBg.getBounds().width))/35);
				GRID_H = ((int) ((canvasBg.getBounds().height))/35);
				fd_canvas.right.offset = GRID_W*35;
				fd_canvas.bottom.offset = GRID_H*35;
				canvas.setSize(GRID_W*35, GRID_H*35);
				fd_bgCanvas.right.offset = GRID_W*35;
				fd_bgCanvas.bottom.offset = GRID_H*35;
				bgCanvas.setSize(GRID_W*35, GRID_H*35);
				
				if (grid != null)
					grid.setSize(GRID_W*35, GRID_H*35);
			}
		});
		
		shellStateChange = shell.getMaximized();
		
		display.timerExec(INTERVAL, new Runnable() {
			public void run() {
				if (canvas.isDisposed()) return;
				if (shellStateChange != shell.getMaximized()) {
					shellStateChange = shell.getMaximized();
					GRID_W = ((int) ((canvasBg.getBounds().width))/35);
					GRID_H = ((int) ((canvasBg.getBounds().height))/35);
					fd_canvas.right.offset = GRID_W*35;
					fd_canvas.bottom.offset = GRID_H*35;
					canvas.setSize(GRID_W*35, GRID_H*35);
					fd_bgCanvas.right.offset = GRID_W*35;
					fd_bgCanvas.bottom.offset = GRID_H*35;
					bgCanvas.setSize(GRID_W*35, GRID_H*35);
						
					if (grid != null)
						grid.setSize(GRID_W*35, GRID_H*35);
				}
					
				// === update info text fields; mousePos and rudimentary ship info
				mGridPosText.setText("(" + (int)(1+Math.floor(mousePos.x/35)) + ", " + (int)(1+Math.floor(mousePos.y/35)) + ")");
				mPosText.setText("(" + mousePos.x + ", " + mousePos.y + ")");
				if (ship != null)
					shipInfoText.setText("rooms: " + ship.rooms.size() + ",  doors: " + ship.doors.size());

				display.timerExec(INTERVAL, this);
			}
		});
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void createContents()
	{
		highlightColor = shell.getDisplay().getSystemColor(SWT.COLOR_GREEN);
		tempImage = SWTResourceManager.getImage(Main.class, "/org/eclipse/jface/dialogs/images/help.gif");
		
	// === Load images to a map for easy access
		
		tempImage = SWTResourceManager.getImage(Main.class, "/img/room.png");
		toolsMap.put("room", tempImage);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/door.png");
		toolsMap.put("door", tempImage);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/pointer.png");
		toolsMap.put("pointer", tempImage);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/mount.png");
		toolsMap.put("mount", tempImage);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/system.png");
		toolsMap.put("system", tempImage);
		
		pinImage = SWTResourceManager.getImage(Main.class, "/img/pin.png");
		tickImage = SWTResourceManager.getImage(Main.class, "/img/check.png");
		crossImage = SWTResourceManager.getImage(Main.class, "/img/cross.png");

	// === Menu bar
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

	// === File menu
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		Menu menu_file = new Menu(mntmFile);
		mntmFile.setMenu(menu_file);
	
		// === File -> New ship
		final MenuItem mntmNewShip = new MenuItem(menu_file, SWT.NONE);
		mntmNewShip.setText("New Ship \tCtrl + N");
		
		// === File -> Load ship
		new MenuItem(menu_file, SWT.SEPARATOR);
		final MenuItem mntmLoadShip = new MenuItem(menu_file, SWT.NONE);
		mntmLoadShip.setText("Load Ship...\tCtrl + L");
		
		// === File -> Open project
		final MenuItem mntmLoadShipProject = new MenuItem(menu_file, SWT.NONE);
		mntmLoadShipProject.setText("Open Project...\tCtrl + O");

		new MenuItem(menu_file, SWT.SEPARATOR);
		
		// === File -> Save project
		final MenuItem mntmSaveShip = new MenuItem(menu_file, SWT.NONE);
		mntmSaveShip.setText("Save Project \tCtrl + S");
		mntmSaveShip.setEnabled(false);
		
		// === File -> Save project as
		final MenuItem mntmSaveShipAs = new MenuItem(menu_file, SWT.NONE);
		mntmSaveShipAs.setText("Save Project As...");
		mntmSaveShipAs.setEnabled(false);
		
		// === File -> Export ship
		final MenuItem mntmExport = new MenuItem(menu_file, SWT.NONE);
		mntmExport.setText("Export Ship... \tCtrl + E");
		mntmExport.setEnabled(false);
		
		new MenuItem(menu_file, SWT.SEPARATOR);
		
		// === File -> Close project
		mntmClose = new MenuItem(menu_file, SWT.NONE);
		mntmClose.setText("Close Project");
		mntmClose.setEnabled(false);
		
	// === Edit menu
		
		MenuItem mntmEdit = new MenuItem(menu, SWT.CASCADE);
		mntmEdit.setText("Edit");
		Menu menu_edit = new Menu(mntmEdit);
		mntmEdit.setMenu(menu_edit);
		
		// === Edit -> Automatic door clean
		MenuItem mntmRemoveDoors = new MenuItem(menu_edit, SWT.CHECK);
		mntmRemoveDoors.setSelection(true);
		mntmRemoveDoors.setText("Automatic Door Cleanup");
		mntmRemoveDoors.setSelection(removeDoor);
		
		MenuItem mntmArbitraryPositionOverride = new MenuItem(menu_edit, SWT.CHECK);
		mntmArbitraryPositionOverride.setText("Arbitrary Position Overrides Pin");
		mntmArbitraryPositionOverride.setSelection(arbitraryPosOverride);
		
		new MenuItem(menu_edit, SWT.SEPARATOR);
		
		mntmConToPlayer = new MenuItem(menu_edit, SWT.NONE);
		mntmConToPlayer.setEnabled(false);
		mntmConToPlayer.setText("Convert To Player");
		
		mntmConToEnemy = new MenuItem(menu_edit, SWT.NONE);
		mntmConToEnemy.setEnabled(false);
		mntmConToEnemy.setText("Convert To Enemy");
		
	// === View menu
		
		MenuItem mntmView = new MenuItem(menu, SWT.CASCADE);
		mntmView.setText("View");
		Menu menu_view = new Menu(mntmView);
		mntmView.setMenu(menu_view);
		
		// === View -> Errors console
		MenuItem mntmOpenErrorsConsole = new MenuItem(menu_view, SWT.NONE);
		mntmOpenErrorsConsole.setText("Open Errors Console");
		
		new MenuItem(menu_view, SWT.SEPARATOR);
		
		// === View -> Show anchor
		final MenuItem mntmShowAnchor = new MenuItem(menu_view, SWT.CHECK);
		mntmShowAnchor.setText("Show Anchor \t&1");
		mntmShowAnchor.setSelection(showAnchor);
		
		// === View -> Show mounts
		final MenuItem mntmShowMounts = new MenuItem(menu_view, SWT.CHECK);
		mntmShowMounts.setText("Show Mounts \t&2");
		mntmShowMounts.setSelection(showMounts);
		
		// === View -> show rooms
		final MenuItem mntmShowRooms = new MenuItem(menu_view, SWT.CHECK);
		mntmShowRooms.setText("Show Rooms And Doors \t&3");
		mntmShowRooms.setSelection(showRooms);
		
		// === View -> graphics
		MenuItem mntmGraphics = new MenuItem(menu_view, SWT.CASCADE);
		mntmGraphics.setText("Graphics");
		
		Menu menu_graphics = new Menu(mntmGraphics);
		mntmGraphics.setMenu(menu_graphics);
		
		// === View -> graphics -> show hull
		final MenuItem mntmShowHull = new MenuItem(menu_graphics, SWT.CHECK);
		mntmShowHull.setText("Show Hull \t&4");
		mntmShowHull.setSelection(showHull);
		
		// === View -> graphics -> show floor
		mntmShowFloor = new MenuItem(menu_graphics, SWT.CHECK);
		mntmShowFloor.setText("Show Floor\t&5");
		mntmShowFloor.setSelection(showFloor);
		
		// === View -> graphics -> show shield
		mntmShowShield = new MenuItem(menu_graphics, SWT.CHECK);
		mntmShowShield.setText("Show Shield\t&6");
		mntmShowShield.setSelection(showShield);
		
		new MenuItem(menu_view, SWT.SEPARATOR);
		
		// === View -> load floor
		final MenuItem mntmLoadFloorGraphic = new MenuItem(menu_view, SWT.CHECK);
		mntmLoadFloorGraphic.setText("Load Floor Graphic");
		mntmLoadFloorGraphic.setSelection(loadFloor);
		
		// === View -> load shield
		final MenuItem mntmLoadShieldGraphic = new MenuItem(menu_view, SWT.CHECK);
		mntmLoadShieldGraphic.setText("Load Shield Graphic");
		mntmLoadShieldGraphic.setSelection(loadShield);
		
		// === View -> load system graphic
		MenuItem mntmLoadSystem = new MenuItem(menu_view, SWT.CHECK);
		mntmLoadSystem.setText("Load System Graphics");
		mntmLoadSystem.setSelection(loadSystem);
		
		new MenuItem(menu_view, SWT.SEPARATOR);
		
		// === View -> constant redraw
		final MenuItem mntmConstantRedraw = new MenuItem(menu_view, SWT.CHECK);
		mntmConstantRedraw.setText("Constant Redraw");
		mntmConstantRedraw.setSelection(constantRedraw);
		
	// === Tool bar
		
		// === Container - holds all the items on the left side of the screen
		Composite toolBarHolder = new Composite(shell, SWT.NONE);
		GridData gd_toolBarHolder = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		gd_toolBarHolder.minimumHeight = -1;
		gd_toolBarHolder.minimumWidth = -1;
		toolBarHolder.setLayoutData(gd_toolBarHolder);
		GridLayout gl_toolBarHolder = new GridLayout(2, false);
		gl_toolBarHolder.marginWidth = 0;
		gl_toolBarHolder.marginHeight = 0;
		toolBarHolder.setLayout(gl_toolBarHolder);

		// === Container -> Tools - tool bar containing the tool icons
		final ToolBar toolBar = new ToolBar(toolBarHolder, SWT.NONE);
		toolBar.setFont(appFont);
		GridData gd_toolBar = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		gd_toolBar.minimumHeight = -1;
		gd_toolBar.minimumWidth = -1;
		toolBar.setLayoutData(gd_toolBar);
		
		// === Container -> Tools -> Pointer
		tltmPointer = new ToolItem(toolBar, SWT.RADIO);
		tltmPointer.setWidth(60);
		tltmPointer.setSelection(true);
		tltmPointer.setImage(toolsMap.get("pointer"));
		tltmPointer.setToolTipText("Selection tool"
									+ShipIO.lineDelimiter+" -Click to selet an object"
									+ShipIO.lineDelimiter+" -Click and hold to move the object around"
									+ShipIO.lineDelimiter+" -For rooms, click on a corner and drag to resize the room" 
									+ShipIO.lineDelimiter+" -Right-click to assign a system to the selected room"
									+ShipIO.lineDelimiter+" -Double click on a room to set its' system's level and power"
									+ShipIO.lineDelimiter+" -For weapon mounts, hull and shields, press down Shift for precision mode");
		
		// === Container -> Tools -> Room creation
		tltmRoom = new ToolItem(toolBar, SWT.RADIO);
		tltmRoom.setWidth(60);
		tltmRoom.setToolTipText("Room creation tool"
								+ShipIO.lineDelimiter+" -Click and drag to create a room"
								+ShipIO.lineDelimiter+" -Hold down Shift and click to split rooms");
		tltmRoom.setImage(toolsMap.get("room"));
		
		// === Container -> Tools -> Door creation
		tltmDoor = new ToolItem(toolBar, SWT.RADIO);
		tltmDoor.setWidth(60);
		tltmDoor.setToolTipText("Door creation tool"
								+ShipIO.lineDelimiter+" - Hover over an edge of a room and click to place door");
		tltmDoor.setImage(toolsMap.get("door"));
		
		// === Container -> Tools -> Weapon mounting
		tltmMount = new ToolItem(toolBar, SWT.RADIO);
		tltmMount.setWidth(60);
		tltmMount.setToolTipText("Weapon mounting tool"
									+ShipIO.lineDelimiter+" -Click to place a weapon mount"
									+ShipIO.lineDelimiter+" -Right-click to change the mount's rotation"
									+ShipIO.lineDelimiter+" -Shift-click to mirror the mount along its axis"
									+ShipIO.lineDelimiter+" -Shift-right-click to change the direction in which the weapon opens"
									+ShipIO.lineDelimiter+" (the last three also work with Selection Tool)");
		tltmMount.setImage(toolsMap.get("mount"));

		// === Container -> Tools -> System operating slot
		tltmSystem = new ToolItem(toolBar, SWT.RADIO);
		tltmSystem.setWidth(60);
		tltmSystem.setToolTipText("System operating station tool"
									+ShipIO.lineDelimiter+" - Click to place an operating station (only mannable systems + medbay)"
									+ShipIO.lineDelimiter+" - Right-click to reset the station to default"
									+ShipIO.lineDelimiter+" - Shift-click to change facing of the station");
		tltmSystem.setImage(toolsMap.get("system"));

		tltmPointer.setEnabled(false);
		tltmRoom.setEnabled(false);
		tltmDoor.setEnabled(false);
		tltmMount.setEnabled(false);
		tltmSystem.setEnabled(false);
		
		// === Container -> buttonComposite
		Composite composite = new Composite(toolBarHolder, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_composite.heightHint = 30;
		gd_composite.minimumWidth = -1;
		gd_composite.minimumHeight = -1;
		composite.setLayoutData(gd_composite);
		GridLayout gl_composite = new GridLayout(13, false);
		gl_composite.marginTop = 2;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		
		Label label = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		
		// === Container -> buttonComposite -> Hull image button
		btnHull = new Button(composite, SWT.NONE);
		GridData gd_btnHull = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_btnHull.minimumWidth = 70;
		btnHull.setLayoutData(gd_btnHull);
		btnHull.setFont(appFont);
		btnHull.setEnabled(false);
		btnHull.setText("Hull");

		// === Container -> buttonComposite -> shield image button
		btnShields = new Button(composite, SWT.NONE);
		GridData gd_btnShields = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_btnShields.minimumWidth = 70;
		btnShields.setLayoutData(gd_btnShields);
		btnShields.setFont(appFont);
		btnShields.setToolTipText("Shield is aligned in relation to rooms. Place a room before choosing shield graphic.");
		btnShields.setEnabled(false);
		btnShields.setText("Shields");

		// === Container -> buttonComposite -> floor image button
		btnFloor = new Button(composite, SWT.NONE);
		GridData gd_btnFloor = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_btnFloor.minimumWidth = 70;
		btnFloor.setLayoutData(gd_btnFloor);
		btnFloor.setFont(appFont);
		btnFloor.setEnabled(false);
		btnFloor.setText("Floor");

		// === Container -> buttonComposite -> cloak image button
		btnCloak = new Button(composite, SWT.NONE);
		GridData gd_btnCloak = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_btnCloak.minimumWidth = 70;
		btnCloak.setLayoutData(gd_btnCloak);
		btnCloak.setSize(70, 25);
		btnCloak.setFont(appFont);
		btnCloak.setEnabled(false);
		btnCloak.setText("Cloak");

		// === Container -> buttonComposite -> miniship image button
		btnMiniship = new Button(composite, SWT.NONE);
		GridData gd_btnMiniship = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_btnMiniship.minimumWidth = 70;
		btnMiniship.setLayoutData(gd_btnMiniship);
		btnMiniship.setSize(70, 25);
		btnMiniship.setEnabled(false);
		btnMiniship.setFont(appFont);
		btnMiniship.setText("MiniShip");
		
		// === Container -> buttonComposite -> Popup Menu
		menu_imageBtns = new Menu(shell);
		btnHull.setMenu(menu_imageBtns);
		btnShields.setMenu(menu_imageBtns);
		btnFloor.setMenu(menu_imageBtns);
		btnCloak.setMenu(menu_imageBtns);
		btnMiniship.setMenu(menu_imageBtns);
		
		mntmPath = new MenuItem(menu_imageBtns, SWT.NONE);
		mntmPath.setEnabled(false);
		
		new MenuItem(menu_imageBtns, SWT.SEPARATOR);

		// === Container -> buttonComposite -> Popup Menu -> Reset Path
		mntmUnload = new MenuItem(menu_imageBtns, SWT.NONE);
		mntmUnload.setText("Unload Image");

		// === Container -> buttonComposite -> Popup Menu -> Show File
		mntmShowFile = new MenuItem(menu_imageBtns, SWT.NONE);
		mntmShowFile.setText("Show Directory");
		
		Label label_1 = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
		label_1.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		
		// === Container -> Properties
		final Button btnShipProperties = new Button(composite, SWT.NONE);
		btnShipProperties.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnShipProperties.setFont(appFont);
		btnShipProperties.setText("Properties");
		btnShipProperties.setEnabled(false);
		
		Label label_2 = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
		label_2.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		
		// === Container -> set position composite
		Composite coSetPosition = new Composite(composite, SWT.NONE);
		coSetPosition.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		GridLayout gl_coSetPosition = new GridLayout(7, false);
		gl_coSetPosition.marginWidth = 0;
		gl_coSetPosition.marginHeight = 0;
		coSetPosition.setLayout(gl_coSetPosition);
		
		// === Cotnainer -> set position composite -> X
		Label lblX = new Label(coSetPosition, SWT.NONE);
		lblX.setFont(appFont);
		GridData gd_lblX = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_lblX.horizontalIndent = 5;
		lblX.setLayoutData(gd_lblX);
		lblX.setText("X:");

		// === Cotnainer -> set position composite -> X buttons container
		Composite setPosXBtnsCo = new Composite(coSetPosition, SWT.NONE);
		GridLayout gl_setPosXBtnsCo = new GridLayout(2, false);
		gl_setPosXBtnsCo.horizontalSpacing = 0;
		gl_setPosXBtnsCo.verticalSpacing = 0;
		gl_setPosXBtnsCo.marginWidth = 0;
		gl_setPosXBtnsCo.marginHeight = 0;
		setPosXBtnsCo.setLayout(gl_setPosXBtnsCo);
		setPosXBtnsCo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));

		// === Cotnainer -> set position composite -> X buttons container -> X minus
		btnXminus = new Button(setPosXBtnsCo, SWT.CENTER);
		btnXminus.setToolTipText("Subtract 35");
		btnXminus.setEnabled(false);
		GridData gd_btnXminus = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_btnXminus.widthHint = 18;
		btnXminus.setLayoutData(gd_btnXminus);
		btnXminus.setFont(appFont);
		btnXminus.setText("-");
		btnXminus.setBounds(0, 0, 18, 25);

		// === Cotnainer -> set position composite -> X buttons container -> X plus
		btnXplus = new Button(setPosXBtnsCo, SWT.CENTER);
		btnXplus.setToolTipText("Add 35");
		btnXplus.setEnabled(false);
		GridData gd_btnXplus = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_btnXplus.widthHint = 18;
		btnXplus.setLayoutData(gd_btnXplus);
		btnXplus.setBounds(0, 0, 75, 25);
		btnXplus.setFont(appFont);
		btnXplus.setText("+");

		// === Cotnainer -> set position composite -> X text field
		txtX = new Text(coSetPosition, SWT.BORDER);
		txtX.setEnabled(false);
		txtX.setFont(appFont);
		txtX.setTextLimit(5);
		txtX.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));

		// === Cotnainer -> set position composite -> Y
		Label lblY = new Label(coSetPosition, SWT.NONE);
		GridData gd_lblY = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_lblY.horizontalIndent = 5;
		lblY.setLayoutData(gd_lblY);
		lblY.setFont(appFont);
		lblY.setText("Y:");

		// === Cotnainer -> set position composite -> Y buttons container
		Composite setPosYBtnsCo = new Composite(coSetPosition, SWT.NONE);
		GridLayout gl_setPosYBtnsCo = new GridLayout(2, false);
		gl_setPosYBtnsCo.verticalSpacing = 0;
		gl_setPosYBtnsCo.marginWidth = 0;
		gl_setPosYBtnsCo.marginHeight = 0;
		gl_setPosYBtnsCo.horizontalSpacing = 0;
		setPosYBtnsCo.setLayout(gl_setPosYBtnsCo);

		// === Cotnainer -> set position composite -> Y buttons container -> Y minus
		btnYminus = new Button(setPosYBtnsCo, SWT.CENTER);
		btnYminus.setToolTipText("Subtract 35");
		GridData gd_btnYminus = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_btnYminus.widthHint = 18;
		btnYminus.setLayoutData(gd_btnYminus);
		btnYminus.setFont(appFont);
		btnYminus.setText("-");
		btnYminus.setEnabled(false);

		// === Cotnainer -> set position composite -> Y buttons container -> Y plus
		btnYplus = new Button(setPosYBtnsCo, SWT.CENTER);
		btnYplus.setToolTipText("Add 35");
		GridData gd_btnYplus = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_btnYplus.widthHint = 18;
		btnYplus.setLayoutData(gd_btnYplus);
		btnYplus.setFont(appFont);
		btnYplus.setText("+");
		btnYplus.setEnabled(false);

		// === Cotnainer -> set position composite -> Y text field
		txtY = new Text(coSetPosition, SWT.BORDER);
		txtY.setEnabled(false);
		txtY.setFont(appFont);
		txtY.setTextLimit(5);
		txtY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		new Label(coSetPosition, SWT.NONE);
		
		Label label_3 = new Label(composite, SWT.SEPARATOR | SWT.RIGHT);
		label_3.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true, 1, 1));

		// === Cotnainer -> state buttons composite
		Composite stateBtnsCo = new Composite(composite, SWT.NONE);
		GridLayout gl_stateBtnsCo = new GridLayout(2, false);
		gl_stateBtnsCo.marginWidth = 0;
		gl_stateBtnsCo.verticalSpacing = 0;
		gl_stateBtnsCo.marginHeight = 0;
		stateBtnsCo.setLayout(gl_stateBtnsCo);
		stateBtnsCo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));

		// === Cotnainer -> state buttons composite -> cloaked
		btnCloaked = new Button(stateBtnsCo, SWT.TOGGLE | SWT.CENTER);
		btnCloaked.setEnabled(false);
		btnCloaked.setFont(appFont);
		btnCloaked.setImage(SWTResourceManager.getImage(Main.class, "/img/smallsys/smallcloak.png"));
		btnCloaked.setToolTipText("View the cloaked version.");
		btnCloaked.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// === Cotnainer -> state buttons composite -> pirate
		btnPirate = new Button(stateBtnsCo, SWT.TOGGLE | SWT.CENTER);
		btnPirate.setEnabled(false);
		btnPirate.setFont(appFont);
		btnPirate.setToolTipText("View the pirate version.");
		btnPirate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnPirate.setImage(SWTResourceManager.getImage(Main.class, "/img/pirate.png"));
		new Label(composite, SWT.NONE);
		
		// Info label
		/*
		helpIcon = new Label(toolBarHolder, SWT.NONE);
		helpIcon.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		helpIcon.setFont(appFont);
		helpIcon.setImage(SWTResourceManager.getImage(Main.class, "/img/message_info.gif"));
		helpIcon.setToolTipText(" -Use the Q, W, E, A, S keys to quickly select the tools (use Alt+[Key] for the first press)."
			+ShipIO.lineDelimiter+" -Press Delete or Shift+D to delete selected object (except hull and shields)"
			+ShipIO.lineDelimiter+" -Click on the anchor and hold to move the entire ship around"
			+ShipIO.lineDelimiter+" -Press down Shift key while dragging the anchor to move only the anchor w/o moving the ship"
			+ShipIO.lineDelimiter+" -Right-click on the anchor and drag to set the vertical offset of the ship");
		*/
		
	// === Canvas
		
		Composite canvasHolder = new Composite(shell, SWT.NONE);
		canvasHolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 4));
		canvasHolder.setLayout(new FormLayout());
		
		// Main screen where ships are displayed
		canvas = new Canvas(canvasHolder, SWT.NONE | SWT.TRANSPARENT | SWT.BORDER | SWT.DOUBLE_BUFFERED);
		Color c = new Color(shell.getDisplay(), 96, 96, 96);
		canvas.setBackground(c);
		c.dispose();
		fd_canvas = new FormData();
		fd_canvas.bottom = new FormAttachment(0, GRID_H*35);
		fd_canvas.right = new FormAttachment(0, GRID_W*35);
		fd_canvas.top = new FormAttachment(0);
		fd_canvas.left = new FormAttachment(0);
		canvas.setLayoutData(fd_canvas);

		layeredPainter = new LayeredPainter();
		canvas.addPaintListener(layeredPainter);
		
		grid = new Grid(GRID_W, GRID_H);
		
		anchor = new Anchor();
		anchor.setLocation(0,0,false);
		anchor.setSize(GRID_W*35, GRID_H*35);
		layeredPainter.add(anchor, LayeredPainter.ANCHOR);
		
		cursor = new CursorBox();
		cursor.setBorderThickness(2);
		cursor.setSize(35, 35);
		cursor.setBorderColor(new RGB(0,0,255));
		layeredPainter.add(cursor, LayeredPainter.SELECTION);

		SystemBox tempBox = new SystemBox(Systems.PILOT);
		tempBox.setImage("/img/systems/s_pilot_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.DOORS);
		tempBox.setImage("/img/systems/s_doors_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.SENSORS);
		tempBox.setImage("/img/systems/s_sensors_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.OXYGEN);
		tempBox.setImage("/img/systems/s_oxygen_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.MEDBAY);
		tempBox.setImage("/img/systems/s_medbay_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.SHIELDS);
		tempBox.setImage("/img/systems/s_shields_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.WEAPONS);
		tempBox.setImage("/img/systems/s_weapons_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.ENGINES);
		tempBox.setImage("/img/systems/s_engines_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.DRONES);
		tempBox.setImage("/img/systems/s_drones_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.TELEPORTER);
		tempBox.setImage("/img/systems/s_teleporter_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.CLOAKING);
		tempBox.setImage("/img/systems/s_cloaking_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		tempBox = new SystemBox(Systems.ARTILLERY);
		tempBox.setImage("/img/systems/s_artillery_overlay.png", true);
		systemsMap.put(tempBox.getSystemName(), tempBox);
		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
		tempBox.setVisible(false);
		
		// canvas supposed to handle drawing of non-dynamic elements of the display (hull, grid, shields, etc), but not really used (only draws grid, IIRC)
		// REMINDER to be scrapped, find a dedicated 2d graphical library and use that
		bgCanvas = new Canvas(canvasHolder, SWT.NONE | SWT.BORDER);
		c = new Color(shell.getDisplay(), 96, 96, 96);
		bgCanvas.setBackground(c);
		c.dispose();
		fd_bgCanvas = new FormData();
		fd_bgCanvas.bottom = new FormAttachment(0, GRID_H*35);
		fd_bgCanvas.right = new FormAttachment(0, GRID_W*35);
		fd_bgCanvas.top = new FormAttachment(0);
		fd_bgCanvas.left = new FormAttachment(0);
		bgCanvas.setLayoutData(fd_bgCanvas);
		
		canvasBg = new Label(canvasHolder, SWT.NONE);
		c = canvas.getDisplay().getSystemColor((SWT.COLOR_DARK_GRAY));
		canvasBg.setBackground(c);
		c.dispose();
		FormData fd_canvasBg = new FormData();
		fd_canvasBg.bottom = new FormAttachment(100);
		fd_canvasBg.right = new FormAttachment(100);
		fd_canvasBg.top = new FormAttachment(0);
		fd_canvasBg.left = new FormAttachment(0);
		canvasBg.setLayoutData(fd_canvasBg);
		
	// === System assignment context menu
			
		// === Systems
		menuSystem = new Menu(canvas);
			
		// === Systems -> Empty
		final MenuItem mntmEmpty = new MenuItem(menuSystem, SWT.RADIO);
		mntmEmpty.setSelection(true);
		mntmEmpty.setText("None");
		
		// === Systems -> Systems
		MenuItem mntmSystems = new MenuItem(menuSystem, SWT.CASCADE);
		mntmSystems.setText("Systems");
		Menu menu_systems = new Menu(mntmSystems);
		mntmSystems.setMenu(menu_systems);

		// === Systems -> Systems -> Oxygen
		final MenuItem mntmOxygen = new MenuItem(menu_systems, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smalloxygen.png");
		mntmOxygen.setImage(tempImage);
		mntmOxygen.setText("Oxygen");

		// === Systems -> Systems -> Medbay
		final MenuItem mntmMedbay = new MenuItem(menu_systems, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallmedbay.png");
		mntmMedbay.setImage(tempImage);
		mntmMedbay.setText("Medbay");

		// === Systems -> Systems -> Shields
		final MenuItem mntmShields = new MenuItem(menu_systems, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallshields.png");
		mntmShields.setImage(tempImage);
		mntmShields.setText("Shields");

		// === Systems -> Systems -> Weapons
		final MenuItem mntmWeapons = new MenuItem(menu_systems, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallweapons.png");
		mntmWeapons.setImage(tempImage);
		mntmWeapons.setText("Weapons");

		// === Systems -> Systems -> Engines
		final MenuItem mntmEngines = new MenuItem(menu_systems, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallengines.png");
		mntmEngines.setImage(tempImage);
		mntmEngines.setText("Engines");

		// === Systems -> Subsystems
		MenuItem mntmSubsystems = new MenuItem(menuSystem, SWT.CASCADE);
		mntmSubsystems.setText("Subsystems");
		Menu menu_subsystems = new Menu(mntmSubsystems);
		mntmSubsystems.setMenu(menu_subsystems);

		// === Systems -> Subsystems -> Pilot
		final MenuItem mntmPilot = new MenuItem(menu_subsystems, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallpilot.png");
		mntmPilot.setImage(tempImage);
		mntmPilot.setText("Pilot");

		// === Systems -> Subsystems -> Doors
		final MenuItem mntmDoors = new MenuItem(menu_subsystems, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smalldoor.png");
		mntmDoors.setImage(tempImage);
		mntmDoors.setText("Doors");

		// === Systems -> Subsystems -> Sensors
		final MenuItem mntmSensors = new MenuItem(menu_subsystems, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallsensors.png");
		mntmSensors.setImage(tempImage);
		mntmSensors.setText("Sensors");

		// === Systems -> Special
		MenuItem mntmSpecial = new MenuItem(menuSystem, SWT.CASCADE);
		mntmSpecial.setText("Special");
		Menu menu_special = new Menu(mntmSpecial);
		mntmSpecial.setMenu(menu_special);

		// === Systems -> Special -> Drones
		final MenuItem mntmDrones = new MenuItem(menu_special, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smalldrones.png");
		mntmDrones.setImage(tempImage);
		mntmDrones.setText("Drones");

		// === Systems -> Special -> Teleporter
		final MenuItem mntmTeleporter = new MenuItem(menu_special, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallteleporter.png");
		mntmTeleporter.setImage(tempImage);
		mntmTeleporter.setText("Teleporter");

		// === Systems -> Special -> Cloaking
		final MenuItem mntmCloaking = new MenuItem(menu_special, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallcloak.png");
		mntmCloaking.setImage(tempImage);
		mntmCloaking.setText("Cloaking");

		// === Systems -> Special -> Artillery
		final MenuItem mntmArtillery = new MenuItem(menu_special, SWT.RADIO);
		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallartillery.png");
		mntmArtillery.setImage(tempImage);
		mntmArtillery.setText("Artillery");
		
		new MenuItem(menuSystem, SWT.SEPARATOR);
		
		// === Systems -> Set System Image
		final MenuItem mntmSysImage = new MenuItem(menuSystem, SWT.NONE);
		mntmSysImage.setEnabled(false);
		mntmSysImage.setText("Set System Image...");
		
		// === Text Info Fields
		
		Composite textHolder = new Composite(shell, SWT.NONE);
		GridData gd_textHolder = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_textHolder.heightHint = 18;
		textHolder.setLayoutData(gd_textHolder);
		textHolder.setLayout(new FormLayout());
		
		// === Position of the pointer on the grid
		mGridPosText = new Label(textHolder, SWT.BORDER | SWT.CENTER);
		mGridPosText.setFont(appFont);
		FormData fd_mGridPosText = new FormData();
		fd_mGridPosText.bottom = new FormAttachment(100);
		fd_mGridPosText.top = new FormAttachment(0);
		fd_mGridPosText.left = new FormAttachment(0);
		mGridPosText.setLayoutData(fd_mGridPosText);
		
		mPosText = new Label(textHolder, SWT.BORDER | SWT.CENTER);
		mPosText.setFont(appFont);
		FormData fd_mPosText = new FormData();
		fd_mPosText.bottom = new FormAttachment(100);
		fd_mPosText.top = new FormAttachment(mGridPosText, 0, SWT.TOP);
		fd_mPosText.left = new FormAttachment(mGridPosText, 6);
		mPosText.setLayoutData(fd_mPosText);
		
		// === Number of rooms and doors in the ship
		shipInfoText = new Label(textHolder, SWT.BORDER);
		fd_mPosText.right = new FormAttachment(shipInfoText, -6);
		fd_mGridPosText.right = new FormAttachment(shipInfoText, -84);
		shipInfoText.setFont(appFont);
		FormData fd_shipInfoText = new FormData();
		fd_shipInfoText.bottom = new FormAttachment(100);
		fd_shipInfoText.top = new FormAttachment(0);
		fd_shipInfoText.left = new FormAttachment(0, 129);
		shipInfoText.setLayoutData(fd_shipInfoText);
		
		// === Status bar
		text = new Label(textHolder, SWT.WRAP | SWT.BORDER);
		fd_shipInfoText.right = new FormAttachment(text, -6);
		text.setFont(appFont);
		FormData fd_text = new FormData();
		fd_text.bottom = new FormAttachment(100);
		fd_text.right = new FormAttachment(100);
		fd_text.left = new FormAttachment(0, 265);
		fd_text.top = new FormAttachment(0);
		text.setLayoutData(fd_text);
		new Label(shell, SWT.NONE);
		
		shell.pack();
		//shell.setMinimumSize(shell.getSize());
		
	//=============================================================
	//=== LISTENERS
		// === BOOKMARK: PAINT
		bgCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e)
			{
				Color c;
				Point p;
				Point pt;
				
				if (canvasActive) {
					// === DRAW SHIELD IMAGE
					if (showShield) {
						// find geometrical center of the ship's rooms
						if (ship.rooms.size() > 0) {
							p = ship.findLowBounds();
							pt = ship.findHighBounds();
						} else {
							p = new Point(ship.imageRect.x, ship.imageRect.y);
							pt = new Point(ship.imageRect.width, ship.imageRect.height);
						}
						pt.x = (p.x+pt.x)/2;
						pt.y = (p.y+pt.y)/2;
						
						if (shieldImage != null && !shieldImage.isDisposed() && loadShield) {
							if (ship.isPlayer) {
								e.gc.drawImage(shieldImage, shieldEllipse.x, shieldEllipse.y);
							} else {
								e.gc.drawImage(shieldImage,  0, 0, shieldImage.getBounds().width, shieldImage.getBounds().height,
										shieldEllipse.x,
										shieldEllipse.y,
										shieldEllipse.width, shieldEllipse.height);
							}
							e.gc.drawPoint(pt.x, pt.y);
					// if no shield graphic is loaded or the image doesn't exist, draw a placeholder
						} else {
							c = new Color(e.display, 16, 96, 255);
							e.gc.setBackground(c);
							e.gc.setAlpha(36);
							e.gc.fillOval(shieldEllipse.x, shieldEllipse.y, shieldEllipse.width, shieldEllipse.height);
							e.gc.setAlpha(255);
							c.dispose();
						}
					}
				}
			}
		});
		
		// the method used to draw stuff on the main display.
		// I'm pretty sure I should NOT be using it this way, putting so many conditions in there, but then I can't really think of another way to do it.
		// Split into more paintControl methods and redraw them separately only when needed to save a bit on performance?
		
		/*
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e)
			{
				Rectangle tempRect = null;
				Rectangle tempDoor = null;
				FTLMount tempMount = null;
				FTLRoom tempFTLRoom = null;
				Color c;
				Point p;
				Image rotatedImg = null;
				Image flippedImg = null;

				e.gc.setFont(appFont);
				
				if (canvasActive) {
					// === DRAW ALREADY PLACES MOUNTS
					if (showMounts) {
						//int index = 0;
						int strips = 0;
						FTLItem it = null;
						FTLMount m = null;
						
						for (int i=0; i < ship.mounts.size(); i++) {
							m = ship.mounts.get(i);
							if (i < ship.weaponSet.size() && !ship.weaponsBySet) {
								it = ShipIO.getItem(ship.weaponSet.get(i));
								if (m != null && it != null) {
									strips = weaponStripMap.get(it.img);
									
									e.gc.setAlpha(255);
									if (!m.mirror) {
										if (m.rotate) { // player - horizontal - have to rotate 90 degrees
											rotatedImg = rotated.get(i);
									        
											e.gc.drawImage(rotatedImg, 0, 0, rotatedImg.getBounds().width, rotatedImg.getBounds().height/strips,
													m.rect.x+m.rect.width/2-rotatedImg.getBounds().width/2,
													m.rect.y+m.rect.height/2-rotatedImg.getBounds().height/strips/2,
													rotatedImg.getBounds().width,
													rotatedImg.getBounds().height/strips);
										} else { // enemy - vertical - leave them be, don't dispose since it's the base image in the map
											rotatedImg = weaponImgMap.get(it.blueprint);
											e.gc.drawImage(rotatedImg, 0, 0, rotatedImg.getBounds().width/strips, rotatedImg.getBounds().height,
													m.rect.x+m.rect.width/2-rotatedImg.getBounds().width/strips/2,
													m.rect.y+m.rect.height/2-rotatedImg.getBounds().height/2,
													rotatedImg.getBounds().width/strips,
													rotatedImg.getBounds().height);
										}
									} else {
										if (m.rotate) { // player - horizontal - rotate, flip along H axis
											flippedImg = rotatedFlipped.get(i);
									        
											e.gc.drawImage(flippedImg, 0, flippedImg.getBounds().height/strips*(strips-1),
													flippedImg.getBounds().width, flippedImg.getBounds().height/strips,
													m.rect.x+m.rect.width/2-flippedImg.getBounds().width/2,
													m.rect.y+m.rect.height/2-flippedImg.getBounds().height/strips/2,
													flippedImg.getBounds().width,
													flippedImg.getBounds().height/strips);
											//flippedImg.dispose();
										} else { // enemy - vertical - don't rotate, flip along V axis
											flippedImg = flipped.get(i);
									        
											e.gc.drawImage(flippedImg, flippedImg.getBounds().width/strips*(strips-1), 0,
													flippedImg.getBounds().width/strips, flippedImg.getBounds().height,
													m.rect.x+m.rect.width/2-flippedImg.getBounds().width/strips/2,
													m.rect.y+m.rect.height/2-flippedImg.getBounds().height/2,
													flippedImg.getBounds().width/strips,
													flippedImg.getBounds().height);
										}
									}
								}
							} else {
								// draw placeholders
								
								c = (selectedMount == m) ? new Color(e.display, 128, 0, 128) : new Color(e.display, 255, 255, 0);
								e.gc.setForeground(c);
								c.dispose();
								c = (selectedMount == m) ? new Color(e.display, 128, 0, 128) : new Color(e.display, 128, 128, 0);
								e.gc.setBackground(c);
								c.dispose();
								
								e.gc.setLineWidth(2);
								e.gc.setAlpha(255);
								e.gc.drawRectangle(m.rect);
								e.gc.setAlpha(192);
								e.gc.fillRectangle(m.rect);
							}
						}
					}
					
				// === DRAW SHIP HULL IMAGE
					e.gc.setAlpha(btnCloaked.getSelection() ? 64 : 255);
					if (showHull && hullImage != null && !hullImage.isDisposed())
						e.gc.drawImage(hullImage, ship.imageRect.x, ship.imageRect.y);
					if (showFloor && floorImage != null && !floorImage.isDisposed() && loadFloor && hullImage != null && !hullImage.isDisposed())
						e.gc.drawImage(floorImage, 0, 0, floorImage.getBounds().width, floorImage.getBounds().height, ship.imageRect.x, ship.imageRect.y, ship.imageRect.width, ship.imageRect.height);
					if (btnCloaked.getSelection()) {
						e.gc.setAlpha(255);
						e.gc.drawImage(cloakImage, 0, 0, cloakImage.getBounds().width, cloakImage.getBounds().height,
										ship.imageRect.x-10, ship.imageRect.y-10, ship.imageRect.width+20, ship.imageRect.height+20);
					}
					
				// === DRAW ALREADY PLACED ROOMS (INSIDES)
					// drawn separetely, so that room borders are drawn over the grid lines, looks nicer that way.
					if (showRooms) {
						e.gc.setAlpha(btnCloaked.getSelection() ? 128 : 255);
						for (FTLRoom rm : ship.rooms) {
							c = new Color(e.display, 230, 225, 220);
							e.gc.setBackground(c);
							c.dispose();
							// draw regular room color
							e.gc.fillRectangle(rm.getBounds().x, rm.getBounds().y, rm.getBounds().width, rm.getBounds().height);
							// draw room images
							if (loadSystem && !ShipIO.isNull(rm.img) && rm.sysImg != null && !rm.sysImg.isDisposed()) {
								e.gc.drawImage(rm.sysImg, 0, 0, rm.sysImg.getBounds().width, rm.sysImg.getBounds().height, rm.getBounds().x, rm.getBounds().y, rm.getBounds().width, rm.getBounds().height);
							}
							// draw disabled-on-start overlay
							if (!ship.startMap.get(rm.sys) && !rm.sys.equals(Systems.EMPTY)) {
								c = new Color(e.display, 128, 0, 0);
								e.gc.setBackground(c);
								c.dispose();
								e.gc.setAlpha(btnCloaked.getSelection() ? 64 : 128);
								e.gc.fillRectangle(rm.getBounds().x, rm.getBounds().y, rm.getBounds().width, rm.getBounds().height);
								e.gc.setAlpha(btnCloaked.getSelection() ? 128 : 255);
							}
							// draw slot overlay
							if (rm.slot != -2) {
								e.gc.setAlpha(btnCloaked.getSelection() ? 96 : 192);
								c = new Color(e.display, 0, 128, 128);
								e.gc.setBackground(c);
								if (!rm.sys.equals(Systems.MEDBAY)) {
									e.gc.fillRectangle(getStationDirected(rm));
								} else {
									e.gc.fillRectangle(getRectFromStation(rm));
								}
								c.dispose();
								e.gc.setAlpha(btnCloaked.getSelection() ? 128 : 255);
							}
							// draw room id
							if (debug) {
								c = new Color(e.display, 0,0,0);
								e.gc.setForeground(c);
								c.dispose();
								e.gc.drawString(""+rm.id, rm.getBounds().x+5, rm.getBounds().y+rm.getBounds().height-17, true);
							}
						}
					}
					
				// === ROOM CREATION TOOL
					if (tltmRoom.getSelection() && onCanvas && !modShift && phantomRect != null && leftMouseDown) {
						int signX = (phantomRect.x >= mousePos.x) ? (-1) : (0);
						int signY = (phantomRect.y >= mousePos.y) ? (-1) : (0);
						e.gc.setAlpha(255);
						e.gc.setLineWidth(3);
						
						allowRoomPlacement = (mousePosLastClick.x > ship.anchor.x && mousePosLastClick.y > ship.anchor.y && mousePos.x > ship.anchor.x+1 && mousePos.y > ship.anchor.y+1 && !doesRectOverlap(fixRect(phantomRect), null));
						if (allowRoomPlacement) {
							c = new Color(e.display, 0, 255, 0);
							e.gc.setForeground(c);
							c.dispose();
							c = new Color(e.display, 230, 225, 220);
							e.gc.setBackground(c);
							e.gc.fillRectangle(phantomRect);
							c.dispose();
						} else {
							c = new Color(e.display, 255, 0 , 0);
							e.gc.setForeground(c);
							c.dispose();
						}
						e.gc.drawRectangle(phantomRect.x + 2*(signX+1), phantomRect.y + 2*(signY+1), phantomRect.width - 4*(signX+1), phantomRect.height - 4*(signY+1));
					}
					
				// === DRAW GRID
					c = new Color(e.display, 128, 128, 128);
					e.gc.setForeground(c);
					c.dispose();
					e.gc.setAlpha(255);
					e.gc.setLineWidth(1);
					
					for (int i=0; i <= GRID_W; i++)
						e.gc.drawLine(i*35, 0, i*35, canvas.getSize().y);
					for (int i=0; i <= GRID_H; i++)
						e.gc.drawLine(0, i*35, canvas.getSize().x, i*35);
					
				// === DRAW ALREADY PLACED ROOMS (BORDERS) AND SYSTEM ICONS
					if (showRooms) {
						e.gc.setAlpha(btnCloaked.getSelection() ? 128 : 255);
						for (FTLRoom rm : ship.rooms) {
							e.gc.setLineWidth(4);
							c = new Color(e.display, 0,0,0);
							e.gc.setForeground(c);
							c.dispose();
							e.gc.drawRectangle(rm.getBounds().x, rm.getBounds().y, rm.getBounds().width, rm.getBounds().height);
							if (rm.sys != Systems.EMPTY) {
								e.gc.drawImage(systemsMap.get(rm.sys), rm.getBounds().x+(rm.getBounds().width-32)/2, rm.getBounds().y+(rm.getBounds().height-32)/2);
							}
						}
						
				// === DRAW ALREADY PLACED DOORS
						e.gc.setLineWidth(1);
						for (FTLDoor dr : ship.doors) {
							dr.drawDoor(e);
						}
					}
					
					if (showMounts) {
						int i = -1;
						for (FTLMount m : ship.mounts) {
							i++;
							if (ship.weaponsBySet || i >= ship.weaponSet.size()) {
								e.gc.setLineWidth(1);
								c = new Color(e.display, 255, 255, 0);
								e.gc.setForeground(c);
								c.dispose();
								c = new Color(e.display, 128, 0, 128);
								e.gc.setBackground(c);
								c.dispose();
								FTLMount.drawMirror(e, m.rotate, m.mirror, m.rect);	
							}
							
							e.gc.setAlpha(255);
							e.gc.setLineWidth(2);
							c = (selectedMount == m) ? new Color(e.display, 255, 255, 0) : new Color(e.display, 128, 0, 128);
							e.gc.setForeground(c);
							c.dispose();
							FTLMount.drawDirection(e, m.slide, m.rect);
						}
					}
					

				// === DRAW ROOM & DOOR HIGHLIGHT FOR TOOLS
					if (onCanvas && canvasActive) {
						if (showMounts)
							tempMount = getMountFromMouse();
						if (showRooms) {
							tempRect = getRectFromMouse();
							tempDoor = getDoorFromMouse();
							tempFTLRoom = getRoomContainingRect(tempRect);
						}
						
						// === pointer tool highlights
						if (tltmPointer.getSelection() && !moveAnchor) {

							if (highlightColor != null && !highlightColor.isDisposed())
								highlightColor.dispose();
							highlightColor = new Color(e.display, 0, 0, 255);
							e.gc.setForeground(highlightColor);
							e.gc.setAlpha(255);
							
							// door highlight
							if (showRooms && tempDoor != null && !moveSelected && !resizeSelected) {
								e.gc.setLineWidth(3);
								e.gc.drawRectangle(tempDoor);
								
							// mount highlight
							} else if (showMounts && (doImagesContain(mousePos) || tempMount != null) && !moveSelected && !resizeSelected) {
								e.gc.setLineWidth(3);
								if (!doImagesContain(mousePos) || ship.weaponsBySet) {
									if (tempMount != null)
										e.gc.drawRectangle(tempMount.rect);
								} else {
									tempMount = getMountFromImage(mousePos);
									if (tempMount != null && tempMount.rotate) {
										tempRect = indexImgMapRotated.get(getMountIndex(tempMount));
										tempRect.x = tempMount.rect.x + tempMount.rect.width/2 - tempRect.width/2;
										tempRect.y = tempMount.rect.y + tempMount.rect.height/2 - tempRect.height/2;
										e.gc.drawRectangle(tempRect);
									} else if (tempMount != null) {
										tempRect = indexImgMapNormal.get(getMountIndex(tempMount));
										tempRect.x = tempMount.rect.x + tempMount.rect.width/2 - tempRect.width/2;
										tempRect.y = tempMount.rect.y + tempMount.rect.height/2 - tempRect.height/2;
										e.gc.drawRectangle(tempRect);
									}
								}
								
							// highlight already placed rooms
							} else if (showRooms && tempRect != null && tempFTLRoom != null && !moveSelected && !resizeSelected) {
								e.gc.setLineWidth(4);
								e.gc.drawRectangle(tempFTLRoom.getBounds().x+2, tempFTLRoom.getBounds().y+2, tempFTLRoom.getBounds().width-4, tempFTLRoom.getBounds().height-4);
								
							// tile highlight (empty grid cells)
							} else if (tempRect != null && !moveSelected && !resizeSelected) { 
								e.gc.setLineWidth(2);
								e.gc.drawRectangle(tempRect.x+1, tempRect.y+1, 34, 34);
							}
							
						// === room creation tool highlight - colored outline
						} else if (tltmRoom.getSelection() && !leftMouseDown) {
							if (!modShift && tempRect != null) {
								if (!inBounds || doesRectOverlap(tempRect, null)) {
									c = new Color(e.display, 255, 0, 0);
									e.gc.setForeground(c);
									c.dispose();
									allowRoomPlacement = false;
								} else {
									c = new Color(e.display, 0, 255, 0);
									e.gc.setForeground(c);
									c.dispose();
									allowRoomPlacement = true;
								}
								e.gc.setLineWidth(2);
								e.gc.drawRectangle(tempRect.x, tempRect.y, 35, 35);
								
							// === room splitting
							} else if (modShift && tempDoor != null) {
								tempFTLRoom = getRoomContainingRect(tempDoor);
								if (tempFTLRoom != null) {
									e.gc.setAlpha(255);
									e.gc.setLineWidth(3);
									if (!inBounds || isDoorAtWall(tempDoor)) {
										c = new Color(e.display, 255, 0, 0);
										e.gc.setForeground(c);
										c.dispose();
										allowRoomPlacement = false;
									} else {
										c = new Color(e.display, 0, 255, 0);
										e.gc.setForeground(c);
										c.dispose();
										allowRoomPlacement = true;
										parseRoom = tempFTLRoom;
										parseRect = tempDoor;
									}
									if (tempDoor.width == 31) {
										e.gc.drawRectangle(tempFTLRoom.getBounds().x, tempDoor.y+1, tempFTLRoom.getBounds().width, 4);
									} else {
										e.gc.drawRectangle(tempDoor.x+1, tempFTLRoom.getBounds().y, 4, tempFTLRoom.getBounds().height);
									}
								}
							}
							
						// === door creation tool highlight
						} else if (tltmDoor.getSelection() && !leftMouseDown && tempDoor != null) {
							e.gc.setForeground(highlightColor);
							e.gc.setBackground(highlightColor);
							e.gc.setAlpha(64);
							e.gc.fillRectangle(tempDoor);
							e.gc.setAlpha(255);
							e.gc.setLineWidth(2);
							e.gc.drawRectangle(tempDoor);
							
						}
					}
					
				// === DRAW SELECTION INDICATORS
						
					// weapon mount selection
					if (showMounts && selectedMount != null) {
						int i = -1;
						int strips = 0;
						Image temp;
						FTLItem it;
						for (FTLMount m : ship.mounts) {
							i++;
							if (m == selectedMount) break;
						}
						
						FTLMount m = selectedMount; // less typing!
						if (i < ship.weaponSet.size()) {
							it = ShipIO.getItem(ship.weaponSet.get(i));
							if (it != null && !ShipIO.isNull(it.img)) {
								strips = weaponStripMap.get(it.img);
		
								c = new Color(e.display, 0, 0, 255);
								e.gc.setForeground(c);
								c.dispose();
								e.gc.setAlpha(255);
								e.gc.setLineWidth(3);
								
								if (!selectedMount.rotate) { // normal
									temp = weaponImgMap.get(it.blueprint);
									if (temp != null)
										e.gc.drawRectangle(m.rect.x+m.rect.width/2-temp.getBounds().width/strips/2,
											m.rect.y+m.rect.height/2-temp.getBounds().height/2,
											temp.getBounds().width/strips,
											temp.getBounds().height);
								} else { // rotated
									temp = rotated.get(i);
									if (temp != null)
										e.gc.drawRectangle(m.rect.x+m.rect.width/2-temp.getBounds().width/2,
											m.rect.y+m.rect.height/2-temp.getBounds().height/strips/2,
											temp.getBounds().width,
											temp.getBounds().height/strips);
								}
							}
						} else {
							c = new Color(e.display, 0, 0, 255);
							e.gc.setForeground(c);
							c.dispose();
							e.gc.setAlpha(255);
							e.gc.setLineWidth(3);
							e.gc.drawRectangle(m.rect);
						}
						
						c = new Color(e.display, 255, 255, 255);
						e.gc.setForeground(c);
						c.dispose();
						c = new Color(e.display, 0,0,0);
						e.gc.setBackground(c);
						c.dispose();
						e.gc.setFont(appFont);
						String s = " "+(getMountIndex(m)+1)+" ";
						e.gc.drawString(s, m.rect.x, m.rect.y-e.gc.stringExtent(s).y, false);
						
					// door selection
					} else if (showRooms && selectedDoor != null) {
						c = new Color(e.display, 0, 0, 255);
						e.gc.setBackground(c);
						e.gc.setForeground(c);
						c.dispose();
						e.gc.setAlpha(128);
						e.gc.fillRectangle(selectedDoor.rect.x-2, selectedDoor.rect.y-2, selectedDoor.rect.width+4, selectedDoor.rect.height+4);
						
						e.gc.setAlpha(196);
						e.gc.setLineWidth(2);
						e.gc.drawRectangle(selectedDoor.rect);
						
					// room selection
					} else if (showRooms && selectedRoom != null) {
						e.gc.setAlpha(255);
						e.gc.setLineWidth(2);
						c = new Color(e.display, 0, 0, 128);
						e.gc.setBackground(c);
						e.gc.setForeground(c);
						c.dispose();
						e.gc.drawRectangle(selectedRoom.getBounds().x+1, selectedRoom.getBounds().y+1, selectedRoom.getBounds().width-2, selectedRoom.getBounds().height-2);
						for (int i=0; i<4; i++)
							e.gc.fillRectangle(corners[i]);

						c = new Color(e.display, 0, 0, 255);
						e.gc.setBackground(c);
						c.dispose();
						e.gc.setAlpha(64);
						e.gc.fillRectangle(selectedRoom.getBounds());
						
					// hull selection
					} else if (showHull && hullSelected) {
						e.gc.setAlpha(255);
						e.gc.setLineWidth(3);
						c = new Color(e.display, 0, 0, 255);
						e.gc.setForeground(c);
						c.dispose();
						e.gc.drawRectangle(ship.imageRect);
					
					// shield selection
					} else if (showHull && shieldSelected) {
						e.gc.setAlpha(255);
						e.gc.setLineWidth(3);
						c = new Color(e.display, 0, 0, 255);
						e.gc.setForeground(c);
						c.dispose();
						e.gc.drawRectangle(shieldEllipse);
					}
					
				// === DOOR CREATION TOOL
					if (tltmDoor.getSelection()) {
						if (highlightColor != null && !highlightColor.isDisposed())
							highlightColor.dispose();
						highlightColor = new Color(e.display, 255, 0, 0);
						phantomRect = getDoorFromMouse();
						if (phantomRect != null && isDoorAtWall(phantomRect) && wallToDoor(getDoorFromMouse()) == null) {
							if (highlightColor != null && !highlightColor.isDisposed())
								highlightColor.dispose();
							highlightColor = new Color(e.display, 0, 255, 0);
							parseRect = (leftMouseDown) ? phantomRect : null;
						}
					}
					
				// === WEAPON MOUNTING TOOL
					if (tltmMount.getSelection()) {
						e.gc.setLineWidth(2);
						e.gc.setAlpha(255);
						if (mountToolHorizontal) {
							phantomRect = new Rectangle(mousePos.x-FTLMount.MOUNT_WIDTH/2, mousePos.y-FTLMount.MOUNT_HEIGHT/2, FTLMount.MOUNT_WIDTH, FTLMount.MOUNT_HEIGHT);
						} else {
							phantomRect = new Rectangle(mousePos.x-FTLMount.MOUNT_HEIGHT/2, mousePos.y-FTLMount.MOUNT_WIDTH/2, FTLMount.MOUNT_HEIGHT, FTLMount.MOUNT_WIDTH);
						}
						if ((!mountToolSlide.equals(Slide.NO) && ship.mounts.size()==Main.ship.weaponSlots) || (ship.mounts.size()==Main.ship.weaponSlots+1)) {
							c = new Color(e.display, 255, 0, 0);
							e.gc.setForeground(c);
							c.dispose();
						} else {
							c = new Color(e.display, 255, 255, 0);
							e.gc.setForeground(c);
							c.dispose();
						}

						e.gc.setAlpha(255);
						e.gc.drawRectangle(phantomRect);

						c = new Color(e.display, 128, 0, 128);
						e.gc.setForeground(c);
						c.dispose();
						FTLMount.drawDirection(e, mountToolSlide, phantomRect);
						
						e.gc.setLineWidth(1);
						c = new Color(e.display, 255, 255, 0);
						e.gc.setForeground(c);
						c.dispose();
						FTLMount.drawMirror(e, mountToolHorizontal, mountToolMirror, phantomRect);
						phantomRect = null;
					}

				// === SYSTEM OPERATING STATION TOOL
					 if (tltmSystem.getSelection()) {
						tempRect = getRectFromMouse();
						
						if (tempRect != null) {
							tempFTLRoom = getRoomContainingRect(tempRect);
							if (leftMouseDown || rightMouseDown) {
								parseRoom = tempFTLRoom;
								
								parseRect = new Rectangle(0,0,0,0);
								parseRect.x = tempRect.x;
								parseRect.y = tempRect.y;
								parseRect.width = tempRect.width;
								parseRect.height = tempRect.height;
							} else {
								if (tempFTLRoom != null && (!tempRect.intersects(getRectFromStation(tempFTLRoom)) || tempFTLRoom.slot == -2)
										&& (tempFTLRoom.sys.equals(Systems.PILOT) || tempFTLRoom.sys.equals(Systems.SHIELDS) || tempFTLRoom.sys.equals(Systems.WEAPONS)
												|| tempFTLRoom.sys.equals(Systems.ENGINES) || tempFTLRoom.sys.equals(Systems.MEDBAY))) {
									c = new Color(e.display, 0, 255, 0);
									e.gc.setForeground(c);
									c.dispose();
								} else {
									c = new Color(e.display, 255, 0, 0);
									e.gc.setForeground(c);
									c.dispose();
								}
								
								e.gc.setLineWidth(2);
								e.gc.setAlpha(255);
								e.gc.drawRectangle(tempRect.x, tempRect.y, 35, 35);
							}
						}
					}

				// === DRAW SHIP ANCHOR
					if (ship != null && canvasActive && showAnchor) {
						if (ship.vertical != 0) {
							e.gc.setAlpha(196);
							e.gc.setLineWidth(2);
							c = new Color(e.display, 0, 0, 255);
							e.gc.setForeground(c);
							c.dispose();
							e.gc.drawLine(0, ship.anchor.y-ship.vertical, GRID_W*35, ship.anchor.y-ship.vertical);
						}
						ship.drawShipAnchor(e);
					}
					
					if (phantomRect != null && debug) {
						e.gc.setLineWidth(2);
						c = new Color(e.display, 128, 0, 128);
						e.gc.setForeground(c);
						c.dispose();
						e.gc.setAlpha(128);
						e.gc.drawRectangle(phantomRect);
					}
					
				// === Pinned indicator
					e.gc.setAlpha(255);
					if (selectedRoom != null && selectedRoom.isPinned()) // room pin indicator
						e.gc.drawImage(pinImage, selectedRoom.getBounds().x+3, selectedRoom.getBounds().y+3);
					if (selectedDoor != null && selectedDoor.pinned) // door pin indicator
						e.gc.drawImage(pinImage, (selectedDoor.horizontal) ? selectedDoor.rect.x+8 : selectedDoor.rect.x+7, (selectedDoor.horizontal) ? selectedDoor.rect.y-17 : selectedDoor.rect.y+8);
					if (selectedMount != null && selectedMount.pinned) // mount pin indicator
						e.gc.drawImage(pinImage, selectedMount.rect.x-16, selectedMount.rect.y);
					if (hullSelected && ship.hullPinned) // hull pin indicator
						e.gc.drawImage(pinImage, ship.imageRect.x+5, ship.imageRect.y+5);
					if (shieldSelected && ship.shieldPinned) // shield pin indicator
						e.gc.drawImage(pinImage, shieldEllipse.x+5, shieldEllipse.y+5);
					
				} else {
					c = new Color(e.display, 255, 255, 255);
					e.gc.setForeground(c);
					c.dispose();
					Font font = new Font(shell.getDisplay(), "Helvetica", 12, SWT.BOLD);
					e.gc.setFont(font);
					String s = "No ship is loaded. Use the file menu to create a new ship or load an existing one.";
					p = e.gc.stringExtent(s);
					e.gc.drawString(s, (GRID_W*35-p.x)/2, 100, true);
					font.dispose();
				}
			}
		});
		
		*/
		
		Integer[] ignoredLayers = {LayeredPainter.SELECTION, LayeredPainter.GRID, LayeredPainter.ANCHOR, LayeredPainter.SYSTEM_ICON};
		MouseInputAdapter mouseListener = new MouseInputAdapter(ignoredLayers);
		canvas.addMouseMoveListener(mouseListener);
		canvas.addMouseTrackListener(mouseListener);
		canvas.addMouseListener(mouseListener);
		
	// === BOOKMARK: MOUSE MOVE
		/*
		
		canvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				int x = 0, y = 0;

				onCanvas = true;
				
				mousePos.x = e.x;
				mousePos.y = e.y;
				
			// === MOVE
				if (tltmPointer.getSelection() && moveSelected && canvasActive) {
					
					// === Move weapon mounts
					if (selectedMount != null && !selectedMount.pinned) {
						if ((modShift || !leftMouseDown) && dragRoomAnchor.x == 0 && dragRoomAnchor.y == 0) {
							dragRoomAnchor.x = selectedMount.rect.x + selectedMount.rect.width/2;
							dragRoomAnchor.y = selectedMount.rect.y + selectedMount.rect.height/2;
						}
						if (!modShift && e.x != mousePosLastClick.x && e.y != mousePosLastClick.y) {
							if (dragRoomAnchor.x != 0 && dragRoomAnchor.y != 0) {
								dragRoomAnchor.x = 0;
								dragRoomAnchor.y = 0;
								selectedMount.rect.x = (int)(dragRoomAnchor.x + ((-dragRoomAnchor.x + mousePos.x)/10) - ship.offset.x*35 - selectedMount.rect.width/2);
								selectedMount.rect.y = (int)(dragRoomAnchor.y + ((-dragRoomAnchor.y + mousePos.y)/10) - ship.offset.y*35 - selectedMount.rect.height/2);
							} else {
								selectedMount.rect.x = mousePos.x - selectedMount.rect.width/2;
								selectedMount.rect.y = mousePos.y - selectedMount.rect.height/2;
							}
						} else {
							selectedMount.rect.x = phantomRect.x + (mousePos.x - dragRoomAnchor.x)/10 - selectedMount.rect.width/2;
							selectedMount.rect.y = phantomRect.y + (mousePos.y - dragRoomAnchor.y)/10 - selectedMount.rect.height/2;
						}
						ShipIO.updateIndexImgMaps();
						
						canvas.redraw();
		
					// === Move door
					} else if (selectedDoor != null && !selectedDoor.isPinned()) {
						phantomRect = new Rectangle(selectedDoor.getBounds().x, selectedDoor.getBounds().y, selectedDoor.getBounds().width, selectedDoor.getBounds().height);
						x = e.x;
						y = e.y;
						
						phantomRect.x = Math.round(x / 35) * 35 + ((selectedDoor.horizontal) ? (2) : (-3));
						phantomRect.y = Math.round(y / 35) * 35 + ((selectedDoor.horizontal) ? (-3) : (2));
						if (x >= ship.anchor.x && x + selectedDoor.getBounds().width < GRID_W * 35 + 35 && wallToDoor(phantomRect) == null && isDoorAtWall(phantomRect)) {
							selectedDoor.getBounds().x = phantomRect.x;
						}
						if (y >= ship.anchor.y && y + selectedDoor.getBounds().height < GRID_H * 35 + 35 && wallToDoor(phantomRect) == null && isDoorAtWall(phantomRect)) {
							selectedDoor.getBounds().y = phantomRect.y;
						}
						
						canvas.redraw();
						
					// === Move room
					} else if (selectedRoom != null && !selectedRoom.isPinned()) {
						phantomRect = new Rectangle(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height);
						x = e.x - dragRoomAnchor.x;
						y = e.y - dragRoomAnchor.y;
						
						phantomRect.x = roundToGrid(x);
						phantomRect.y = roundToGrid(y);
						
						phantomRect.x = (phantomRect.x+phantomRect.width < GRID_W*35+35) ? (phantomRect.x) : (selectedRoom.getBounds().x);
						phantomRect.y = (phantomRect.y+phantomRect.height < GRID_H*35+35) ? (phantomRect.y) : (selectedRoom.getBounds().y);
						
						phantomRect.width =  (phantomRect.x < ship.anchor.x)
												? (phantomRect.width + (ship.anchor.x - phantomRect.x))
												: (selectedRoom.getBounds().width);
												
						phantomRect.height = (phantomRect.y < ship.anchor.y)
												? (phantomRect.height + (ship.anchor.y - phantomRect.y))
												: (selectedRoom.getBounds().height);
						
						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds())) {
							selectedRoom.getBounds().x = (x >= ship.anchor.x)
													? ((x + selectedRoom.getBounds().width < GRID_W * 35 + 35)
														? (phantomRect.x)
														: (selectedRoom.getBounds().x))
													: (ship.anchor.x);
							selectedRoom.getBounds().y = (y >= ship.anchor.y)
													? ((y + selectedRoom.getBounds().height < GRID_H * 35 + 35)
														? (phantomRect.y)
														: (selectedRoom.getBounds().y))
													: (ship.anchor.y);
						}
						updateCorners(selectedRoom);
						
						removeUnalignedDoors();
						
						canvas.redraw();
						
					// === Move ship hull
					} else if (hullSelected && !ship.hullPinned) {
						if (!modShift) {
							ship.imageRect.x = e.x - dragRoomAnchor.x;
							ship.imageRect.y = e.y - dragRoomAnchor.y;
						} else {
							ship.imageRect.x = phantomRect.width + (int)((e.x - phantomRect.x)/10);
							ship.imageRect.y = phantomRect.height + (int)((e.y - phantomRect.y)/10);
						}
						canvas.redraw();
						
					// === Move shield
					} else if (shieldSelected && !ship.shieldPinned) {
						if (!modShift) {
							shieldEllipse.x = e.x - dragRoomAnchor.x;
							shieldEllipse.y = e.y - dragRoomAnchor.y;
						} else {
							shieldEllipse.x = phantomRect.width + (int)((e.x - dragRoomAnchor.x)/10);
							shieldEllipse.y = phantomRect.height + (int)((e.y - dragRoomAnchor.y)/10);
						}
						ship.ellipse.x = (shieldEllipse.x + shieldEllipse.width/2) - (ship.findLowBounds().x + ship.computeShipSize().x/2);
						ship.ellipse.y = (shieldEllipse.y + shieldEllipse.height/2) - (ship.findLowBounds().y + ship.computeShipSize().y/2) - ((ship.isPlayer) ? 0 : 110);
						ship.ellipse.width = shieldEllipse.width/2;
						ship.ellipse.height = shieldEllipse.height/2;

						canvas.redraw();
					}
						
			// === RESIZE
				// === Resize Room
				} else if (tltmPointer.getSelection() && resizeSelected && selectedRoom != null && canvasActive && !selectedRoom.isPinned()) {
					phantomRect = new Rectangle(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height);
		
					x = e.x - dragRoomAnchor.x;
					y = e.y - dragRoomAnchor.y;
						
					x = (int) ((x > 0) ? roundToGrid(x)+35 : roundToGrid(x)-35);
					y = (int) ((y > 0) ? roundToGrid(y)+35 : roundToGrid(y)-35);
						
						
					if (dragRoomAnchor.equals(((FTLRoom) selectedRoom).corners[0])) {
						phantomRect.width = x;
						phantomRect.height = y;
					} else if (dragRoomAnchor.equals(((FTLRoom) selectedRoom).corners[1])) {
						phantomRect.x = dragRoomAnchor.x+x;
						phantomRect.width = -x;
						phantomRect.height = y;
					} else if (dragRoomAnchor.equals(((FTLRoom) selectedRoom).corners[2])) {
						phantomRect.width = x;
						phantomRect.height = -y;
						phantomRect.y = dragRoomAnchor.y+y;
					} else if (dragRoomAnchor.equals(((FTLRoom) selectedRoom).corners[3])) {
						phantomRect.x = dragRoomAnchor.x+x;
						phantomRect.y = dragRoomAnchor.y+y;
						phantomRect.width = -x;
						phantomRect.height = -y;
					}
		
					phantomRect = fixRect(phantomRect);
					
					if (phantomRect.x >= ship.anchor.x && phantomRect.x+phantomRect.width < GRID_W*35+35 && !doesRectOverlap(phantomRect, selectedRoom.getBounds())) {
						selectedRoom.getBounds().x = phantomRect.x;
						selectedRoom.getBounds().width = (phantomRect.width == 0) ? 35 : phantomRect.width;
					}
					if (phantomRect.y >= ship.anchor.y && phantomRect.y+phantomRect.height < GRID_H * 35+35 && !doesRectOverlap(phantomRect, selectedRoom.getBounds())) {
						selectedRoom.getBounds().y = phantomRect.y;
						selectedRoom.getBounds().height = (phantomRect.height == 0) ? 35 : phantomRect.height;
					}
					
					updateCorners(selectedRoom);
					
					removeUnalignedDoors();
					
					canvas.redraw();
					
				// === Resize Shield
				} else if (shieldSelected && !ship.shieldPinned && resizeSelected) {
					int d = Math.abs(dragRoomAnchor.x - e.x);
					shieldEllipse.x = dragRoomAnchor.x - d;
					shieldEllipse.width = 2*d;

					d = Math.abs(e.y - dragRoomAnchor.y);
					shieldEllipse.y = dragRoomAnchor.y - d;
					shieldEllipse.height = 2*d;
					
					ship.ellipse.x = (shieldEllipse.x + shieldEllipse.width/2) - (ship.findLowBounds().x + ship.computeShipSize().x/2);
					ship.ellipse.y = (shieldEllipse.y + shieldEllipse.height/2) - (ship.findLowBounds().y + ship.computeShipSize().y/2) - ((ship.isPlayer) ? 0 : 110);
					ship.ellipse.width = shieldEllipse.width/2;
					ship.ellipse.height = shieldEllipse.height/2;
					
			// === MOVE ANCHOR
				} else if (moveAnchor && canvasActive) {
					if (leftMouseDown) {
						Point p = ship.computeShipSize();
						Point low = ship.findLowBounds();
						Point a;
						x = downToGrid(e.x);
						y = downToGrid(e.y);
						
						if (e.x >= 0 && (x + p.x + low.x - ship.anchor.x) <= GRID_W*35 && e.x < GRID_W*35+35) {
							if (!modShift) {
								a = new Point(x, ship.anchor.y);
								ship.updateElements(a, FTLShip.AxisFlag.X);
								ship.anchor.x = x;
							} else if (e.x < low.x+35) {
								ship.anchor.x = x;
								ship.offset.x = (ship.findLowBounds().x - ship.anchor.x) / 35;
							}
						}
						if (e.y >= 0 && (y + p.y + low.y - ship.anchor.y) <= GRID_H*35 && e.y < GRID_H*35+35) {
							if (!modShift) {
								a = new Point(ship.anchor.x, y);
								ship.updateElements(a, FTLShip.AxisFlag.Y);
								ship.anchor.y = y;
							} else if (e.y < low.y+35) {
								ship.anchor.y = y;
								ship.offset.y = (ship.findLowBounds().y - ship.anchor.y) / 35;
							}
						}
					} else if (rightMouseDown) {
						ship.vertical = ship.anchor.y-e.y;
					}
					canvas.redraw();
					
			// === ROOM CREATION (DRAGGING)
				} else if (tltmRoom.getSelection() && phantomRect != null && leftMouseDown) {
		
					phantomRect.x = downToGrid(mousePosLastClick.x) + ((e.x > mousePosLastClick.x) ? (0) : (35));
					phantomRect.y = downToGrid(mousePosLastClick.y) + ((e.y > mousePosLastClick.y) ? (0) : (35));
					
					x = ((e.x > mousePosLastClick.x)
							? Math.min(GRID_W * 35 - phantomRect.x, upToGrid(e.x - phantomRect.x)+35)
							: upToGrid(mousePos.x - phantomRect.x)-35);
					y = ((e.y > mousePosLastClick.y)
							? Math.min(GRID_H * 35 - phantomRect.y, upToGrid(e.y - phantomRect.y)+35)
							: upToGrid(mousePos.y - phantomRect.y)-35);
					
					phantomRect.width = x;
					phantomRect.height = y;
					
					canvas.redraw();
				}
				
				if (ship != null && (e.x >= ship.anchor.x && e.y >= ship.anchor.y)) {
					inBounds = true;
				} else {
					inBounds = false;
				}
				
				updateSelectedPosText();
			}
		});
		
		canvas.addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				onCanvas = true;
			}
			public void mouseExit(MouseEvent e) {
				onCanvas = false;
			}
			public void mouseHover(MouseEvent e) {
				// so that the canvas is not being redrawn if no changes are being made.
				onCanvas = false;
			}
		});


	// === BOOKMARK: MOUSE DOWN
		
		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(MouseEvent e) {
				for (FTLRoom r : ship.rooms) {
					r.mouseDown(e);
				}
				
				mousePosLastClick.x = e.x;
				mousePosLastClick.y = e.y;
				
				if (e.button == 1)
					leftMouseDown = true;
				if (e.button == 3)
					rightMouseDown = true;
				
				onCanvas = true;
				
				if (canvasActive) {
					if (tltmPointer.getSelection() && showAnchor && (e.x >= ship.anchor.x-FTLShip.ANCHOR && ((ship.anchor.x == 0) ? (e.x <= FTLShip.ANCHOR) : ((e.x <= ship.anchor.x)))
							&& e.y >= ship.anchor.y-FTLShip.ANCHOR && ((ship.anchor.y == 0) ? (e.y <= FTLShip.ANCHOR) : ((e.y <= ship.anchor.y))))) {
						moveAnchor = true;
					}
					if (!moveAnchor && tltmPointer.getSelection() && onCanvas) {
						hullSelected = false;
						shieldSelected = false;
						
						// selection priorities; door > mount > room > hull
						// cheking if previous selection variable is null prevents selecting multiple objects at once.
						selectedDoor = (showRooms) ? wallToDoor(getDoorFromMouse()) : null;

						if (showMounts && selectedDoor == null) {
							selectedMount = getMountFromImage(mousePosLastClick);
							selectedMount = selectedMount == null ? getMountFromMouse() : selectedMount;
						}
						
						selectedRoom = (showRooms && selectedMount == null && selectedDoor == null) ? getRoomContainingRect(getRectFromClick()) : null;
						
						if (showHull && selectedRoom == null && selectedDoor == null && selectedMount == null) {
							hullSelected = ship.imageRect.contains(mousePos) && leftMouseDown && !rightMouseDown;
							shieldSelected = shieldEllipse.contains(mousePos) && !leftMouseDown && rightMouseDown;
							
							resizeSelected = !ship.isPlayer && shieldSelected && modAlt;
							if (resizeSelected) {
								dragRoomAnchor.x = shieldEllipse.x + shieldEllipse.width/2;
								dragRoomAnchor.y = shieldEllipse.y + shieldEllipse.height/2;
								
								Point p = canvas.toDisplay(shieldEllipse.x+shieldEllipse.width, shieldEllipse.y+shieldEllipse.height);
								Robot robot;
								try {
									robot = new Robot();
									robot.mouseMove(p.x, p.y);
								} catch (AWTException ex) {
								}
							}

							moveSelected = hullSelected || (shieldSelected && !resizeSelected);
							if (hullSelected && moveSelected) {
								dragRoomAnchor.x = e.x - ship.imageRect.x;
								dragRoomAnchor.y = e.y - ship.imageRect.y;
			            		
			            		if (phantomRect == null) phantomRect = new Rectangle(0,0,0,0);
		            			phantomRect.x = mousePos.x;
		            			phantomRect.y = mousePos.y;
		            			phantomRect.width = ship.imageRect.x;
		            			phantomRect.height = ship.imageRect.y;
							}
							if (shieldSelected && moveSelected) {
			            		dragRoomAnchor.x = mousePos.x - shieldEllipse.x;
			            		dragRoomAnchor.y = mousePos.y - shieldEllipse.y;
			            		
			            		if (phantomRect == null) phantomRect = new Rectangle(0,0,0,0);
		            			phantomRect.x = mousePos.x;
		            			phantomRect.y = mousePos.y;
		            			phantomRect.width = shieldEllipse.x;
		            			phantomRect.height = shieldEllipse.y;
							}
						}
						if (selectedRoom != null) {
							updateCorners(selectedRoom);
							selectedDoor = null;
						}
						
						updateSelectedPosText();
					}
					
					if (tltmPointer.getSelection() && onCanvas && e.button == 1) {
						if (selectedMount != null && (doImagesContain(mousePosLastClick) || selectedMount.rect.contains(mousePosLastClick))) {
							moveSelected = true;
		            		dragRoomAnchor.x = mousePos.x;
		            		dragRoomAnchor.y = mousePos.y;
		            		if (phantomRect == null) phantomRect = new Rectangle(0,0,0,0);
	            			phantomRect.x = mousePos.x;
	            			phantomRect.y = mousePos.y;
	            			phantomRect.width = selectedMount.rect.x;
	            			phantomRect.height = selectedMount.rect.y;
						} if (selectedRoom != null && selectedRoom.getBounds().contains(mousePos)) {
							if (corners[0].contains(mousePosLastClick) || corners[1].contains(mousePosLastClick) || corners[2].contains(mousePosLastClick) || corners[3].contains(mousePosLastClick)) {
								dragRoomAnchor = findFarthestCorner((FTLRoom) selectedRoom, mousePosLastClick);
								resizeSelected = true;
							} else if (selectedRoom.getBounds().contains(mousePosLastClick)) {
								moveSelected = true;
								dragRoomAnchor.x = e.x - selectedRoom.getBounds().x;
								dragRoomAnchor.y = e.y - selectedRoom.getBounds().y;
							}
						} else if (selectedDoor != null && selectedDoor.getBounds().contains(mousePos)) {
							moveSelected = true;
						}
					} else if (tltmRoom.getSelection() && onCanvas) {
					   	phantomRect = getRectFromClick();
					} else if (e.button == 1) {
						moveSelected = false;
						resizeSelected = false;
					}
				}
				
				canvas.redraw();
			}
			
	// === BOOKMARK: MOUSE UP
			@Override
			public void mouseUp(MouseEvent e) {
				
				onCanvas = true;
				moveSelected = false;
				
				if (highlightColor != null && !highlightColor.isDisposed())
					highlightColor.dispose();
				highlightColor = new Color(shell.getDisplay(), 0, 255, 0);
				if (canvasActive) {
					if (selectedRoom != null && resizeSelected) {
						//selectedRoom.getBounds() = fixRect(selectedRoom.getBounds());
						selectedRoom.setBounds(fixRect(selectedRoom.getBounds()));
					}
					
					if (tltmPointer.getSelection()) {
						if (selectedMount != null) {
							if (leftMouseDown && e.button == 1) {
								dragRoomAnchor.x = 0;
								dragRoomAnchor.y = 0;
							}
							if (((modShift && leftMouseDown) || !leftMouseDown) && onCanvas) {
								selectedMount.mirror = (mousePos.x == mousePosLastClick.x && mousePos.y == mousePosLastClick.y && modShift && e.button == 1) ? !selectedMount.mirror : selectedMount.mirror;
								selectedMount.slide = (modShift && e.button == 3)
										? ((selectedMount.slide.equals(Slide.UP))
											? (Slide.RIGHT)
											: (selectedMount.slide.equals(Slide.RIGHT))
												? (Slide.DOWN)
												: (selectedMount.slide.equals(Slide.DOWN))
													? (Slide.LEFT)
													: (selectedMount.slide.equals(Slide.LEFT))
														? (Slide.NO)
														: (selectedMount.slide.equals(Slide.NO))
															? (Slide.UP)
															: selectedMount.slide)
										: selectedMount.slide;

								if (!modShift && e.button==3) {
									selectedMount.rotate = !selectedMount.rotate;
									selectedMount.rect.x += (selectedMount.rotate) ? (selectedMount.rect.width/2-selectedMount.rect.height/2) : (-selectedMount.rect.height/2+selectedMount.rect.width/2);
									selectedMount.rect.y += (selectedMount.rotate) ? (selectedMount.rect.height/2-selectedMount.rect.width/2) : (-selectedMount.rect.width/2+selectedMount.rect.height/2);
									selectedMount.rect.width = (selectedMount.rotate) ? (FTLMount.MOUNT_WIDTH) : (FTLMount.MOUNT_HEIGHT);
									selectedMount.rect.height = (selectedMount.rotate) ? (FTLMount.MOUNT_HEIGHT) : (FTLMount.MOUNT_WIDTH);
									ShipIO.updateIndexImgMaps();
								}
							} else if (leftMouseDown && !modShift && e.button == 3 && onCanvas) { // reset the position
								ship.updateMount(selectedMount);
							}
							
							// === move weapon mounts (update actual pos)
							if (e.button != 3) {
								// if the weapon mount gets dragged off the screen, then don't update the actual position (the mount will revert to it's last position)
								// the below condition actually passes when the position IS to be updated.
								if ((modShift || (e.x > 0 && e.y > 0 && e.x < GRID_W*35 && e.y < GRID_H*35)) && e.x != mousePosLastClick.x && e.y != mousePosLastClick.y) {
									
									mountRect.x = selectedMount.rect.x + selectedMount.rect.width/2 + ((hullImage != null) ? (-ship.anchor.x - ship.offset.x*35) : (-ship.imageRect.x));
									mountRect.y = selectedMount.rect.y + selectedMount.rect.height/2 + ((hullImage != null) ? (-ship.anchor.y - ship.offset.y*35) : (-ship.imageRect.y));

									selectedMount.rect.x = (Main.hullImage != null) ? (ship.anchor.x + ship.offset.x*35 + Main.mountRect.x) : (Main.mountRect.x);
									selectedMount.rect.y = (Main.hullImage != null) ? (ship.anchor.y + ship.offset.y*35 + Main.mountRect.y) : (Main.mountRect.y);
										
									selectedMount.pos.x = selectedMount.rect.x - ship.imageRect.x;
									selectedMount.pos.y = selectedMount.rect.y - ship.imageRect.y;
									
									selectedMount.rect.x -= (selectedMount.rotate) ? (FTLMount.MOUNT_WIDTH/2) : (FTLMount.MOUNT_HEIGHT/2);
									selectedMount.rect.y -= (selectedMount.rotate) ? (FTLMount.MOUNT_HEIGHT/2) : (FTLMount.MOUNT_WIDTH/2);
								} else if (e.x < 0 || e.y < 0 || e.x >= GRID_W*35 || e.y >= GRID_H*35) {
									selectedMount.rect.x = 70;
									selectedMount.rect.y = 70;
								}
							}
						} else if (hullSelected && moveSelected && e.button == 1 && snapMountsToHull) {
							if (snapMountsToHull) {
								for (FTLMount m : ship.mounts) {
									mountRect.x = m.pos.x - ((m.rotate) ? (FTLMount.MOUNT_WIDTH/2) : (FTLMount.MOUNT_HEIGHT/2));
									mountRect.y = m.pos.y - ((m.rotate) ? (FTLMount.MOUNT_HEIGHT/2) : (FTLMount.MOUNT_WIDTH/2));
									
									m.rect.x = (hullImage != null) ? (ship.imageRect.x + mountRect.x) : (mountRect.x);
									m.rect.y = (hullImage != null) ? (ship.imageRect.y + mountRect.y) : (mountRect.y);
								}
							}
						}
					} else if (tltmRoom.getSelection() && onCanvas && e.button == 1 && phantomRect != null) {
						
							//	=== room creation
						if (!modShift && allowRoomPlacement) {
							parseRect = fixRect(phantomRect);
						
							FTLRoom r = new FTLRoom(parseRect);
	
							r.id = getLowestId();
							idList.add(r.id);
	
							ship.rooms.add(r);
							layeredPainter.add(r, LayeredPainter.ROOM);
							parseRect = null;
							phantomRect = null;
							
							if (ship.rooms.size() > 0) {
								btnShields.setEnabled(ship.isPlayer);
								btnShields.setToolTipText(null);
							}
							
							// === room splitting
						} else if (modShift && parseRoom != null && parseRect != null) {
							FTLRoom r1 = null;
							parseRect.x = roundToGrid(parseRect.x)+35;
							parseRect.y = roundToGrid(parseRect.y)+35;
							if (parseRect.width == 31) {
								// horizontal
								r1 = new FTLRoom(parseRoom.getBounds().x, parseRoom.getBounds().y, parseRoom.getBounds().width, parseRect.y-parseRoom.getBounds().y);
								parseRoom.getBounds().height = parseRoom.getBounds().y+parseRoom.getBounds().height-parseRect.y;
								parseRoom.getBounds().y = parseRect.y;
							} else {
								// vertical
								r1 = new FTLRoom(parseRoom.getBounds().x, parseRoom.getBounds().y, parseRect.x - parseRoom.getBounds().x, parseRoom.getBounds().height);
								parseRoom.getBounds().width = parseRoom.getBounds().x+parseRoom.getBounds().width-parseRect.x;
								parseRoom.getBounds().x = parseRect.x;
							}
							parseRoom.sys = Systems.EMPTY;
							parseRoom = null;
							selectedRoom = null;
							
							ship.rooms.add(r1);
							layeredPainter.add(r1, LayeredPainter.ROOM);
							r1.id = getLowestId();
							idList.add(r1.id);
							ship.reassignID();
						}
						
						// === door creation
					} else if (tltmDoor.getSelection() && onCanvas && parseRect != null && e.button == 1) {
						boolean horizontal = parseRect.height == 6;
						FTLDoor d = new FTLDoor(parseRect.x, parseRect.y, horizontal);
						
						d.add(ship);
						parseRect = null;
						
						// === weapon mount creation
					} else if (tltmMount.getSelection() && onCanvas) {
						mountToolHorizontal = (!modShift && e.button == 3) ? ( (mountToolHorizontal) ? (false) : (true) ) : (mountToolHorizontal);
						mountToolSlide = (modShift && e.button == 3)
											? ((mountToolSlide.equals(Slide.UP))
												? (Slide.RIGHT)
												: (mountToolSlide.equals(Slide.RIGHT))
													? (Slide.DOWN)
													: (mountToolSlide.equals(Slide.DOWN))
														? (Slide.LEFT)
														: (mountToolSlide.equals(Slide.LEFT))
															? (Slide.NO)
															: (mountToolSlide.equals(Slide.NO))
																? (Slide.UP)
																: mountToolSlide)
											: mountToolSlide;
						mountToolMirror = (modShift && e.button == 1) ? !mountToolMirror : mountToolMirror ;
						
						if (((!mountToolSlide.equals(Slide.NO) && ship.mounts.size()<Main.ship.weaponSlots) || (mountToolSlide.equals(Slide.NO) && ship.mounts.size()<Main.ship.weaponSlots+1)) && e.button == 1 && !modShift) {
							FTLMount m = new FTLMount();

							m.rotate = mountToolHorizontal;
							m.mirror = mountToolMirror;
							m.gib = 0; // TODO gibs
							m.slide = mountToolSlide;
							m.rect.x = e.x + m.rect.width/2;
							m.rect.y = e.y + m.rect.height/2;
							
							mountRect = new Rectangle(0,0,0,0);
							mountRect.x = ((hullImage != null) ? (m.rect.x) : (m.rect.x));
							mountRect.y = ((hullImage != null) ? (m.rect.y) : (m.rect.y));
							
							m.pos.x = m.rect.x - ship.imageRect.x;
							m.pos.y = m.rect.y - ship.imageRect.y;

							m.rect.width = (m.rotate) ? (FTLMount.MOUNT_WIDTH) : (FTLMount.MOUNT_HEIGHT);
							m.rect.height = (m.rotate) ? (FTLMount.MOUNT_HEIGHT) : (FTLMount.MOUNT_WIDTH);
							m.rect.x -= (m.rotate) ? (FTLMount.MOUNT_WIDTH/2) : (FTLMount.MOUNT_HEIGHT/2);
							m.rect.y -= (m.rotate) ? (FTLMount.MOUNT_HEIGHT/2) : (FTLMount.MOUNT_WIDTH/2);
							
							ship.mounts.add(m);
						}
						
						// === operating slot creation tool
					} else if (tltmSystem.getSelection() && onCanvas && parseRect != null && parseRoom != null
							&& (parseRoom.sys.equals(Systems.PILOT) || parseRoom.sys.equals(Systems.SHIELDS) || parseRoom.sys.equals(Systems.WEAPONS)
									|| parseRoom.sys.equals(Systems.ENGINES) || parseRoom.sys.equals(Systems.MEDBAY))) {
						if (!modShift) {
							if (e.button == 1) {
								parseRoom.slot = getStationFromRect(parseRect);
							} else if (e.button == 3) {
								parseRoom.slot = -2;
							}
						} else if (e.button == 1 && parseRoom.slot != -2) {
							parseRoom.dir =((parseRoom.dir.equals(Slide.UP))
											? (Slide.RIGHT)
											: (parseRoom.dir.equals(Slide.RIGHT))
												? (Slide.DOWN)
												: (parseRoom.dir.equals(Slide.DOWN))
													? (Slide.LEFT)
													: (parseRoom.dir.equals(Slide.LEFT))
														? (Slide.UP)
														: (parseRoom.dir));
						}
					}
					
					parseRect = getRectFromMouse();
					if (!shieldSelected && selectedMount == null && selectedDoor == null && !hullSelected && e.button == 3 && tltmPointer.getSelection() && onCanvas && parseRect !=null  && doesRectOverlap(parseRect, null)) {
						menuSystem.setVisible(true);
						selectedRoom = getRoomContainingRect(parseRect);
						updateCorners(selectedRoom);
					}
				}
					
				parseRect = null;

				moveAnchor = false;
				moveSelected = false;
				resizeSelected = false;
				if (e.button == 1) {
					leftMouseDown = false;
				} else if (e.button == 3) {
					rightMouseDown = false;
				}
				
				canvas.redraw();
			}
			
			
			public void mouseDoubleClick(MouseEvent e) {
				if (canvasActive) {
					if (tltmPointer.getSelection()) {
							// open the room level and power editing dialog
						if (selectedRoom != null) {
							sysDialog.open();
						}
					}
				}
			}
		});

		*/
		// === SELECTED ITEM POSITION
		
			txtX.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent e) {
					String string = e.text;
					char[] chars = new char[string.length()];
					string.getChars(0, chars.length, chars, 0);
					for (int i = 0; i < chars.length; i++) {
						if (!('0' <= chars[i] && chars[i] <= '9') && ('-'!=chars[i])) {
							e.doit = false;
							return;
						}
					}
				}
			});

			txtY.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent e) {
					String string = e.text;
					char[] chars = new char[string.length()];
					string.getChars(0, chars.length, chars, 0);
					for (int i = 0; i < chars.length; i++) {
						if (!('0' <= chars[i] && chars[i] <= '9') && ('-'!=chars[i])) {
							e.doit = false;
							return;
						}
					}
				}
			});
			
			txtX.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_RETURN) {
						updateSelectedPosition();
					} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
						updateSelectedPosText();	        	  
					}
					canvas.forceFocus();
					e.doit = false;
				}
			});
			
			txtY.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
			        if (e.detail == SWT.TRAVERSE_RETURN) {
						updateSelectedPosition();
					} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
						updateSelectedPosText();    	  
					}
					canvas.forceFocus();
					e.doit = false;
				}
			});

			btnXminus.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					txtX.setText(""+(Integer.valueOf(txtX.getText())-((selectedRoom==null) ? 35 : 1)));
					updateSelectedPosition();
				} });

			btnXplus.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					txtX.setText(""+(Integer.valueOf(txtX.getText())+((selectedRoom==null) ? 35 : 1)));
					updateSelectedPosition();
				} });

			btnYminus.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					txtY.setText(""+(Integer.valueOf(txtY.getText())-((selectedRoom==null) ? 35 : 1)));
					updateSelectedPosition();
				} });

			btnYplus.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					txtY.setText(""+(Integer.valueOf(txtY.getText())+((selectedRoom==null) ? 35 : 1)));
					updateSelectedPosition();
				} });
			
			
		// === IMAGE BUTTONS
			
			btnMiniship.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					String[] filterExtensions = new String[] {"*.png"};
					dialog.setFilterExtensions(filterExtensions);
					dialog.setFilterPath(resPath);
					String path = dialog.open();
					
					if (!ShipIO.isNull(path)) {
						Main.ship.miniPath = path;
					}
					updateButtonImg();
				}
			});
			
			btnFloor.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					String[] filterExtensions = new String[] {"*.png"};
					dialog.setFilterExtensions(filterExtensions);
					dialog.setFilterPath(resPath);
					String path = dialog.open();
					
					if (!ShipIO.isNull(path)) {
						Main.ship.floorPath = path;
						
						ShipIO.loadImage(path, "floor");
						canvas.redraw();
					}
					updateButtonImg();
				}
			});
			
			btnCloak.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					String[] filterExtensions = new String[] {"*.png"};
					dialog.setFilterExtensions(filterExtensions);
					dialog.setFilterPath(resPath);
					String path = dialog.open();
					
					if (!ShipIO.isNull(path)) {
						if (ShipIO.isDefaultResource(new File(path)))
							Main.ship.cloakOverride = path;
						
						Main.ship.cloakPath = path;
						btnCloaked.setEnabled(true);
						
						ShipIO.loadImage(path, "cloak");
						canvas.redraw();
					}
					updateButtonImg();
				}
			});
			
			btnShields.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					String[] filterExtensions = new String[] {"*.png"};
					dialog.setFilterExtensions(filterExtensions);
					dialog.setFilterPath(resPath);
					String path = dialog.open();
					Main.ship.shieldOverride = null;
					
					if (!ShipIO.isNull(path)) {
						if (ShipIO.isDefaultResource(new File(path)))
							Main.ship.shieldOverride = path;
						
						Main.ship.shieldPath = path;
						
						ShipIO.loadImage(path, "shields");
						
						if (ship.isPlayer)
							if (shieldImage != null && !shieldImage.isDisposed()) {
								Rectangle temp = shieldImage.getBounds();
								shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - temp.width/2 + ship.ellipse.x;
								shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - temp.height/2 + ship.ellipse.y;
								shieldEllipse.width = temp.width;
								shieldEllipse.height = temp.height;
							}
						
						updateButtonImg();
						
						canvas.redraw();
					}
				}
			});
			
			btnHull.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					String[] filterExtensions = new String[] {"*.png"};
					dialog.setFilterExtensions(filterExtensions);
					dialog.setFilterPath(resPath);
					String path = dialog.open();
					
					if (!ShipIO.isNull(path)) {
						Main.ship.imagePath = path;
						
						ShipIO.loadImage(path, "hull");
						canvas.redraw();
					}
					updateButtonImg();
				}
			});
			
			btnShipProperties.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					shell.setEnabled(false);
					shipDialog.open();
					
					shell.setEnabled(true);
					canvas.redraw();
				}
			});

		btnHull.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				sourceBtn = (Button) e.widget;
			} });
		btnShields.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				sourceBtn = (Button) e.widget;
			} });
		btnFloor.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				sourceBtn = (Button) e.widget;
			} });
		btnCloak.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				sourceBtn = (Button) e.widget;
			} });
		btnMiniship.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				sourceBtn = (Button) e.widget;
			} });
		
		menu_imageBtns.addMenuListener(new MenuAdapter() {
			String s = null;
			public void menuShown(MenuEvent e) {
				if (sourceBtn == btnHull) {
					s = ship.imagePath;
				} else if (sourceBtn == btnShields) {
					s = ship.shieldPath;
				} else if (sourceBtn == btnFloor) {
					s = ship.floorPath;
				} else if (sourceBtn == btnCloak) {
					s = ship.cloakPath;
				} else if (sourceBtn == btnMiniship) {
					s = ship.miniPath;
				}
				mntmUnload.setEnabled(!ShipIO.isNull(s));
				mntmShowFile.setEnabled(!ShipIO.isNull(s));
				if (!ShipIO.isNull(s)) {
					mntmPath.setText("..."+s.substring(s.lastIndexOf(ShipIO.pathDelimiter)));
				} else {
					mntmPath.setText("");
				}
			}
		});
		
		mntmUnload.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (sourceBtn == btnHull) {
					ship.imagePath = null;
					if (hullImage != null && !hullImage.isDisposed() && !ShipIO.loadingSwitch)
						hullImage.dispose();
					ship.imageRect.x = 0;
					ship.imageRect.y = 0;
					ship.imageRect.width = 0;
					ship.imageRect.height = 0;
				} else if (sourceBtn == btnShields) {
					ship.shieldPath = null;
					ship.shieldOverride = null;
					if (shieldImage != null && !shieldImage.isDisposed() && !ShipIO.loadingSwitch) {
						shieldImage.dispose();
					}
				} else if (sourceBtn == btnFloor) {
					ship.floorPath = null;
					if (floorImage != null && !floorImage.isDisposed() && !ShipIO.loadingSwitch)
						floorImage.dispose();
				} else if (sourceBtn == btnCloak) {
					ship.cloakPath = null;
					ship.cloakOverride = null;
					btnCloaked.setEnabled(false);
					if (cloakImage != null && !cloakImage.isDisposed() && !ShipIO.loadingSwitch)
						cloakImage.dispose();
				} else if (sourceBtn == btnMiniship) {
					ship.miniPath = null;
				}
				updateButtonImg();
				canvas.redraw();
				bgCanvas.redraw();
			}
		});
		
		mntmShowFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				File file = null;
				if (sourceBtn == btnHull) {
					file = new File(ship.imagePath);
				} else if (sourceBtn == btnShields) {
					file = new File(ship.shieldPath);
				} else if (sourceBtn == btnFloor) {
					file = new File(ship.floorPath);
				} else if (sourceBtn == btnCloak) {
					file = new File(ship.cloakPath);
				} else if (sourceBtn == btnMiniship) {
					file = new File(ship.miniPath);
				}

				if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
					if (file.exists() && desktop != null) {
						try {
							desktop.open(file.getParentFile());
						} catch (IOException ex) {
						}
					}
				} else {
					erDialog.print("Error: show file - desktop not supported.");
				}
			}
		});
		
	// === STATE BUTTONS

		btnCloaked.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				canvas.redraw();
			}
		});

	// === SHELL
		
		shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {
			public void handleEvent(Event e)
			{
            	if (e.keyCode == SWT.SHIFT)
            		modShift = false;
            	if (e.keyCode == SWT.ALT)
            		modAlt = false;
            	
            	
            	if (modShift && (hullSelected || shieldSelected) && moveSelected) {
            		hullSelected = false;
            		shieldSelected = false;
            		moveSelected = false;
            	}
            	if (hullSelected && moveSelected) {
            		dragRoomAnchor.x = mousePos.x - ship.imageRect.x;
            		dragRoomAnchor.y = mousePos.y - ship.imageRect.y;
            	} else if (shieldSelected && moveSelected) {
            		dragRoomAnchor.x = mousePos.x - shieldEllipse.x;
            		dragRoomAnchor.y = mousePos.y - shieldEllipse.y;
            	} else if (selectedMount != null && moveSelected) {
            		dragRoomAnchor.x = mousePos.x - selectedMount.rect.x;
            		dragRoomAnchor.y = mousePos.y - selectedMount.rect.y;
            	}
			}
		});
		
		shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
            public void handleEvent(Event e) {
            	if (e.keyCode == SWT.ALT)
            		modAlt = true;
            	if (e.keyCode == SWT.SHIFT)
            		modShift = true;
            	
            	// check to make sure that the hotkeys won't be triggered while the user is modifying fields in another window
            	if (shell.isEnabled() && !txtX.isFocusControl() && !txtY.isFocusControl()) {
            		
            			// === element deletion
	            	if (canvasActive && (selectedMount != null || selectedRoom != null || selectedDoor != null) && (e.keyCode == SWT.DEL || (e.stateMask == SWT.SHIFT && e.keyCode == 'd'))) {
	            		if (selectedRoom != null) {
	            			selectedRoom.dispose();
	            			ship.rooms.remove(selectedRoom);
	            			selectedRoom = null;
	                		removeUnalignedDoors();
	    					ship.reassignID();
		            		canvas.redraw();
		            		
		            		if (ship.rooms.size() == 0) {
		            			btnShields.setEnabled(false);
		            			btnShields.setToolTipText("Shield is aligned in relation to rooms. Place a room before choosing shield graphic.");
		            		}
	            		} else if (ship.doors.remove(selectedDoor)) {
	            			selectedDoor = null;
	            			canvas.redraw();
	            		} else if (ship.mounts.remove(selectedMount)) {
	            			selectedMount = null;
	            			canvas.redraw();
	            		}
	            		
	            		// === deselect
	            	} else if (e.keyCode == SWT.ESC) {
	            		selectedRoom = null;
	            		selectedDoor = null;
	            		selectedMount = null;
	            		canvas.redraw();
	            		
	            		// === file menu options
	            	} else if (e.stateMask == SWT.CTRL && e.keyCode == 's' && mntmSaveShip.getEnabled()) {
	            		mntmSaveShip.notifyListeners(SWT.Selection, null);
	            	} else if (e.stateMask == SWT.CTRL && e.keyCode == 'n') {
	            		mntmNewShip.notifyListeners(SWT.Selection, null);
	            	} else if (e.stateMask == SWT.CTRL && e.keyCode == 'l') {
	            		mntmLoadShip.notifyListeners(SWT.Selection, null);
	            	} else if (e.stateMask == SWT.CTRL && e.keyCode == 'o') {
	            		mntmLoadShipProject.notifyListeners(SWT.Selection, null);
	            	} else if (e.stateMask == SWT.CTRL && e.keyCode == 'e' && mntmExport.getEnabled()) {
	            		mntmExport.notifyListeners(SWT.Selection, null);
	            		
	            		// === show / hide graphics
	            	} else if (e.keyCode == '1') {
	            		showAnchor = !showAnchor;
	            		mntmShowAnchor.setSelection(showAnchor);
	            		canvas.redraw();
	            	} else if (e.keyCode == '2') {
	            		showMounts = !showMounts;
	            		mntmShowMounts.setSelection(showMounts);
	            		canvas.redraw();
	            	} else if (e.keyCode == '3') {
	            		showRooms = !showRooms;
	            		mntmShowRooms.setSelection(showRooms);
	            		canvas.redraw();
	            	} else if (e.keyCode == '4') {
	            		showHull = !showHull;
	            		mntmShowHull.setSelection(showHull);
	            		canvas.redraw();
	            	} else if (e.keyCode == '5') {
	            		showFloor = !showFloor;
	            		mntmShowFloor.setSelection(showFloor);
	            		canvas.redraw();
	            	} else if (e.keyCode == '6') {
	            		showShield = !showShield;
	            		mntmShowShield.setSelection(showShield);
	            		canvas.redraw();
	            		
	            		// === pin
	            	} else if (e.keyCode == '`') {
	            		if (selectedRoom != null) selectedRoom.setPinned(!selectedRoom.isPinned());
	            		if (selectedDoor != null) selectedDoor.setPinned(!selectedDoor.isPinned());
	            		if (selectedMount != null) selectedMount.pinned = !selectedMount.pinned;
	            		if (hullSelected) ship.hullPinned = !ship.hullPinned;
	            		if (shieldSelected) ship.shieldPinned = !ship.shieldPinned;
	            		canvas.redraw();
	            		
	            		// === tool hotkeys
	            	} else if (e.stateMask == SWT.NONE && (e.keyCode == 'q' || e.keyCode == 'w' || e.keyCode == 'e' || e.keyCode == 'r' || e.keyCode == 't')) {
	            		tltmPointer.setSelection(e.keyCode == 'q');
	            		tltmRoom.setSelection(e.keyCode == 'w');
	            		tltmDoor.setSelection(e.keyCode == 'e');
	            		tltmMount.setSelection(e.keyCode == 'r');
	            		tltmSystem.setSelection(e.keyCode == 't');
	            		
		        		// === nudge function
	            	} else if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT) {
	            		// sending it to an auxiliary function as to not make a clutter here
	            		nudgeSelected(e.keyCode);
    				}
            	}
            }
        });
		
	// === SYSTEM CONTEXT MENU
		
		menuSystem.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				if (selectedRoom != null) {
					mntmEmpty.setSelection(selectedRoom.getSystem() == Systems.EMPTY);
					// === subsystems
					mntmPilot.setSelection(selectedRoom.getSystem() == Systems.PILOT);
					mntmPilot.setEnabled(!isSystemAssigned(Systems.PILOT, selectedRoom));
					mntmSensors.setSelection(selectedRoom.getSystem() == Systems.SENSORS);
					mntmSensors.setEnabled(!isSystemAssigned(Systems.SENSORS, selectedRoom));
					mntmDoors.setSelection(selectedRoom.getSystem() == Systems.DOORS);
					mntmDoors.setEnabled(!isSystemAssigned(Systems.DOORS, selectedRoom));
					// === systems
					mntmEngines.setSelection(selectedRoom.getSystem() == Systems.ENGINES);
					mntmEngines.setEnabled(!isSystemAssigned(Systems.ENGINES, selectedRoom));
					mntmMedbay.setSelection(selectedRoom.getSystem() == Systems.MEDBAY);
					mntmMedbay.setEnabled(!isSystemAssigned(Systems.MEDBAY, selectedRoom));
					mntmOxygen.setSelection(selectedRoom.getSystem() == Systems.OXYGEN);
					mntmOxygen.setEnabled(!isSystemAssigned(Systems.OXYGEN, selectedRoom));
					mntmShields.setSelection(selectedRoom.getSystem() == Systems.SHIELDS);
					mntmShields.setEnabled(!isSystemAssigned(Systems.SHIELDS, selectedRoom));
					mntmWeapons.setSelection(selectedRoom.getSystem() == Systems.WEAPONS);
					mntmWeapons.setEnabled(!isSystemAssigned(Systems.WEAPONS, selectedRoom));
					// === special
					mntmArtillery.setSelection(selectedRoom.getSystem() == Systems.ARTILLERY);
					mntmArtillery.setEnabled(!isSystemAssigned(Systems.ARTILLERY, selectedRoom));
					mntmCloaking.setSelection(selectedRoom.getSystem() == Systems.CLOAKING);
					mntmCloaking.setEnabled(!isSystemAssigned(Systems.CLOAKING, selectedRoom));
					mntmDrones.setSelection(selectedRoom.getSystem() == Systems.DRONES);
					mntmDrones.setEnabled(!isSystemAssigned(Systems.DRONES, selectedRoom));
					mntmTeleporter.setSelection(selectedRoom.getSystem() == Systems.TELEPORTER);
					mntmTeleporter.setEnabled(!isSystemAssigned(Systems.TELEPORTER, selectedRoom));
					// ===
					mntmSysImage.setEnabled(!selectedRoom.getSystem().equals(Systems.EMPTY));
				}
			}
		});
		
		mntmEmpty.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedRoom.assignSystem(Systems.EMPTY);
				canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
			} });
		mntmOxygen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.OXYGEN, selectedRoom)) {
					selectedRoom.assignSystem(Systems.OXYGEN);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmMedbay.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.MEDBAY, selectedRoom)) {
					selectedRoom.assignSystem(Systems.MEDBAY);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmShields.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.SHIELDS, selectedRoom)) {
					selectedRoom.assignSystem(Systems.SHIELDS);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmWeapons.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.WEAPONS, selectedRoom)) {
					selectedRoom.assignSystem(Systems.WEAPONS);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmEngines.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.ENGINES, selectedRoom)) {
					selectedRoom.assignSystem(Systems.ENGINES);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmDoors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.DOORS, selectedRoom)) {
					selectedRoom.assignSystem(Systems.DOORS);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmPilot.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.PILOT, selectedRoom)) {
					selectedRoom.assignSystem(Systems.PILOT);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmSensors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.SENSORS, selectedRoom)) {
					selectedRoom.assignSystem(Systems.SENSORS);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmDrones.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.DRONES, selectedRoom)) {
					selectedRoom.assignSystem(Systems.DRONES);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmArtillery.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.ARTILLERY, selectedRoom)) {
					selectedRoom.assignSystem(Systems.ARTILLERY);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmTeleporter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.TELEPORTER, selectedRoom)) {
					selectedRoom.assignSystem(Systems.TELEPORTER);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmCloaking.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!isSystemAssigned(Systems.CLOAKING, selectedRoom)) {
					selectedRoom.assignSystem(Systems.CLOAKING);
					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
				}
			} });
		mntmSysImage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				String[] filterExtensions = new String[] {"*.png"};
				dialog.setFilterExtensions(filterExtensions);
				dialog.setFilterPath(resPath);
				dialog.setText("");
				String path = dialog.open();
				
				if (!ShipIO.isNull(path) && selectedRoom != null) {
					selectedRoom.img = path;

					ShipIO.loadSystemImage(selectedRoom);
					canvas.redraw();
				}
			}
		});
		
		
	// === FILE MENU
		
		mntmNewShip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				int create = new NewShipWindow(shell).open();
				shell.setEnabled(true);
				if (create != 0) {
	        		mntmClose.notifyListeners(SWT.Selection, null);
	        		
					ship = new FTLShip();
					ship.isPlayer = create == 1;
					ship.anchor.x = 140;
					ship.anchor.y = 140;
					print("New ship created.");
					
					anchor.setVisible(true);
					
					canvasActive = true;
					tltmPointer.setEnabled(true);
					tltmRoom.setEnabled(true);
					tltmDoor.setEnabled(true);
					tltmMount.setEnabled(true);
					tltmSystem.setEnabled(true);
					btnHull.setEnabled(true);
					if (ship.rooms.size() > 0) {
						btnShields.setEnabled(ship.isPlayer);
						btnShields.setToolTipText(null);
					}
					btnCloak.setEnabled(true);
					btnFloor.setEnabled(ship.isPlayer);
					btnMiniship.setEnabled(ship.isPlayer);
					btnShipProperties.setEnabled(true);
					updateButtonImg();
					
					if (!ship.isPlayer) {
						ship.shieldPath = resPath + ShipIO.pathDelimiter + "img" + ShipIO.pathDelimiter + "ship" + ShipIO.pathDelimiter + "enemy_shields.png";
						ShipIO.loadImage(ship.shieldPath, "shields");
						shieldEllipse.x = GRID_W*35/2-100;
						shieldEllipse.y = GRID_H*35/2-100;
						shieldEllipse.width = 200;
						shieldEllipse.height = 200;
					}
					
					mntmSaveShip.setEnabled(true);
					mntmSaveShipAs.setEnabled(true);
					mntmExport.setEnabled(true);
					mntmClose.setEnabled(true);
					
					currentPath = null;
					
					mntmConToPlayer.setEnabled(!ship.isPlayer);
					mntmConToEnemy.setEnabled(ship.isPlayer);

					canvas.redraw();
				}
			}
		});
		
		mntmLoadShip.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("static-access")
			public void widgetSelected(SelectionEvent e) {
				ShipBrowser shipBrowser = new ShipBrowser(shell);
				shipBrowser.shell.open();

				shipBrowser.shell.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e)
					{
						if (ship != null) {
							canvasActive = true;
							
							anchor.setVisible(true);
							
							tltmPointer.setEnabled(true);
							tltmRoom.setEnabled(true);
							tltmDoor.setEnabled(true);
							tltmMount.setEnabled(true);
							tltmSystem.setEnabled(true);
							btnHull.setEnabled(true);
							if (ship.rooms.size() > 0) {
								btnShields.setEnabled(ship.isPlayer);
								btnShields.setToolTipText(null);
							}
							btnCloak.setEnabled(true);
							btnFloor.setEnabled(ship.isPlayer);
							btnMiniship.setEnabled(ship.isPlayer);
							btnShipProperties.setEnabled(true);
							updateButtonImg();
							
							mntmSaveShip.setEnabled(true);
							mntmSaveShipAs.setEnabled(true);
							mntmExport.setEnabled(true);
							mntmClose.setEnabled(true);
							
							if (ship.isPlayer) {
								if (loadShield && shieldImage != null && !shieldImage.isDisposed()) {
									Rectangle temp = shieldImage.getBounds();
									shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - temp.width/2 + ship.ellipse.x;
									shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - temp.height/2 + ship.ellipse.y;
									shieldEllipse.width = temp.width;
									shieldEllipse.height = temp.height;
								} else {
									shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - ship.ellipse.width + ship.ellipse.x;
									shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - ship.ellipse.height + ship.ellipse.y;
									shieldEllipse.width = ship.ellipse.width*2;
									shieldEllipse.height = ship.ellipse.height*2;
								}
							} else {
								shieldEllipse.width = ship.ellipse.width*2;
								shieldEllipse.height = ship.ellipse.height*2;
								shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 + ship.ellipse.x - ship.ellipse.width;
								shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 + ship.ellipse.y - ship.ellipse.height + 110;
							}
							ShipIO.updateIndexImgMaps();
							
							btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath));
							
							currentPath = null;
							
							mntmConToPlayer.setEnabled(!ship.isPlayer);
							mntmConToEnemy.setEnabled(ship.isPlayer);
							
							canvas.redraw();
						}
						if (ShipIO.errors.size() == 0 && Main.ship != null) {
							Main.print(((Main.ship.shipName!=null)?(Main.ship.shipClass + " - " + Main.ship.shipName):(Main.ship.shipClass)) + " [" + Main.ship.blueprintName + "] loaded successfully.");
						} else if (ShipIO.errors.size() > 0) {
							Main.print("Errors occured during ship loading; some data may be missing.");
							Main.erDialog.printErrors(ShipIO.errors);
							Main.erDialog.open();
							
							ShipIO.errors.clear();
						}
					}
				});
			}
		});
		
		mntmSaveShip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentPath == null) {
					ShipIO.askSaveDir();
				} else {
					ShipIO.saveShipProject(currentPath);
					
					ConfigIO.saveConfig();
					print("Project saved successfully.");
				}
				
				ConfigIO.saveConfig();
			}
		});
		
		mntmSaveShipAs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ShipIO.askSaveDir();
				
				ConfigIO.saveConfig();
				print("Project saved successfully.");
			}
		});
		
		mntmExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exDialog = new ExportDialog(shell);
				exDialog.open();
				
				shell.setEnabled(true);
			}
		});
		
		mntmLoadShipProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ShipIO.askLoadDir();
				
				if (ship != null) {
					anchor.setVisible(true);
					
					canvasActive = true;
					tltmPointer.setEnabled(true);
					tltmRoom.setEnabled(true);
					tltmDoor.setEnabled(true);
					tltmMount.setEnabled(true);
					tltmSystem.setEnabled(true);
					btnHull.setEnabled(true);
					if (ship.rooms.size() > 0) {
						btnShields.setEnabled(ship.isPlayer);
						btnShields.setToolTipText(null);
					}
					btnCloak.setEnabled(true);
					btnFloor.setEnabled(ship.isPlayer);
					btnMiniship.setEnabled(ship.isPlayer);
					btnShipProperties.setEnabled(true);
					updateButtonImg();

					if (ship.isPlayer) {
						if (loadShield && shieldImage != null && !shieldImage.isDisposed()) {
							Rectangle temp = shieldImage.getBounds();
							shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - temp.width/2 + ship.ellipse.x;
							shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - temp.height/2 + ship.ellipse.y;
							shieldEllipse.width = temp.width;
							shieldEllipse.height = temp.height;
						} else {
							shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - ship.ellipse.width + ship.ellipse.x;
							shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - ship.ellipse.height + ship.ellipse.y;
							shieldEllipse.width = ship.ellipse.width*2;
							shieldEllipse.height = ship.ellipse.height*2;
						}
					} else {
						shieldEllipse.width = ship.ellipse.width*2;
						shieldEllipse.height = ship.ellipse.height*2;
						shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 + ship.ellipse.x - ship.ellipse.width;
						shieldEllipse.y = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().y/2 + ship.ellipse.y - ship.ellipse.height + 110;
					}
					ShipIO.updateIndexImgMaps();
					
					btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath));

					mntmSaveShip.setEnabled(true);
					mntmSaveShipAs.setEnabled(true);
					mntmExport.setEnabled(true);
					mntmClose.setEnabled(true);
					
					mntmConToPlayer.setEnabled(!ship.isPlayer);
					mntmConToEnemy.setEnabled(ship.isPlayer);
					
					ConfigIO.saveConfig();
					
					canvas.redraw();
				}
				
				if (ShipIO.errors.size() == 0) {
					Main.print("Project loaded successfully.");
				} else {
					Main.print("Errors occured during project loading. Some data may be missing");
					Main.erDialog.printErrors(ShipIO.errors);
					Main.erDialog.open();
					ShipIO.errors.clear();
				}
			}
		});
		
		mntmClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (ship != null) {
					for (FTLRoom r : ship.rooms) {
						r.dispose();
					}
					ship.rooms.clear();
					
					ship.doors.clear();
					if (!ShipIO.loadingSwitch && hullImage != null && !hullImage.isDisposed())
						hullImage.dispose();
					if (!ShipIO.loadingSwitch && shieldImage != null && !shieldImage.isDisposed())
						shieldImage.dispose();
					if (!ShipIO.loadingSwitch && floorImage != null && !floorImage.isDisposed())
						floorImage.dispose();
					if (!ShipIO.loadingSwitch && cloakImage != null && !cloakImage.isDisposed())
						cloakImage.dispose();
				}
				
				btnCloaked.setEnabled(false);
				hullImage = null;
				shieldImage = null;
				floorImage = null;
				ship = null;
				idList.clear();
				clearButtonImg();
				currentPath = null;
				shieldEllipse.x = 0;
				shieldEllipse.y = 0;
				shieldEllipse.width = 0;
				shieldEllipse.height = 0;
				
				anchor.setVisible(false);

				canvasActive = false;
				
				tltmPointer.setEnabled(false);
				tltmRoom.setEnabled(false);
				tltmDoor.setEnabled(false);
				tltmMount.setEnabled(false);
				tltmSystem.setEnabled(false);
				btnHull.setEnabled(false);
				btnShields.setEnabled(false);
				btnShields.setToolTipText("Shield is aligned in relation to rooms. Place a room before choosing shield graphic.");
				btnCloak.setEnabled(false);
				btnFloor.setEnabled(false);
				btnMiniship.setEnabled(false);
				btnShipProperties.setEnabled(false);
				txtX.setEnabled(false);
				txtY.setEnabled(false);

				mntmSaveShip.setEnabled(false);
				mntmSaveShipAs.setEnabled(false);
				mntmExport.setEnabled(false);
				mntmClose.setEnabled(false);
				
				canvas.redraw();
			}
		});
		
	// === EDIT MENU
		
		mntmRemoveDoors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeDoor = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});

		mntmArbitraryPositionOverride.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				arbitraryPosOverride = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
			}
		});
		
		mntmConToPlayer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				convertToPlayer();
			}
		});
		
		mntmConToEnemy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				convertToEnemy();
			}
		});
	
	// === VIEW MENU
		
		mntmOpenErrorsConsole.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				erDialog.open();
			}
		});

		mntmShowAnchor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showAnchor = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});
		
		mntmShowMounts.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showMounts = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});
		
		mntmShowRooms.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showRooms = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});
		
		mntmShowHull.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showHull = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});
		
		mntmShowFloor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showFloor = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});
		
		mntmShowShield.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showShield = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});

		mntmLoadFloorGraphic.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadFloor = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});

		mntmLoadShieldGraphic.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadShield = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});

		mntmLoadSystem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadSystem = ((MenuItem) e.widget).getSelection();
				ConfigIO.saveConfig();
				canvas.redraw();
			}
		});
		
		mntmConstantRedraw.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				constantRedraw = mntmConstantRedraw.getSelection();
				ConfigIO.saveConfig();
			}
		});
	}
	
//======================================================
// === BOOKMARK: AUXILIARY METHODS
	
	// === SHIP CONVERSIONS
	
	public void convertToPlayer() {
		if (Main.ship != null) {
			ship.isPlayer = true;
			
			btnShields.setEnabled(ship.rooms.size() > 0);
			
			ship.shieldPath = null;
			ship.shieldOverride = null;
			if (shieldImage != null && !shieldImage.isDisposed() && !ShipIO.loadingSwitch)
				shieldImage.dispose();
			shieldImage = null;
			
			shieldEllipse.x = 0;
			shieldEllipse.y = 0;
			shieldEllipse.width = 0;
			shieldEllipse.height = 0;
			
			updateButtonImg();
			
			btnFloor.setEnabled(true);
			btnMiniship.setEnabled(true);
			
			if (ship.weaponsBySet) ship.weaponSet.clear();
			if (ship.dronesBySet) ship.droneSet.clear();
			ship.weaponsBySet = false;
			ship.dronesBySet = false;
			
			ship.minSec = 0;
			ship.maxSec = 0;
			
			ship.crewMax = 8;

			mntmConToPlayer.setEnabled(false);
			mntmConToEnemy.setEnabled(true);
			
			print("Ship converted to player.");
		}
	}
	
	public void convertToEnemy() {
		if (Main.ship != null) {
			ship.isPlayer = false;
			
			ship.shipName = null;
			ship.descr = null;
			
			btnShields.setEnabled(false);
			
			if (shieldImage != null && !shieldImage.isDisposed() && !ShipIO.loadingSwitch)
				shieldImage.dispose();
			shieldImage = null;

			ship.shieldOverride = null;
			ship.shieldPath = resPath + ShipIO.pathDelimiter + "img" + ShipIO.pathDelimiter + "ship" + ShipIO.pathDelimiter + "enemy_shields.png";
			ShipIO.loadImage(ship.shieldPath, "shields");
			
			if (floorImage != null && !floorImage.isDisposed() && !ShipIO.loadingSwitch)
				floorImage.dispose();
			floorImage = null;
			ship.floorPath = null;
			ship.cloakOverride = null;
			
			ship.miniPath = null;
			
			btnFloor.setEnabled(false);
			btnMiniship.setEnabled(false);
			
			updateButtonImg();
			
			mntmConToPlayer.setEnabled(true);
			mntmConToEnemy.setEnabled(false);
			
			print("Ship converted to enemy.");
		}
	}

	// === ROUNDING TO GRID
	
	/**
	 * Aligns to closest line of the grid.
	 */
	public static int roundToGrid(int a) {
		return Math.round(a/35)*35;
	}
	/**
	 * Aligns to the lowest (left-most / top-most) line of the grid.
	 */
	public static int downToGrid(int a) {
		return (int) (Math.ceil(a/35)*35);
	}
	/**
	 * Aligns to the highest (right-most / bottom-most) line of the grid.
	 */
	public static int upToGrid(int a) {
		return (int) (Math.floor(a/35)*35);
	}
	

	//=================
	// === GENERAL

	public static void canvasRedraw(Rectangle rect, boolean all) {
		Main.canvas.redraw(rect.x, rect.y, rect.width, rect.height, all);
	}
	
	public static void copyRect(Rectangle source, Rectangle destination) {
		if (source != null && destination != null) {
			destination.x = source.x;
			destination.y = source.y;
			destination.width = source.width;
			destination.height = source.height;
		}
	}
	
	public static Rectangle cloneRect(Rectangle rect) {
		return new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Fixes a rectangle to have positive values height and width, moving its (x,y) origin as needed.
	 * The end effect is a rectangle that stays in exactly the same place, but has positive height and width.
	 * @param r Rectangle that needs to be fixed.
	 * @return Fixed rectangle
	 */
	public static Rectangle fixRect(Rectangle r) {
		Rectangle rect = new Rectangle(0,0,0,0);
		rect.x = r.width<0 ? r.x+r.width : r.x;
		rect.y = r.height<0 ? r.y+r.height : r.y;
		rect.width = r.width<0 ? -r.width : r.width;
		rect.height = r.height<0 ? -r.height : r.height;
		return rect;
	}

	/**
	 * Checks if given rect overlaps any of the already placed rooms. If given rect is inside the roomsList set, it doesn't perform check against that rect (meaning it won't return true).
	 * 
	 * @param rect rectangle to be checked
	 * @param treatAs if set to another rectangle, the self-exclusive check will be performed against that rectangle, and not the one in the first parameter. Can be set to null if not used.
	 * @return true if rect given in parameter overlaps any of already placed rooms/rects.
	 */
	public static boolean doesRectOverlap(Rectangle rect, Rectangle treatAs) {
		for (FTLRoom r : ship.rooms) {
			if (rect.intersects(r.getBounds()) && ((treatAs != null && r.getBounds() != treatAs) || (treatAs == null && r.getBounds() != rect)) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if a rect is wholly contained within another.
	 */
	public static boolean containsRect(Rectangle r1, Rectangle r2) {
		return r1.contains(r2.x, r2.y) && r1.contains(r2.x+r2.width, r2.y+r2.height); 
	}
	
	public Rectangle getRectFromClick() {
		Rectangle tempRect = new Rectangle(0,0,35,35);
		for(int x=0; x<GRID_W; x++) {
			for(int y=0; y<GRID_H; y++) {
				tempRect.x = x*35;
				tempRect.y = y*35;
				if (tempRect.contains(mousePosLastClick)) {
					return tempRect;
				}
			}
		}
		return null;
	}
	
	public static Rectangle getRectAt(int x, int y) {
		Rectangle tempRect = new Rectangle(0,0,35,35);
		Point p = new Point(x, y);
		for(int i=0; i<GRID_W; i++) {
			for(int j=0; j<GRID_H; j++) {
				tempRect.x = i*35;
				tempRect.y = j*35;
				if (tempRect.contains(p)) {
					return tempRect;
				}
			}
		}
		return null;
	}
	
	public Rectangle getRectFromMouse() {
		Rectangle tempRect = new Rectangle(0,0,35,35);
		for(int x=0; x<GRID_W; x++) {
			for(int y=0; y<GRID_H; y++) {
				tempRect.x = x*35;
				tempRect.y = y*35;
				if (tempRect.contains(mousePos)) {
					return tempRect;
				}
			}
		}
		return null;
	}

	public void nudgeSelected(int event) {
		// check so that if none is selected the function won't even bother going in
		if (hullSelected || shieldSelected || selectedMount != null || selectedRoom != null) {
			switch (event) {
				case (SWT.ARROW_UP):
					if (selectedRoom != null) {
						phantomRect = cloneRect(selectedRoom.getBounds());
						phantomRect.y -= 35;
						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds()) && phantomRect.y >= ship.anchor.y)
							selectedRoom.getBounds().y -= 35;
					}
					if (selectedMount != null) selectedMount.rect.y -= (modShift) ? 35 : 1;
					if (hullSelected) ship.imageRect.y -= (modShift) ? 35 : 1;
					if (shieldSelected) shieldEllipse.y -= (modShift) ? 35 : 1;
					break;
				case (SWT.ARROW_DOWN):
					if (selectedRoom != null) {
						phantomRect = cloneRect(selectedRoom.getBounds());
						phantomRect.y += 35;
						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds()) && phantomRect.y + phantomRect.height <= GRID_H*35)
							selectedRoom.getBounds().y += 35;
					}
					if (selectedMount != null) selectedMount.rect.y += (modShift) ? 35 : 1;
					if (hullSelected) ship.imageRect.y += (modShift) ? 35 : 1;
					if (shieldSelected) shieldEllipse.y += (modShift) ? 35 : 1;
					break;
				case (SWT.ARROW_LEFT):
					if (selectedRoom != null) {
						phantomRect = cloneRect(selectedRoom.getBounds());
						phantomRect.x -= 35;
						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds()) && phantomRect.x >= ship.anchor.x)
							selectedRoom.getBounds().x -= 35;
					}
					if (selectedMount != null) selectedMount.rect.x -= (modShift) ? 35 : 1;
					if (hullSelected) ship.imageRect.x -= (modShift) ? 35 : 1;
					if (shieldSelected) shieldEllipse.x -= (modShift) ? 35 : 1;
					break;
				case (SWT.ARROW_RIGHT):
					if (selectedRoom != null) {
						phantomRect = cloneRect(selectedRoom.getBounds());
						phantomRect.x += 35;
						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds()) && phantomRect.x + phantomRect.width <= GRID_W*35)
							selectedRoom.getBounds().x += 35;
					}
					if (selectedMount != null) selectedMount.rect.x += (modShift) ? 35 : 1;
					if (hullSelected) ship.imageRect.x += (modShift) ? 35 : 1;
					if (shieldSelected) shieldEllipse.x += (modShift) ? 35 : 1;
					break;
				default: break;
			}
			
			if (selectedRoom != null) updateCorners(selectedRoom);
			if (shieldSelected) {
				ship.ellipse.x = (shieldEllipse.x + shieldEllipse.width/2) - (ship.findLowBounds().x + ship.computeShipSize().x/2);
				ship.ellipse.y = (shieldEllipse.y + shieldEllipse.height/2) - (ship.findLowBounds().y + ship.computeShipSize().y/2) - ((ship.isPlayer) ? 0 : 110);
				ship.ellipse.width = shieldEllipse.width/2;
				ship.ellipse.height = shieldEllipse.height/2;
			}
			updateSelectedPosText();
			
			canvas.redraw();
		}
	}
	

	//=================
	// === ROOM RELATED
	
	public static void updateCorners(FTLRoom r) {
		corners[0] = new Rectangle(r.getBounds().x, r.getBounds().y, CORNER, CORNER);
		corners[1] = new Rectangle(r.getBounds().x+r.getBounds().width-CORNER, r.getBounds().y, CORNER, CORNER);
		corners[2] = new Rectangle(r.getBounds().x, r.getBounds().y+r.getBounds().height-CORNER, CORNER, CORNER);
		corners[3] = new Rectangle(r.getBounds().x+r.getBounds().width-CORNER, r.getBounds().y+r.getBounds().height-CORNER, CORNER, CORNER);
	}

	public static int getLowestId() {
		int i = -1;
		idList.add(-1);
		while(i < GRID_W*GRID_H && idList.contains(i)) {
			i++;
		}
		return i;
	}
	
	public static Point findFarthestCorner(FTLRoom r, Point p) {
		double d = 0;
		double t = 0;
		Point pt = null;
		for (int i = 0; i < 4; i++) {
			t = Math.sqrt( Math.pow(r.corners[i].x - p.x, 2) + Math.pow(r.corners[i].y - p.y,2) );
			if (d<t) {
				d = t;
				pt = r.corners[i];
			}	
		}
		return pt;
	}
	
	public static FTLRoom getRoomContainingRect(Rectangle rect) {
		if (rect != null) {
			for (FTLRoom r : ship.rooms) {
				if (r.getBounds().intersects(rect))
					return r;
			}
		}
		return null;
	}

	public static boolean isSystemAssigned(Systems sys, FTLRoom r) {
		for (FTLRoom rm : ship.rooms) {
			if (r != null && rm != r && rm.getSystem() == sys)
				return true;
		}
		return false;
	}
	
	public static boolean isSystemAssigned(Systems sys) {
		for (FTLRoom rm : ship.rooms) {
			if (rm.getSystem() == sys)
				return true;
		}
		return false;
	}
	
	public static FTLRoom getRoomWithSystem(Systems sys) {
		for (FTLRoom rm : ship.rooms) {
			if (rm.getSystem().equals(sys))
				return rm;
		}
		return null;
	}
	
	public static Rectangle getRectFromStation(FTLRoom r) {
		int w = r.getBounds().width/35;
		int y = (int) Math.floor(r.slot/w);
		int x = r.slot - y* w;
		
		return new Rectangle(r.getBounds().x+x*35, r.getBounds().y+y*35, 35, 35);
	}

	public static int getStationFromRect(Rectangle rect) {
		int x,y,slot=-2;
		for (FTLRoom r : ship.rooms) {
			if (r.getBounds().intersects(rect)) {
				x = (rect.x - r.getBounds().x)/35;
				y = (rect.y - r.getBounds().y)/35;
				slot = r.getBounds().width/35 * y + x;
			}
		}
		
		return slot;
	}
	
	public static Rectangle getStationDirected(FTLRoom r) {
		final int STATION_SIZE = 15;
		Rectangle rect = getRectFromStation(r);
		
		if (r.dir.equals(Slide.UP)) {
			rect.height = STATION_SIZE;
		} else if (r.dir.equals(Slide.RIGHT)) {
			rect.x += 35 - STATION_SIZE;
			rect.width = STATION_SIZE;
		} else if (r.dir.equals(Slide.DOWN)) {
			rect.y += 35 - STATION_SIZE;
			rect.height = STATION_SIZE;
		} else if (r.dir.equals(Slide.LEFT)) {
			rect.width = STATION_SIZE;
		}
		
		return rect;
	}
	
	//=================
	// === DOOR RELATED
	
	public static void removeUnalignedDoors() {
		if (removeDoor) {
			Object[] array = ship.doors.toArray();
			for (Object o : array) {
				FTLDoor d = (FTLDoor) o;
				if (!isDoorAtWall(d.getBounds())) {
					ship.doors.remove(d);
					d = null;
				}
			}
			array = null;
		}
	}
	
	/**
	 * 
	 * @param rect Rectangle which matches the parameters of a wall;
	 * @return FTLDoor at the given rect, if there is one.
	 */
	public FTLDoor wallToDoor(Rectangle rect) {
		for (FTLDoor dr : ship.doors) {
			if (rect != null && rect.intersects(dr.getBounds()) && rect.width == dr.getBounds().width) {
				return dr;
			}
		}
		return null;
	}

	public static boolean isDoorAtWall(Rectangle rect) {
		for (FTLRoom r : ship.rooms) {
			if (rect != null && r != null && rect.intersects(r.getBounds()) && !containsRect(r.getBounds(), rect))
				return true;
		}
		return false;
	}
	
	public static Rectangle getDoorAt(int x, int y) {
		Rectangle dr = new Rectangle(0,0,0,0);
		Point p = new Point(x, y);
		for(int i=0; i<GRID_W; i++) {
			for(int j=0; j<GRID_H; j++) {
				// horizontal
				dr.x = i*35+2; dr.y = j*35-3; dr.width = 31; dr.height = 6;
				if (dr.contains(p))
					return dr;
				
				dr.x = i*35-3; dr.y = j*35+2; dr.width = 6; dr.height = 31;
				if (dr.contains(p))
					return dr;
			}
		}
		return null;
	}
	
	public Rectangle getDoorFromMouse() {
		Rectangle dr = new Rectangle(0,0,0,0);
		for(int x=0; x<GRID_W; x++) {
			for(int y=0; y<GRID_H; y++) {
				// horizontal
				dr.x = x*35+2; dr.y = y*35-3; dr.width = 31; dr.height = 6;
				if (dr.contains(mousePos))
					return dr;
				
				dr.x = x*35-3; dr.y = y*35+2; dr.width = 6; dr.height = 31;
				if (dr.contains(mousePos))
					return dr;
			}
		}
		return null;
	}
	
	//=================
	// === MOUNT RELATED
	
	public static FTLMount getMountFromImage(Point p) {
		for (Integer index : indexImgMapNormal.keySet()) {
			if (indexImgMapNormal.get(index).contains(p)) {
				return ship.mounts.get(index);
			}
		}
		for (Integer index : indexImgMapRotated.keySet()) {
			if (indexImgMapRotated.get(index).contains(p)) {
				return ship.mounts.get(index);
			}
		}
		return null;
	}
	
	public static boolean doImagesContain(Point p) {
		boolean result = false;
		for (Integer index : indexImgMapNormal.keySet()) {
			result = indexImgMapNormal.get(index).contains(p);
			if (result) break;
		}
		if (!result) {
			for (Integer index : indexImgMapRotated.keySet()) {
				result = indexImgMapRotated.get(index).contains(p);
				if (result) break;
			}
		}
		return result;
	}
	
	public static int getMountIndex(FTLMount m) {
		int i = -1;
		for (FTLMount mt : ship.mounts) {
			i++;
			if (mt == m) break;
		}
		return i;
	}
	
	public static FTLMount getMountFromMouse() {
		for (FTLMount m : ship.mounts) {
			if (m.rect.contains(mousePos)) {
				return m;
			}
		}
		
		return null;
	}
	
	//=========================
	// === AUXILIARY / LAZYNESS
	
	public void clearButtonImg() {
		btnHull.setImage(null);
		btnShields.setImage(null);
		btnFloor.setImage(null);
		btnCloak.setImage(null);
		btnMiniship.setImage(null);
	}
	
	public void updateButtonImg() {
		btnHull.setImage((ShipIO.isNull(ship.imagePath) ? crossImage : tickImage));
		btnShields.setImage((ShipIO.isNull(ship.shieldPath) ? crossImage : tickImage));
		btnFloor.setImage((ShipIO.isNull(ship.floorPath) ? crossImage : tickImage));
		btnCloak.setImage((ShipIO.isNull(ship.cloakPath) ? crossImage : tickImage));
		btnMiniship.setImage((ShipIO.isNull(ship.miniPath) ? crossImage : tickImage));
	}
	
	public void updateSelectedPosText() {
		if (canvasActive) {
			boolean enable = selectedMount != null || selectedRoom != null || hullSelected || shieldSelected;
			
			txtX.setEnabled(enable);
			btnXplus.setEnabled(enable);
			btnXminus.setEnabled(enable);
			txtY.setEnabled(enable);
			btnYplus.setEnabled(enable);
			btnYminus.setEnabled(enable);
			
			if (!enable) {
				txtX.setText("");
				txtY.setText("");
			}
			
			if (selectedMount != null) {
				txtX.setText(""+(selectedMount.rect.x+selectedMount.rect.width/2));
				txtY.setText(""+(selectedMount.rect.y+selectedMount.rect.height/2));
			} else if (selectedDoor != null) {
				txtX.setText(""+(selectedDoor.getBounds().x/35+1));
				txtY.setText(""+(selectedDoor.getBounds().y/35+1));
			} else if (selectedRoom != null) {
				txtX.setText(""+(selectedRoom.getBounds().x/35+1));
				txtY.setText(""+(selectedRoom.getBounds().y/35+1));
			} else if (hullSelected) {
				txtX.setText(""+(ship.imageRect.x));
				txtY.setText(""+(ship.imageRect.y));
			} else if (shieldSelected) {
				txtX.setText(""+(shieldEllipse.x));
				txtY.setText(""+(shieldEllipse.y));
			}
		}
	}
	
	public void updateSelectedPosition() {
		int x=0, y=0;
		boolean doit = true;
		try {
			doit = !ShipIO.isNull(txtY.getText());
			x = Integer.parseInt(txtX.getText());
			y = Integer.parseInt(txtY.getText());
		} catch (NumberFormatException e) {
			doit = false;
		}
		
		if (doit) {
			if (selectedMount != null && (!selectedMount.pinned || arbitraryPosOverride)) {
				if (x >= GRID_W*35) x = GRID_W*35-15;
				if (y >= GRID_H*35) y = GRID_H*35-15;
				if (x <= -selectedMount.rect.width) x = 15-selectedMount.rect.width;
				if (y <= -selectedMount.rect.height) y = 15-selectedMount.rect.height;
				
				selectedMount.rect.x = x;
				selectedMount.rect.y = y;
				selectedMount.pos.x = selectedMount.rect.x - ship.imageRect.x;
				selectedMount.pos.y = selectedMount.rect.y - ship.imageRect.y;
				
				selectedMount.rect.x -= (selectedMount.rotate) ? (FTLMount.MOUNT_WIDTH/2) : (FTLMount.MOUNT_HEIGHT/2);
				selectedMount.rect.y -= (selectedMount.rotate) ? (FTLMount.MOUNT_HEIGHT/2) : (FTLMount.MOUNT_WIDTH/2);
			} else if (selectedDoor != null && (!selectedDoor.isPinned() || arbitraryPosOverride)) {
				//selectedDoor.rect.x = 
			} else if (selectedRoom != null && (!selectedRoom.isPinned() || arbitraryPosOverride)) {
				if (x > GRID_W-selectedRoom.getBounds().width/35) x = GRID_W - selectedRoom.getBounds().width/35 + 1;
				if (y > GRID_H-selectedRoom.getBounds().height/35) y = GRID_H - selectedRoom.getBounds().height/35 + 1;
				if (x <= ship.anchor.x/35) x = ship.anchor.x/35 + 1;
				if (y <= ship.anchor.y/35) y = ship.anchor.y/35 + 1;
				
				Rectangle collisionCheck = new Rectangle((x-1)*35, (y-1)*35, selectedRoom.getBounds().width, selectedRoom.getBounds().height);
				if (!doesRectOverlap(collisionCheck, selectedRoom.getBounds())) {
					selectedRoom.getBounds().x = (x-1) * 35;
					selectedRoom.getBounds().y = (y-1) * 35;
					updateCorners(selectedRoom);
				}
			} else if (hullSelected && (!ship.hullPinned || arbitraryPosOverride)) {
				if (x >= GRID_W*35) x = GRID_W*35-15;
				if (y >= GRID_H*35) y = GRID_H*35-15;
				if (x <= -ship.imageRect.width) x = 15-ship.imageRect.width;
				if (y <= -ship.imageRect.height) y = 15-ship.imageRect.height;
				
				ship.imageRect.x = x;
				ship.imageRect.y = y;
			} else if (shieldSelected && (!ship.shieldPinned || arbitraryPosOverride)) {
				if (x >= GRID_W*35) x = GRID_W*35-15;
				if (y >= GRID_H*35) y = GRID_H*35-15;
				if (x <= -shieldEllipse.width) x = 15-shieldEllipse.width;
				if (y <= -shieldEllipse.height) y = 15-shieldEllipse.height;
				
				shieldEllipse.x = x;
				shieldEllipse.y = y;
				ship.ellipse.x = (shieldEllipse.x + shieldEllipse.width/2) - (ship.findLowBounds().x + ship.computeShipSize().x/2);
				ship.ellipse.y = (shieldEllipse.y + shieldEllipse.height/2) - (ship.findLowBounds().y + ship.computeShipSize().y/2) - ((ship.isPlayer) ? 0 : 110);
			}
			
			canvas.redraw();
		}
	}
	
	public void drawToolIcon(PaintEvent e, String name) {
		e.gc.setAlpha(255);
		e.gc.drawImage(toolsMap.get(name),0, 0, 24, 24, mousePos.x+10, mousePos.y+10, 19, 20);
	}
	
	/**
	 * Prints given message to the box in top right corner of the app.
	 */
	public static void print(String msg) {
		if (!msg.equals("")) {
			lastMsg = msg;
		}
		text.setText(lastMsg);
	}
	
	public static void debug(String msg) {
		if (debug)
			System.out.println(msg);
	}
	
	
	// === Graphics
	
	static ImageData flip(ImageData srcData, boolean vertical) {
		int bytesPerPixel = srcData.bytesPerLine / srcData.width;
		int destBytesPerLine = srcData.width * bytesPerPixel;
		byte[] newData = new byte[srcData.data.length];
	    
		for (int srcY = 0; srcY < srcData.height; srcY++) {
			for (int srcX = 0; srcX < srcData.width; srcX++) {
				int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
				if (vertical) {
					destX = srcX;
					destY = srcData.height - srcY - 1;
				} else {
					destX = srcData.width - srcX - 1;
					destY = srcY;
				}
				destIndex = (destY * destBytesPerLine)
						+ (destX * bytesPerPixel);
				srcIndex = (srcY * srcData.bytesPerLine)
						+ (srcX * bytesPerPixel);
				System.arraycopy(srcData.data, srcIndex, newData, destIndex,
						bytesPerPixel);
			}
		}
		// destBytesPerLine is used as scanlinePad to ensure that no padding is
		// required

	    Color white = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);
	    Color black = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);
	    PaletteData palette = new PaletteData(new RGB[] { white.getRGB(), black.getRGB() });
	    final ImageData sourceData = new ImageData(srcData.width, srcData.height, srcData.depth, palette, destBytesPerLine, newData);
	    sourceData.transparentPixel = 0;
	    
		//return new ImageData(srcData.width, srcData.height, srcData.depth, srcData.palette, destBytesPerLine, newData);
	    return sourceData;
	}
	
	public static ImageData rotate(ImageData srcData, int direction) {
		int bytesPerPixel = srcData.bytesPerLine / srcData.width;
		int destBytesPerLine = (direction == SWT.DOWN) ? srcData.width
				* bytesPerPixel : srcData.height * bytesPerPixel;
		byte[] newData = new byte[srcData.data.length];
		int width = 0, height = 0;
		
		for (int srcY = 0; srcY < srcData.height; srcY++) {
			for (int srcX = 0; srcX < srcData.width; srcX++) {
				int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
				switch (direction) {
		        	case SWT.LEFT: // left 90 degrees
		        		destX = srcY;
		        		destY = srcData.width - srcX - 1;
		        		width = srcData.height;
		        		height = srcData.width;
		        		break;
		        	case SWT.RIGHT: // right 90 degrees
		        		destX = srcData.height - srcY - 1;
		        		destY = srcX;
		        		width = srcData.height;
		        		height = srcData.width;
		        		break;
		        	case SWT.DOWN: // 180 degrees
		        		destX = srcData.width - srcX - 1;
		        		destY = srcData.height - srcY - 1;
		        		width = srcData.width;
		        		height = srcData.height;
		        		break;
		        }
				destIndex = (destY * destBytesPerLine)
						+ (destX * bytesPerPixel);
				srcIndex = (srcY * srcData.bytesPerLine)
						+ (srcX * bytesPerPixel);
				System.arraycopy(srcData.data, srcIndex, newData, destIndex, bytesPerPixel);
			}
		}
		// destBytesPerLine is used as scanlinePad to ensure that no padding is
		// required

	    Color white = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);
	    Color black = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);
	    PaletteData palette = new PaletteData(new RGB[] { white.getRGB(), black.getRGB() });
	    final ImageData sourceData = new ImageData(width, height, srcData.depth, palette, destBytesPerLine, newData);
	    sourceData.transparentPixel = 0;
	    
		//return new ImageData(width, height, srcData.depth, srcData.palette, destBytesPerLine, newData);
		return sourceData;
	}
	
	public static void updatePainter() {
		anchor.setLocation(ship.anchor.x, ship.anchor.y,true);
		for (FTLRoom rm : ship.rooms) {
			rm.assignSystem(rm.getSystem());
			if (!rm.getSystem().equals(Systems.EMPTY)) {
				rm.getSysBox().setVisible(true);
			}
		}
	}
}


