package com.noop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.noop.R

/**
 * Opt-in two-minute-rule composer within the existing Journal.
 *
 * This creates a boolean Behaviour catalog item, *not* an answered entry.
 * The existing Yes/No chips, Insights engine, backup and import rules own the rest.
 */
@Composable
internal fun TwoMinuteHabitComposer(
    items: List<JournalCatalogItem>,
    onAdd: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var cue by rememberSaveable { mutableStateOf("") }
    var action by rememberSaveable { mutableStateOf("") }
    val question = twoMinuteHabitQuestion(cue, action)
    val exists = question != null &&
        items.any { normJournalKey(it.canonical) == normJournalKey(question) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            // Expose the disclosure header as an actionable button to TalkBack.
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.habit_stack_title),
                style = NoopType.headline,
                color = Palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(if (expanded) "−" else "+", style = NoopType.headline, color = Palette.textSecondary)
        }
        if (expanded) {
            Text(
                stringResource(R.string.habit_stack_explainer),
                style = NoopType.footnote,
                color = Palette.textSecondary,
            )
            // An action from today belongs under tomorrow's wake-day key in the existing Journal.
            // Explain the attribution; do not silently change the user's selected day or log Yes.
            Text(
                stringResource(R.string.habit_stack_wake_day_hint),
                style = NoopType.footnote,
                color = Palette.textTertiary,
            )
            OutlinedTextField(
                value = cue,
                onValueChange = { cue = it },
                label = { Text(stringResource(R.string.habit_stack_cue)) },
                singleLine = true,
                textStyle = NoopType.body,
                colors = journalFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = action,
                onValueChange = { action = it },
                label = { Text(stringResource(R.string.habit_stack_action)) },
                singleLine = true,
                textStyle = NoopType.body,
                colors = journalFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            NoopButton(
                text = stringResource(R.string.habit_stack_add),
                kind = NoopButtonKind.Secondary,
                enabled = question != null && !exists,
                fullWidth = true,
                onClick = { question?.let(onAdd) },
            )
            if (exists) {
                Text(
                    stringResource(R.string.habit_stack_existing),
                    style = NoopType.footnote,
                    color = Palette.textSecondary,
                )
            }
        }
    }
}
