package com.selfhosttinker.timestable.data.export

import android.content.Context
import com.selfhosttinker.timestable.data.repository.ClassRepository
import com.selfhosttinker.timestable.data.repository.TaskRepository
import com.selfhosttinker.timestable.domain.model.SchoolClass
import com.selfhosttinker.timestable.domain.model.StudyTask
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class TimetableExport(
    val exportDate: String,
    val classes: List<ClassExport>,
    val tasks: List<TaskExport>
)

@Serializable
data class ClassExport(
    val id: String,
    val name: String,
    val room: String? = null,
    val teacher: String? = null,
    val notes: String? = null,
    val dayOfWeek: Int,
    val weekIndex: Int,
    val startTime: String,   // "HH:mm"
    val endTime: String,
    val hexColor: String
)

@Serializable
data class TaskExport(
    val id: String,
    val title: String,
    val detail: String? = null,
    val dueDate: String,     // ISO8601
    val isCompleted: Boolean,
    val hexColor: String,
    val linkedClassID: String? = null
)

@Singleton
class ExportManager @Inject constructor(
    private val classRepository: ClassRepository,
    private val taskRepository: TaskRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val iso8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun exportToFile(context: Context): File {
        val classes = classRepository.getAllClasses().first()
        val tasks = taskRepository.getAllTasks().first()

        val export = TimetableExport(
            exportDate = iso8601.format(Date()),
            classes = classes.map { it.toExport() },
            tasks = tasks.map { it.toExport() }
        )

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "timetable_${System.currentTimeMillis()}.json")
        file.writeText(json.encodeToString(export))
        return file
    }

    suspend fun importFromJson(jsonString: String) {
        val export = json.decodeFromString<TimetableExport>(jsonString)
        export.classes.forEach { classExport ->
            classRepository.saveClass(classExport.toDomain())
        }
        export.tasks.forEach { taskExport ->
            taskRepository.saveTask(taskExport.toDomain())
        }
    }

    private fun SchoolClass.toExport() = ClassExport(
        id = id,
        name = name,
        room = room,
        teacher = teacher,
        notes = notes,
        dayOfWeek = dayOfWeek,
        weekIndex = weekIndex,
        startTime = minsToTimeStr(startTimeMs),
        endTime = minsToTimeStr(endTimeMs),
        hexColor = hexColor
    )

    private fun StudyTask.toExport() = TaskExport(
        id = id,
        title = title,
        detail = detail,
        dueDate = iso8601.format(Date(dueDateMs)),
        isCompleted = isCompleted,
        hexColor = hexColor,
        linkedClassID = linkedClassId
    )

    private fun ClassExport.toDomain(): SchoolClass {
        return SchoolClass(
            id = id,
            name = name,
            room = room,
            teacher = teacher,
            notes = notes,
            dayOfWeek = dayOfWeek,
            weekIndex = weekIndex,
            startTimeMs = timeStrToMs(startTime),
            endTimeMs = timeStrToMs(endTime),
            hexColor = hexColor
        )
    }

    private fun TaskExport.toDomain(): StudyTask {
        return StudyTask(
            id = id,
            title = title,
            detail = detail,
            dueDateMs = try { iso8601.parse(dueDate)?.time ?: System.currentTimeMillis() }
                        catch (e: Exception) { System.currentTimeMillis() },
            isCompleted = isCompleted,
            hexColor = hexColor,
            subjectName = "",
            linkedClassId = linkedClassID
        )
    }

    private fun minsToTimeStr(ms: Long): String {
        val totalMinutes = (ms / 60_000).toInt()
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        return "%02d:%02d".format(h, m)
    }

    private fun timeStrToMs(time: String): Long {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return (h * 60 + m) * 60_000L
    }
}
