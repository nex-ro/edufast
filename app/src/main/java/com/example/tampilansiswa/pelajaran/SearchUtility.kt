package com.example.tampilansiswa.pelajaran

import com.example.tampilansiswa.Data.Course
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class SearchUtility {

    companion object {
        suspend fun searchCourses(
            firestore: FirebaseFirestore,
            searchQuery: String,
            selectedSubject: String? = null,
            limit: Int = 20
        ): List<Course> {

            if (searchQuery.isEmpty()) {
                return getCoursesWithFilter(firestore, selectedSubject, limit)
            }

            val searchTerms = searchQuery.lowercase().split(" ").filter { it.isNotEmpty() }
            val allCourses = mutableListOf<Course>()

            // Search in course name
            val courseNameResults = searchInField(firestore, "courseName", searchQuery, selectedSubject, limit)
            allCourses.addAll(courseNameResults)

            // Search in subject (if not filtering by subject)
            if (selectedSubject == null || selectedSubject == "Top") {
                val subjectResults = searchInField(firestore, "subject", searchQuery, selectedSubject, limit)
                allCourses.addAll(subjectResults)
            }

            // Search in level
            val levelResults = searchInField(firestore, "level", searchQuery, selectedSubject, limit)
            allCourses.addAll(levelResults)

            // Search in location
            val locationResults = searchInField(firestore, "location", searchQuery, selectedSubject, limit)
            allCourses.addAll(locationResults)

            // Remove duplicates and return
            return allCourses.distinctBy { it.id }
                .filter { course ->
                    // Additional filtering to ensure relevance
                    searchTerms.any { term ->
                        course.courseName.lowercase().contains(term) ||
                                course.subject.lowercase().contains(term) ||
                                course.level.lowercase().contains(term) ||
                                course.location.lowercase().contains(term) ||
                                course.city.lowercase().contains(term)
                    }
                }
                .take(limit)
        }

        private suspend fun searchInField(
            firestore: FirebaseFirestore,
            fieldName: String,
            searchQuery: String,
            selectedSubject: String?,
            limit: Int
        ): List<Course> {
            try {
                var query = firestore.collection("courses")
                    .whereEqualTo("active", true)
                    .orderBy(fieldName)
                    .startAt(searchQuery)
                    .endAt(searchQuery + "\uf8ff")
                    .limit(limit.toLong())

                // Apply subject filter if specified
                if (selectedSubject != null && selectedSubject != "Top") {
                    query = firestore.collection("courses")
                        .whereEqualTo("active", true)
                        .whereEqualTo("subject", selectedSubject)
                        .orderBy(fieldName)
                        .startAt(searchQuery)
                        .endAt(searchQuery + "\uf8ff")
                        .limit(limit.toLong())
                }

                val snapshot = query.get().await()
                return snapshot.toObjects(Course::class.java)
            } catch (e: Exception) {
                return emptyList()
            }
        }

        private suspend fun getCoursesWithFilter(
            firestore: FirebaseFirestore,
            selectedSubject: String?,
            limit: Int
        ): List<Course> {
            try {
                var query = firestore.collection("courses")
                    .whereEqualTo("active", true)
                    .orderBy("courseName")
                    .limit(limit.toLong())

                if (selectedSubject != null && selectedSubject != "Top") {
                    query = query.whereEqualTo("subject", selectedSubject)
                }

                val snapshot = query.get().await()
                return snapshot.toObjects(Course::class.java)
            } catch (e: Exception) {
                return emptyList()
            }
        }

        /**
         * Get all available subjects from courses
         */
        suspend fun getAvailableSubjects(firestore: FirebaseFirestore): List<String> {
            return try {
                val snapshot = firestore.collection("courses")
                    .whereEqualTo("active", true)
                    .get()
                    .await()

                val subjects = mutableSetOf<String>()
                for (document in snapshot.documents) {
                    val subject = document.getString("subject")
                    if (!subject.isNullOrEmpty()) {
                        subjects.add(subject)
                    }
                }

                subjects.sorted()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}