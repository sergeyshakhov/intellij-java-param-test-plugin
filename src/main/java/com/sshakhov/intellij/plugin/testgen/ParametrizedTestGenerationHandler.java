package com.sshakhov.intellij.plugin.testgen;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.GenerateMembersHandlerBase;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class ParametrizedTestGenerationHandler extends GenerateMembersHandlerBase {
    public ParametrizedTestGenerationHandler(String title) {
        super(title);
    }

    @Override
    protected ClassMember[] getAllOriginalMembers(PsiClass psiClass) {
        return stream(psiClass.getMethods())
                .map(PsiMethodMember::new)
                .toArray(ClassMember[]::new);
    }

    @Override
    protected GenerationInfo[] generateMemberPrototypes(
            PsiClass psiClass,
            ClassMember originalMember
    ) {
        var project = psiClass.getManager().getProject();
        var factory = JavaPsiFacade.getInstance(project).getElementFactory();
        var originalMethod = ((PsiMethodMember) originalMember).getElement();
        var editor = FileEditorManager.getInstance(project);

        var testClassName = psiClass.getName() + "Test";

        var directory = findOrCreateTestDirectory(project, psiClass);
        var testClass = findOrCreateTestClass(directory, testClassName);

        var testMethodText = buildTestMethodText(originalMethod);
        var testMethod = factory.createMethodFromText(testMethodText, testClass);
        var methodSourceMethodText = buildMethodSourceMethod(testMethod.getName());
        var methodSourceMethod = factory.createMethodFromText(methodSourceMethodText, testClass);

        testClass.add(methodSourceMethod);
        testClass.add(testMethod);
        addMissingImports(testClass, factory);

        editor.openFile(testClass.getContainingFile().getVirtualFile(), true);

        return new GenerationInfo[0];
    }

    private PsiDirectory findOrCreateTestDirectory(Project project, PsiClass containingClass) {
        var containingFile = containingClass.getContainingFile();
        var sourceFile = containingFile.getVirtualFile();

        var sourceRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(sourceFile);

        var testPath = sourceRoot.getPath().replace("/main/", "/test/");
        var testRoot = VfsUtil.findFileByIoFile(new File(testPath), true);

        if (testRoot == null) {
            try {
                testRoot = VfsUtil.createDirectoryIfMissing(testPath);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        var testDirectory = PsiManager.getInstance(project).findDirectory(testRoot);

        var packageName = containingClass.getQualifiedName();
        if (packageName == null) {
            return testDirectory;
        }

        var packageParts = packageName.split("\\.");
        for (int i = 0; i < packageParts.length - 1; i++) {
            var part = packageParts[i];
            var subDir = testDirectory.findSubdirectory(part);
            if (subDir == null) {
                subDir = testDirectory.createSubdirectory(part);
            }
            testDirectory = subDir;
        }

        return testDirectory;
    }

    private PsiClass findOrCreateTestClass(PsiDirectory directory, String testClassName) {
        var testFile = (PsiJavaFile) directory.findFile(testClassName + ".java");

        if (testFile != null) {
            var classes = testFile.getClasses();
            if (classes.length > 0) {
                return classes[0];
            }
        }

        var directoryService = JavaDirectoryService.getInstance();

        return directoryService.createClass(directory, testClassName);
    }

    private void addMissingImports(PsiClass psiClass, PsiElementFactory factory) {
        var javaFile = (PsiJavaFile) psiClass.getContainingFile();
        var importList = javaFile.getImportList();
        if (importList == null) {
            return;
        }

        addImportIfMissing(factory, importList, "org.junit.jupiter.params");
        addImportIfMissing(factory, importList, "org.junit.jupiter.params.provider");
        addImportIfMissing(factory, importList, "java.util.stream");
        addStaticImportIfMissing(factory, importList, "org.assertj.core.api.Assertions", "assertThat");

        JavaCodeStyleManager.getInstance(psiClass.getProject()).optimizeImports(javaFile);
    }

    private void addImportIfMissing(PsiElementFactory factory, PsiImportList importList, String importText) {
        if (importList.getText().contains(importText)) {
            return;
        }

        var importStatement = factory.createImportStatementOnDemand(importText);
        importList.add(importStatement);
    }

    private void addStaticImportIfMissing(
            PsiElementFactory factory,
            PsiImportList importList,
            String qualifiedName,
            String memberName
    ) {
        if (importList.getText().contains(qualifiedName + "." + memberName)) {
            return;
        }

        var importClass = factory.createTypeByFQClassName(qualifiedName).resolve();
        var importStatement = factory.createImportStaticStatement(importClass, memberName);
        importList.add(importStatement);
    }

    private String buildTestMethodText(PsiMethod originalMethod) {
        var originalClassName = originalMethod.getContainingClass().getName();
        var originalMethodName = originalMethod.getName();

        var testMethodParams = stream(originalMethod.getParameterList().getParameters())
                .map(PsiElement::getText)
                .collect(joining(","));
        testMethodParams += "," + originalMethod.getReturnType().getPresentableText() + " expected";

        var instanceCallParams = stream(originalMethod.getParameterList().getParameters())
                .map(PsiParameter::getName)
                .collect(joining(","));

        return """
                @ParameterizedTest
                @MethodSource
                void test_%s(%s) {
                    // Given
                    var instance = new %s();
                
                    // When
                    var result = instance.%s(%s);
                
                    // Then
                    assertThat(result).isEqualTo(expected);
                }
                """.formatted(
                originalMethodName,
                testMethodParams,
                originalClassName,
                originalMethodName,
                instanceCallParams
        );
    }

    private String buildMethodSourceMethod(String testMethodName) {
        return """
                public static Stream<Arguments> %s() {
                    return Stream.of(
                            Arguments.of()
                    );
                }
                """.formatted(
                testMethodName
        );
    }
}
