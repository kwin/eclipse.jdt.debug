/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;

/**
 * Tests default source containers
 */
public class DefaultSourceContainerTests extends AbstractDebugTest {
	
	public DefaultSourceContainerTests(String name) {
		super(name);
	}
	
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testDefaultSourceContainerMemento() throws Exception {
		JavaSourceLookupDirector director = new JavaSourceLookupDirector();
		ILaunchConfiguration configuration = getLaunchConfiguration("Breakpoints");
		director.initializeDefaults(configuration);		
		ISourceContainer[] containers = director.getSourceContainers();
		assertEquals("expected one default container", 1, containers.length);
		assertTrue("Wrond default container", containers[0] instanceof DefaultSourceContainer);
		DefaultSourceContainer container = (DefaultSourceContainer)containers[0];
		String memento = director.getMemento();
		JavaSourceLookupDirector director2 = new JavaSourceLookupDirector();
		director2.initializeFromMemento(memento, configuration);
		DefaultSourceContainer restore = (DefaultSourceContainer) director2.getSourceContainers()[0];
		assertEquals("Default source container memento failed", container, restore);
	}			
}
