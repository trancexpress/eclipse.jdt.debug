/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Default handler for the Interrupt Thread command
 * 
 * @since 3.6.200
 */
public class InterruptThreadsHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection sel = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if(sel != null && !sel.isEmpty()) {
			for(Iterator<?> i = sel.iterator(); i.hasNext();) {
				try {
					Object o = i.next();
					if(o instanceof JDIThread) {
						JDIThread thread = (JDIThread) o;
						thread.interrupt();
						thread.fireChangeEvent(DebugEvent.STATE);
					}
					else if(o instanceof JDIStackFrame) {
						JDIThread thread = (JDIThread) ((JDIStackFrame)o).getThread();
						thread.interrupt();
						thread.fireChangeEvent(DebugEvent.STATE);
					}
				}
				catch(DebugException de) {
					JDIDebugUIPlugin.log(de);
				}
			}
		}
		return null;
	}
}
