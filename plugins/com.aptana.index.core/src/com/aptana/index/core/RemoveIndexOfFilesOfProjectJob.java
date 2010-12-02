/**
 * Copyright (c) 2005-2010 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.index.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

class RemoveIndexOfFilesOfProjectJob extends IndexRequestJob
{

	private final IProject project;
	private final Set<IFile> files;

	RemoveIndexOfFilesOfProjectJob(IProject project, Set<IFile> files)
	{
		super(MessageFormat.format(Messages.RemoveIndexOfFilesOfProjectJob_Name, project.getName()), project
				.getLocationURI());
		this.project = project;
		this.files = files;
	}

	@Override
	public IStatus run(IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, files.size());
		if (sub.isCanceled())
		{
			return Status.CANCEL_STATUS;
		}
		if (!project.isAccessible() || getContainerURI() == null)
		{
			return Status.CANCEL_STATUS;
		}

		Index index = getIndex();
		try
		{
			// Cleanup indices for files
			for (IFile file : files)
			{
				if (monitor.isCanceled())
				{
					return Status.CANCEL_STATUS;
				}
				index.remove(file.getLocationURI());
				sub.worked(1);
			}
		}
		finally
		{
			try
			{
				index.save();
			}
			catch (IOException e)
			{
				IndexActivator.logError(e.getMessage(), e);
			}
			sub.done();
		}
		return Status.OK_STATUS;
	}

}