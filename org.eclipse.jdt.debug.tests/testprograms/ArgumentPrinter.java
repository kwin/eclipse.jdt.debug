/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class ArgumentPrinter {
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			System.out.print(args[i]);
		}
		System.out.println();
	}	
}