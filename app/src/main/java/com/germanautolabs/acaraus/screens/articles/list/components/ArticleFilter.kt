package com.germanautolabs.acaraus.screens.articles.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germanautolabs.acaraus.screens.components.Dropdown
import com.germanautolabs.acaraus.screens.components.IntervalDatePickerScreen
import java.time.LocalDate

data class ArticleFilterState(

    val isVisible: Boolean = false,

    val query: String = "",
    val setQuery: (String) -> Unit = {},

    val setSortBy: (String) -> Unit = {},
    val sortBy: String = "MostRecent",
    val sortByOptions: Set<String> = emptySet(),

    val source: String = "All",
    val sourceOptions: Set<String> = emptySet(),
    val setSource: (String) -> Unit = {},

    val language: String = "English",
    val languageOptions: Set<String> = emptySet(),
    val setLanguage: (String) -> Unit = {},

    val fromOldestDate: LocalDate = LocalDate.now().minusMonths(1),
    val setFromDate: (LocalDate) -> Unit = {},

    val toNewestDate: LocalDate = LocalDate.now(),
    val setToDate: (LocalDate) -> Unit = {},

    val show: () -> Unit = {},
    val hide: () -> Unit = {},
    val apply: () -> Unit = {},
    val reset: () -> Unit = {},

) {
    val isSet get() =
        query.isNotBlank() ||
            sortBy != "MostRecent" ||
            source != "All" ||
            fromOldestDate != LocalDate.now().minusMonths(1) ||
            toNewestDate != LocalDate.now()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleFilter(
    modifier: Modifier = Modifier,
    filterState: ArticleFilterState,
    // initialValue: SheetValue = SheetValue.Hidden,
) {
    if (filterState.isVisible.not()) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = filterState.hide,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = filterState.query,
                onValueChange = filterState.setQuery,
                placeholder = { Text("Search topics") },
                trailingIcon = {
                    if (filterState.query.isNotBlank()) {
                        IconButton(onClick = { filterState.setQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                            )
                        }
                    }
                },
            )
            Dropdown(
                modifier = Modifier.padding(top = 16.dp),
                label = "Sort by",
                selection = filterState.sortBy,
                options = filterState.sortByOptions,
                onSelect = filterState.setSortBy,
            )
            Dropdown(
                modifier = Modifier.padding(top = 16.dp),
                label = "Sources",
                selection = filterState.source,
                options = filterState.sourceOptions,
                onSelect = filterState.setSource,
            )
            Dropdown(
                modifier = Modifier.padding(top = 16.dp),
                label = "Language",
                selection = filterState.language,
                options = filterState.languageOptions,
                onSelect = filterState.setLanguage,
            )
            IntervalDatePickerScreen(
                modifier = Modifier.padding(top = 16.dp),
                from = filterState.fromOldestDate,
                to = filterState.toNewestDate,
                onFromChange = filterState.setFromDate,
                onToChange = filterState.setToDate,
            )
            Row(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = filterState.reset,
                ) { Text("Reset") }
                Button(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = filterState.apply,
                ) { Text("Apply") }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ArticleFilterPreview() {
    // ArticleFilter(filterState = ArticleFilterState().copy(isVisible = true), initialValue = SheetValue.Expanded)
}