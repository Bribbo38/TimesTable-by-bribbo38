import SwiftUI
import SwiftData
import AppIntents
import UniformTypeIdentifiers

struct SettingsView: View {
    @Environment(\.modelContext) private var modelContext
    @Query private var classes: [SchoolClass]
    @Query private var tasks: [StudyTask]

    @AppStorage("showWeekends") private var showWeekends = true
    @AppStorage("numberOfWeeks") private var numberOfWeeks = 1
    @AppStorage("notificationsEnabled") private var notificationsEnabled = false
    @AppStorage("iCloudEnabled") private var iCloudEnabled = false

    @ObservedObject private var notifManager = NotificationManager.shared

    @State private var showingResetConfirm = false
    @State private var showingExportSheet = false
    @State private var exportURL: URL?
    @State private var showingImportPicker = false
    @State private var showingImportError = false
    @State private var importErrorMessage = ""

    var body: some View {
#if os(macOS)
        macOSSettings
#else
        iOSSettings
#endif
    }

    // MARK: - macOS layout
#if os(macOS)
    private var macOSSettings: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {

                settingsGroup(title: "Schedule", icon: "calendar", iconColors: [.blue, .cyan]) {
                    Toggle("Show Weekends", isOn: $showWeekends)
                    Divider()
                    Stepper(value: $numberOfWeeks, in: 1...4) {
                        HStack {
                            Text("Repeating Weeks")
                            Spacer()
                            Text("\(numberOfWeeks)")
                                .foregroundStyle(.secondary)
                                .monospacedDigit()
                        }
                    }
                }

                settingsGroup(title: "Share & Backup", icon: "square.and.arrow.up", iconColors: [.green, .mint]) {
                    Button {
                        do {
                            exportURL = try ExportManager.shared.exportToURL(classes: classes, tasks: tasks)
                            showingExportSheet = true
                        } catch {
                            importErrorMessage = error.localizedDescription
                            showingImportError = true
                        }
                    } label: {
                        Label("Export Timetable", systemImage: "square.and.arrow.up")
                    }
                    .buttonStyle(.link)
                    Divider()
                    Button {
                        showingImportPicker = true
                    } label: {
                        Label("Import Timetable", systemImage: "square.and.arrow.down")
                    }
                    .buttonStyle(.link)
                }

                settingsGroup(title: "iCloud", icon: "icloud.fill", iconColors: [.blue, .indigo]) {
                    Toggle("iCloud Sync", isOn: $iCloudEnabled)
                    if iCloudEnabled {
                        Label("Restart the app to apply iCloud changes.", systemImage: "info.circle")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                settingsGroup(title: "Reset", icon: "trash.fill", iconColors: [.red, .orange]) {
                    Button(role: .destructive) {
                        showingResetConfirm = true
                    } label: {
                        Label("Reset Timetable", systemImage: "trash")
                            .foregroundStyle(.red)
                    }
                    .buttonStyle(.link)
                    Text("This will permanently delete all classes and tasks.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
            .padding(24)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .navigationTitle("Settings")
        .sheet(isPresented: $showingExportSheet) {
            if let url = exportURL {
                ShareSheet(url: url)
            }
        }
        .fileImporter(
            isPresented: $showingImportPicker,
            allowedContentTypes: [UTType.json],
            allowsMultipleSelection: false
        ) { result in
            if case .success(let urls) = result, let url = urls.first {
                do {
                    try ExportManager.shared.importFrom(url: url, context: modelContext)
                } catch {
                    importErrorMessage = error.localizedDescription
                    showingImportError = true
                }
            }
        }
        .confirmationDialog("Reset Timetable", isPresented: $showingResetConfirm, titleVisibility: .visible) {
            Button("Delete Everything", role: .destructive) { resetAll() }
        } message: {
            Text("All classes and tasks will be permanently deleted. This cannot be undone.")
        }
        .alert("Import Error", isPresented: $showingImportError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(importErrorMessage)
        }
    }
#endif

    // MARK: - iOS layout
#if os(iOS)
    private var iOSSettings: some View {
        Form {
            Section {
                Toggle("Show Weekends", isOn: $showWeekends)
                Stepper(value: $numberOfWeeks, in: 1...4) {
                    HStack {
                        Text("Repeating Weeks")
                        Spacer()
                        Text("\(numberOfWeeks)")
                            .foregroundStyle(.secondary)
                            .monospacedDigit()
                    }
                }
            } header: {
                Label("Schedule", systemImage: "calendar")
            }

            Section {
                Toggle("Class Reminders", isOn: $notificationsEnabled)
                    .onChange(of: notificationsEnabled) { _, enabled in
                        if enabled {
                            Task {
                                await notifManager.requestAuthorization()
                                if notifManager.isAuthorized {
                                    classes.forEach { notifManager.scheduleNotification(for: $0) }
                                } else {
                                    notificationsEnabled = false
                                }
                            }
                        } else {
                            notifManager.removeAllNotifications()
                        }
                    }
                if notificationsEnabled && !notifManager.isAuthorized {
                    Label("Permission denied. Enable in Settings.", systemImage: "exclamationmark.triangle")
                        .foregroundStyle(.orange)
                        .font(.caption)
                }
            } header: {
                Label("Notifications", systemImage: "bell.badge.fill")
            }

            Section {
                Toggle("iCloud Sync", isOn: $iCloudEnabled)
                if iCloudEnabled {
                    Label("Restart the app to apply iCloud changes.", systemImage: "info.circle")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            } header: {
                Label("iCloud", systemImage: "icloud.fill")
            } footer: {
                Text("Enable iCloud sync to keep your timetable updated across all your devices.")
            }

            Section {
                Button {
                    do {
                        exportURL = try ExportManager.shared.exportToURL(classes: classes, tasks: tasks)
                        showingExportSheet = true
                    } catch {
                        importErrorMessage = error.localizedDescription
                        showingImportError = true
                    }
                } label: {
                    Label("Export Timetable", systemImage: "square.and.arrow.up")
                }
                Button {
                    showingImportPicker = true
                } label: {
                    Label("Import Timetable", systemImage: "square.and.arrow.down")
                }
            } header: {
                Label("Share & Backup", systemImage: "square.and.arrow.up.on.square")
            }

            Section {
                SiriTipView(intent: NextClassIntent(), isVisible: .constant(true))
                SiriTipView(intent: TodayScheduleIntent(), isVisible: .constant(true))
            } header: {
                Label("Siri & Shortcuts", systemImage: "waveform")
            }

            Section {
                Button(role: .destructive) {
                    showingResetConfirm = true
                } label: {
                    HStack {
                        Spacer()
                        Label("Reset Timetable", systemImage: "trash.fill")
                            .foregroundStyle(.white)
                            .font(.subheadline.bold())
                        Spacer()
                    }
                    .padding(.vertical, 10)
                    .background(
                        LinearGradient(
                            colors: [Color(hex: "#FF453A") ?? .red, Color(hex: "#FF375F") ?? .pink],
                            startPoint: .leading, endPoint: .trailing
                        ),
                        in: RoundedRectangle(cornerRadius: 12)
                    )
                }
                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                .listRowBackground(Color.clear)
            } footer: {
                Text("This will permanently delete all classes and tasks.")
            }
        }
        .navigationTitle("Settings")
        .sheet(isPresented: $showingExportSheet) {
            if let url = exportURL {
                ShareSheet(url: url)
            }
        }
        .fileImporter(
            isPresented: $showingImportPicker,
            allowedContentTypes: [UTType.json],
            allowsMultipleSelection: false
        ) { result in
            if case .success(let urls) = result, let url = urls.first {
                do {
                    try ExportManager.shared.importFrom(url: url, context: modelContext)
                } catch {
                    importErrorMessage = error.localizedDescription
                    showingImportError = true
                }
            }
        }
        .confirmationDialog("Reset Timetable", isPresented: $showingResetConfirm, titleVisibility: .visible) {
            Button("Delete Everything", role: .destructive) { resetAll() }
        } message: {
            Text("All classes and tasks will be permanently deleted. This cannot be undone.")
        }
        .alert("Import Error", isPresented: $showingImportError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(importErrorMessage)
        }
    }
#endif

    // MARK: - Helpers

    private func resetAll() {
        notifManager.removeAllNotifications()
        classes.forEach { modelContext.delete($0) }
        tasks.forEach { modelContext.delete($0) }
    }

    // Reusable settings group for macOS
    private func settingsGroup<Content: View>(
        title: String,
        icon: String,
        iconColors: [Color],
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Label {
                Text(title).font(.headline)
            } icon: {
                Image(systemName: icon)
                    .foregroundStyle(
                        LinearGradient(colors: iconColors, startPoint: .topLeading, endPoint: .bottomTrailing)
                    )
            }
            .foregroundStyle(.primary)

            VStack(alignment: .leading, spacing: 10) {
                content()
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.secondary.opacity(0.06))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(Color.secondary.opacity(0.1), lineWidth: 0.5)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }
}

// MARK: - macOS Settings Group helper (kept for backward compat)

#if os(macOS)
struct SettingsGroup<Content: View>: View {
    let title: String
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .font(.headline)
                .foregroundStyle(.secondary)
            VStack(alignment: .leading, spacing: 10) {
                content
            }
            .padding(12)
            .background(Color.secondary.opacity(0.08))
            .cornerRadius(10)
        }
    }
}
#endif

// MARK: - Share Sheet

#if os(iOS)
struct ShareSheet: UIViewControllerRepresentable {
    let url: URL
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: [url], applicationActivities: nil)
    }
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
#else
struct ShareSheet: View {
    let url: URL
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "square.and.arrow.up")
                .font(.largeTitle)
                .foregroundStyle(
                    LinearGradient(colors: [.blue, .purple], startPoint: .topLeading, endPoint: .bottomTrailing)
                )
            Text(url.lastPathComponent)
                .font(.headline)
            ShareLink(item: url) {
                Label("Share…", systemImage: "square.and.arrow.up")
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(24)
    }
}
#endif
