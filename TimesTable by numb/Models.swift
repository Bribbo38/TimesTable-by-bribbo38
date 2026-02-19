import Foundation
import SwiftData
import SwiftUI

// MARK: - SchoolClass

@Model
final class SchoolClass {
    var id: UUID
    var name: String
    var room: String?
    var teacher: String?
    var notes: String?
    var dayOfWeek: Int        // 1=Mon … 7=Sun
    var weekIndex: Int        // 1–4 (for multi-week schedules)
    var startTime: Date
    var endTime: Date
    var hexColor: String
    var attachmentPaths: [String]  // file paths stored locally

    @Relationship(deleteRule: .cascade)
    var tasks: [StudyTask]

    init(
        name: String,
        room: String? = nil,
        teacher: String? = nil,
        notes: String? = nil,
        dayOfWeek: Int,
        weekIndex: Int = 1,
        startTime: Date,
        endTime: Date,
        hexColor: String = "#3498db",
        attachmentPaths: [String] = []
    ) {
        self.id = UUID()
        self.name = name
        self.room = room
        self.teacher = teacher
        self.notes = notes
        self.dayOfWeek = dayOfWeek
        self.weekIndex = weekIndex
        self.startTime = startTime
        self.endTime = endTime
        self.hexColor = hexColor
        self.attachmentPaths = attachmentPaths
        self.tasks = []
    }

    var color: Color {
        Color(hex: hexColor) ?? .blue
    }

    var gradient: LinearGradient {
        AppTheme.cardGradient(for: color)
    }
}

// MARK: - StudyTask

@Model
final class StudyTask {
    var id: UUID
    var title: String
    var detail: String?
    var dueDate: Date
    var isCompleted: Bool
    var hexColor: String

    @Relationship(inverse: \SchoolClass.tasks)
    var linkedClass: SchoolClass?

    init(
        title: String,
        detail: String? = nil,
        dueDate: Date = Date(),
        isCompleted: Bool = false,
        hexColor: String = "#e74c3c",
        linkedClass: SchoolClass? = nil
    ) {
        self.id = UUID()
        self.title = title
        self.detail = detail
        self.dueDate = dueDate
        self.isCompleted = isCompleted
        self.hexColor = hexColor
        self.linkedClass = linkedClass
    }

    var color: Color {
        Color(hex: hexColor) ?? .red
    }
}

// MARK: - Color Hex Extension

extension Color {
    /// Convert a Color back to a hex string (#RRGGBB)
    func toHex() -> String {
#if os(iOS)
        let uiColor = UIColor(self)
        var r: CGFloat = 0; var g: CGFloat = 0; var b: CGFloat = 0; var a: CGFloat = 0
        uiColor.getRed(&r, green: &g, blue: &b, alpha: &a)
#else
        let nsColor = NSColor(self).usingColorSpace(.sRGB) ?? NSColor(self)
        var r: CGFloat = 0; var g: CGFloat = 0; var b: CGFloat = 0; var a: CGFloat = 0
        nsColor.getRed(&r, green: &g, blue: &b, alpha: &a)
#endif
        return String(format: "#%02X%02X%02X",
                      Int((r * 255).rounded()),
                      Int((g * 255).rounded()),
                      Int((b * 255).rounded()))
    }

    init?(hex: String) {
        let h = hex.trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "#", with: "")
        var rgb: UInt64 = 0
        guard Scanner(string: h).scanHexInt64(&rgb) else { return nil }
        switch h.count {
        case 6:
            self.init(
                red:   Double((rgb & 0xFF0000) >> 16) / 255,
                green: Double((rgb & 0x00FF00) >> 8)  / 255,
                blue:  Double( rgb & 0x0000FF)         / 255
            )
        case 8:
            self.init(
                red:   Double((rgb & 0xFF000000) >> 24) / 255,
                green: Double((rgb & 0x00FF0000) >> 16) / 255,
                blue:  Double((rgb & 0x0000FF00) >> 8)  / 255,
                opacity: Double(rgb & 0x000000FF)        / 255
            )
        default:
            return nil
        }
    }
}
