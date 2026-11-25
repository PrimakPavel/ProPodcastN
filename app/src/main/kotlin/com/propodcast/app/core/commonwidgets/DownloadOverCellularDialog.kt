package com.propodcast.app.core.commonwidgets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R

/** Asks before downloading over mobile data; [sizeLabel] is the estimated size ("≈ 60 MB"), if known. */
@Composable
fun DownloadOverCellularDialog(
    sizeLabel: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.download_cellular_title)) },
        text = {
            Text(
                if (sizeLabel != null) {
                    stringResource(R.string.download_cellular_message_size, sizeLabel)
                } else {
                    stringResource(R.string.download_cellular_message)
                },
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_download)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.download_cancel)) } },
    )
}
