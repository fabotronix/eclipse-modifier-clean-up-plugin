package org.loewner.visibilityCleanup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class VisibilityModifierCleanUp implements ICleanUp {

	public static final String REDUCE_CONSTRUCTOR_VISIBILITY_KEY = "org.loewner.checkstyle_cleanup.CheckstyleCleanup_REDUCE_CONSTRUCTOR_VISIBILITY_KEY";
	public static final String REMOVE_ENUM_CONSTRUCTOR_MODIFIERS_KEY = "org.loewner.checkstyle_cleanup.CheckstyleCleanup_REMOVE_ENUM_CONSTRUCTOR_MODIFIERS_KEY";
	public static final String REMOVE_STATIC_FROM_ENUMS_KEY = "org.loewner.checkstyle_cleanup.CheckstyleCleanup_REMOVE_STATIC_FROM_ENUMS_KEY";
	private CleanUpOptions _options;

	@Override
	public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
		final boolean reduceConstructorVisibility = _options.isEnabled(REDUCE_CONSTRUCTOR_VISIBILITY_KEY);
		final boolean removeEnumConstructorModifiers = _options.isEnabled(REMOVE_ENUM_CONSTRUCTOR_MODIFIERS_KEY);
		final boolean removeStaticFromEnumsModifiers = _options.isEnabled(REMOVE_STATIC_FROM_ENUMS_KEY);
		if (!reduceConstructorVisibility && !removeEnumConstructorModifiers && !removeStaticFromEnumsModifiers) {
			return null;
		}
		final CompilationUnit cu = context.getAST();
		final Collection<ASTNode> toRemove = new ArrayList<>();
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(MethodDeclaration node) {
				if (!node.isConstructor()) {
					return false;
				}
				if (reduceConstructorVisibility && node.getParent() instanceof TypeDeclaration) {
					final TypeDeclaration classDecl = (TypeDeclaration) node.getParent();
					final Visibility classVisibility = getVisibility(classDecl);
					if (classVisibility != Visibility.PUBLIC && classVisibility != Visibility.PROTECTED) {
						final List<?> modifiers = node.modifiers();
						if (modifiers != null) {
							for (final Object modifierNode : modifiers) {
								if (modifierNode instanceof Modifier) {
									if (((Modifier) modifierNode).isPublic()) {
										toRemove.add((Modifier) modifierNode);
									}
								}
							}
						}
					}
				}
				if (removeEnumConstructorModifiers && node.getParent() instanceof EnumDeclaration) {
					final List<?> modifiers = node.modifiers();
					if (modifiers != null) {
						for (final Object modifierNode : modifiers) {
							if (modifierNode instanceof Modifier) {
								final Modifier modifier = (Modifier) modifierNode;
								if (modifier.isPublic() || modifier.isPrivate() || modifier.isProtected()) {
									toRemove.add((Modifier) modifierNode);
								}
							}
						}
					}
				}
				return false;
			}

			@Override
			public boolean visit(EnumDeclaration node) {
				if (removeStaticFromEnumsModifiers) {
					final List<?> modifiers = node.modifiers();
					if (modifiers != null) {
						for (final Object modifierNode : modifiers) {
							if (modifierNode instanceof Modifier) {
								if (((Modifier) modifierNode).isStatic()) {
									toRemove.add((Modifier) modifierNode);
								}
							}
						}
					}
				}
				return true;
			}

		});
		if (toRemove.isEmpty()) {
			return null;
		}
		return new VisibilityCleanUpFix(context, toRemove);
	}

	private Visibility getVisibility(TypeDeclaration classDecl) {
		if ((classDecl.getModifiers() & Modifier.PUBLIC) != 0) {
			return Visibility.PUBLIC;
		}
		final ASTNode parent = classDecl.getParent();
		if (parent instanceof TypeDeclaration) {
			final TypeDeclaration outerClassDecl = (TypeDeclaration) parent;
			if (outerClassDecl.isInterface() && (outerClassDecl.getModifiers() & Modifier.PUBLIC) == 1) {
				return Visibility.PUBLIC;
			}
		}
		if ((classDecl.getModifiers() & Modifier.PROTECTED) != 0) {
			return Visibility.PROTECTED;
		}
		return Visibility.DEFAULT;
	}

	@Override
	public void setOptions(CleanUpOptions options) {
		_options = options;

	}

	@Override
	public String[] getStepDescriptions() {
		final List<String> steps = new ArrayList<>();
		if (_options.isEnabled(REDUCE_CONSTRUCTOR_VISIBILITY_KEY)) {
			steps.add("Reducing constructor visibility");
		}
		if (_options.isEnabled(REMOVE_ENUM_CONSTRUCTOR_MODIFIERS_KEY)) {
			steps.add("Removing enum constructor modifiers");
		}
		if (_options.isEnabled(REMOVE_STATIC_FROM_ENUMS_KEY)) {
			steps.add("Removing static from enums");
		}
		return steps.toArray(new String[steps.size()]);
	}

	@Override
	public CleanUpRequirements getRequirements() {
		return new CleanUpRequirements(true, false, false, null);
	}

	@Override
	public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits,
			IProgressMonitor monitor) throws CoreException {
		return new RefactoringStatus();
	}

}
