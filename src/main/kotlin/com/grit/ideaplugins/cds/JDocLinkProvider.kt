package com.grit.ideaplugins.cds

import com.intellij.ide.actions.DumbAwareCopyPathProvider
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class JDocLinkProvider : DumbAwareCopyPathProvider() {

    companion object {
      @JvmField val SHORT_NAME: DataKey<Boolean> = DataKey.create<Boolean>("JDocLinkProvider.shortName")
    }


    private val mavenModuleAnalyzer = MavenModuleAnalyzer()

    override fun update(e: AnActionEvent) {
        super.update(e.withDataContext(customizedDataCtxForShortName(e.dataContext)))
    }

    private fun customizedDataCtxForShortName(ctx: DataContext): DataContext {
        return SimpleDataContext.builder()
          .setParent(ctx)
          .add(SHORT_NAME, true)
          .build()
    }

    override fun getQualifiedName(
        project: Project,
        elements: List<PsiElement>,
        editor: Editor?,
        dataContext: DataContext
    ): String? {
        val shortName = dataContext.getData(SHORT_NAME) ?: false
        val refs = elements
            .flatMap { linkTo(it, shortName) }
            .ifEmpty { CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext)?.mapNotNull { getPathToElement(project, it, editor) } }
            .orEmpty()

        return if (refs.isNotEmpty()) refs.joinToString("\n") else null
    }

    private fun linkTo(element: PsiElement, shortName: Boolean): List<String> {
        val containerNames = determineName(element, shortName)
        return if (containerNames?.isNotEmpty() == true) {
            val modulename = if (shortName) "..."  else mavenModuleAnalyzer.findMavenModulePath(element)
            containerNames.map { "<jdoc://$modulename/$it>" }
        } else emptyList()
    }

    /** Finds the enclosing Java class or method. If a file is selected in the project tree, returns all classes in the file. */
    private fun determineName(element: PsiElement?, shortName: Boolean): List<String>? {
        return when (element) {
            null -> null
            is PsiJavaFile -> element.classes.mapNotNull { if (shortName) it.name  else it.qualifiedName }
            is PsiClass -> className(element, shortName)?.let { listOf(it) }
            is PsiMethod -> listOf(methodSignature(element, shortName))
            else -> determineName(PsiTreeUtil.getParentOfType(element, PsiMethod::class.java), shortName)
        }
    }

    /** Method signature including a full list of all parameters. Types of typed parameters are removed. */
    private fun methodSignature(m: PsiMethod, shortName: Boolean): String {
        val clzName = className(PsiTreeUtil.getParentOfType(m, PsiClass::class.java)!!, shortName)
        val methodName = m.name

        return if (shortName) {
            "$clzName#$methodName"
        } else {
            val params = mutableListOf<String>()
            val pl = m.parameterList

            for (i in 0 until pl.parametersCount) {
                params.add(cutTypeParameters(pl.getParameter(i)!!.type.canonicalText))
            }

            "${clzName}#${methodName}(${params.joinToString(",")})"
        }
    }

    private fun className(element: PsiClass, shortName: Boolean): String? = if (shortName) element.name  else element.qualifiedName

    private fun cutTypeParameters(canonicalText: String): String {
        val start = canonicalText.indexOf('<')
        val end = canonicalText.indexOf('>')
        return if (start in 0..end) canonicalText.removeRange(start, end+1) else canonicalText
    }
}
