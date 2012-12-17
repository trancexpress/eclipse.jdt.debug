/*******************************************************************************
 * Copyright (c) Nov 16, 2012 IBM Corporation and others.
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
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;

/**
 * Default handler for handlers that set a view action filter
 * 
 * @since 3.6.200
 */
public abstract class ViewFilterHandler extends AbstractHandler {


	/**
	 * Tries to collect the view that this handler is active in
	 * @return the {@link IViewPart} or <code>null</code>
	 */
	protected abstract IViewPart getView();
	
	/**
	 * Tries to collect the viewer from the site this handler is operating in
	 * @return the debug view or <code>null</code>
	 */
	protected StructuredViewer getStructuredViewer() {
		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
		if (view != null) {
			Viewer viewer = view.getViewer();
			if (viewer instanceof StructuredViewer) {
				return (StructuredViewer)viewer;
			}
		}		
		return null;
	}
}
