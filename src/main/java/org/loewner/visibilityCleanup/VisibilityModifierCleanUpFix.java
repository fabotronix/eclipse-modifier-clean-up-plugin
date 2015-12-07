package org.loewner.visibilityCleanup;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;

public class VisibilityModifierCleanUpFix implements ICleanUpFix {

	private final CleanUpContext _context;
	private final Collection<ASTNode> _toRemove;

	VisibilityModifierCleanUpFix(CleanUpContext context, Collection<ASTNode> toRemove) {
		_context = context;
		_toRemove = toRemove;
	}

	@Override
	public CompilationUnitChange createChange(IProgressMonitor progressMonitor) throws CoreException {

		final CompilationUnitChange change = new CompilationUnitChange("Remove unnecessary visibility modifiers",
				_context.getCompilationUnit());
		final AST ast = _context.getAST().getAST();
		final ASTRewrite rewriter = ASTRewrite.create(ast);
		for (final ASTNode node : _toRemove) {
			rewriter.remove(node, null);
		}
		change.setEdit(rewriter.rewriteAST());
		return change;
	}

}
