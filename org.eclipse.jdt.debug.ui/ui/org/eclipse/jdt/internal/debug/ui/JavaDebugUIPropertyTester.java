/*******************************************************************************
 * Copyright (c) Jan 2, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;

/**
 *
 */
public class JavaDebugUIPropertyTester extends PropertyTester {

	public static final String SNIPPET_RUNNING = "snippetRunning"; //$NON-NLS-1$


	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(SNIPPET_RUNNING.equals(property)) {
			if(receiver instanceof JavaSnippetEditor) {
				JavaSnippetEditor editor = (JavaSnippetEditor) receiver;
				return editor.isEvaluating();
			}
		}
		return false;
	}
}
