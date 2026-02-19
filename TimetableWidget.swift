// TimetableWidget.swift
// Add this file to the Widget Extension target (see instructions below)
//
// HOW TO ADD THE WIDGET TARGET IN XCODE:
// 1. File → New → Target
// 2. Choose "Widget Extension"
// 3. Name it "TimetableWidget"
// 4. Uncheck "Include Live Activity" and "Include Configuration App Intent"
// 5. Click Finish
// 6. Add this file to that new target
// 7. In the Widget target's Build Settings, add the App Group:
//    - Signing & Capabilities → + Capability → App Groups
//    - Create group: group.com.yourname.TimesTable
// 8. Do the same in the main app target
// 9. Update the ModelConfiguration in TimesTable_by_numbApp.swift to use the App Group URL

import WidgetKit
import SwiftUI
import SwiftData

// MARK: - Shared Data Fetcher

struct WidgetClass: Codable {
    var name: String
    var room: String?
    var startTime: Date
    var endTime: Date
    var hexColor: String
}

// MARK: - Timeline Entry

struct TimetableEntry: TimelineEntry {
    let date: Date
    let nextClass: WidgetClass?
    let todayClasses: [WidgetClass]
}

// MARK: - Timeline Provider

struct TimetableProvider: TimelineProvider {
    func placeholder(in context: Context) -> TimetableEntry {
        TimetableEntry(
            date: Date(),
            nextClass: WidgetClass(name: "Mathematics", room: "Room 101",
                                   startTime: Date().addingTimeInterval(900),
                                   endTime: Date().addingTimeInterval(4500), hexColor: "#3498db"),
            todayClasses: []
        )
    }

    func getSnapshot(in context: Context, completion: @escaping (TimetableEntry) -> Void) {
        completion(placeholder(in: context))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<TimetableEntry>) -> Void) {
        let entry = fetchEntry()
        // Refresh every 30 minutes
        let nextUpdate = Calendar.current.date(byAdding: .minute, value: 30, to: Date()) ?? Date()
        let timeline = Timeline(entries: [entry], policy: .after(nextUpdate))
        completion(timeline)
    }

    private func fetchEntry() -> TimetableEntry {
        // Read from shared UserDefaults (App Group) — populated by main app
        let defaults = UserDefaults(suiteName: "group.com.yourname.TimesTable")
        guard let data = defaults?.data(forKey: "widgetClasses"),
              let classes = try? JSONDecoder().decode([WidgetClass].self, from: data) else {
            return TimetableEntry(date: Date(), nextClass: nil, todayClasses: [])
        }

        let now = Date()
        let cal = Calendar.current
        let weekday = cal.component(.weekday, from: now)
        let todayIndex = weekday == 1 ? 7 : weekday - 1

        // Filter today's classes (we stored dayOfWeek in the shared data)
        // For simplicity, all classes in the shared data are today's
        let todayClasses = classes
        let nextClass = todayClasses.first { $0.startTime > now }

        return TimetableEntry(date: now, nextClass: nextClass, todayClasses: todayClasses)
    }
}

// MARK: - Small Widget View (Next Class)

struct SmallWidgetView: View {
    let entry: TimetableEntry

    var body: some View {
        if let next = entry.nextClass {
            VStack(alignment: .leading, spacing: 6) {
                Label("Next Class", systemImage: "clock.fill")
                    .font(.caption2.bold())
                    .foregroundStyle(.secondary)

                Spacer()

                RoundedRectangle(cornerRadius: 3)
                    .fill(Color(hex: next.hexColor) ?? .blue)
                    .frame(width: 30, height: 4)

                Text(next.name)
                    .font(.headline.bold())
                    .lineLimit(2)

                if let room = next.room {
                    Text(room)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                Text(next.startTime, style: .time)
                    .font(.caption.bold())
                    .foregroundStyle(Color(hex: next.hexColor) ?? .blue)
            }
            .padding()
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        } else {
            VStack {
                Image(systemName: "checkmark.circle.fill")
                    .font(.title)
                    .foregroundStyle(.green)
                Text("No more classes today")
                    .font(.caption)
                    .multilineTextAlignment(.center)
            }
            .padding()
        }
    }
}

// MARK: - Medium Widget View (Today's Schedule)

struct MediumWidgetView: View {
    let entry: TimetableEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Label("Today", systemImage: "calendar")
                .font(.caption2.bold())
                .foregroundStyle(.secondary)
                .padding(.bottom, 2)

            if entry.todayClasses.isEmpty {
                Spacer()
                Text("No classes today")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Spacer()
            } else {
                ForEach(entry.todayClasses.prefix(4), id: \.name) { cls in
                    HStack(spacing: 8) {
                        RoundedRectangle(cornerRadius: 2)
                            .fill(Color(hex: cls.hexColor) ?? .blue)
                            .frame(width: 4, height: 28)
                        VStack(alignment: .leading, spacing: 1) {
                            Text(cls.name)
                                .font(.caption.bold())
                                .lineLimit(1)
                            Text(cls.startTime, style: .time)
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                    }
                }
                if entry.todayClasses.count > 4 {
                    Text("+\(entry.todayClasses.count - 4) more")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
    }
}

// MARK: - Widget Entry View

struct TimetableWidgetEntryView: View {
    @Environment(\.widgetFamily) var family
    var entry: TimetableEntry

    var body: some View {
        switch family {
        case .systemSmall:
            SmallWidgetView(entry: entry)
        case .systemMedium:
            MediumWidgetView(entry: entry)
        default:
            MediumWidgetView(entry: entry)
        }
    }
}

// MARK: - Widget Configuration

struct TimetableWidget: Widget {
    let kind: String = "TimetableWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: TimetableProvider()) { entry in
            TimetableWidgetEntryView(entry: entry)
                .containerBackground(.fill.tertiary, for: .widget)
        }
        .configurationDisplayName("TimesTable")
        .description("See your next class or today's full schedule.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

// MARK: - Widget Bundle

@main
struct TimetableWidgetBundle: WidgetBundle {
    var body: some Widget {
        TimetableWidget()
    }
}
