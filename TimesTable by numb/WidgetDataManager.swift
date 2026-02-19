import Foundation
import WidgetKit

/// Writes today's classes to the shared App Group UserDefaults
/// so the Widget extension can read them without direct SwiftData access.
@MainActor
final class WidgetDataManager {
    static let shared = WidgetDataManager()
    private let suiteName = "group.com.yourname.TimesTable" // ← change to your App Group ID

    private init() {}

    struct WidgetClass: Codable {
        var name: String
        var room: String?
        var startTime: Date
        var endTime: Date
        var hexColor: String
    }

    func updateWidgetData(classes: [SchoolClass]) {
        let cal = Calendar.current
        let weekday = cal.component(.weekday, from: Date())
        let todayIndex = weekday == 1 ? 7 : weekday - 1

        let todayClasses = classes
            .filter { $0.dayOfWeek == todayIndex }
            .sorted { $0.startTime < $1.startTime }
            .map {
                WidgetClass(
                    name: $0.name,
                    room: $0.room,
                    startTime: $0.startTime,
                    endTime: $0.endTime,
                    hexColor: $0.hexColor
                )
            }

        guard let defaults = UserDefaults(suiteName: suiteName),
              let data = try? JSONEncoder().encode(todayClasses) else { return }
        defaults.set(data, forKey: "widgetClasses")

        WidgetCenter.shared.reloadAllTimelines()
    }
}
