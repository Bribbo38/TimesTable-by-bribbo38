import SwiftUI
import SwiftData

struct ContentView: View {
    @State private var selectedDay: Int = {
        let w = Calendar.current.component(.weekday, from: Date())
        return w == 1 ? 7 : w - 1
    }()

    var body: some View {
        NavigationSplitView {
            SidebarView(selectedDay: $selectedDay)
        } detail: {
            ScheduleView(selectedDay: $selectedDay)
        }
#if os(iOS)
        .tint(Color(hex: "#0A84FF"))
#endif
    }
}

// MARK: - Sidebar

struct SidebarView: View {
    @Binding var selectedDay: Int

    private let items: [(String, String, AnyView)] = []

    var body: some View {
        List {
            NavigationLink {
                ScheduleView(selectedDay: $selectedDay)
            } label: {
                Label { Text("Schedule") } icon: {
                    Image(systemName: "calendar")
                        .foregroundStyle(
                            LinearGradient(colors: [.blue, .cyan], startPoint: .topLeading, endPoint: .bottomTrailing)
                        )
                }
            }
            NavigationLink {
                TaskListView()
            } label: {
                Label { Text("Tasks") } icon: {
                    Image(systemName: "checklist")
                        .foregroundStyle(
                            LinearGradient(colors: [.orange, .yellow], startPoint: .topLeading, endPoint: .bottomTrailing)
                        )
                }
            }
            NavigationLink {
                SettingsView()
            } label: {
                Label { Text("Settings") } icon: {
                    Image(systemName: "gearshape.fill")
                        .foregroundStyle(
                            LinearGradient(colors: [.gray, .secondary], startPoint: .topLeading, endPoint: .bottomTrailing)
                        )
                }
            }
        }
        .navigationTitle("TimesTable+")
    }
}

// MARK: - Schedule View

struct ScheduleView: View {
    @Binding var selectedDay: Int
    @Query(sort: \SchoolClass.startTime) private var classes: [SchoolClass]
    @State private var showingAddClass = false
    @AppStorage("showWeekends") private var showWeekends = true

    @Environment(\.horizontalSizeClass) private var hSizeClass
#if os(iOS)
    @State private var orientation = UIDevice.current.orientation
#endif

    private var isLandscape: Bool {
#if os(iOS)
        return orientation.isLandscape
#else
        return false
#endif
    }

    private var dayTags: [(Int, String, String)] {
        let all: [(Int, String, String)] = [
            (1, "Mon", "M"), (2, "Tue", "T"), (3, "Wed", "W"),
            (4, "Thu", "T"), (5, "Fri", "F"), (6, "Sat", "S"), (7, "Sun", "S")
        ]
        return showWeekends ? all : Array(all.prefix(5))
    }

    private var todayIndex: Int {
        let w = Calendar.current.component(.weekday, from: Date())
        return w == 1 ? 7 : w - 1
    }

    var filteredClasses: [SchoolClass] {
        classes.filter { $0.dayOfWeek == selectedDay }
    }

    var body: some View {
        Group {
            if isLandscape {
                WeekGridView()
            } else {
                portraitView
            }
        }
        .navigationTitle(isLandscape ? "Week View" : "Schedule")
        .toolbar {
            if !isLandscape {
#if os(iOS)
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button { showingAddClass.toggle() } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title3)
                            .symbolRenderingMode(.hierarchical)
                    }
                }
#else
                ToolbarItem {
                    Button { showingAddClass.toggle() } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title3)
                            .symbolRenderingMode(.hierarchical)
                    }
                }
#endif
            }
        }
        .sheet(isPresented: $showingAddClass) {
            AddEditClassView()
        }
#if os(iOS)
        .onReceive(NotificationCenter.default.publisher(for: UIDevice.orientationDidChangeNotification)) { _ in
            orientation = UIDevice.current.orientation
        }
