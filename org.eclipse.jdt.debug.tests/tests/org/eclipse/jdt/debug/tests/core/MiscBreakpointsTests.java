package org.eclipse.jdt.debug.tests.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Home for breakpoint tests that don't fit elsewhere
 */
public class MiscBreakpointsTests extends AbstractDebugTest {

	private static final String COMPILE_ERROR_CONTENTS = 
	 "public class CompileError {\npublic static void main(String[] args) {\nString foo = \"foo\" + bar;\n}	\n}";

	public MiscBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * This method DEPENDS on the default setting of the 'suspend on uncaught exceptions'
	 * preference being TRUE.
	 */
	public void testSuspendOnUncaughtExceptions() throws Exception {
		String typeName = "ThrowsNPE";
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, true);
				
		IJavaThread thread = null;
		try {
			thread= launchAndSuspend(typeName);
			
			assertTrue("suspendee was not an IJavaThread", thread instanceof IJavaThread);
			IJavaThread javaThread = (IJavaThread) thread;
			int stackLine = javaThread.getTopStackFrame().getLineNumber();
			assertTrue("line number should be '26', but was " + stackLine, stackLine == 26);
		
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	/**
	 * This method DEPENDS on the default setting of the 'suspend on compilation errors'
	 * preference being TRUE.
	 */
	public void testSuspendOnCompilationErrors() throws Exception {
		String typeName = "CompileError";
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, true);		
		
		IType type = fJavaProject.findType(typeName);
		ICompilationUnit cu = type.getCompilationUnit();
		IBuffer buffer = cu.getBuffer();
		buffer.setContents(COMPILE_ERROR_CONTENTS);
		cu.save(new NullProgressMonitor(), true);
		
		IJavaThread thread = null;
		try {
			thread= launchAndSuspend(typeName);
			
			assertTrue("suspendee was not an IJavaThread", thread instanceof IJavaThread);
			IJavaThread javaThread = (IJavaThread) thread;
			int stackLine = javaThread.getTopStackFrame().getLineNumber();
			assertTrue("line number should be '3', but was " + stackLine, stackLine == 3);
		
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	public void testDontSuspendOnCompilationErrors() throws Exception {
		String typeName = "CompileError";
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, false);		
		
		IType type = fJavaProject.findType(typeName);
		ICompilationUnit cu = type.getCompilationUnit();
		IBuffer buffer = cu.getBuffer();
		buffer.setContents(COMPILE_ERROR_CONTENTS);
		cu.save(new NullProgressMonitor(), true);
		
		IJavaDebugTarget debugTarget = null;
		try {
			debugTarget= launchAndTerminate(typeName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}		
	}

	public void testDontSuspendOnUncaughtExceptions() throws Exception {
		String typeName = "ThrowsNPE";
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);		
		
		IJavaDebugTarget debugTarget= null;
		try {
			debugTarget = launchAndTerminate(typeName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}		
	}
	
	protected IPreferenceStore getPrefStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();		
	}
}
