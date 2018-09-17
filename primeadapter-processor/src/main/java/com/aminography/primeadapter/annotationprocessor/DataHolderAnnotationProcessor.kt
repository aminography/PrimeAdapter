package com.aminography.primeadapter.annotationprocessor

import com.aminography.primeadapter.annotation.DataHolder
import com.aminography.primeadapter.annotationprocessor.DataHolderAnnotationProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.google.auto.service.AutoService
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.FileObject
import javax.tools.StandardLocation

@Suppress("unused")
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class DataHolderAnnotationProcessor : AbstractProcessor() {

    private var filer: Filer? = null

    override fun init(processingEnvironment: ProcessingEnvironment?) {
        super.init(processingEnvironment)
        filer = processingEnv.filer
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        println("process")
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.errormessage { "Can't find the target directory for generated Kotlin files." }
            return false
        }

        val elementList = ArrayList<String>()
        try {
            for (element in roundEnv.getElementsAnnotatedWith(DataHolder::class.java)) {
                if (element.kind == ElementKind.CLASS) {
                    println("Processing: ${element.simpleName}")
                    val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
                    elementList.add(String.format("%s.%s", packageName, element.simpleName))
                }
            }
        } catch (ignored: Exception) {
        }
        generateViewTypeManager(elementList)

        return true
    }

//    private fun generateViewTypeManager2(elementList: ArrayList<String>) {
//        val stringBuilder = StringBuilder()
//        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
//        if (generatedSourcesRoot.isEmpty()) {
//            processingEnv.messager.errormessage { "Can't find the target directory for generated Kotlin files." }
//            return
//        }
//
//        stringBuilder.apply {
//            elementList.forEach { append(it).append("\n") }
//        }
//
//        val file = File(generatedSourcesRoot)
//        file.mkdir()
//        writeTo(stringBuilder.toString(), filer!!)
//    }

    private fun generateViewTypeManager(elementList: ArrayList<String>) {
        val stringBuilder = StringBuilder()
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.errormessage { "Can't find the target directory for generated Kotlin files." }
            return
        }

        stringBuilder.apply {
            append(String.format("package %s", "com.aminography.primeadapter")).append("\n")
            append("\n")
            append("class ViewTypeManager {").append("\n")
            append("\n")
            append("    init {").append("\n")
            for ((index, element) in elementList.withIndex()) {
                append(String.format("        map[%s::class.java] = %d", element, index + 1)).append("\n")
                append(String.format("        reverseMap[%d] = %s::class.java", index + 1, element)).append("\n")
            }
            append("    }").append("\n")
            append("\n")
            append("    companion object {").append("\n")
            append("        private val map = mutableMapOf<Class<*>, Int>()").append("\n")
            append("        private val reverseMap = mutableMapOf<Int, Class<*>>()").append("\n")
            append("\n")
            append("        fun getViewType(clazz: Class<*>): Int = map.getOrElse(clazz) { -1 }").append("\n")
            append("        fun getDataHolderClass(viewType: Int): Class<*>? = reverseMap[viewType]").append("\n")
            append("    }")
            append("\n")
            append("}")
        }

        val file = File(generatedSourcesRoot)
        file.mkdir()
        val packageName = "com.aminography.primeadapter"
        val fileName = "ViewTypeManager.kt"
        writeToSource(stringBuilder.toString(), packageName, fileName)
    }

    private fun writeToSource(content: String, packageName: String, fileName: String) {
        var filerSourceFile: FileObject
        filer?.let {
            try {
                filerSourceFile = it.createResource(StandardLocation.SOURCE_OUTPUT, packageName, fileName)
                filerSourceFile.delete()
                val writer = filerSourceFile.openWriter()
                writer.write(content)
                writer.flush()
                writer.close()
            } catch (e: Exception) {
            }
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(DataHolder::class.java.canonicalName)
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

}