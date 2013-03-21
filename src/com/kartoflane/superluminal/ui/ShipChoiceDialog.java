package com.kartoflane.superluminal.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kartoflane.superluminal.core.Main;
import com.kartoflane.superluminal.core.ShipIO;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class ShipChoiceDialog {
	Shell shell;
	List list;
	private Button btnCancel;
	private Button btnLoad;
	private Display display;
	
	public String result;
	public int declaration;
	private boolean exit = false;
	
	private int width = 0;
	private int height = 0;
	private boolean initialized = false;
	
	public ShipChoiceDialog(Shell parent) {
		shell = new Shell(parent, SWT.BORDER | SWT.RESIZE | SWT.TITLE);
		shell.setText(Main.APPNAME + " - Ship Choice");
		shell.setLayout(new GridLayout(1, false));
		
		display = Display.getCurrent();
		
		createContents();
		
		shell.pack();
		width = shell.getBounds().width;
		height = shell.getBounds().height;
		shell.setMinimumSize(width, height);
		initialized = true;
	}
	
	public String open() {
		Main.shell.setEnabled(false);
		btnLoad.setEnabled(false);
		shell.setVisible(true);
		shell.setActive();
		result = "";
		exit = false;
		
		while (shell.isVisible()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		Main.shell.setEnabled(true);
		return (exit) ? null : result;
	}
	
	private void createContents() {
		Label lblSuperluminalFoundSeveral = new Label(shell, SWT.WRAP);
		GridData gd_lblSuperluminalFoundSeveral = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblSuperluminalFoundSeveral.widthHint = 350;
		lblSuperluminalFoundSeveral.setLayoutData(gd_lblSuperluminalFoundSeveral);
		lblSuperluminalFoundSeveral.setText("Superluminal found several ships sharing the same blueprint name; choose which one you'd like to load:");
		lblSuperluminalFoundSeveral.setFont(Main.appFont);
		
		list = new List(shell, SWT.BORDER | SWT.V_SCROLL);
		GridData gd_list = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_list.minimumHeight = 100;
		list.setLayoutData(gd_list);
		list.setFont(Main.appFont);

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		btnLoad = new Button(composite, SWT.NONE);
		GridData gd_btnLoad = new GridData(SWT.RIGHT, SWT.BOTTOM, true, false, 1, 1);
		gd_btnLoad.widthHint = 80;
		btnLoad.setLayoutData(gd_btnLoad);
		btnLoad.setFont(Main.appFont);
		btnLoad.setText("Load");

		btnCancel = new Button(composite, SWT.NONE);
		GridData gd_btnCancel = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1);
		gd_btnCancel.widthHint = 80;
		btnCancel.setLayoutData(gd_btnCancel);
		btnCancel.setFont(Main.appFont);
		btnCancel.setText("Cancel");
		
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.setVisible(false);
				Main.shell.setEnabled(true);
				Main.shell.setActive();
				exit = true;
			}
		});
		
		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Pattern pattern = Pattern.compile(".*?\\[(.*?)\\].*?");
				Matcher matcher = pattern.matcher(list.getSelection()[0]);
				if (matcher.find()) {
					result = matcher.group(1);
					declaration = 0;
					
					for (String s : list.getItems()) {
						matcher = pattern.matcher(s);
						if (matcher.find() && matcher.group(1).equals(result)) {
							declaration++;
							if (s.equals(list.getSelection()[0])) break;
						}
					}
				} else {
					Main.erDialog.add("Error: no match found in ShipChoiceDialog list. This should never EVER happen.");
				}
				shell.setVisible(false);
				initialized = false;
				Main.shell.setEnabled(true);
				Main.shell.setActive();
			}
		});
		
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				btnCancel.notifyListeners(SWT.Selection, null);
			}
		});

		shell.addControlListener(new ControlAdapter() {
			public void controlMoved(ControlEvent e) {
			}
			
			public void controlResized(ControlEvent e) {
				if (initialized) {
					height = shell.getSize().y;
					shell.setSize(width, height);
				}
			}
		});
		
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (list.getSelectionCount()>0)
					btnLoad.notifyListeners(SWT.Selection, null);
			}
		});
		
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnLoad.setEnabled(list.getSelection() != null && list.getSelection().length > 0 && list.getSelection()[0] != null);
			}
		});
	}
	
	public void setChoices(Collection<String> list, File fileToScan) {
		this.list.removeAll();
		Collection<String> previous = new ArrayList<String>();
		for (String s : list) {
			this.list.add(ShipIO.getShipName(s, fileToScan, (previous.contains(s) ? previous.size()+1 : 1))+" ["+s+"]");
			previous.add(s);
		}
		previous.clear();
		previous = null;
	}
}
