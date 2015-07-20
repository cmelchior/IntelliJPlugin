package io.realm;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class MissingRealmClassAnnotationInspection extends BaseJavaLocalInspectionTool {

    private static final Logger LOG = Logger.getInstance("#io.MissingRealmClassAnnotationInspection");
    private static final Set<String> REALM_CLASS_ANNOTATION = Collections.singleton("io.realm.annotation.RealmClass");

    @NotNull
    public String getDisplayName() {
        return "Class is missing @RealmClass annotation";
    }

    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String getShortName() {
        return "MissingRealmClass"; // Short name is also used to reference the html file with the inspection description.
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                super.visitAnnotation(annotation);
                annotation.getParameterList();
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(expression.getProject());
                PsiConstantEvaluationHelper constHelper = javaPsiFacade.getConstantEvaluationHelper();
                java.util.List<Integer> parameterIndex = new ArrayList<Integer>();

                // Find any parameters on the target method with the @RealmClass annotation.
                PsiMethod method = expression.resolveMethod();
                PsiParameter[] params = method.getParameterList().getParameters();
                for (int i = 0; i < params.length; i++) {
                    PsiAnnotation annotation = AnnotationUtil.findAnnotationInHierarchy(params[i], REALM_CLASS_ANNOTATION);
                    if (annotation != null) {
                        parameterIndex.add(i);
                    }
                }

                // Validate that input parameter also has the @RealmClass annotation
                // TODO Add inspection that only allow @RealmClass annotations on Class parameters
                PsiExpression[] arguments = expression.getArgumentList().getExpressions();
                for (Integer index : parameterIndex) {
                    PsiExpression arg = arguments[index];

//                    Odd, this doesn't work?
//                    if (PsiUtil.isConstantExpression(arg)) {
//                        try {
//                            Object result = constHelper.computeConstantExpression(arg);
//                        } catch (ConstantEvaluationOverflowException ignore) {
//                        }
//                    }

                    // Hard coded hack, assume that argument is the class reference.
                    // Verify that argument class has the @RealmClass annotation, otherwise flag as an error.
                    // TODO Quickfix to add annotation automatically
                    if (arg instanceof PsiClassObjectAccessExpression) {
                        PsiTypeElement typeElement = ((PsiClassObjectAccessExpression) arg).getOperand();
                        PsiClass javaClass = (PsiClass) typeElement.getInnermostComponentReferenceElement().resolve();
                        if (javaClass.getModifierList().findAnnotation("io.realm.annotation.RealmClass") == null) {
                            holder.registerProblem(typeElement, "Boom", ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
            }
        };
    }

    public boolean isEnabledByDefault() {
        return true;
    }
}
