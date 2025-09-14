package com.grit.ideaplugins.cds

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.intellij.plugins.markdown.lang.psi.MarkdownElementVisitor
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownParagraph


private const val START_PATTERN = "<jdoc://"
private const val END_PATTERN = ">"

/** @see https://github.com/JetBrains/intellij-community/blob/master/plugins/markdown/core/src/org/intellij/plugins/markdown/folding/MarkdownFoldingBuilder.kt */
class JDocLinkFoldingBuilder : CustomFoldingBuilder(), DumbAware {

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        if (root.language !== root.containingFile.viewProvider.baseLanguage) {
            return
        }

        root.accept(object : MarkdownElementVisitor() {

            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                element.acceptChildren(this)
            }

            override fun visitParagraph(paragraph: MarkdownParagraph) {
                addFoldingRegions(descriptors, paragraph)
                super.visitParagraph(paragraph)
            }
        })
    }

    private fun addFoldingRegions(descriptors: MutableList<FoldingDescriptor>, paragraph: MarkdownParagraph) {
        val start = paragraph.textRange.startOffset
        val end = paragraph.textRange.endOffset
        val text = paragraph.text

        var i = start
        while (i < end) {
            val descriptor = findNextSection(paragraph, text, i, start) ?: break
            descriptors.add(descriptor)
            i = descriptor.range.endOffset + END_PATTERN.length
        }
    }

    private fun findNextSection(paragraph: MarkdownParagraph, text: String, i: Int, offset: Int): FoldingDescriptor? {
        val start = text.indexOf(START_PATTERN, i - offset)
        if (start < 0) return null
        val end = text.indexOf(">", start + START_PATTERN.length)
        if (end < 0) return null

        return FoldingDescriptor(
            paragraph.node,
            TextRange(start + offset + 1, end + offset),
            null,
            placeHolderText(text.substring(start + START_PATTERN.length, end)),
            true,
            emptySet()
        )
    }

    private fun placeHolderText(text: String): String {
        val methodSep = text.indexOf('#')
        val placeholder = if (methodSep < 0) {
            // className only
            val classBegin = text.lastIndexOf('.')
            text.substring(classBegin + 1)
        } else {
            // className + methodName
            var methodEnd = text.indexOf('(', methodSep + 1)
            if (methodEnd < 0) {
                methodEnd = text.length
            }
            val classBegin = text.lastIndexOf('.', methodSep - 1)
            text.substring(classBegin + 1, methodEnd)
        }
        return "~${placeholder}"
    }


    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? = null

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean = true
}