#endif
    }

    // MARK: Portrait View

    private var portraitView: some View {
        VStack(spacing: 0) {
            dayPickerBar
            if filteredClasses.isEmpty {
                emptyState
            } else {
                classList
            }
        }
    }

    // MARK: Day Picker (animated pills)

    private var dayPickerBar: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(dayTags, id: \.0) { tag, label, _ in
                        Button {
                            withAnimation(AppTheme.smooth) {
                                selectedDay = tag
                            }
#if os(iOS)
                            Haptic.selection()
#endif
                        } label: {
                            VStack(spacing: 4) {
                                Text(label)
                                    .font(.subheadline.bold())
                                // Today dot
                                Circle()
                                    .fill(tag == todayIndex ? Color(hex: "#FF453A") ?? .red : .clear)
                                    .frame(width: 5, height: 5)
                            }
                            .frame(width: 52, height: 52)
                            .background(
                                Group {
                                    if selectedDay == tag {
                                        LinearGradient(
                                            colors: [Color(hex: "#0A84FF") ?? .blue, Color(hex: "#5E5CE6") ?? .indigo],
                                            startPoint: .topLeading, endPoint: .bottomTrailing
                                        )
                                    } else {
                                        Color.secondary.opacity(0.1)
                                    }
                                }
                            )
                            .foregroundStyle(selectedDay == tag ? .white : .primary)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                            .shadow(color: selectedDay == tag ? (Color(hex: "#0A84FF") ?? .blue).opacity(0.3) : .clear, radius: 6, y: 3)
                        }
                        .buttonStyle(.plain)
                        .id(tag)
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 10)
            }
            .onChange(of: selectedDay) { _, newVal in
                withAnimation(AppTheme.smooth) {
                    proxy.scrollTo(newVal, anchor: .center)
                }
            }
        }
    }

    // MARK: Class List

    private var classList: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(filteredClasses) { sc in
                    NavigationLink {
                        ClassDetailView(schoolClass: sc)
                    } label: {
                        ClassRow(schoolClass: sc)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal)
            .padding(.top, 8)
        }
    }

    // MARK: Empty State

    private var emptyState: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "calendar.badge.plus")
                .font(.system(size: 56, weight: .light))
                .foregroundStyle(
                    LinearGradient(colors: [Color(hex: "#0A84FF") ?? .blue, Color(hex: "#BF5AF2") ?? .purple],
                                   startPoint: .topLeading, endPoint: .bottomTrailing)
                )
                .pulsating()

            GradientText("No Classes", font: .title2.bold())

            Text("Tap + to add a class for this day.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Spacer()
        }
    }
}

// MARK: - Class Row (Glass Card)

struct ClassRow: View {
    let schoolClass: SchoolClass
    @State private var isPressed = false

    var body: some View {
        HStack(spacing: 0) {
            // Gradient accent strip
            RoundedRectangle(cornerRadius: 3)
                .fill(AppTheme.stripGradient(for: schoolClass.color))
                .frame(width: 5)
                .padding(.vertical, 6)

            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 5) {
                    Text(schoolClass.name)
                        .font(.headline)
                        .foregroundStyle(.primary)

                    HStack(spacing: 10) {
                        if let room = schoolClass.room, !room.isEmpty {
                            Label(room, systemImage: "mappin.circle.fill")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        if let teacher = schoolClass.teacher, !teacher.isEmpty {
                            Label(teacher, systemImage: "person.circle.fill")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(.leading, 12)

                Spacer()

                VStack(alignment: .trailing, spacing: 3) {
                    Text(schoolClass.startTime, style: .time)
                        .font(.subheadline.bold().monospacedDigit())
                    Text(schoolClass.endTime, style: .time)
                        .font(.caption.monospacedDigit())
                        .foregroundStyle(.secondary)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 14)
        }
        .background(
            RoundedRectangle(cornerRadius: AppTheme.cardRadius)
                .fill(schoolClass.color.opacity(0.08))
                .overlay(
                    RoundedRectangle(cornerRadius: AppTheme.cardRadius)
                        .strokeBorder(schoolClass.color.opacity(0.15), lineWidth: 0.5)
                )
        )
        .clipShape(RoundedRectangle(cornerRadius: AppTheme.cardRadius))
        .shadow(color: schoolClass.color.opacity(0.12), radius: 8, x: 0, y: 4)
        .scaleEffect(isPressed ? 0.97 : 1.0)
        .animation(AppTheme.bouncy, value: isPressed)
        .onLongPressGesture(minimumDuration: .infinity, pressing: { pressing in
            isPressed = pressing
        }) {}
    }
}

#Preview {
    ContentView()
        .modelContainer(for: [SchoolClass.self, StudyTask.self], inMemory: true)
}
