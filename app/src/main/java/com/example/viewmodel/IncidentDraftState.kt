package com.example.viewmodel

import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentEvidenceEntity
import com.example.data.model.TimelineEntryItem

data class IncidentDraftState(
    val incidentId: String = "",
    // Step 0: Required Terms (Section 2)
    val termsRightToSubmitConfirmed: Boolean = false,
    val termsResponsibleConfirmed: Boolean = false,
    val termsStorageUnderstoodConfirmed: Boolean = false,
    val termsAcceptedAt: Long = 0L,
    // Step 1: Incident Date and Time (Section 5)
    val incidentDateTime: Long = System.currentTimeMillis(),
    // Step 2: Location (Governorate is required, Map pin is optional - Section 3 & 4)
    val governorate: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationNotes: String = "",
    // Step 3: Category & Description (Section 6 & 7)
    val category: String = "Police / security incident",
    val title: String = "",
    val description: String = "",
    // Step 4: Officer / Person Information (Optional - Section 8)
    val officerName: String = "",
    val officerBadge: String = "",
    val officerDepartment: String = "",
    val vehicleRegistration: String = "",
    val otherIdentifyingInfo: String = "",
    // Step 5: Evidence Attachments (Section 9 & 10)
    val attachments: List<IncidentEvidenceEntity> = emptyList(),
    // Timeline (Section 11)
    val timeline: List<TimelineEntryItem> = emptyList(),
    // Wizard step index (0 to 7)
    val currentStep: Int = 0,
    val isSubmitting: Boolean = false,
    val submittedIncident: IncidentEntity? = null
) {
    val areTermsAccepted: Boolean
        get() = termsRightToSubmitConfirmed &&
                termsResponsibleConfirmed &&
                termsStorageUnderstoodConfirmed

    val isLocationValid: Boolean
        get() = governorate.isNotBlank()

    val isCategoryValid: Boolean
        get() = category.isNotBlank()

    val hasEvidenceAttached: Boolean
        get() = attachments.isNotEmpty()
}
