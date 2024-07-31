package lottiefiles.icons

/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Outlined.FileDownload: ImageVector
    get() {
        if (_fileDownload != null) {
            return _fileDownload!!
        }
        _fileDownload = materialIcon(name = "Outlined.FileDownload") {
            materialPath {
                moveTo(18.0f, 15.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineTo(18.0f)
                close()
                moveTo(17.0f, 11.0f)
                lineToRelative(-1.41f, -1.41f)
                lineTo(13.0f, 12.17f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(8.17f)
                lineTo(8.41f, 9.59f)
                lineTo(7.0f, 11.0f)
                lineToRelative(5.0f, 5.0f)
                lineTo(17.0f, 11.0f)
                close()
            }
        }
        return _fileDownload!!
    }

private var _fileDownload: ImageVector? = null
