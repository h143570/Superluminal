package com.kartoflane.superluminal.undo;

/**
 * All undoable edits should fall into one of the categories below, defining the Edit class that defines their undo/redo behaviour.
 * 
 * @author kartoFlane
 *
 */
public class Undoable {
	public static final int MOVE = 0;
	public static final int RESIZE = 1;
	public static final int PIN = 2;
	public static final int SHIP_OFFSET = 3; // unused
	/** Assign/unassign system to/from room */
	public static final int ASSIGN_SYSTEM = 4;
	/** Mount rotate flag */
	public static final int ROTATE = 5;
	/** System stations direction, mount slide direction */
	public static final int DIRECTION = 6;
	/** Ex. mount mirror flag */
	public static final int FACING = 7;
	/** System stations */
	public static final int SLOT = 8;
	/** Room interior, hull graphic, etc */
	public static final int IMAGE = 9;
	public static final int SYS_LEVEL = 10;
	public static final int SYS_MAX = 11;
	public static final int LINK = 12;
	public static final int LAYER = 13;
	public static final int CREATE_ROOM = 14;
	public static final int CREATE_DOOR = 15;
	public static final int CREATE_MOUNT = 16;
	public static final int CREATE_GIB = 17;
	public static final int SPLIT = 18;
	// TODO list all feasible undoable edits
}
