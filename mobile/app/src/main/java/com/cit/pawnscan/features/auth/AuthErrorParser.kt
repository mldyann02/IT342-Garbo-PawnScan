package com.cit.pawnscan.features.auth

import org.json.JSONObject

object AuthErrorParser {
    fun parse(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) {
            return fallback
        }

        return try {
            val json = JSONObject(errorBody)
            val fieldErrors = json.optJSONObject("errors")
            if (fieldErrors != null && fieldErrors.length() > 0) {
                val messages = mutableListOf<String>()
                val keys = fieldErrors.keys()
                while (keys.hasNext()) {
                    messages.add(fieldErrors.optString(keys.next()))
                }
                messages.filter { it.isNotBlank() }.joinToString(" ").ifBlank { fallback }
            } else {
                json.optString("message", fallback).ifBlank { fallback }
            }
        } catch (e: Exception) {
            fallback
        }
    }
}
