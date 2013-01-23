/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.model.elements.DebugTargetMementoProvider;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;

/**
 * 
 */
public class JavaDebugTargetMementoProvider extends DebugTargetMementoProvider {

	@Override
	protected String getElementName(Object element) throws CoreException {
		if (element instanceof IJavaDebugTarget) {
			ILaunchConfiguration launchConfig = ((IJavaDebugTarget)element).getLaunch().getLaunchConfiguration();
			if (launchConfig != null) {
				return launchConfig.getName();
			}
		}
		return super.getElementName(element);
	}

}
