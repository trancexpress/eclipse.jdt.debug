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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;

/**
 * Default handler for the Show Constants variables view menu
 * 
 * @since 3.6.200
 */
public class ShowConstantsHandler extends ViewFilterHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}

	@Override
	protected IViewPart getView() {
		return null;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
	}
	
	@Override
	public void dispose() {
	}
}
