import Foundation
import UserNotifications
import Combine

@MainActor
final class NotificationManager: ObservableObject {
    static let shared = NotificationManager()

    @Published var isAuthorized = false


    private init() {
        Task { await checkStatus() }
    }

    func checkStatus() async {
        let settings = await UNUserNotificationCenter.current().notificationSettings()
        isAuthorized = settings.authorizationStatus == .authorized
    }

    func requestAuthorization() async {
        do {
            let granted = try await UNUserNotificationCenter.current()
                .requestAuthorization(options: [.alert, .sound, .badge])
            isAuthorized = granted
        } catch {
            isAuthorized = false
        }
    }

    /// Schedule a 15-min-before notification for a class on a given weekday.
    func scheduleNotification(for schoolClass: SchoolClass) {
        guard isAuthorized else { return }

        let center = UNUserNotificationCenter.current()
        let id = schoolClass.id.uuidString

        // Remove existing notification for this class
        center.removePendingNotificationRequests(withIdentifiers: [id])

        let content = UNMutableNotificationContent()
        content.title = schoolClass.name
        var locationParts: [String] = []
        if let room = schoolClass.room, !room.isEmpty { locationParts.append("Room: \(room)") }
        if let teacher = schoolClass.teacher, !teacher.isEmpty { locationParts.append(teacher) }
        content.body = locationParts.isEmpty
            ? "Starting in 15 minutes"
            : locationParts.joined(separator: " · ") + " — in 15 min"
        content.sound = .default

        // Build trigger: weekday + time - 15 min
        let cal = Calendar.current
        let startComponents = cal.dateComponents([.hour, .minute], from: schoolClass.startTime)
        guard let hour = startComponents.hour, let minute = startComponents.minute else { return }

        var notifMinute = minute - 15
        var notifHour = hour
        if notifMinute < 0 {
            notifMinute += 60
            notifHour -= 1
        }
        if notifHour < 0 { return }

        // dayOfWeek: 1=Mon→2, 2=Tue→3 ... 7=Sun→1 (Calendar weekday)
        let calWeekday = schoolClass.dayOfWeek == 7 ? 1 : schoolClass.dayOfWeek + 1

        var triggerComponents = DateComponents()
        triggerComponents.weekday = calWeekday
        triggerComponents.hour = notifHour
        triggerComponents.minute = notifMinute

        let trigger = UNCalendarNotificationTrigger(dateMatching: triggerComponents, repeats: true)
        let request = UNNotificationRequest(identifier: id, content: content, trigger: trigger)
        center.add(request)
    }

    func removeNotification(for schoolClass: SchoolClass) {
        UNUserNotificationCenter.current()
            .removePendingNotificationRequests(withIdentifiers: [schoolClass.id.uuidString])
    }

    func removeAllNotifications() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
    }
}
