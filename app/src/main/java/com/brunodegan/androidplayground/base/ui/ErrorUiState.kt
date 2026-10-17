package com.brunodegan.androidplayground.base.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.res.ResourcesCompat
import com.brunodegan.androidplayground.R
import com.brunodegan.androidplayground.ui.model.ErrorUiModel

@Composable
fun ErrorUiState(
    modifier: Modifier = Modifier,
    errorData: ErrorUiModel,
    onRetryButtonClicked: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    color =
                        Color.Black.copy(
                            alpha =
                                ResourcesCompat.getFloat(
                                    LocalResources.current,
                                    R.dimen.progress_circular_indicator_alpha,
                                ),
                        ),
                ),
    ) {
        Text(
            text = errorData.message ?: stringResource(R.string.http_response_generic_error_message),
            style = MaterialTheme.typography.titleLarge,
            overflow = TextOverflow.Visible,
            maxLines = integerResource(R.integer.card_lines),
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.double_padding))
                    .align(Alignment.Center),
        )
        Spacer(
            modifier =
                Modifier
                    .padding(
                        top = dimensionResource(R.dimen.double_padding),
                        bottom = dimensionResource(R.dimen.double_padding),
                    ).wrapContentSize(),
        )
        Button(
            contentPadding = PaddingValues(dimensionResource(R.dimen.base_padding)),
            elevation =
                ButtonDefaults.elevatedButtonElevation(
                    pressedElevation =
                        dimensionResource(
                            R.dimen.card_elevation,
                        ),
                ),
            shape = ButtonDefaults.elevatedShape,
            border =
                BorderStroke(
                    dimensionResource(R.dimen.tiny_padding),
                    color = colorResource(R.color.teal_700),
                ),
            modifier =
                Modifier
                    .background(Color.Transparent)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(dimensionResource(R.dimen.double_padding))
                    .align(Alignment.BottomCenter),
            onClick = {
                onRetryButtonClicked()
            },
        ) {
            Text(
                text = stringResource(R.string.try_again_label),
                modifier =
                    Modifier
                        .wrapContentSize()
                        .padding(dimensionResource(R.dimen.card_padding)),
            )
        }
    }
}
