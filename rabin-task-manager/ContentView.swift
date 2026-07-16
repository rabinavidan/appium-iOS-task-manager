//
//  ContentView.swift
//  rabin-task-manager
//
//  Created by root26 on 15/07/2026.
//

import SwiftUI

struct TaskItem: Identifiable, Codable, Equatable {
    enum Priority: String, CaseIterable, Codable, Identifiable {
        case low = "Low"
        case medium = "Medium"
        case high = "High"

        var id: String { rawValue }

        var color: Color {
            switch self {
            case .low:
                return .green
            case .medium:
                return .orange
            case .high:
                return .red
            }
        }
    }

    let id: UUID
    var title: String
    var notes: String
    var priority: Priority
    var dueDate: Date
    var isCompleted: Bool
    var createdAt: Date

    init(
        id: UUID = UUID(),
        title: String,
        notes: String = "",
        priority: Priority = .medium,
        dueDate: Date = .now,
        isCompleted: Bool = false,
        createdAt: Date = .now
    ) {
        self.id = id
        self.title = title
        self.notes = notes
        self.priority = priority
        self.dueDate = dueDate
        self.isCompleted = isCompleted
        self.createdAt = createdAt
    }
}

struct ContentView: View {
    enum TaskFilter: String, CaseIterable, Identifiable {
        case all = "All"
        case open = "Open"
        case done = "Done"

        var id: String { rawValue }
    }

    @AppStorage("savedTasks") private var savedTasksData: Data = Data()
    @State private var tasks: [TaskItem] = []
    @State private var title = ""
    @State private var notes = ""
    @State private var priority: TaskItem.Priority = .medium
    @State private var dueDate = Date()
    @State private var filter: TaskFilter = .all
    @State private var isShowingClearConfirmation = false

    private var filteredTasks: [TaskItem] {
        tasks
            .filter { task in
                switch filter {
                case .all:
                    return true
                case .open:
                    return !task.isCompleted
                case .done:
                    return task.isCompleted
                }
            }
            .sorted { first, second in
                if first.isCompleted != second.isCompleted {
                    return !first.isCompleted
                }
                return first.dueDate < second.dueDate
            }
    }

    private var openTaskCount: Int {
        tasks.filter { !$0.isCompleted }.count
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                inputPanel

                Picker("Filter", selection: $filter) {
                    ForEach(TaskFilter.allCases) { filter in
                        Text(filter.rawValue)
                            .tag(filter)
                            .accessibilityIdentifier("filter-\(filter.rawValue.lowercased())")
                    }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("task-filter-picker")
                .padding(.horizontal)
                .padding(.vertical, 10)

                if filteredTasks.isEmpty {
                    ContentUnavailableView(
                        emptyStateTitle,
                        systemImage: "checklist",
                        description: Text(emptyStateMessage)
                    )
                    .accessibilityIdentifier("empty-state")
                } else {
                    List {
                        ForEach(filteredTasks) { task in
                            taskRow(task)
                        }
                        .onDelete(perform: deleteTasks)
                    }
                    .listStyle(.plain)
                    .accessibilityIdentifier("task-list")
                }
            }
            .navigationTitle("Tasks")
            .accessibilityIdentifier("tasks-screen")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(role: .destructive) {
                        isShowingClearConfirmation = true
                    } label: {
                        Label("Clear", systemImage: "trash")
                    }
                    .disabled(tasks.isEmpty)
                    .accessibilityIdentifier("clear-all-tasks-button")
                }

