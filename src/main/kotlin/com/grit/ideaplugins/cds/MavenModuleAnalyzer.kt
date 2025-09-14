package com.grit.ideaplugins.cds

import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText

/** From an IntelliJ module, tries to locate the maven pom.xml to find out about the groupId and optionally parent.
 *  This information is needed if we want to provide cross-module links, but it cannot be derived from the maven dependencies by a maven plugin.
 */
internal class MavenModuleAnalyzer {

    fun findMavenModulePath(element: PsiElement): String {
        val module = ModuleUtil.findModuleForPsiElement(element)
        module?.let { return findPomCoordinates(it) ?: it.name }
        return ""
    }

    private fun findPomCoordinates(module: com.intellij.openapi.module.Module): String? {
        return findPomFile(module)?.let { readPomContents(module, it) }
    }

    private fun findPomFile(module: com.intellij.openapi.module.Module): VirtualFile? {
        for (vf in ModuleRootManager.getInstance(module).contentRoots) {
            val pomFile = VfsUtil.findRelativeFile(vf, "pom.xml")
            if (pomFile != null) return pomFile
        }
        return null
    }

    /** Read the groupId or parent groupId/artifactId, and the artifactId from the pom */
    private fun readPomContents(module: com.intellij.openapi.module.Module, pomFile: VirtualFile): String? {
        val documentManager = PsiDocumentManager.getInstance(module.project)
        val psiManager = PsiManager.getInstance(module.project)

        val psiFile = documentManager.getPsiFile(psiManager.findViewProvider(pomFile)!!.document) as XmlFile

        val root = psiFile.rootTag
        return root?.let {
            val artifactId = textOf(it.findFirstSubTag("artifactId"))
            val groupId = textOf(it.findFirstSubTag("groupId"))
            val parentGroupId = textOf(it.findFirstSubTag("parent")?.findFirstSubTag("groupId"))
            val parentArtifactId = textOf(it.findFirstSubTag("parent")?.findFirstSubTag("artifactId"))

            if (artifactId == null) return ""
            if (groupId != null) return "[${groupId}.${artifactId}]"
            return "[${parentGroupId}[~${parentArtifactId}].${artifactId}]"
        }
    }

    /** Extract the trimmed plain text inside an XmlTag */
    private fun textOf(tag: XmlTag?): String? = tag?.children?.joinToString("", "", "") { if (it is XmlText) it.text else "" }?.trim()
}
