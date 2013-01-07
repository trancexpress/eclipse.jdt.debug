/*******************************************************************************
 * Copyright (c) Jan 4, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

/**
 * Default base class for all handlers that perform some kind of evaluation
 * 
 * @since 3.6.200
 */
public abstract class AbstractEvaluateHandler extends AbstractHandler implements IEvaluationListener {

	/**
	 * Display the given evaluation result.
	 */
	abstract protected void displayResult(IEvaluationResult result);
	
	/**
	 * Returns the target part this handler should use to resolve debug UI elements against
	 * 
	 * @return the target {@link IWorkbenchPart}
	 */
	abstract protected IWorkbenchPart getTargetPart();
	
	/**
	 * Displays a failed evaluation message in the data display.
	 */
	protected void reportErrors(IEvaluationResult result) {
		String message= getErrorMessage(result);
		reportError(message);
	}
	
	protected Shell getShell() {
		if (getTargetPart() != null) {
			return getTargetPart().getSite().getShell();
		}
		return JDIDebugUIPlugin.getActiveWorkbenchShell();
	}
	
	protected void reportError(String message) {
		IDataDisplay dataDisplay= getDirectDataDisplay();
		if (dataDisplay != null) {
			if (message.length() != 0) {
				dataDisplay.displayExpressionValue(NLS.bind(ActionMessages.EvaluateAction__evaluation_failed__Reason, new String[] {format(message)})); 
			} else {
				dataDisplay.displayExpressionValue(ActionMessages.EvaluateAction__evaluation_failed__1); 
			}
		} else {
			Status status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, message, null);
			ErrorDialog.openError(getShell(), ActionMessages.Evaluate_error_title_eval_problems, null, status); 
		}
	}
	
	protected String getErrorMessage(IEvaluationResult result) {
		String[] errors= result.getErrorMessages();
		if (errors.length == 0) {
			return getExceptionMessage(result.getException());
		}
		return getErrorMessage(errors);
	}
	
	protected String getErrorMessage(String[] errors) {
		String message= ""; //$NON-NLS-1$
		for (int i= 0; i < errors.length; i++) {
			String msg= errors[i];
			if (i == 0) {
				message= msg;
			} else {
				message= NLS.bind(ActionMessages.Evaluate_error_problem_append_pattern, new Object[] { message, msg }); 
			}
		}
		return message;
	}
	
	public static String getExceptionMessage(Throwable exception) {
		if (exception instanceof CoreException) {
			CoreException ce = (CoreException)exception;
			Throwable throwable= ce.getStatus().getException();
			if (throwable instanceof com.sun.jdi.InvocationException) {
				return getInvocationExceptionMessage((com.sun.jdi.InvocationException)throwable);
			} else if (throwable instanceof CoreException) {
				// Traverse nested CoreExceptions
				return getExceptionMessage(throwable);
			}
			return ce.getStatus().getMessage();
		}
		String message= NLS.bind(ActionMessages.Evaluate_error_message_direct_exception, new Object[] { exception.getClass() }); 
		if (exception.getMessage() != null) {
			message= NLS.bind(ActionMessages.Evaluate_error_message_exception_pattern, new Object[] { message, exception.getMessage() }); 
		}
		return message;
	}
	
	/**
	 * Returns a message for the exception wrapped in an invocation exception
	 */
	protected static String getInvocationExceptionMessage(com.sun.jdi.InvocationException exception) {
			InvocationException ie= exception;
			ObjectReference ref= ie.exception();
			return NLS.bind(ActionMessages.Evaluate_error_message_wrapped_exception, new Object[] { ref.referenceType().name() }); 
	}
	
	private String format(String message) {
		StringBuffer result= new StringBuffer();
		int index= 0, pos;
		while ((pos= message.indexOf('\n', index)) != -1) {
			result.append("\t\t").append(message.substring(index, index= pos + 1)); //$NON-NLS-1$
		}
		if (index < message.length()) {
			result.append("\t\t").append(message.substring(index)); //$NON-NLS-1$
		}
		return result.toString();
	}
	
	protected IDataDisplay getDataDisplay() {
		IDataDisplay display= getDirectDataDisplay();
		if (display != null) {
			return display;
		}
		IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				IViewPart view = page.findView(IJavaDebugUIConstants.ID_DISPLAY_VIEW);
				if (view == null) {
					try {
						view= page.showView(IJavaDebugUIConstants.ID_DISPLAY_VIEW);
					} catch (PartInitException e) {
						JDIDebugUIPlugin.statusDialog(ActionMessages.EvaluateAction_Cannot_open_Display_view, e.getStatus()); 
					} finally {
						page.activate(activePart);
						
					}
				}
				if (view != null) {
					page.bringToTop(view);
					return (IDataDisplay)view.getAdapter(IDataDisplay.class);
				}			
			}
		}
		
		return null;		
	}
	
	protected IDataDisplay getDirectDataDisplay() {
		IWorkbenchPart part= getTargetPart();
		if (part != null) {
			IDataDisplay display= (IDataDisplay)part.getAdapter(IDataDisplay.class);
			if (display != null) {
				IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
				if (page != null) {
					IWorkbenchPart activePart= page.getActivePart();
					if (activePart != null) {
						if (activePart != part) {
							page.activate(part);
						}
					}
				}
				return display;
			}
		}
		IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				IDataDisplay display= (IDataDisplay)activePart.getAdapter(IDataDisplay.class);
				if (display != null) {
					return display;
				}	
			}
		}
		return null;
	}
	
	/**
	 * @see IEvaluationListener#evaluationComplete(IEvaluationResult)
	 */
	public void evaluationComplete(final IEvaluationResult result) {
		// if plug-in has shutdown, ignore - see bug# 8693
		if (JDIDebugUIPlugin.getDefault() == null) {
			return;
		}
		
		final IJavaValue value= result.getValue();
		if (result.hasErrors() || value != null) {
			final Display display= JDIDebugUIPlugin.getStandardDisplay();
			if (display.isDisposed()) {
				return;
			}
			displayResult(result);
		}
	}
}
