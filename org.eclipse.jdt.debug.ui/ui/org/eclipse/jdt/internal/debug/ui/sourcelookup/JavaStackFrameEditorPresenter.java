/*******************************************************************************
 * Copyright (c) 2025, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.LambdaUtils;
import org.eclipse.jdt.internal.debug.ui.actions.ToggleBreakpointAdapter;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class JavaStackFrameEditorPresenter {

	private class ClassFileUnhighlightingListener implements IDebugContextListener {

		private final IDebugContextService service;

		public ClassFileUnhighlightingListener(IDebugContextService service) {
			this.service = service;
		}
		@Override
		public void debugContextChanged(DebugContextEvent event) {
			if ((event.getFlags() & DebugContextEvent.STATE) > 0) {
				if (event.getContext() instanceof IStructuredSelection selection && selection.getFirstElement() instanceof JDIStackFrame frame
						&& frame.equals(currentClassFileFrame)) {
					return;
				}
				if (currentClassFileEditor != null) {
					currentClassFileEditor.unhighlight();
					currentClassFileEditor = null;
					currentClassFileFrame = null;
					service.removeDebugContextListener(this);
					classFileUnhighlighterRegistered = false;
				}
			}
		}
	}

	private boolean classFileUnhighlighterRegistered = false;
	private JDIStackFrame currentClassFileFrame;
	private ClassFileEditor currentClassFileEditor;

	public void present(IEditorPart editor, IStackFrame stackFrame) {
		if (stackFrame instanceof JDIStackFrame jdiFrame) {
			try {
				if (editor instanceof ClassFileEditor classEditor && classEditor.getDocumentProvider().getDocument(editor.getEditorInput()).getLength() == 0) {
					String methodName = jdiFrame.getMethodName();
					if (jdiFrame.isConstructor()) {
						methodName = jdiFrame.getDeclaringTypeName();
						methodName = methodName.substring(methodName.lastIndexOf('.') + 1);
					}
					classEditor.highlightInstruction(methodName, jdiFrame.getSignature(), jdiFrame.getCodeIndex());
					currentClassFileEditor = classEditor;
					currentClassFileFrame = jdiFrame;
					ensureListenerRegistered(editor.getSite().getPage());
				} else if (LambdaUtils.isLambdaFrame(jdiFrame) && editor instanceof ITextEditor textEditor) {
					IDocumentProvider provider = JavaUI.getDocumentProvider();
					IEditorInput editorInput = textEditor.getEditorInput();
					provider.connect(editorInput); // XXX needed?
					IDocument document = provider.getDocument(editorInput);
					IRegion region = document.getLineInformation(jdiFrame.getLineNumber() - 1);
					List<LambdaExpression> inLineLambdas = ToggleBreakpointAdapter.findLambdaExpressions(textEditor, region);
					for (LambdaExpression exp : inLineLambdas) {
						IMethodBinding methodBinding = exp.resolveMethodBinding();
						String key = methodBinding.getKey();
						if (key.contains(jdiFrame.getName())) {
							textEditor.selectAndReveal(exp.getStartPosition(), exp.getLength());
						}
					}
				}
			} catch (CoreException | BadLocationException e) {
				DebugUIPlugin.log(e);
			}
		}
	}

	private void ensureListenerRegistered(IWorkbenchPage page) {
		if (classFileUnhighlighterRegistered) {
			return;
		}
		IDebugContextService service = DebugUITools.getDebugContextManager().getContextService(page.getWorkbenchWindow());
		service.addDebugContextListener(new ClassFileUnhighlightingListener(service));
		classFileUnhighlighterRegistered = true;
	}
}
