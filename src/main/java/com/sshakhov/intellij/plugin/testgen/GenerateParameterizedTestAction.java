package com.sshakhov.intellij.plugin.testgen;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

public class GenerateParameterizedTestAction extends BaseGenerateAction {

    protected GenerateParameterizedTestAction() {
        super(new ParametrizedTestGenerationHandler("Parametrized Test Generator"));
    }
}