package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides the tree data for the deadlock view
 */
public class DeadLocksViewContentProvider implements ITreeContentProvider {
	
	Object[] fRoots= null;
	
	/**
	 * ThreadWrapper for the deadlock view
	 * Uses the data of ThreadWrapper
	 * We use this to differentiate the same thread in a deadlock list
	 * (the first and the last element in the list)
	 * @see ThreadWrapper
	 */
	public class ContentThreadWrapper {
		public IJavaThread fThread;
		public Object fParent= null;
		public boolean caughtInADeadLock;
		public Object fChild= null;
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (!(obj instanceof ContentThreadWrapper)) {
				return false;
			}
			ContentThreadWrapper other= (ContentThreadWrapper)obj;
			
			return other.fThread.equals(fThread);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return fThread.hashCode();
		}

	}

	/**
	 * MonitorWrapper for the deadlock view
	 */	
	public class ContentMonitorWrapper {
		public IJavaObject fMonitor;
		public Object fParent= null;
		public Object fChild= null;
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (!(obj instanceof ContentMonitorWrapper)) {
				return false;
			}
			ContentMonitorWrapper other= (ContentMonitorWrapper)obj;
			
			return other.fMonitor.equals(fMonitor);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return fMonitor.hashCode();
		}
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {
		Object object= null;
		if (parentElement instanceof ContentThreadWrapper) {
			object= ((ContentThreadWrapper)parentElement).fChild;
		} else if (parentElement instanceof ContentMonitorWrapper) {
			object= ((ContentMonitorWrapper)parentElement).fChild;
		}
		if (object != null) {
			return new Object[]{object};
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof ContentThreadWrapper) {
			return ((ContentThreadWrapper)element).fParent;
		} else if (element instanceof ContentMonitorWrapper) {
			return ((ContentMonitorWrapper)element).fParent;
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		
		if (element instanceof ContentThreadWrapper) {
			return ((ContentThreadWrapper)element).fChild != null;
		} else if (element instanceof ContentMonitorWrapper) {
			return ((ContentMonitorWrapper)element).fChild != null;
		}
		
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (fRoots == null) {
			MonitorManager manager= (MonitorManager) inputElement;
			int numDeadLocks= manager.getNumberOfDeadlocks();
			//the list of roots elements
			fRoots= new Object[numDeadLocks];
			
			for (int i = 0; i < numDeadLocks; i++) {
				
				//all the root elements are ContentThreadWrapper
				ContentThreadWrapper rootWrapper = new ContentThreadWrapper();
				rootWrapper.fThread = manager.getStartThread(i);
				List deadlockList = manager.getDeadlockList(i);
				Map tree= new HashMap(deadlockList.size());
				tree.put(rootWrapper, rootWrapper);
				buildDeadlockTree(rootWrapper, tree, rootWrapper, deadlockList);
				fRoots[i] = rootWrapper;
			}
		}
		return fRoots;
	}

	protected void buildDeadlockTree(ContentThreadWrapper ctw, Map tree, Object parent, List deadlockList) {
		Object next;
		Object object;
		List childFinder= new ArrayList(deadlockList.size());
		for (int j= 1; j < deadlockList.size(); j++) {
			next= deadlockList.get(j);
			
			if (next instanceof IJavaObject) {
				ContentMonitorWrapper wrapper= new ContentMonitorWrapper();
				wrapper.fMonitor = (IJavaObject)next;
				wrapper.fParent = parent;
				object= wrapper;
			} else {
				ContentThreadWrapper wrapper= new ContentThreadWrapper();
				wrapper.fThread = (IJavaThread)next;
				wrapper.fParent = parent;
				object = wrapper;
			}
			if (j == 1) {
				ctw.fChild= object;
			}
			Object inTree= tree.get(object);
			if (inTree != null) {
				if (inTree instanceof ContentThreadWrapper) {
					((ContentThreadWrapper)inTree).caughtInADeadLock= true;
					((ContentThreadWrapper)object).caughtInADeadLock= true;
					List threadsInDeadlock= new ArrayList(2);
					threadsInDeadlock.add(inTree);
					threadsInDeadlock.add(object);
					tree.put(object, threadsInDeadlock);
				} else if (inTree instanceof ContentMonitorWrapper) {
				} else {
					List threadsInDeadlock= (List)inTree;
					((ContentThreadWrapper)object).caughtInADeadLock= true;
					threadsInDeadlock.add(object);
				}
			} else {
				tree.put(object, object);
			}
			parent= object;
			childFinder.add(object);
		}
		
		for (int j = 0; j < childFinder.size() - 1; j++) {
			Object element = childFinder.get(j);
			if (element instanceof ContentMonitorWrapper) {
				((ContentMonitorWrapper)element).fChild= childFinder.get(j+1);
			} else {
				((ContentThreadWrapper)element).fChild= childFinder.get(j+1);
			}	
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		MonitorManager.getDefault().removeDeadlockUpdateListener();
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		MonitorManager.getDefault().addDeadlockUpdateListener(this);
	}
	
	protected void clearDeadlockInformation() {
		fRoots= null;
	}
}
