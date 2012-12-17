/*******************************************************************************
 * Copyright (c) Nov 14, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.SWTFactory;

/**
 * Default handler for the Java Preferences command in the variables views
 * 
 * @since 3.7
 */
public class VariableOptionsHandler extends AbstractHandler {


	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SWTFactory.showPreferencePage("org.eclipse.jdt.debug.ui.JavaDetailFormattersPreferencePage",  //$NON-NLS-1$
    			new String[] {"org.eclipse.jdt.debug.ui.JavaDetailFormattersPreferencePage", //$NON-NLS-1$
    							"org.eclipse.jdt.debug.ui.JavaLogicalStructuresPreferencePage",  //$NON-NLS-1$
    							"org.eclipse.jdt.debug.ui.heapWalking",  //$NON-NLS-1$
    							"org.eclipse.jdt.debug.ui.JavaPrimitivesPreferencePage"}); //$NON-NLS-1$
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}
