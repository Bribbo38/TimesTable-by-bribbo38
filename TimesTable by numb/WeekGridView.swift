import SwiftUI
import SwiftData

struct WeekGridView: View {
    @Query(sort: \SchoolClass.startTime) private var classes: [SchoolClass]
    @AppStorage("showWeekends") private var showWeekends = true
    @AppStorage("numberOfWeeks") private var numberOfWeeks = 1
    @AppStorage("repeatingWeeksEnabled") private var repeatingWeeksEnabled = false
    @State private var selectedWeek: Int = 1

    private var days: [(Int, String)] {
        let short = Calendar.current.shortWeekdaySymbols
        let all: [(Int, String)] = (1...7).map { tag in
            (tag, short[tag % 7])
        }
        return showWeekends ? all : Array(all.prefix(5))
    }

    private var todayIndex: Int {
        let w = Calendar.current.component(.weekday, from: Date())
        return w == 1 ? 7 : w - 1
    }

    private let startHour: CGFloat = 7
    private let endHour: CGFloat = 22
    private let hourHeight: CGFloat = 60

    private var totalHeight: CGFloat { (endHour - startHour) * hourHeight }

    // Current time position
    private var currentTimeOffset: CGFloat? {
        let cal = Calendar.current
        let now = Date()
        let h = CGFloat(cal.component(.hour, from: now))
        let m = CGFloat(cal.component(.minute, from: now))
        let offset = (h + m / 60 - startHour) * hourHeight
        return (offset >= 0 && offset <= totalHeight) ? offset : nil
    }

    var body: some View {
        VStack(spacing: 0) {
            if numberOfWeeks > 1 {
                weekPickerBar
            }
            gridContent
        }
        .navigationTitle("Week View")
        .onAppear {
            if repeatingWeeksEnabled && numberOfWeeks > 1 {
                let weekOfYear = Calendar.current.component(.weekOfYear, from: Date())
                selectedWeek = ((weekOfYear - 1) % numberOfWeeks) + 1
            }
        }
    }

    private var weekPickerBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(1...numberOfWeeks, id: \.self) { week in
                    Button {
                        withAnimation(AppTheme.smooth) { selectedWeek = week }
                    } label: {
                        Text("W\(week)")
                            .font(.subheadline.bold())
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(
                                selectedWeek == week
                                    ? AnyShapeStyle(LinearGradient(
                                        colors: [Color(hex: "#0A84FF") ?? .blue, Color(hex: "#5E5CE6") ?? .indigo],
                                        startPoint: .leading, endPoint: .trailing
                                      ))
                                    : AnyShapeStyle(Color.secondary.opacity(0.1))
                            )
                            .foregroundStyle(selectedWeek == week ? .white : .primary)
                            .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
    }

    private var gridContent: some View {
        ScrollView([.horizontal, .vertical]) {
            HStack(alignment: .top, spacing: 0) {
                // Time column
                VStack(alignment: .trailing, spacing: 0) {
                    Color.clear.frame(height: 40) // header spacer
                    ForEach(Int(startHour)...Int(endHour - 1), id: \.self) { hour in
                        Text(String(format: "%02d:00", hour))
                            .font(.caption2.monospacedDigit())
                            .foregroundStyle(.tertiary)
                            .frame(height: hourHeight, alignment: .top)
                            .padding(.trailing, 6)
                    }
                }
                .frame(width: 48)

                // Day columns
                ForEach(days, id: \.0) { dayIndex, dayName in
                    let isToday = dayIndex == todayIndex

                    VStack(spacing: 0) {
                        // Day header
                        VStack(spacing: 2) {
                            Text(dayName)
                                .font(.caption.bold())
                            if isToday {
                                Circle()
                                    .fill(Color(hex: "#0A84FF") ?? .blue)
                                    .frame(width: 5, height: 5)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 40)
                        .background(
                            isToday
                                ? AnyShapeStyle(LinearGradient(
                                    colors: [(Color(hex: "#0A84FF") ?? .blue).opacity(0.15), .clear],
                                    startPoint: .top, endPoint: .bottom))
                                : AnyShapeStyle(Color.secondary.opacity(0.06))
                        )

                        // Grid body
                        ZStack(alignment: .topLeading) {
                            // Hour lines
                            VStack(spacing: 0) {
                                ForEach(Int(startHour)...Int(endHour - 1), id: \.self) { _ in
                                    Divider().opacity(0.3)
                                    Spacer().frame(height: hourHeight - 0.5)
                                }
                            }
                            .frame(height: totalHeight)

                            // Today column tint
                            if isToday {
                                Rectangle()
                                    .fill((Color(hex: "#0A84FF") ?? .blue).opacity(0.03))
                                    .frame(height: totalHeight)
                            }

                            // Current time line
                            if isToday, let offset = currentTimeOffset {
                                HStack(spacing: 0) {
                                    Circle()
                                        .fill(Color(hex: "#FF453A") ?? .red)
                                        .frame(width: 8, height: 8)
                                    Rectangle()
                                        .fill(Color(hex: "#FF453A") ?? .red)
                                        .frame(height: 1.5)
                                }
                                .offset(y: offset - 4)
                            }

                            // Class blocks
                            ForEach(classes.filter {
                                $0.dayOfWeek == dayIndex && (numberOfWeeks <= 1 || $0.weekIndex == selectedWeek)
                            }) { sc in
                                ClassBlock(schoolClass: sc, startHour: startHour, hourHeight: hourHeight)
                            }
                        }
                        .frame(height: totalHeight)
                    }
                    .frame(width: 130)

                    Divider().opacity(0.2)
                }
            }
        }
    }
}

// MARK: - Class Block

struct ClassBlock: View {
    let schoolClass: SchoolClass
    let startHour: CGFloat
    let hourHeight: CGFloat

    @State private var showDetail = false

    private var topOffset: CGFloat {
        let cal = Calendar.current
        let comps = cal.dateComponents([.hour, .minute], from: schoolClass.startTime)
        let h = CGFloat(comps.hour ?? 0)
        let m = CGFloat(comps.minute ?? 0)
        return (h + m / 60 - startHour) * hourHeight
    }

    private var blockHeight: CGFloat {
        let duration = schoolClass.endTime.timeIntervalSince(schoolClass.startTime) / 3600
        return max(CGFloat(duration) * hourHeight, 24)
    }

    var body: some View {
        Button {
            showDetail = true
        } label: {
            VStack(alignment: .leading, spacing: 2) {
                Text(schoolClass.name)
                    .font(.caption2.bold())
                    .lineLimit(2)
                if let room = schoolClass.room, !room.isEmpty {
                    Text(room)
                        .font(.system(size: 9))
                        .opacity(0.85)
                }
            }
            .padding(5)
            .frame(maxWidth: .infinity, alignment: .leading)
            .frame(height: blockHeight)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(schoolClass.gradient)
            )
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .shadow(color: schoolClass.color.opacity(0.3), radius: 4, y: 2)
        }
        .buttonStyle(.plain)
        .offset(y: topOffset)
        .padding(.horizontal, 3)
        .sheet(isPresented: $showDetail) {
            NavigationStack {
                ClassDetailView(schoolClass: schoolClass)
            }
        }
    }
}
