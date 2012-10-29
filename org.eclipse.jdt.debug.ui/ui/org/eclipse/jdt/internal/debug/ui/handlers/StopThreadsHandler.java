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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

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
					JDIThread jdt = i.next();
					VirtualMachine vm = jdt.getJavaDebugTarget().getVM();
					List<ReferenceType> rts = vm.classesByName("java.lang.Throwable"); //$NON-NLS-1$
					ClassType clazz = (ClassType) rts.get(0);
					Method constructor = clazz.concreteMethodByName("<init>", "(Ljava/lang/String;)V"); //$NON-NLS-1$ //$NON-NLS-2$
					List<Value> args = new ArrayList<Value>();
					args.add(vm.mirrorOf(jdt.getModelIdentifier()));
					ObjectReference oref = clazz.newInstance(
							jdt.getUnderlyingThread(), 
							constructor, 
							args, 
							ClassType.INVOKE_SINGLE_THREADED);
					jdt.getUnderlyingThread().stop(oref);
				}
				catch (InvalidTypeException e) {
					JDIDebugUIPlugin.log(e);
				}
				catch (ClassNotLoadedException e) {
					JDIDebugUIPlugin.log(e);
				}
				catch (IncompatibleThreadStateException e) {
					JDIDebugUIPlugin.log(e);
				}
				catch (InvocationException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}
		return null;
	}
}
