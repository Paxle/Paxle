/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.tools.console.impl.desktop;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JTextArea;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class Console extends JTextArea {
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Indicates that the user is currently not allowed to enter new commands.
	 */
	private boolean inputEnabled = true;

	/**
	 * A stream to read the command-output from
	 */
	private ConsoleOutputStream consoleOut;
	
	/**
	 * A stream to write the user command to
	 */
	private ConsoleInputStream consoleIn;
	
	private int inputStart = 0;
	private int inputOffset = 0;
	
	/**
	 * A text-area to display the user-input and command-output
	 */
	private JTextArea textArea;

	public Console() {
		super();
		
		// init the text-area
		this.textArea = this;
		this.textArea.setLineWrap(true);
		this.textArea.setWrapStyleWord(true);
		this.textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		this.setInputEnabled(false);
		
		// register listeners
		this.initListeners();

		// init streams
		this.consoleOut = new ConsoleOutputStream(this);
		this.consoleIn = new ConsoleInputStream(this);
	}

	public OutputStream getOutStream() {
		return this.consoleOut;
	}

	public void close(){		
		try {
			this.consoleOut.close();
			this.consoleIn.close();
		} catch (IOException e) {
			this.logger.error(e);
		}
	}
	public InputStream getInStream() {
		return consoleIn;
	}

	private void initListeners() {
		for (KeyListener k : getKeyListeners()) {
			removeKeyListener(k);
		}
		for (MouseListener m : getMouseListeners()) {
			removeMouseListener(m);
		}
		for (MouseMotionListener m : getMouseMotionListeners()) {
			removeMouseMotionListener(m);
		}

		this.addKeyListener(new ConsoleKeyListener());
	}
	
	
	synchronized void print(byte[] b, ConsoleOutputStream os) {
		this.insert(new String(b, 0, b.length), inputStart);
		this.inputStart += b.length;
		this.setCaretPosition(getCaretPosition() + b.length);
	}

	void setInputEnabled(boolean inputEnabled) {
		this.inputEnabled = inputEnabled;
	}

	private class ConsoleKeyListener implements KeyListener {
		synchronized public void keyPressed(KeyEvent event) {
			// mark event as consumed
			event.consume();
			if (!inputEnabled) return; 
			
			// process the event
			try {
				int caretPos = getCaretPosition();
				
				switch (event.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						setInputEnabled(false);
						
						// read the new command
						final String command = textArea.getText(inputStart,inputOffset);
						textArea.insert("\n", inputStart + inputOffset);
						inputStart += (inputOffset + 1);
						inputOffset = 0;
						setCaretPosition(inputStart);
						
						// write the command into the console-input stream for reading
						consoleIn.fill((command + '\n').getBytes());							
						break;
						
					case KeyEvent.VK_LEFT:
						if (caretPos > inputStart) {
							setCaretPosition(caretPos - 1);
						}
						break;
						
					case KeyEvent.VK_RIGHT:
						if (caretPos < inputStart + inputOffset) {
							setCaretPosition(caretPos + 1);
						}
						break;
						
					case KeyEvent.VK_HOME:
						setCaretPosition(inputStart);
						break;
						
					case KeyEvent.VK_END:
						setCaretPosition(inputStart + inputOffset);
						break;
						
					case KeyEvent.VK_DELETE:
						if (caretPos < inputStart + inputOffset) {
							textArea.replaceRange("", caretPos, caretPos+1);
							inputOffset--;
						}
						break;
						
					case KeyEvent.VK_BACK_SPACE:
						if (caretPos > inputStart) {
							textArea.replaceRange("", caretPos-1, caretPos);
							inputOffset--;
						}
						break;
						
					default:
						final char keyChar = event.getKeyChar();
						if (keyChar > 31 && keyChar < 127) {
							textArea.insert(String.valueOf(keyChar), getCaretPosition());
							inputOffset++;
						}
						break;
				}
				
				// notify the session reader that new bytes are available
				consoleIn.notifyReader();
			} catch (Exception e) {
				logger.error(e);
			}
		}

		public void keyTyped(KeyEvent event) {
			event.consume();
		}

		public void keyReleased(KeyEvent event) {
			event.consume();
		}
	}
}
