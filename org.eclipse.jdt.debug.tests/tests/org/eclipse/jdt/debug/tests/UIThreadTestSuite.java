/*******************************************************************************
 * Copyright (c) 2026, Daniel Schmid and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Daniel Schmid - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * Allows running tests in the UI thread.
 */
public class UIThreadTestSuite extends TestSuite {

	public UIThreadTestSuite(Class<?> clazz) {
		super(clazz);
	}
	@Override
	public void run(TestResult result) {
		DebugUIPlugin.getStandardDisplay().syncExec(() -> super.run(result));
	}
}
