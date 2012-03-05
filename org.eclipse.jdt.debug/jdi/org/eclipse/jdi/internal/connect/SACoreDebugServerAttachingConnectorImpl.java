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
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.VirtualMachineManagerImpl;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.Transport;

/**
 *  The JDI implementation of the SA core debug server attaching connector
 * 
 * @see http://docs.oracle.com/javase/6/docs/technotes/guides/jpda/conninv.html#Connectors
 * @since 3.8
 */
public class SACoreDebugServerAttachingConnectorImpl extends ConnectorImpl implements AttachingConnector {

	public static final String DEBUG_SERVER_NAME = "debugServerName"; //$NON-NLS-1$
	public static final String JDI_CONNECTOR_ID = "sun.jvm.hotspot.jdi.SADebugServerAttachingConnector"; //$NON-NLS-1$

	Transport fTransport = new Transport() {
		public String name() {
			return "RMI"; //$NON-NLS-1$
		}
	};
	
	/**
	 * Constructor
	 * @param virtualMachineManager
	 */
	public SACoreDebugServerAttachingConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
		super(virtualMachineManager);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.connect.Connector#defaultArguments()
	 */
	public Map<String, Argument> defaultArguments() {
		HashMap<String, Argument> args = new HashMap<String, Connector.Argument>();
		StringArgumentImpl arg = new StringArgumentImpl(DEBUG_SERVER_NAME, ConnectMessages.SACoreDebugServerAttachingConnectorImpl_0, ConnectMessages.SACoreDebugServerAttachingConnectorImpl_1, true);
		arg.setValue(""); //$NON-NLS-1$
		args.put(DEBUG_SERVER_NAME, arg);
		return args;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.connect.AttachingConnector#attach(java.util.Map)
	 */
	public VirtualMachine attach(Map<String, ? extends Argument> arguments) throws IOException, IllegalConnectorArgumentsException {
		String servername = arguments.get(DEBUG_SERVER_NAME).value();
		if(servername == null) {
			throw new IllegalConnectorArgumentsException(ConnectMessages.SACoreDebugServerAttachingConnectorImpl_3, DEBUG_SERVER_NAME);
		}
		if(servername.length() < 1) {
			throw new IllegalConnectorArgumentsException(ConnectMessages.SACoreDebugServerAttachingConnectorImpl_4, DEBUG_SERVER_NAME);
		}
		VirtualMachineImpl vm = null;
		try {
			Remote remote = Naming.lookup("rmi://"+servername); //$NON-NLS-1$
			System.out.println(remote.toString());
			vm = new VirtualMachineImpl();
		}
		catch (NotBoundException e) {
			throw new ConnectException(ConnectMessages.SACoreDebugServerAttachingConnectorImpl_5, e);
		}
		catch(RemoteException re) {
			throw new ConnectException(ConnectMessages.SACoreDebugServerAttachingConnectorImpl_5, re);
		}
		return vm;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.internal.connect.ConnectorImpl#description()
	 */
	@Override
	public String description() {
		return ConnectMessages.SACoreDebugServerAttachingConnectorImpl_2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.internal.connect.ConnectorImpl#name()
	 */
	@Override
	public String name() {
		return JDI_CONNECTOR_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdi.internal.connect.ConnectorImpl#transport()
	 */
	@Override
	public Transport transport() {
		return fTransport;
	}
}
