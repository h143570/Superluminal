package com.kartoflane.superluminal.elements;

import java.util.TreeMap;

import com.kartoflane.superluminal.core.Main;
import com.kartoflane.superluminal.painter.LayeredPainter;

public class Grid
{
	int horizontalCells;
	int verticalCells;
	TreeMap<Integer, TreeMap<Integer, GridBox>> gridArray;
	
	public Grid(int w, int h) {
		gridArray = new TreeMap<Integer, TreeMap<Integer, GridBox>>();
		horizontalCells = w;
		verticalCells = h;
		
		for (int x=1; x <= w; x++) {
			gridArray.put(x, new TreeMap<Integer, GridBox>());
			for (int y=1; y <= h; y++) {
				allocateNewCell(x,y);
			}
		}
	}
	
	public void setSize(int w, int h) {
		setSizeCell(w/35, h/35);
	}
	
	public void setSizeCell(int w, int h) {
		setWidthCell(w);
		setHeightCell(h);
	}
	
	public void setWidthCell(int w) {
		GridBox box;
		if (horizontalCells < w) {
			for (int x=horizontalCells; x <= w; x++)
				if (gridArray.get(x) == null) {
					gridArray.put(x, new TreeMap<Integer, GridBox>());
					for (int y=1; y <= verticalCells; y++) {
						if (getCellAt(x,y) == null)
							allocateNewCell(x, y);
					}
				} else {
					/*
					for (int y=1; y < verticalCells-1; y++) {
						box = getCellAt(x,y);
						if (box != null) box.setVisible(true);
					}
					*/
				}
		} else if (horizontalCells > w) {
			/*
			for (int x=horizontalCells; x > w; x--)
				for (int y=1; y < verticalCells-1; y++) {
					box = getCellAt(x,y);
					if (box != null) box.setVisible(false);
				}
			*/
		}
		
		horizontalCells = w;
	}
	
	public void setHeightCell(int h) {
		GridBox box;
		if (verticalCells < h) {
			for (int x=1; x <= verticalCells; x++)
				if (gridArray.get(x) == null) {
					gridArray.put(x, new TreeMap<Integer, GridBox>());
					for (int y=verticalCells; y <= h; y++) {
						if (getCellAt(x,y) == null)
							allocateNewCell(x, y);
					}
				} else {
					/*
					for (int y=1; y < h-1; y++) {
						box = getCellAt(x,y);
						if (box != null) box.setVisible(true);
					}
					*/
				}
		} else if (verticalCells > h) {
			/*
			for (int x=1; x < horizontalCells; x++)
				for (int y=verticalCells; y > h; y--) {
					box = getCellAt(x,y);
					if (box != null) box.setVisible(false);
				}
			*/
		}
		
		verticalCells = h;
	}
	
	private void allocateNewCell(int x, int y) {
		GridBox box = new GridBox();
		box.setLocation(x*35-35, y*35-35);
		box.setSize(35, 35);
		Main.layeredPainter.add(box, LayeredPainter.GRID);
		gridArray.get(x).put(y, box);
	}
	
	public GridBox getCellAt(int x, int y) {
		return gridArray.get(x).get(y);
	}
	
	private void remove(int x, int y) {
		gridArray.get(x).get(y).dispose();
	}
}
