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
package org.eclipse.jdt.debug.tests.sourcelookup;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.sourcelookup.JavaStackFrameSourceDisplayAdapter;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.PlatformUI;
import org.junit.Assume;

public class ClassFileEditorHighlightingTest extends AbstractDebugTest {

	private static final String CLASS_NAME = "OneToTen";

	public ClassFileEditorHighlightingTest(String name) {
		super(name);
	}

	public void testDisplaySourceWithClassFileEditorHighlightsLine() throws Exception {
		IJavaProject javaProject = getProjectContext();
		createLineBreakpoint(21, CLASS_NAME);

		JavaStackFrameSourceDisplayAdapter display = new JavaStackFrameSourceDisplayAdapter();

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(CLASS_NAME);

			ClassFileEditor editor = (ClassFileEditor) EditorUtility.openInEditor(javaProject.getProject().getFile("bin/" + CLASS_NAME + ".class"));
			IClassFileEditorInput editorInput = (IClassFileEditorInput) editor.getEditorInput();
			IClassFile classFile = editorInput.getClassFile();
			thread.getTopStackFrame().getLaunch().setSourceLocator(stackFrame -> classFile);

			openCurrentFrameAndExpectHighlightedText(display, thread, editor, "     0  getstatic java.lang.System.out : java.io.PrintStream [16]");

			stepOver((IJavaStackFrame) thread.getTopStackFrame());
			openCurrentFrameAndExpectHighlightedText(display, thread, editor, "     8  getstatic java.lang.System.out : java.io.PrintStream [16]");

			thread.resume();

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testConstructorInPackage() throws Exception {
		IJavaProject javaProject = getProjectContext();
		createMethodBreakpoint("org.eclipse.debug.tests.targets", "ClassOne.java", "ClassOne", "<init>", "()V", true, false);

		JavaStackFrameSourceDisplayAdapter display = new JavaStackFrameSourceDisplayAdapter();

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint("org.eclipse.debug.tests.targets.CallStack");

			ClassFileEditor editor = (ClassFileEditor) EditorUtility.openInEditor(
					javaProject.getProject().getFile("bin/org/eclipse/debug/tests/targets/ClassOne.class")
			);
			IClassFileEditorInput editorInput = (IClassFileEditorInput) editor.getEditorInput();
			IClassFile classFile = editorInput.getClassFile();
			thread.getTopStackFrame().getLaunch().setSourceLocator(stackFrame -> classFile);

			openCurrentFrameAndExpectHighlightedText(display, thread, editor, "    0  aload_0 [this]");

			thread.resume();

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testDisplaySourceWithClassFileEditorHighlightsLineInConstructor() throws Exception {
		IJavaProject javaProject = getProjectContext();
		createMethodBreakpoint("", "MethodCall.java", "MethodCall", "<init>", "()V", true, false);

		JavaStackFrameSourceDisplayAdapter display = new JavaStackFrameSourceDisplayAdapter();

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint("MethodCall");

			ClassFileEditor editor = (ClassFileEditor) EditorUtility.openInEditor(javaProject.getProject().getFile("bin/MethodCall.class"));
			IClassFileEditorInput editorInput = (IClassFileEditorInput) editor.getEditorInput();
			IClassFile classFile = editorInput.getClassFile();
			thread.getTopStackFrame().getLaunch().setSourceLocator(stackFrame -> classFile);

			openCurrentFrameAndExpectHighlightedText(display, thread, editor, "     0  aload_0 [this]");

			thread.resume();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testClassFileWithoutDebuggingInformation() throws Exception {
		IJavaProject javaProject = getProjectContext();
		URI uri = ResourcesPlugin.getWorkspace().getRoot().getFile(javaProject.getOutputLocation()).getLocationURI();

		compileWithJavac("NoSources.java", """
				class NoSources {
					private static int i;
					public static void main(String[] args) {
						i++;
						System.out.println(i);
					}
				}
				""", List.of("-g:none", "-d", new File(uri).getAbsolutePath(), "--release", "8"));
		String mainTypeName = "NoSources";
		ILaunchConfiguration config = createLaunchConfiguration(javaProject, mainTypeName);
		ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		config = workingCopy.doSave();

		JavaStackFrameSourceDisplayAdapter sourceDisplay = new JavaStackFrameSourceDisplayAdapter();

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(config);

			javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			IFile classResource = javaProject.getProject().getFile("bin/" + mainTypeName + ".class");
			ClassFileEditor editor = (ClassFileEditor) EditorUtility.openInEditor(classResource, true);

			IClassFileEditorInput editorInput = (IClassFileEditorInput) editor.getEditorInput();
			IClassFile classFile = editorInput.getClassFile();
			thread.getTopStackFrame().getLaunch().setSourceLocator(stackFrame -> classFile);

			List<String> expectedHighlights = List.of(/* @formatter:off */
					"     0  getstatic NoSources.i : int [7]",
					"     3  iconst_1", "     4  iadd",
					"     5  putstatic NoSources.i : int [7]",
					"     8  getstatic java.lang.System.out : java.io.PrintStream [13]",
					"    11  getstatic NoSources.i : int [7]",
					"    14  invokevirtual java.io.PrintStream.println(int) : void [19]",
					"    17  return"/* @formatter:on */
			);
			for (int i = 0; i < expectedHighlights.size(); i++) {
				openCurrentFrameAndExpectHighlightedText(sourceDisplay, thread, editor, expectedHighlights.get(i));

				if (i < expectedHighlights.size() - 1) {
					stepOver((IJavaStackFrame) thread.getTopStackFrame());
				}
			}
			thread.resume();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private void openCurrentFrameAndExpectHighlightedText(JavaStackFrameSourceDisplayAdapter sourceDisplay, IJavaThread thread, ClassFileEditor editor, String expectedHighlightedText) throws DebugException {
		StyledText noSourceTextWidget = editor.getNoSourceTextWidget();
		IStackFrame topStackFrame = thread.getTopStackFrame();
		assertNotNull(topStackFrame);
		sourceDisplay.displaySource(topStackFrame, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), true);
		StyleRange[] styleRanges = noSourceTextWidget.getStyleRanges();
		assertEquals(1, styleRanges.length);
		String highlightedText = noSourceTextWidget.getContent().getTextRange(styleRanges[0].start, styleRanges[0].length);
		assertEquals(expectedHighlightedText, highlightedText);
	}

	private void compileWithJavac(String className, String source, List<String> compilerOptions) {
        JavaFileObject fileObject = new SourceJavaFileObject(className, source);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assume.assumeNotNull(compiler);

		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		CompilationTask task = compiler.getTask(
				null, fileManager, collector, compilerOptions, null, List.of(fileObject)
		);
		Boolean result = task.call();
		assertTrue(String.valueOf(collector.getDiagnostics()), result);
    }

	private static class SourceJavaFileObject extends SimpleJavaFileObject {

		private final String code;

		protected SourceJavaFileObject(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return code;
		}
	}
}
