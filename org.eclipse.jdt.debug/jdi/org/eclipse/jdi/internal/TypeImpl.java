package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class TypeImpl extends AccessibleImpl implements Type {
	/** Text representation of this type. */
	protected String fName = null;
	/** JNI-style signature for this type. */
	protected String fSignature = null;

	/**
	 * Creates new instance, used for REFERENCE types.
	 */
	protected TypeImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}
	
	/**
	 * Creates new instance, used for PRIMITIVE types or VOID.
	 */
	protected TypeImpl(String description, VirtualMachineImpl vmImpl, String name, String signature) {
		super(description, vmImpl);
		setName(name);
		setSignature(signature);
	}

	/**
	 * @return Returns new instance based on signature and (if it is a ReferenceType) classLoader.
	 * @throws ClassNotLoadedException when type is a ReferenceType and it has not been loaded
	 * by the specified class loader.
	 */
	public static TypeImpl create(VirtualMachineImpl vmImpl, String signature, ClassLoaderReference classLoader) throws ClassNotLoadedException {
		// For void values, a VoidType is always returned.
		if (TypeImpl.isVoidSignature(signature))
			return new VoidTypeImpl(vmImpl);
		
		// For primitive variables, an appropriate PrimitiveType is always returned. 
		if (TypeImpl.isPrimitiveSignature(signature))
			return PrimitiveTypeImpl.create(vmImpl, signature);
		
		// For object variables, the appropriate ReferenceType is returned if it has
		// been loaded through the enclosing type's class loader.
		return ReferenceTypeImpl.create(vmImpl, signature, classLoader);
	}

	/**
	 * Assigns name.
	 */
	public void setName(String name) {
		fName = name;
	}
	
	/**
	 * Assigns signature.
	 */
	public void setSignature(String signature) {
		fSignature = signature;
	}
	
	/**
	 * @return Returns description of Mirror object.
	 */
	public String toString() {
		try {
			return name();
		} catch (ClassNotPreparedException e) {
			return JDIMessages.getString("TypeImpl.(Unloaded_Type)_1"); //$NON-NLS-1$
		} catch (Exception e) {
			return fDescription;
		}
	}
	
	/**
	 * @return Create a null value instance of the type.
	 */
	public abstract Value createNullValue();

	/**
	 * @return Returns text representation of this type.
	 */
	public String name() {
		return fName;
	}
	
	/**
	 * @return JNI-style signature for this type.
	 */
	public String signature() {
		return fSignature;
	}
	
	/**
	 * @return Returns modifier bits.
	 */
	public abstract int modifiers();
	
	/**
	 * @returns Returns true if signature is a class signature.
	 */
	public static boolean isClassSignature(String signature) {
		return signature.charAt(0) == 'L';
	}

	/**
	 * @returns Returns true if signature is an array signature.
	 */
	public static boolean isArraySignature(String signature) {
		return signature.charAt(0) == '[';
	}

	/**
	 * @returns Returns true if signature is an primitive signature.
	 */
	public static boolean isPrimitiveSignature(String signature) {
		switch (signature.charAt(0)) {
			case 'Z':
			case 'B':
			case 'C':
			case 'S':
			case 'I':
			case 'J':
			case 'F':
			case 'D':
				return true;
		}
		return false;
	}

	/**
	 * @returns Returns true if signature is void signature.
	 */
	public static boolean isVoidSignature(String signature) {
		return (signature.charAt(0) == 'V');
	}
	
	/**
	 * Converts a class name to a JNI signature.
	 */
	public static String classNameToSignature(String qualifiedName) {
		// L<classname>;  : fully-qualified-class
		/* JNI signature examples:
		 * int[][]             -> [[I
		 * long[]              -> [J
		 * java.lang.String    -> Ljava/lang/String;
		 * java.lang.String[]  -> [Ljava/lang/String;
		 */
		StringBuffer signature= new StringBuffer();

		int firstBrace= qualifiedName.indexOf('[');
		if (firstBrace < 0) {
			// Not an array type. Must be class type.
			signature.append('L');
			signature.append(qualifiedName.replace('.','/'));
			signature.append(';');
			return signature.toString();
		}
		
		int index= 0;
		while ((index= (qualifiedName.indexOf('[', index) + 1)) > 0) {
			signature.append('[');
		}

		String name= qualifiedName.substring(0, firstBrace);
		switch (name.charAt(0)) {
			// Check for primitive array type
			case 'b':
				if (name.equals("byte")) { //$NON-NLS-1$
					signature.append('B');
					return signature.toString();
				} else if (name.equals("boolean")) { //$NON-NLS-1$
					signature.append('Z');
					return signature.toString();
				}
				break;
			case 'i':
				if (name.equals("int")) { //$NON-NLS-1$
					signature.append('I');
					return signature.toString();
				}
				break;
			case 'd':
				if (name.equals("double")) { //$NON-NLS-1$
					signature.append('D');
					return signature.toString();
				}
				break;
			case 's':
				if (name.equals("short")) { //$NON-NLS-1$
					signature.append('S');
					return signature.toString();
				}
				break;
			case 'c':
				if (name.equals("char")) { //$NON-NLS-1$
					signature.append('C');
					return signature.toString();
				}
				break;
			case 'l':			
				if (name.equals("long")) { //$NON-NLS-1$
					signature.append('J');
					return signature.toString();
				}
				break;
			case 'f':
				if (name.equals("float")) { //$NON-NLS-1$
					signature.append('F');
					return signature.toString();
				}
				break;
		}
		// Class type array
		signature.append('L');
		signature.append(name.replace('.','/'));
		signature.append(';');
		return signature.toString();
	}

	/**
	 * Converts a JNI class signature to a name.
	 */
	public static String classSignatureToName(String signature) {
		// L<classname>;  : fully-qualified-class  
		return signature.substring(1, signature.length() - 1).replace('/','.');
	}

	/**
	 * Converts a JNI array signature to a name.
	 */
	public static String arraySignatureToName(String signature) {
		// [<type> : array of type <type>
		if (signature.indexOf('[') < 0) {
			return signature;
		}
		StringBuffer name= new StringBuffer();
		String type= signature.substring(signature.lastIndexOf('[') + 1);
		if (type.length() == 1 && isPrimitiveSignature(type)) {
			name.append(getPrimitiveSignatureToName(type.charAt(0)));
		} else {
			name.append(classSignatureToName(type));
		}
		int index= 0;
		while ((index= (signature.indexOf('[', index) + 1)) > 0) {
			name.append('[').append(']');
		}
		return signatureToName(signature.substring(1)) + "[]"; //$NON-NLS-1$
	}

	/**
	 * @returns Returns Type Name, converted from a JNI signature.
	 */
	public static String signatureToName(String signature) {
		// See JNI 1.1 Specification, Table 3-2 Java VM Type Signatures.
		String primitive= getPrimitiveSignatureToName(signature.charAt(0));
		if (primitive != null) {
			return primitive;
		}
		switch (signature.charAt(0)) {
			case 'V':
				return "void"; //$NON-NLS-1$
			case 'L':
				return classSignatureToName(signature);
			case '[':
				return arraySignatureToName(signature);
			case '(':
				throw new InternalError(JDIMessages.getString("TypeImpl.Can__t_convert_method_signature_to_name_2")); //$NON-NLS-1$
		}
		throw new InternalError(JDIMessages.getString("TypeImpl.Invalid_signature____3") + signature + JDIMessages.getString("TypeImpl.__4")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private static String getPrimitiveSignatureToName(char signature) {
		switch (signature) {
			case 'Z':
				return "boolean"; //$NON-NLS-1$
			case 'B':
				return "byte"; //$NON-NLS-1$
			case 'C':
				return "char"; //$NON-NLS-1$
			case 'S':
				return "short"; //$NON-NLS-1$
			case 'I':
				return "int"; //$NON-NLS-1$
			case 'J':
				return "long"; //$NON-NLS-1$
			case 'F':
				return "float"; //$NON-NLS-1$
			case 'D':
				return "double"; //$NON-NLS-1$
			default:
				return null;
		}
	}
	
	/**
	 * @returns Returns length of the Class type-string in the signature at the given index.
	 */
	private static int signatureClassTypeStringLength(String signature, int index) {
		int endPos = signature.indexOf(';', index + 1);
		if (endPos < 0)
			throw new InternalError(JDIMessages.getString("TypeImpl.Invalid_Class_Type_signature_5")); //$NON-NLS-1$
			
		return endPos - index + 1;
	}
	
	/**
	 * @returns Returns length of the first type-string in the signature at the given index.
	 */
	public static int signatureTypeStringLength(String signature, int index) {
		switch (signature.charAt(index)) {
			case 'Z':
			case 'B':
			case 'C':
			case 'S':
			case 'I':
			case 'J':
			case 'F':
			case 'D':
			case 'V':
				return 1;
			case 'L':
				return signatureClassTypeStringLength(signature, index);
			case '[':
				return 1 + signatureTypeStringLength(signature, index + 1);
			case '(':
				throw new InternalError(JDIMessages.getString("TypeImpl.Can__t_covert_method_signature_to_name_6")); //$NON-NLS-1$
		}
		throw new InternalError(JDIMessages.getString("TypeImpl.Invalid_signature____7") + signature + JDIMessages.getString("TypeImpl.__8")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @returns Returns Jdwp Tag, converted from a JNI signature.
	 */
	public static byte signatureToTag(String signature) {
		switch (signature.charAt(0)) {
			case 'Z':
				return BooleanValueImpl.tag;
			case 'B':
				return ByteValueImpl.tag;
			case 'C':
				return CharValueImpl.tag;
			case 'S':
				return ShortValueImpl.tag;
			case 'I':
				return IntegerValueImpl.tag;
			case 'J':
				return LongValueImpl.tag;
			case 'F':
				return FloatValueImpl.tag;
			case 'D':
				return DoubleValueImpl.tag;
			case 'V':
				return VoidValueImpl.tag;
			case 'L':
				return ObjectReferenceImpl.tag;
			case '[':
				return ArrayReferenceImpl.tag;
			case '(':
				throw new InternalError(JDIMessages.getString("TypeImpl.Can__t_covert_method_signature_to_tag___9") + signature); //$NON-NLS-1$
		}
		throw new InternalError(JDIMessages.getString("TypeImpl.Invalid_signature____10") + signature + JDIMessages.getString("TypeImpl.__11")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
