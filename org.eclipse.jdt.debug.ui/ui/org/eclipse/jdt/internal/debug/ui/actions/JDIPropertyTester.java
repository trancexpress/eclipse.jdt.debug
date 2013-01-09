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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Default property tester for JDT Debug UI
 * 
 * @since 3.6.200
 */
public class JDIPropertyTester extends PropertyTester {

	public static final String IS_JAVA_THREADS_RUNNING = "isJavaThreadsRunning"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(IS_JAVA_THREADS_RUNNING.equals(property) && receiver instanceof IStructuredSelection) {
			if(receiver instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) receiver;
				boolean running = true;
				for (Iterator<?> i = selection.iterator(); i.hasNext();) {
					Object o = i.next();
					if(o instanceof JDIThread) {
						JDIThread thread = (JDIThread) o;
						running &= !thread.isSuspended();
						if(!running) {
							return false;
						}
					}
					else {
						return false;
					}
				}
				return running;
			}
		}
		return false;
	}
}
