package com.lifespark.walkmini.Controller

import com.google.firebase.firestore.FirebaseFirestore
import com.lifespark.walkmini.Data.Patient
import kotlinx.coroutines.tasks.await
data class Pattern(
    val motors: List<Map<String, Any>> = emptyList(),
    val loopTime: String = "",
    val nameofMode: String = "",
    val timestamp: Any? = ""
)


suspend fun fetchPatients(): List<Patient> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val patientSnapshots = firestore.collection("patients")
            .get()
            .await()
        patientSnapshots.documents.mapNotNull { document ->
            val name = document.getString("name") // Fetch the "name" field
            name?.let { Patient(id = document.id, name = it) } // Create Patient object
        }

    } catch (e: Exception) {
        e.printStackTrace()
        emptyList() // Return an empty list if there is an error
    }
}

fun addPatientToFirestore(
    firestore: FirebaseFirestore,
    name: String,
    onResult: (Boolean) -> Unit
) {
    val documentId = firestore.collection("patients").document().id // Generate unique ID
    val patientData = hashMapOf(
        "id" to documentId,  // Explicitly store the ID
        "name" to name
    )

    firestore.collection("patients")
        .add(patientData)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}
