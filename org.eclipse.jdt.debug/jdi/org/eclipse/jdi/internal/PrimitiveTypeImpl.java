package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.PrimitiveType;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class PrimitiveTypeImpl extends TypeImpl implements PrimitiveType {
	/**
	 * Creates new instance.
	 */
	public PrimitiveTypeImpl(String description, VirtualMachineImpl vmImpl, String name, String signature) {
		super(description, vmImpl, name, signature);
	}
	
	/**
	 * Creates new instance based on primitive signature.
	 */
	public static PrimitiveTypeImpl create(VirtualMachineImpl vmImpl, String signature) {
		// Notice that Primitive Types are not stored or cached because they don't 'remember' any information.

		// See JNI 1.1 Specification, Table 3-2 Java VM Type Signatures.
		switch (signature.charAt(0)) {
			case 'Z': return new BooleanTypeImpl(vmImpl);
			case 'B': return new ByteTypeImpl(vmImpl);
			case 'C': return new CharTypeImpl(vmImpl);
			case 'S': return new ShortTypeImpl(vmImpl);
			case 'I': return new IntegerTypeImpl(vmImpl);
			case 'J': return new LongTypeImpl(vmImpl);
			case 'F': return new FloatTypeImpl(vmImpl);
			case 'D': return new DoubleTypeImpl(vmImpl);
		}
		throw new InternalError(JDIMessages.getString("PrimitiveTypeImpl.Invalid_primitive_signature____1") + signature + JDIMessages.getString("PrimitiveTypeImpl.__2")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @returns primitive type tag.
	 */
	public abstract byte tag();

	/**
	 * @return Returns modifier bits.
	 */
	public int modifiers() {
		throw new InternalError(JDIMessages.getString("PrimitiveTypeImpl.A_PrimitiveType_does_not_have_modifiers_3")); //$NON-NLS-1$
	}
}
