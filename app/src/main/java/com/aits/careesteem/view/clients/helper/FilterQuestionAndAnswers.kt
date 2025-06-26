/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.helper

import com.aits.careesteem.view.clients.model.ClientCarePlanAssessment
import com.aits.careesteem.view.clients.model.ClientsList
import kotlin.reflect.full.memberProperties

object FilterQuestionAndAnswers {
    fun filterQuestionsAndAnswersActivityAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.ActivityAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.ActivityAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersEnvironmentAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.EnvironmentAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.EnvironmentAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersFinancialAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.FinancialAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.FinancialAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)
                // Replace "<agency>" dynamically
                question = question.replace("<agency>", "Care Esteem")

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersMentalHealthAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.MentalHealthAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.MentalHealthAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersCommunicationAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.CommunicationAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.CommunicationAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersPersonalHygieneAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.PersonalHygieneAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.PersonalHygieneAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersMedicationAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.MedicationAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.MedicationAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersClinicalAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.ClinicalAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.ClinicalAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)
                // Replace "<agency>" dynamically
                question = question.replace("<agency name>", "Care Esteem")

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersCulturalSpiritualSocialRelationshipsAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.CulturalSpiritualSocialRelationshipsAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.CulturalSpiritualSocialRelationshipsAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersBehaviourAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.BehaviourAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.BehaviourAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersOralCareAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.OralCareAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.OralCareAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersBreathingAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.BreathingAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.BreathingAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersContinenceAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.ContinenceAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.ContinenceAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersDomesticAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.DomesticAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.DomesticAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersEquipmentAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.EquipmentAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.EquipmentAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersMovingHandlingAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.MovingHandlingAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.MovingHandlingAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersPainAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.PainAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.PainAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersSleepingAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.SleepingAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.SleepingAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersSkinAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.SkinAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.SkinAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }

    fun filterQuestionsAndAnswersNutritionHydrationAssessmentData(
        clientData: ClientsList.Data,
        data: ClientCarePlanAssessment.Data.NutritionHydrationAssessmentData
    ): List<Triple<String, String, String>> {
        val filteredList = mutableListOf<Triple<String, String, String>>()

        // Get all properties dynamically
        val properties =
            ClientCarePlanAssessment.Data.NutritionHydrationAssessmentData::class.memberProperties.associateBy { it.name }

        // Iterate through questions dynamically
        properties.filterKeys { it.startsWith("questions_name_") }
            .forEach { (questionKey, questionProperty) ->
                val index =
                    questionKey.removePrefix("questions_name_") // Extract index (e.g., "1", "2", "3")
                val statusProperty =
                    properties["status_$index"] // Find corresponding status property
                val commentProperty =
                    properties["comment_$index"] // Find corresponding status property

                var question = questionProperty.get(data) as? String ?: "N/A"
                val status = statusProperty?.get(data) as? String ?: "N/A"
                val comment = commentProperty?.get(data) as? String ?: "N/A"

                // Replace "<name>" dynamically
                question = question.replace("<name>", clientData.full_name)

                //if (status.isNotEmpty()) {
                filteredList.add(Triple(question, status, comment))
                //}
            }

        return filteredList
    }
}