import Foundation

/// Creates a normal custom Journal question, never an answer or a second habit store.
/// Keep this text contract in lockstep with Android TwoMinuteHabit.kt: the
/// canonical question is the existing cross-platform Journal identity.
///
/// The UTF-16 length check intentionally matches Kotlin String.length rather
/// than Swift Character count, which differs for emoji and combining sequences.
func twoMinuteHabitQuestion(_ cueInput: String, _ actionInput: String) -> String? {
    let cue = cleanHabitPart(cueInput)
    let action = cleanHabitPart(actionInput)
    guard !cue.isEmpty, !action.isEmpty, cue.utf16.count <= 80, action.utf16.count <= 80 else {
        return nil
    }
    return "After \(cue), did you \(action) for two minutes?"
}

/// Collapse Unicode whitespace into single ASCII spaces while dropping
/// trailing punctuation, matching the Android journal-composer normalization.
private func cleanHabitPart(_ input: String) -> String {
    var out = ""
    var pendingSpace = false
    for scalar in input.unicodeScalars {
        if CharacterSet.whitespacesAndNewlines.contains(scalar) {
            if !out.isEmpty { pendingSpace = true }
        } else {
            if pendingSpace { out.append(" ") }
            out.unicodeScalars.append(scalar)
            pendingSpace = false
        }
    }
    while let last = out.last, " .,;?!:".contains(last) { out.removeLast() }
    return out
}
