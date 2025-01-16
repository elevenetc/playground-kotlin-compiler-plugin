package utils

import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtilRt
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.PsiFileFactoryImpl
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils

fun buildKtFile(
    name: String,
    source: String,
    project: Project,
    path: String = "",
    ignoreParseErrors: Boolean = false
): KtFile {
    val shortName = name.substring(name.lastIndexOf('/') + 1).let { subName ->
        subName.substring(subName.lastIndexOf('\\') + 1)
    }

    val virtualFile = object : LightVirtualFile(
        /* name = */ shortName,
        /* language = */ KotlinLanguage.INSTANCE,
        /* text = */ StringUtilRt.convertLineSeparators(source),
    ) {
        override fun getPath(): String = "${path}/$name"
    }

    val factory = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
    val ktFile = factory.trySetupPsiForFile(
        /* lightVirtualFile = */ virtualFile,
        /* language = */ KotlinLanguage.INSTANCE,
        /* physical = */ true,
        /* markAsCopy = */ false,
    ) as KtFile

    if (!ignoreParseErrors) AnalyzingUtils.checkForSyntacticErrors(ktFile)
    return ktFile
}