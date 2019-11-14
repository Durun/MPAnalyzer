package yoshikihigo.cpanalyzer.logger.statement

import yoshikihigo.cpanalyzer.data.Statement
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths

object StatementsPairLogger {
    private val outDir = Paths.get("dump")
    private val mdOutput: PrintWriter

    init {
        Files.createDirectories(outDir)
        mdOutput = Files.createTempFile(
                outDir,
                "statements-",
                ".log.md"
        ).toFile().printWriter()

        val header = """
            | MPAnalyzer.rText | nitron.rText | MPAnalyzer.nText | nitron.nText |
            |----|----|----|----|
        """.trimIndent()
        mdOutput.println(header)
    }

    fun log(one: List<Statement>, other: List<Statement>) {
        // align
        val newOne: MutableList<Statement?> = one.toMutableList()
        other.forEachIndexed { index, it ->
            val rText = it.rText.trim()
            if (!(
                            rText.endsWith('{') ||
                                    rText.endsWith('}') ||
                                    rText.endsWith(';')
                            )) {
                newOne.addSafely(index + 1, null)
            } else if (rText == "}") {
                newOne.addSafely(index, null)
            }
        }
        // process and print
        newOne.pack(other)
                .asSequence()
                .map { (one, other) ->
                    (one?.rText to other?.rText) to (one?.nText to other?.nText)
                }
                .filter { (rText, nText) ->
                    (rText.first != rText.second) || (nText.first != nText.second)
                }
                .map { (rText, nText) ->
                    highlightDiff(rText) to highlightDiff(nText)
                }
                .map { (rText, nText) ->
                    (if (rText.first == rText.second) rText.first to "-" else rText) to nText
                }
                // replace NewLine
                .map { (rText, nText) ->
                    val trimNl = { it: String? -> it?.replace('\n', '\\') }
                    rText.map(trimNl) to nText.map(trimNl)
                }
                // replace |
                .map { (rText, nText) ->
                    val trimBar = { it: String? -> it?.replace('|', '｜') }
                    rText.map(trimBar) to nText.map(trimBar)
                }
                .forEach { (rText, nText) ->
                    mdOutput.println("| ${rText.first} | ${rText.second} | ${nText.first} | ${nText.second} |")
                }
    }

    private fun highlightDiff(pair: Pair<String?, String?>): Pair<String, String> {
        return when (true) {
            (pair.first == null) -> "`null`" to pair.second.orEmpty()
            (pair.second == null) -> pair.first.orEmpty() to "`null`"
            else -> {
                val (same, diff1, diff2) = splitDifference(pair.map { it.orEmpty() })
                "${same}${diff1.boldItalic()}" to "${same}${diff2.boldItalic()}"
            }
        }
    }

    private fun String.boldItalic(): String {
        return when (true) {
            this.isEmpty() -> this
            this.isBlank() -> this
            else -> " ___${this.trim()}___ "
        }
    }

    /**
     * @return 共通文字列, diff1, diff2
     */
    private fun splitDifference(pair: Pair<String, String>): Triple<String, String, String> {
        val sameSize = pair.first.zip(pair.second)
                .takeWhile { (one, other) -> one == other }
                .size
        return Triple(
                pair.first.take(sameSize),  // same
                pair.first.drop(sameSize),  // diff1
                pair.second.drop(sameSize)  // diff2
        )
    }

    private fun <T, R> List<T>.pack(other: List<R>): List<Pair<T?, R?>> {
        val newThis: List<T?>
        val newOther: List<R?>
        when (this.size < other.size) {
            true -> {
                newThis = this.toMutableList() + nulls<T>(other.size - this.size)
                newOther = other
            }
            false -> {
                newThis = this
                newOther = other.toMutableList() + nulls<R>(this.size - other.size)
            }
        }
        assert(newThis.size == newOther.size)
        return newThis.zip(newOther)
    }

    private fun <T> nulls(n: Int): List<T?> {
        assert(n >= 0)
        val list = mutableListOf<T?>()
        for (i in 1..n) {
            list.add(null)
        }
        return list
    }

    private fun <T, R> Pair<T, T>.map(transform: (T) -> R): Pair<R, R> {
        return transform(this.first) to transform(this.second)
    }

    private fun <T> MutableList<T?>.addSafely(index: Int, element: T?) {
        if (index <= this.lastIndex) this.add(index, null)
    }
}