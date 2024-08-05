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
import kotlin.Deprecated

@Deprecated(
    "Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowBackIos",
    ReplaceWith( "Icons.AutoMirrored.Rounded.ArrowBackIos",
        "androidx.compose.material.icons.automirrored.rounded.ArrowBackIos"),
)
public val Icons.Rounded.ArrowBackIos: ImageVector
    get() {
        if (_arrowBackIos != null) {
            return _arrowBackIos!!
        }
        _arrowBackIos = materialIcon(name = "Rounded.ArrowBackIos") {
            materialPath {
                moveTo(16.62f, 2.99f)
                curveToRelative(-0.49f, -0.49f, -1.28f, -0.49f, -1.77f, 0.0f)
                lineTo(6.54f, 11.3f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(8.31f, 8.31f)
                curveToRelative(0.49f, 0.49f, 1.28f, 0.49f, 1.77f, 0.0f)
                reflectiveCurveToRelative(0.49f, -1.28f, 0.0f, -1.77f)
                lineTo(9.38f, 12.0f)
                lineToRelative(7.25f, -7.25f)
                curveToRelative(0.48f, -0.48f, 0.48f, -1.28f, -0.01f, -1.76f)
                close()
            }
        }
        return _arrowBackIos!!
    }

private var _arrowBackIos: ImageVector? = null
