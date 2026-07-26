package com.skillsync.app.util

import android.content.Context
import org.json.JSONArray

data class QuestionBankItem(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = "",
    val explanation: String = "",
    val difficulty: String = "",
    val skill: String = "",
    val topic: String = ""
)

object QuestionBankHelper {

    fun load5RandomQuestions(context: Context, skill: String, difficulty: String): List<QuestionBankItem> {
        return try {
            val assetPath = "QuestionBank/$skill/$difficulty.json"
            val inputStream = context.assets.open(assetPath)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            val allQuestions = mutableListOf<QuestionBankItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val optsArray = obj.optJSONArray("options")
                val optsList = mutableListOf<String>()
                if (optsArray != null) {
                    for (j in 0 until optsArray.length()) {
                        optsList.add(optsArray.getString(j))
                    }
                }
                allQuestions.add(
                    QuestionBankItem(
                        question = obj.optString("question", ""),
                        options = optsList,
                        answer = obj.optString("answer", ""),
                        explanation = obj.optString("explanation", ""),
                        difficulty = obj.optString("difficulty", difficulty),
                        skill = obj.optString("skill", skill),
                        topic = obj.optString("topic", "")
                    )
                )
            }

            val uniqueQuestions = allQuestions
                .filter { q -> q.question.isNotBlank() && q.options.isNotEmpty() && q.answer.isNotBlank() }
                .distinctBy { q -> q.question.trim().lowercase() }
                .map { q ->
                    val cleanOpts = q.options.map { it.trim() }.distinct()
                    q.copy(options = cleanOpts)
                }
                .filter { q -> q.options.contains(q.answer.trim()) }

            if (uniqueQuestions.isEmpty()) emptyList()
            else uniqueQuestions.shuffled().take(5)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