                ToolbarItem(placement: .topBarTrailing) {
                    Text("\(openTaskCount) open")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(.secondary)
                        .accessibilityIdentifier("open-task-count")
                }
            }
        }
        .onAppear(perform: loadTasks)
        .onChange(of: tasks) { _, _ in
            saveTasks()
        }
        .confirmationDialog(
            "Clear all tasks?",
            isPresented: $isShowingClearConfirmation,
            titleVisibility: .visible
        ) {
            Button("Clear All Tasks", role: .destructive) {
                clearAllTasks()
            }
            .accessibilityIdentifier("confirm-clear-all-tasks-button")

            Button("Cancel", role: .cancel) {
                isShowingClearConfirmation = false
            }
        } message: {
            Text("This removes every task from the list.")
        }
    }

    private var inputPanel: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("Task name", text: $title)
                .textFieldStyle(.roundedBorder)
                .accessibilityIdentifier("task-title-field")

            TextField("Notes", text: $notes, axis: .vertical)
                .lineLimit(2, reservesSpace: true)
                .textFieldStyle(.roundedBorder)
                .accessibilityIdentifier("task-notes-field")

            HStack {
                Picker("Priority", selection: $priority) {
                    ForEach(TaskItem.Priority.allCases) { priority in
                        Text(priority.rawValue)
                            .tag(priority)
                            .accessibilityIdentifier("priority-\(priority.rawValue.lowercased())")
                    }
                }
                .pickerStyle(.menu)
                .accessibilityIdentifier("priority-picker")

                Spacer()

                DatePicker("Due", selection: $dueDate, displayedComponents: .date)
                    .labelsHidden()
                    .accessibilityIdentifier("due-date-picker")
            }

            Button(action: addTask) {
                Label("Add Task", systemImage: "plus.circle.fill")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            .accessibilityIdentifier("add-task-button")
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .accessibilityIdentifier("task-input-panel")
    }

    private var emptyStateTitle: String {
        switch filter {
        case .all:
            return "No tasks yet"
        case .open:
            return "Nothing open"
        case .done:
            return "Nothing completed"
        }
    }

    private var emptyStateMessage: String {
        switch filter {
        case .all:
            return "Add your first task above."
        case .open:
            return "Completed tasks stay available in Done."
        case .done:
            return "Finish a task to see it here."
        }
    }

    private func taskRow(_ task: TaskItem) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Button {
                toggleCompletion(for: task)
            } label: {
                Image(systemName: task.isCompleted ? "checkmark.circle.fill" : "circle")
                    .font(.title3)
                    .foregroundStyle(task.isCompleted ? .green : .secondary)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(task.isCompleted ? "Mark \(task.title) incomplete" : "Mark \(task.title) complete")
            .accessibilityIdentifier("complete-task-\(task.id.uuidString)")

            VStack(alignment: .leading, spacing: 6) {
                Text(task.title)
                    .font(.headline)
                    .strikethrough(task.isCompleted)
                    .foregroundStyle(task.isCompleted ? .secondary : .primary)
                    .accessibilityIdentifier("task-title-\(task.id.uuidString)")

                if !task.notes.isEmpty {
                    Text(task.notes)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)
                        .accessibilityIdentifier("task-notes-\(task.id.uuidString)")
                }

                HStack(spacing: 8) {
                    Label(task.priority.rawValue, systemImage: "flag.fill")
                        .foregroundStyle(task.priority.color)
                        .accessibilityIdentifier("task-priority-\(task.id.uuidString)")

                    Label(task.dueDate.formatted(date: .abbreviated, time: .omitted), systemImage: "calendar")
                        .foregroundStyle(.secondary)
                        .accessibilityIdentifier("task-due-date-\(task.id.uuidString)")
                }
                .font(.caption.weight(.semibold))
            }

            Spacer(minLength: 8)

            Button(role: .destructive) {
                deleteTask(task)
            } label: {
                Image(systemName: "trash")
                    .font(.title3)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Delete \(task.title)")
            .accessibilityIdentifier("delete-task-\(task.id.uuidString)")
        }
        .padding(.vertical, 6)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("task-row-\(task.id.uuidString)")
    }

    private func addTask() {
        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedNotes = notes.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedTitle.isEmpty else { return }

        let task = TaskItem(
            title: trimmedTitle,
            notes: trimmedNotes,
            priority: priority,
            dueDate: dueDate
        )
        tasks.append(task)

        title = ""
        notes = ""
        priority = .medium
        dueDate = .now
    }

    private func toggleCompletion(for task: TaskItem) {
        guard let index = tasks.firstIndex(where: { $0.id == task.id }) else { return }
        tasks[index].isCompleted.toggle()
    }

    private func deleteTasks(at offsets: IndexSet) {
        let idsToDelete = offsets.map { filteredTasks[$0].id }
        tasks.removeAll { idsToDelete.contains($0.id) }
    }

    private func deleteTask(_ task: TaskItem) {
        tasks.removeAll { $0.id == task.id }
    }

    private func clearAllTasks() {
        tasks.removeAll()
        filter = .all
    }

    private func loadTasks() {
        guard !savedTasksData.isEmpty else { return }

        do {
            tasks = try JSONDecoder().decode([TaskItem].self, from: savedTasksData)
        } catch {
            tasks = []
        }
    }

    private func saveTasks() {
        do {
            savedTasksData = try JSONEncoder().encode(tasks)
        } catch {
            savedTasksData = Data()
        }
    }
}
