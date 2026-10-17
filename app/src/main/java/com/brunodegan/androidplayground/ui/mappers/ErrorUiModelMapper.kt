package com.brunodegan.androidplayground.ui.mappers

import com.brunodegan.androidplayground.base.network.base.ErrorType
import com.brunodegan.androidplayground.data.mappers.BaseMapper
import com.brunodegan.androidplayground.ui.model.ErrorUiModel
import org.koin.core.annotation.Factory

@Factory
class ErrorUiModelMapper : BaseMapper<ErrorType, ErrorUiModel> {
    override fun map(input: ErrorType): ErrorUiModel = ErrorUiModel(message = input.message)
}
