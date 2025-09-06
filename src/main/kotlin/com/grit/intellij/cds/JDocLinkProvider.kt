package com.grit.intellij.cds

import com.intellij.ide.actions.DumbAwareCopyPathProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class JDocLinkProvider : DumbAwareCopyPathProvider() {

    private val mavenModuleAnalyzer = MavenModuleAnalyzer()

    override fun getQualifiedName(
        project: Project,
        elements: List<PsiElement>,
        editor: Editor?,
        dataContext: DataContext
    ): String? {
        val refs = elements
            .flatMap { linkTo(it) }
            .ifEmpty { CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext)?.mapNotNull { getPathToElement(project, it, editor) } }
            .orEmpty()

        return if (refs.isNotEmpty()) refs.joinToString("\n") else null
    }

    private fun linkTo(element: PsiElement): List<String> {
        val containerNames = outerScope(element)
        return if (containerNames?.isNotEmpty() == true) {
            val modulename = mavenModuleAnalyzer.findMavenModulePath(element)
            containerNames.map { "<jdoc://$modulename/$it>" }
        } else emptyList()
    }

    /** Finds the enclosing Java class or method. If a file is selected in the project tree, returns all classes in the file. */
    private fun outerScope(element: PsiElement?): List<String>? {
        return when (element) {
            null -> null
            is PsiJavaFile -> element.classes.mapNotNull { it.qualifiedName }
            is PsiClass -> element.qualifiedName?.let { listOf(it) }
            is PsiMethod -> listOf(methodSignature(element))
            else -> outerScope(PsiTreeUtil.getParentOfType(element, PsiMethod::class.java))
        }
    }

    /** Method signature including a full list of all parameters. Types of typed parameters are removed. */
    private fun methodSignature(m: PsiMethod): String {
        val clzName = PsiTreeUtil.getParentOfType(m, PsiClass::class.java)!!.qualifiedName
        val methodName = m.name

        val params = mutableListOf<String>()
        val pl = m.parameterList

        for (i in 0 until pl.parametersCount) {
            params.add(cutTypeParameters(pl.getParameter(i)!!.type.canonicalText))
        }

        return "${clzName}#${methodName}(${params.joinToString(",")})"
    }

    private fun cutTypeParameters(canonicalText: String): String {
        val start = canonicalText.indexOf('<')
        val end = canonicalText.indexOf('>')
        return if (start in 0..end) canonicalText.removeRange(start, end+1) else canonicalText
    }
}
