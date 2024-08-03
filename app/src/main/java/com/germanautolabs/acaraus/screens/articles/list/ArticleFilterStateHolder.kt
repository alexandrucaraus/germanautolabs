package com.germanautolabs.acaraus.screens.articles.list

import com.germanautolabs.acaraus.models.ArticleFilter
import com.germanautolabs.acaraus.models.ArticleSource
import com.germanautolabs.acaraus.models.Error
import com.germanautolabs.acaraus.models.Result
import com.germanautolabs.acaraus.models.SortBy
import com.germanautolabs.acaraus.screens.articles.list.components.ArticleFilterState
import com.germanautolabs.acaraus.usecase.GetLocale
import com.germanautolabs.acaraus.usecase.GetNewsLanguage
import com.germanautolabs.acaraus.usecase.ObserveSources
import com.germanautolabs.acaraus.usecase.SetLocale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class ArticleFilterStateHolder(
    observeSources: ObserveSources,
    private val setLocale: SetLocale,
    private val getLocale: GetLocale,
    private val newsLanguage: GetNewsLanguage,
    currentScope: CoroutineScope,
) {

    private val defaultFilterState = ArticleFilterState(
        show = ::showFilter,
        hide = ::hideFilter,
        setQuery = ::setFilterQuery,
        setSortBy = ::setFilterSortBy,
        sortByOptions = SortBy.entries.map { it.name }.toSet(),
        setSource = ::setFilterSource,
        setLanguage = ::setFilterLanguage,
        languageOptions = newsLanguage.options(),
        setFromDate = ::setFilterFromDate,
        setToDate = ::setFilterToDate,
        reset = ::resetFilter,
        apply = ::applyFilter,
    )

    val filterEditorState = MutableStateFlow(defaultFilterState)
    val currentFilter = MutableStateFlow(ArticleFilter())

    private val currentSources = MutableStateFlow(emptyList<ArticleSource>())

    init {

        observeSources.stream().onEach { updateFilterSources(it) }.launchIn(currentScope)

        currentSources.onEach {
            filterEditorState.update { it.copy(sourceOptions = buildSourceOptions()) }
        }.launchIn(currentScope)
    }

    private fun updateFilterSources(result: Result<List<ArticleSource>, Error>) {
        when {
            result.isSuccess -> currentSources.update { result.success.orEmpty() }
            result.isError -> currentFilter.update {
                it.copy()
                /* todo handle error */
            }
        }
    }

    private fun showFilter() {
        filterEditorState.update { it.copy(isVisible = true) }
    }

    private fun hideFilter() {
        filterEditorState.update { it.copy(isVisible = false) }
    }

    private fun setFilterQuery(query: String) {
        filterEditorState.update { it.copy(query = query) }
    }

    private fun setFilterSortBy(sortBy: String) {
        val sortOrder = SortBy.entries.find { it.name == sortBy } ?: SortBy.MostRecent
        filterEditorState.update { it.copy(sortBy = sortOrder.name) }
    }

    private fun setFilterSource(source: String) {
        val articleSource = currentSources.value.find { it.name == source } ?: return
        filterEditorState.update {
            it.copy(source = articleSource.name)
        }
    }

    private fun setFilterLanguage(language: String) {
        val languageCode = newsLanguage.getLanguageCodeByName(language)
        val source =
            if (getLocale.languageCode() !== languageCode) "All" else filterEditorState.value.source
        filterEditorState.update {
            it.copy(
                language = language,
                source = source,
                sourceOptions = buildSourceOptions(languageCode),
            )
        }
        setLocale.languageCode(languageCode)
    }

    private fun setFilterFromDate(date: LocalDate) {
        filterEditorState.update { it.copy(fromOldestDate = date) }
    }

    private fun setFilterToDate(date: LocalDate) {
        filterEditorState.update { it.copy(toNewestDate = date) }
    }

    private fun resetFilter() {
        currentFilter.update { ArticleFilter() }
        filterEditorState.update {
            defaultFilterState.copy(
                language = newsLanguage.getLanguageCodeByName(getLocale.languageCode()),
                sourceOptions = buildSourceOptions(),
            )
        }
    }

    private fun applyFilter() {
        currentFilter.update { filterEditorState.value.toArticleFilter() }
        filterEditorState.update { it.copy(isVisible = false) }
    }

    private fun buildSourceOptions(
        languageCode: String = getLocale.languageCode(),
    ): Set<String> =
        setOf("All") + currentSources.value.filter { it.language == languageCode }.map { it.name }

    private fun ArticleFilterState.toArticleFilter(): ArticleFilter = ArticleFilter(
        query = query,
        sortedBy = sortBy.let { SortBy.valueOf(it) },
        language = getLocale.languageCode(),
        sources = currentSources.value.filter { it.name == source },
        fromDate = fromOldestDate,
        toDate = toNewestDate,
    )
}