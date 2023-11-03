package org.bndtools.core.resolve.ui;

import java.util.Collections;
import java.util.Date;

import org.bndtools.core.resolve.ResolutionResult;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import aQute.bnd.build.model.BndEditModel;
import aQute.bnd.help.instructions.ResolutionInstructions.ResolveMode;
import biz.aQute.resolve.RunResolution;

public class ResolutionWizard extends Wizard {
	private final ResolutionResultsWizardPage	resultsPage;
	private final BndEditModel					model;
	private boolean								preserveRunBundleUnresolved;
	private Date								lastModelChangedAtOpening;

	@SuppressWarnings("unused")
	public ResolutionWizard(BndEditModel model, IFile file, ResolutionResult result) {
		this.model = model;
		this.lastModelChangedAtOpening = model.getLastChangedAt();

		resultsPage = new ResolutionResultsWizardPage(model);
		resultsPage.setResult(result);

		setWindowTitle("Resolve");
		setNeedsProgressMonitor(true);

		addPage(resultsPage);
	}

	@Override
	public boolean performFinish() {
		ResolutionResult result = resultsPage.getResult();

		if (!model.getLastChangedAt()
			.equals(lastModelChangedAtOpening)) {
			MessageDialog.openError(getShell(), "Error",
				"Wizard cannot continue and will now exit: Model has changed on " + model.getLastChangedAt()
					+ " since we opened it at " + lastModelChangedAtOpening);
			getContainer().getShell()
				.close();
			return false;
			// throw new IllegalStateException("Model has changed on " +
			// model.getLastChangedAt()
			// + " since we opened it at " + lastModelChangedAtOpening);
		}

		if (result != null && result.getOutcome() == ResolutionResult.Outcome.Resolved) {
			RunResolution resolution = result.getResolution();
			assert resolution.isOK();

			if (model.getResolveMode() == ResolveMode.beforelaunch) {
				resolution.cache();
			} else {
				resolution.updateBundles(model);
			}
		} else {
			if (!preserveRunBundleUnresolved)
				model.setRunBundles(Collections.emptyList());
		}
		return true;
	}

	@Override
	public void dispose() {
		if (resultsPage.getResult() != null && resultsPage.getResult()
			.getLogger() != null) {
			resultsPage.getResult()
				.getLogger()
				.close();
		}
		super.dispose();
	}

	public void setAllowFinishUnresolved(boolean allowFinishUnresolved) {
		resultsPage.setAllowCompleteUnresolved(allowFinishUnresolved);
	}

	public void setPreserveRunBundlesUnresolved(boolean preserve) {
		this.preserveRunBundleUnresolved = preserve;
	}
}
