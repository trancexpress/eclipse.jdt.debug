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
package org.eclipse.jdi.internal.connect;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdi.internal.VirtualMachineManagerImpl;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * The JDI implementation of the SA core attaching connector
 * 
 * @see http://docs.oracle.com/javase/6/docs/technotes/guides/jpda/conninv.html#Connectors
 * @since 3.8
 */
public class SACoreAttachingConnectorImpl extends ConnectorImpl implements AttachingConnector {

	public static final String CORE_PATH = "core"; //$NON-NLS-1$
	public static final String JAVA_EXECUTABLE_PATH = "javaExecutable"; //$NON-NLS-1$
	public static final String JDI_CONNECTOR_ID = "sun.jvm.hotspot.jdi.SACoreAttachingConnector"; //$NON-NLS-1$

	/**
	 * Constructor
	 * @param virtualMachineManager
	 */
	public SACoreAttachingConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
		super(virtualMachineManager);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.connect.Connector#defaultArguments()
	 */
	public Map<String, Argument> defaultArguments() {
		HashMap<String, Argument> args = new HashMap<String, Connector.Argument>();
		StringArgumentImpl arg = new StringArgumentImpl(CORE_PATH, ConnectMessages.SACoreAttachingConnectorImpl_0, ConnectMessages.SACoreAttachingConnectorImpl_1, false);
		arg.setValue(""); //$NON-NLS-1$
		args.put(CORE_PATH, arg);
		arg = new StringArgumentImpl(JAVA_EXECUTABLE_PATH, ConnectMessages.SACoreAttachingConnectorImpl_2, ConnectMessages.SACoreAttachingConnectorImpl_3, true);
		arg.setValue(""); //$NON-NLS-1$
		args.put(JAVA_EXECUTABLE_PATH, arg);
		return args;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.connect.AttachingConnector#attach(java.util.Map)
	 */
	public VirtualMachine attach(Map<String, ? extends Argument> arguments) throws IOException, IllegalConnectorArgumentsException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.internal.connect.ConnectorImpl#description()
	 */
	@Override
	public String description() {
		return ConnectMessages.SACoreAttachingConnectorImpl_4;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.internal.connect.ConnectorImpl#name()
	 */
	@Override
	public String name() {
		return JDI_CONNECTOR_ID;
	}
}
