import AppIntents
import SwiftData
import SwiftUI

// MARK: - Shared container accessor for App Intents
// Must be nonisolated (not @MainActor) because AppIntent.perform() is called
// from outside the main actor in Swift 6 strict concurrency mode.
private nonisolated func makeIntentContainer() -> ModelContainer? {
    try? ModelContainer(
        for: SchoolClass.self, StudyTask.self,
        configurations: ModelConfiguration(isStoredInMemoryOnly: false)
    )
}

// MARK: - Next Class Intent

struct NextClassIntent: AppIntent {
    static var title: LocalizedStringResource = "What's my next class?"
    static var description = IntentDescription("Tells you your next upcoming class today.")

    func perform() async throws -> some ProvidesDialog {
        guard let container = makeIntentContainer() else {
            return .result(dialog: "Could not access your timetable.")
        }
        let context = ModelContext(container)

        let now = Date()
        let weekday = Calendar.current.component(.weekday, from: now)
        let todayIndex = weekday == 1 ? 7 : weekday - 1

        let descriptor = FetchDescriptor<SchoolClass>(
            predicate: #Predicate { $0.dayOfWeek == todayIndex },
            sortBy: [SortDescriptor(\.startTime)]
        )
        let todayClasses = (try? context.fetch(descriptor)) ?? []
        let upcoming = todayClasses.first { $0.startTime > now }

        if let next = upcoming {
            let formatter = DateFormatter()
            formatter.timeStyle = .short
            let timeStr = formatter.string(from: next.startTime)
            let room = next.room.map { " in \($0)" } ?? ""
            return .result(dialog: "Your next class is \(next.name) at \(timeStr)\(room).")
        } else {
            return .result(dialog: "You have no more classes today.")
        }
    }
}

// MARK: - Today Schedule Intent

struct TodayScheduleIntent: AppIntent {
    static var title: LocalizedStringResource = "What's my schedule today?"
    static var description = IntentDescription("Lists all your classes for today.")

    func perform() async throws -> some ProvidesDialog {
        guard let container = makeIntentContainer() else {
            return .result(dialog: "Could not access your timetable.")
        }
        let context = ModelContext(container)

        let weekday = Calendar.current.component(.weekday, from: Date())
        let todayIndex = weekday == 1 ? 7 : weekday - 1

        let descriptor = FetchDescriptor<SchoolClass>(
            predicate: #Predicate { $0.dayOfWeek == todayIndex },
            sortBy: [SortDescriptor(\.startTime)]
        )
        let todayClasses = (try? context.fetch(descriptor)) ?? []

        if todayClasses.isEmpty {
            return .result(dialog: "You have no classes today.")
        }

        let formatter = DateFormatter()
        formatter.timeStyle = .short
        let list = todayClasses
            .map { "\($0.name) at \(formatter.string(from: $0.startTime))" }
            .joined(separator: ", ")
        return .result(dialog: "Today you have: \(list).")
    }
}

// MARK: - Shortcuts Provider

struct TimetableShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: NextClassIntent(),
            phrases: [
                "What's my next class in \(.applicationName)",
                "Next class \(.applicationName)",
                "What class do I have next in \(.applicationName)"
            ],
            shortTitle: "Next Class",
            systemImageName: "clock.fill"
        )
        AppShortcut(
            intent: TodayScheduleIntent(),
            phrases: [
                "What's my schedule today in \(.applicationName)",
                "Today's classes in \(.applicationName)",
                "Show my timetable in \(.applicationName)"
            ],
            shortTitle: "Today's Schedule",
            systemImageName: "calendar"
        )
    }
}
