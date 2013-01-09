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
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.core.model.JDIClassObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sun.jdi.ClassType;
import com.sun.jdi.ReferenceType;

/**
 * Default handler for the Stop Threads command
 * 
 * @since 3.6.200
 */
public class StopThreadsHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection sel = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if(sel != null && !sel.isEmpty()) {
			//create the new exception class type once
			for(Iterator<JDIThread> i = sel.iterator(); i.hasNext();) {
				try {
					JDIThread thread = i.next();
					IJavaObject ex = getException(thread);
					if(ex != null) {
						thread.stop(ex);
					}
				}
				catch(DebugException de) {
					JDIDebugUIPlugin.log(de);
				}
			}
		}
		return null;
	}
	
	/**
	 * Compute the exception class to return with the stop. First try to compute <code>java.lang.ThreadDeath</code> and if
	 * that class has not been loaded use <code>java.lang.Throwable</code>.
	 * <br><br>
	 * We could try loading the type in the remote VM, but it would only work if we were trying to stop a suspended thread, or if we had 
	 * a suspended thread to send the load class message on.
	 * 
	 * @param thread the thread we want to stop
	 * @return the {@link IJavaObject} representing the {@link Throwable} we want to pass to the stop call
	 */
	IJavaObject getException(JDIThread thread) {
		ThreadReferenceImpl threadref = (ThreadReferenceImpl) thread.getUnderlyingThread();
		VirtualMachineImpl vm = threadref.virtualMachineImpl();
		List<ReferenceType> refs = vm.classesByName("java.lang.ThreadDeath"); //$NON-NLS-1$
		if(refs.isEmpty()) {
			refs = vm.classesByName("java.lang.Throwable"); //$NON-NLS-1$
		}
		if(!refs.isEmpty()) {
			ClassType clazz = (ClassType) refs.get(0);
			return new JDIClassObjectValue(thread.getJavaDebugTarget(), clazz.classObject());
		}
		return null;
	}
}
