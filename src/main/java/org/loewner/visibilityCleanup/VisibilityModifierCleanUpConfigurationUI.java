package org.loewner.visibilityCleanup;

import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpConfigurationUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class VisibilityModifierCleanUpConfigurationUI implements ICleanUpConfigurationUI {

	private CleanUpOptions _options;

	@Override
	public void setOptions(CleanUpOptions options) {
		_options = options;
	}

	@Override
	public Composite createContents(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 1;
		createCheckbox(composite, "Reduce constructor visibility to class visibility",
				VisibilityModifierCleanUp.REDUCE_CONSTRUCTOR_VISIBILITY_KEY);
		createCheckbox(composite, "Remove enum constructor modifiers",
				VisibilityModifierCleanUp.REMOVE_ENUM_CONSTRUCTOR_MODIFIERS_KEY);
		createCheckbox(composite, "Remove static from enum declarations",
				VisibilityModifierCleanUp.REMOVE_STATIC_FROM_ENUMS_KEY);
		return composite;
	}

	private void createCheckbox(final Composite composite, final String label, final String optionName) {
		final Button convertButton = new Button(composite, SWT.CHECK);
		convertButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		convertButton.setText(label);
		convertButton.setSelection(_options.isEnabled(optionName));
		convertButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				_options.setOption(optionName,
						convertButton.getSelection() ? CleanUpOptions.TRUE : CleanUpOptions.FALSE);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				_options.setOption(optionName,
						convertButton.getSelection() ? CleanUpOptions.TRUE : CleanUpOptions.FALSE);
			}
		});
	}

	@Override
	public int getCleanUpCount() {
		return 3;
	}

	@Override
	public int getSelectedCleanUpCount() {
		int count = 0;
		if (_options.isEnabled(VisibilityModifierCleanUp.REDUCE_CONSTRUCTOR_VISIBILITY_KEY)) {
			count++;
		}
		if (_options.isEnabled(VisibilityModifierCleanUp.REMOVE_ENUM_CONSTRUCTOR_MODIFIERS_KEY)) {
			count++;
		}
		if (_options.isEnabled(VisibilityModifierCleanUp.REMOVE_STATIC_FROM_ENUMS_KEY)) {
			count++;
		}
		return count;
	}

	@Override
	public String getPreview() {
		return "n/a";
	}

}
